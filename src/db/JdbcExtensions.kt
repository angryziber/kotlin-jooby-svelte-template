package db

import org.intellij.lang.annotations.Language
import util.toType
import java.net.URL
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneOffset.UTC
import java.util.*
import javax.sql.DataSource
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

fun <R> DataSource.query(table: String, id: UUID, mapper: ResultSet.() -> R): R =
  query(table, mapOf("id" to id), mapper = mapper).firstOrNull() ?: throw NoSuchElementException("$table:$id not found")

fun <R> DataSource.query(table: String, where: Map<String, Any?>, suffix: String = "", mapper: ResultSet.() -> R): List<R> =
  select("select * from $table", where, suffix, mapper)

fun <R> DataSource.select(select: String, where: Map<String, Any?>, suffix: String = "", mapper: ResultSet.() -> R): List<R> = withConnection {
  prepareStatement("$select${whereExpr(where)} $suffix").use { stmt ->
    stmt.setAll(whereValues(where))
    stmt.executeQuery().map(mapper)
  }
}

fun DataSource.exec(expr: String, values: Sequence<Any?> = emptySequence()): Int = withConnection {
  prepareStatement(expr).use { stmt ->
    stmt.setAll(values)
    stmt.executeUpdate()
  }
}

fun DataSource.insert(table: String, values: Map<String, *>): Int =
  exec(insertExpr(table, values), values.values.asSequence())

fun DataSource.upsert(table: String, values: Map<String, *>, uniqueFields: String = "id"): Int {
  val valuesByIndex = values.values.asSequence()
  return exec(insertExpr(table, values) +
    " on conflict ($uniqueFields) do update set ${setExpr(values)}", valuesByIndex + valuesByIndex)
}

private fun insertExpr(table: String, values: Map<String, *>) = """
  insert into $table (${values.keys.joinToString()})
    values (${values.entries.joinToString { (it.value as? SqlComputed)?.expr ?: "?" }})""".trimIndent()

fun DataSource.update(table: String, where: Map<String, Any?>, values: Map<String, *>): Int =
  exec("update $table set ${setExpr(values)}${whereExpr(where)}", values.values.asSequence() + whereValues(where))

fun DataSource.delete(table: String, where: Map<String, Any?>): Int =
  exec("delete from $table${whereExpr(where)}", whereValues(where))

private fun setExpr(values: Map<String, *>) = values.keys.joinToString { "$it = ?" }

private fun whereExpr(where: Map<String, Any?>) = if (where.isEmpty()) "" else " where " +
  where.entries.joinToString(" and ") { (k, v) -> whereExpr(k, v) }

private fun whereExpr(k: String, v: Any?) = when(v) {
  null -> "$k is null"
  is SqlExpression -> v.expr(k)
  is Iterable<*> -> inExpr(k, v)
  is Array<*> -> inExpr(k, v.toList())
  is SqlOperator -> "$k ${v.op} ?"
  else -> "$k = ?"
}

private fun inExpr(k: String, v: Iterable<*>) = "$k in (${v.joinToString { "?" }})"

private fun whereValues(where: Map<String, Any?>) = where.values.asSequence().filterNotNull().flatMap { it.toIterable() }

private fun Any?.toIterable(): Iterable<Any?> = when (this) {
  is Array<*> -> toList()
  is Iterable<*> -> this
  is SqlExpression -> toIterable()
  else -> listOf(this)
}

operator fun PreparedStatement.set(i: Int, value: Any?) = setObject(i, toDBType(value))
fun PreparedStatement.setAll(values: Sequence<Any?>) = values.forEachIndexed { i, v -> this[i + 1] = v }

private fun toDBType(v: Any?): Any? = when(v) {
  is SqlOperator -> v.value
  is Enum<*> -> v.name
  is Instant -> v.atOffset(UTC)
  is Period, is URL -> v.toString()
  is Collection<*> -> v.map { it.toString() }.toTypedArray()
  else -> v
}

private fun <R> ResultSet.map(mapper: ResultSet.() -> R): List<R> = mutableListOf<R>().also {
  while (next()) it += mapper()
}

fun ResultSet.getId(column: String = "id") = getString(column).toId()
fun ResultSet.getIdOrNull(column: String) = getString(column)?.toId()
fun ResultSet.getInstant(column: String) = getTimestamp(column).toInstant()
fun ResultSet.getLocalDate(column: String) = getDate(column).toLocalDate()
fun ResultSet.getPeriod(column: String) = Period.parse(getString(column))
fun ResultSet.getIntOrNull(column: String) = getObject(column)?.let { (it as Number).toInt() }

fun String.toId(): UUID = UUID.fromString(this)

inline fun <reified T: Enum<T>> ResultSet.getEnum(column: String) = enumValueOf<T>(getString(column))

inline fun <reified T: Any> ResultSet.fromValues(vararg values: Pair<KProperty1<T, *>, Any?>) = fromValues(T::class, *values)
fun <T: Any> ResultSet.fromValues(type: KClass<T>, vararg values: Pair<KProperty1<T, *>, Any?>) = type.primaryConstructor!!.let { constructor ->
  val extraArgs = values.associate { it.first.name to it.second }
  val args = constructor.parameters.associateWith { extraArgs[it.name] ?: fromDBType(getObject(it.name), it.type) }
  constructor.callBy(args)
}

private fun fromDBType(v: Any?, target: KType): Any? = when(target.jvmErasure) {
  Instant::class -> (v as Timestamp).toInstant()
  LocalDate::class -> (v as? Date)?.toLocalDate()
  LocalDateTime::class -> (v as Timestamp).toLocalDateTime()
  URL::class -> v?.let { URL(v as String) }
  List::class -> ((v as java.sql.Array).array as Array<*>).map { fromDBType(it, target.arguments[0].type!!) }.toList()
  Set::class -> ((v as java.sql.Array).array as Array<*>).map { fromDBType(it, target.arguments[0].type!!) }.toSet()
  else -> if (target.jvmErasure.isSubclassOf(Enum::class)) (v as String).toType(target)
  else v
}

interface SqlExpression {
  fun expr(key: String): String
  fun toIterable(): Iterable<Any?>
}

open class SqlExpressionImpl(@Language("SQL") val expr: String, vararg val values: Any?): SqlExpression {
  override fun expr(key: String) = expr
  override fun toIterable() = values.toList()
}

open class SqlComputed(@Language("SQL") val expr: String): SqlExpression {
  override fun expr(key: String) = "$key = $expr"
  override fun toIterable() = emptyList<Any?>()
}

open class SqlOperator(val op: String, val value: Any?): SqlExpression {
  override fun expr(key: String) = "$key $op ?"
  override fun toIterable() = listOf(value)
}

open class Between(val since: Any, val until: Any): SqlExpression {
  override fun expr(key: String) = "$key between ? and ?"
  override fun toIterable() = listOf(since, until)
}

class NullOrOperator(op: String, value: Any?): SqlOperator(op, value) {
  override fun expr(key: String) = "($key is null or $key $op ?)"
}

open class NotIn(private val values: Iterable<*>): SqlExpression {
  override fun expr(key: String) = inExpr(key, values).replace(" in ", " not in ")
  override fun toIterable() = values
}
