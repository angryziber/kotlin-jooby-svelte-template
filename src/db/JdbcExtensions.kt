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

fun DataSource.insert(table: String, values: Map<String, *>): Int = withConnection {
  val valuesByIndex = values.values.asSequence()
  prepareStatement("""insert into $table (${values.keys.joinToString(",") { it }})
    values (${values.entries.joinToString(",") { (it.value as? SqlComputed)?.expr ?: "?" }})
  """).use { stmt ->
    stmt.setAll(valuesByIndex)
    stmt.executeUpdate()
  }
}

fun DataSource.upsert(table: String, values: Map<String, *>, uniqueFields: String = "id"): Int = withConnection {
  val valuesByIndex = values.values.asSequence()
  prepareStatement("""insert into $table (${values.keys.joinToString(",") { it }})
    values (${values.entries.joinToString(",") { (it.value as? SqlComputed)?.expr ?: "?" }})
    on conflict ($uniqueFields) do update set ${setExpr(values)}
  """).use { stmt ->
    stmt.setAll(valuesByIndex + valuesByIndex)
    stmt.executeUpdate()
  }
}

fun DataSource.update(table: String, where: Map<String, Any?>, values: Map<String, *>): Int = withConnection {
  prepareStatement("update $table set ${setExpr(values)}${whereExpr(where)}").use { stmt ->
    stmt.setAll(values.values.asSequence() + whereValues(where))
    stmt.executeUpdate()
  }
}

fun DataSource.delete(table: String, where: Map<String, Any?>): Int = withConnection {
  prepareStatement("delete from $table${whereExpr(where)}").use { stmt ->
    stmt.setAll(whereValues(where))
    stmt.executeUpdate()
  }
}

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

fun fromDBType(v: Any?, type: KType): Any? = when {
  type.jvmErasure == Instant::class -> (v as Timestamp).toInstant()
  type.jvmErasure == LocalDate::class -> (v as? Date)?.toLocalDate()
  type.jvmErasure == LocalDateTime::class -> (v as Timestamp).toLocalDateTime()
  type.jvmErasure.isSubclassOf(Enum::class) -> (v as String).toType(type)
  type.jvmErasure == URL::class -> v?.let { URL(v as String) }
  type.jvmErasure == List::class -> ((v as java.sql.Array).array as Array<String>).map { fromDBType(it, type.arguments[0].type!!) }.toList()
  type.jvmErasure == Set::class -> ((v as java.sql.Array).array as Array<String>).map { fromDBType(it, type.arguments[0].type!!) }.toSet()
  else -> v
}

private fun <R> ResultSet.map(mapper: ResultSet.() -> R): List<R> = mutableListOf<R>().also {
  while (next()) it += mapper()
}

fun ResultSet.getInstant(column: String) = getTimestamp(column).toInstant()
fun ResultSet.getInstantNullable(column: String) = getTimestamp(column)?.toInstant()

fun ResultSet.getLocalDate(column: String) = getDate(column).toLocalDate()
fun ResultSet.getLocalDateNullable(column: String) = getDate(column)?.toLocalDate()
fun ResultSet.getPeriod(column: String) = Period.parse(getString(column))
fun ResultSet.getPeriodNullable(column: String) = getString(column)?.let { Period.parse(it) }

fun ResultSet.getId(column: String = "id") = getString(column).toId()
fun ResultSet.getIdNullable(column: String) = getString(column)?.toId()
fun ResultSet.getIntNullable(column: String) = getObject(column)?.let { (it as Number).toInt() }

fun String.toId(): UUID = UUID.fromString(this)

inline fun <reified T: Enum<T>> ResultSet.getEnum(column: String) = enumValueOf<T>(getString(column))
inline fun <reified T: Enum<T>> ResultSet.getEnumNullable(column: String): T? = getString(column)?.let { enumValueOf<T>(it) }

inline fun <reified T: Any> ResultSet.fromValues(vararg values: Pair<KProperty1<T, *>, Any?>) = T::class.primaryConstructor!!.let { constructor ->
  val extraArgs = values.associate { it.first.name to it.second }
  val args = constructor.parameters.associateWith { extraArgs[it.name] ?: fromDBType(getObject(it.name), it.type) }
  constructor.callBy(args)
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
