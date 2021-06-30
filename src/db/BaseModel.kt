package db

import util.toType
import java.net.URL
import java.sql.Date
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

interface BaseModel {
  val id: UUID
}

inline fun <reified T: Any> T.toValues() = toValuesSkipping()

inline fun <reified T: Any> T.toValuesSkipping(vararg skip: KProperty1<T, *>): Map<String, Any?> =
  (T::class.memberProperties - skip).filter { it.javaField != null }.map { it.name to it.get(this) }.toMap()

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
