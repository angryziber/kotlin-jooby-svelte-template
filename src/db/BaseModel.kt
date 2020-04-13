package db

import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

interface BaseModel {
  val id: UUID
}

fun Any.toValues() = toValuesSkipping()

@Suppress("UNCHECKED_CAST")
fun Any.toValuesSkipping(vararg skip: String) = this::class.memberProperties
  .filterNot { skip.contains(it.name) }
  .map { it.name to (it as KProperty1<Any, *>).get(this) }.toMap()
