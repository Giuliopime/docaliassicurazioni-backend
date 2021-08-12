package it.docaliassicurazioni

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.request.ContentTransformationException
import io.ktor.response.*
import io.ktor.serialization.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import io.ktor.util.date.*
import it.docaliassicurazioni.cache.RedisClient
import it.docaliassicurazioni.data.Error
import it.docaliassicurazioni.data.SessionID
import it.docaliassicurazioni.database.MongoDBClient
import it.docaliassicurazioni.v1.routes.v1Routes
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val loggerContext: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    loggerContext.getLogger("org.mongodb.driver").level = Level.WARN

    MongoDBClient
    EngineMain.main(args)
}

fun shutdown(reason: String): Nothing {
    logger.warn("Shutting down, reason:\n$reason")
    exitProcess(1)
}

fun Application.main() {
    install(ContentNegotiation) {
        json()
    }

    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)

        header(HttpHeaders.Accept)
        header(HttpHeaders.AcceptLanguage)
        header(HttpHeaders.ContentLanguage)
        header(HttpHeaders.ContentType)

        host(
            if(Env.testing) "localhost:3000" else "docaliassicurazioni.it",
            subDomains = listOf("www", "documenti"),
            schemes = listOf("http", "https")
        )
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
        /* When nuxtjs auth module will support basic auth I'll be able to use this
        basic("auth-basic") {

            realm = "Access to the '/login' path"
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
        */

        session<SessionID>("auth-session") {
            validate { session ->
                try {
                    val sessionData = RedisClient.getSession(session.id)
                    if (getTimeMillis() - sessionData.creationTimestamp >= 86400000) {
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
        cookie<SessionID>("session_id")
    }

    intercept(ApplicationCallPipeline.Monitoring) {
        logger.info("Request received on route ${call.request.path()}")
    }

    intercept(ApplicationCallPipeline.Features) {
        if (call.request.path().contains("admin")) {
            val sessionID = call.sessions.get<SessionID>()!!
            val sessionData = RedisClient.getSession(sessionID.id)
            val user = MongoDBClient.getUser(sessionData.email)
            if (!user.admin) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    Error(
                        HttpStatusCode.Forbidden.description,
                        "You need admin privileges to access this route."
                    )
                )
                return@intercept finish()
            }
        }
    }

    v1Routes()
}
