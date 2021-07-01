package util

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import java.io.OutputStream

inline fun <reified T: Any> ObjectMapper.parse(json: String) = readValue(json, T::class.java)
inline fun <reified T: Any> ObjectMapper.parse(json: InputStream) = readValue(json, T::class.java)
inline fun ObjectMapper.parse(json: InputStream) = readTree(json)

inline fun ObjectMapper.stringify(o: Any) = writeValueAsString(o)
inline fun ObjectMapper.streamify(o: Any, out: OutputStream) = writeValue(out, o)
