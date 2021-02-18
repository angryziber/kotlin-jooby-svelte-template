package db

import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

interface BaseModel {
  val id: UUID
}

inline fun <reified T: Any> T.toValues() = toValuesSkipping()

inline fun <reified T: Any> T.toValuesSkipping(vararg skip: KProperty1<T, *>): Map<String, Any?> =
  (T::class.memberProperties - skip).filter { it.javaField != null }.map { it.name to it.get(this) }.toMap()
