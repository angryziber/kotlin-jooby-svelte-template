package util

import java.lang.System.currentTimeMillis
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors.newScheduledThreadPool
import java.util.concurrent.TimeUnit.MILLISECONDS

const val defaultMaxAgeMillis = 3_600_000L

class Cache(val maxAgeMillis: Long = defaultMaxAgeMillis) {
  companion object {
    private val caches = mutableListOf<Cache>()

    init {
      newScheduledThreadPool(1).scheduleAtFixedRate(::dropExpiredEntries, defaultMaxAgeMillis, defaultMaxAgeMillis, MILLISECONDS)
    }

    fun dropExpiredEntries() {
      caches.forEach { cache ->
        val expirationTime = currentTimeMillis() - cache.maxAgeMillis
        cache.store.values.removeIf { it.createdAt <= expirationTime }
      }
    }
  }

  init {
    caches += this
  }

  private val store = ConcurrentHashMap<Any, Entry>()

  @Suppress("UNCHECKED_CAST")
  operator fun <R: Any> invoke(key: Any, block: () -> R): R =
    store.computeIfAbsent(key) { Entry(block()) }.data as R

  class Entry(val data: Any, val createdAt: Long = currentTimeMillis())
}
