package it.docaliassicurazioni.v1.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import it.docaliassicurazioni.cache.RedisClient
import it.docaliassicurazioni.data.UserSession
import it.docaliassicurazioni.database.MongoDBClient

fun Application.v1Routes() {
    routing {
        route("/v1") {

            authenticate("auth-form") {
                post("/login") {
                    val email = call.principal<UserIdPrincipal>()!!.name

                    val userSession = UserSession(generateSessionId(), email)
                    RedisClient.createSession(userSession)
                    call.sessions.set(userSession)

                    val user = MongoDBClient.getUser(email)
                    call.respond(user)
                }
            }

            authenticate("auth-session-admin") {
                adminUserRoutes()
            }

            authenticate("auth-session") {
                userRoute()
            }
        }
    }
}
