package it.docaliassicurazioni.v1.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import it.docaliassicurazioni.cache.RedisClient
import it.docaliassicurazioni.data.UserSessionID
import it.docaliassicurazioni.database.MongoDBClient

fun Route.userRoute() {
    get("/user") {
        val sessionID = call.sessions.get<UserSessionID>()!!.id
        val sessionData = RedisClient.getSession(sessionID)
        val user = MongoDBClient.getUser(sessionData.email)
        call.respond(HttpStatusCode.OK, user)
    }
}
