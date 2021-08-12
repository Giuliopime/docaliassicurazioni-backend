package it.docaliassicurazioni

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.ContentTransformationException
import io.ktor.response.*
import io.ktor.serialization.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import io.ktor.util.date.*
import it.docaliassicurazioni.cache.RedisClient
import it.docaliassicurazioni.data.Error
import it.docaliassicurazioni.data.UserSession
import it.docaliassicurazioni.database.MongoDBClient
import it.docaliassicurazioni.v1.routes.v1Routes
import mu.KotlinLogging
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) = EngineMain.main(args)

fun shutdown(reason: String) {
    logger.warn("Shutting down, reason:\n$reason")
    exitProcess(1)
}

fun Application.main() {
    install(ContentNegotiation) {
        json()
    }

    install(StatusPages) {
        exception<NoSuchElementException> {
            call.respond(HttpStatusCode.NotFound, Error(HttpStatusCode.NotFound.toString(), "The entity doesn't exist in the database anymore, might have been deleted."))
        }

        exception<ContentTransformationException> {
            call.respond(HttpStatusCode.BadRequest, Error(HttpStatusCode.BadRequest.toString(), "Invalid request body object."))
        }

        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
            logger.error("Error encountered", cause)
        }
    }

    install(Authentication) {
        form("auth-form") {
            userParamName = "email"
            passwordParamName = "password"
            validate { credentials ->
                try {
                    val user = MongoDBClient.getUser(credentials.name)
                    if (user.password == credentials.password)
                        UserIdPrincipal(user.email)
                    else
                        null
                } catch (e: NoSuchElementException) {
                    null
                }
            }
        }

        session<UserSession>("auth-session-admin") {
            validate { session ->
                try {
                    val storedSession = RedisClient.getSession(session.id)
                    if (getTimeMillis() - storedSession.creationTimestamp >= 86400000) {
                        RedisClient.deleteSession(session.id)
                        null
                    } else {
                        val user = MongoDBClient.getUser(storedSession.email)
                        if (user.admin)
                            session
                        else
                            null
                    }
                } catch (e: NoSuchElementException) {
                    null
                }
            }
            challenge {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    Error(
                        HttpStatusCode.Unauthorized.toString(),
                        "Session expired, use the /login route to get a new one."
                    )
                )
            }
        }

        session<UserSession>("auth-session") {
            validate { session ->
                try {
                    val storedSession = RedisClient.getSession(session.id)
                    if (getTimeMillis() - storedSession.creationTimestamp >= 86400000) {
                        RedisClient.deleteSession(session.id)
                        null
                    } else
                        session
                } catch (e: NoSuchElementException) {
                    null
                }
            }
            challenge {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    Error(
                        HttpStatusCode.Unauthorized.toString(),
                        "Session expired, use the /login route to get a new one."
                    )
                )
            }
        }
    }

    install(Sessions) {
        cookie<UserSession>("user_session")
    }

    v1Routes()
}
