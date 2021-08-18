package it.docaliassicurazioni.v1.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.date.*
import it.docaliassicurazioni.cache.RedisClient
import it.docaliassicurazioni.data.Error
import it.docaliassicurazioni.data.SessionData
import it.docaliassicurazioni.data.SessionID
import it.docaliassicurazioni.data.UserLoginData
import it.docaliassicurazioni.database.MongoDBClient

fun Application.v1Routes() {
    routing {
        route("/api/v1") {

            post("/login") {
                val userLoginData = call.receive<UserLoginData>()
                val user = MongoDBClient.getUser(userLoginData.email)
                if (user.password != userLoginData.password)
                    return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        Error(
                            HttpStatusCode.Unauthorized.description,
                            "Incorrect login data"
                        )
                    )

                val sessionID = SessionID(generateSessionId())

                val sessionData = SessionData(
                    sessionID.id,
                    user.email,
                    getTimeMillis()
                )
                RedisClient.createSession(sessionData)

                call.sessions.set(sessionID)

                call.respond(HttpStatusCode.OK)
            }

            authenticate("auth-session") {
                userRoutes()
                adminUserRoutes()
            }
        }
    }
}
