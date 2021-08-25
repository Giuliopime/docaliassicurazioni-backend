package it.docaliassicurazioni.cache

import io.ktor.util.date.*

object EmailsCache {
    private val hashmap: MutableMap<String, Long> = mutableMapOf()

    fun onCooldown(email: String): Boolean {
        val currentTime = getTimeMillis()

        val timestamp = hashmap[email]

        if (timestamp == null) {
            hashmap[email] = currentTime
            return false
        }

        return if (currentTime - timestamp < 600000) {
            true
        } else {
            hashmap[email] = currentTime
            false
        }
    }
}
