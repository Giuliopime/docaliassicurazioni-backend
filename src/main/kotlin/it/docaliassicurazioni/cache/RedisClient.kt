package it.docaliassicurazioni.cache

import it.docaliassicurazioni.data.SessionData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import redis.clients.jedis.JedisPool

object RedisClient {
    private val client = JedisPool()
    private val sessionsHashName = "sessions"

    @Throws(NoSuchElementException::class)
    fun getSession(id: String): SessionData {
        client.resource.use {
            val json = it.hget(sessionsHashName, id) ?: throw NoSuchElementException()
            return Json.decodeFromString(json)
        }
    }

    fun createSession(session: SessionData) {
        client.resource.use {
            it.hset(sessionsHashName, session.id, Json.encodeToString(session))
        }
    }

    @Throws(NoSuchElementException::class)
    fun deleteSession(id: String) {
        client.resource.use {
            val result = it.hdel(sessionsHashName, id)
            if (result == 0L)
                throw NoSuchElementException()
        }
    }
}
