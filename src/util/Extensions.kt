package util

import java.util.*

inline val Int.d get() = toBigDecimal()
inline val Double.d get() = toBigDecimal()
inline val String.d get() = toBigDecimal()

inline val String.c get() = Currency.getInstance(this)
