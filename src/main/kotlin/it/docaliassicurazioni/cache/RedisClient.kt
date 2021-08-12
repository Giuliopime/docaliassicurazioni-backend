package it.docaliassicurazioni.cache

import it.docaliassicurazioni.data.UserSessionData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import redis.clients.jedis.JedisPool
import kotlin.jvm.Throws

object RedisClient {
    private val client = JedisPool()
    private val sessionsHashName = "sessions"

    @Throws(NoSuchElementException::class)
    fun getSession(id: String): UserSessionData {
        client.resource.use {
            val json = it.hget(sessionsHashName, id) ?: throw NoSuchElementException()
            return Json.decodeFromString(json)
        }
    }

    fun createSession(session: UserSessionData) {
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
