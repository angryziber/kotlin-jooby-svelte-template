package db

import java.util.*
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

interface BaseModel {
  val id: UUID
}

fun Any.toValues() = toValuesSkipping()

@Suppress("UNCHECKED_CAST")
fun <T: Any> T.toValuesSkipping(vararg skip: KProperty<T>) = (this::class.memberProperties - skip)
  .map { it.name to (it as KProperty1<Any, *>).get(this) }.toMap()
