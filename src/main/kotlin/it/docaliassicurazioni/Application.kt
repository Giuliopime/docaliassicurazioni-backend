package it.docaliassicurazioni

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import it.docaliassicurazioni.database.MongoDBClient
import it.docaliassicurazioni.v1.routes.v1Routes
import mu.KotlinLogging
import javax.naming.AuthenticationException
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) = EngineMain.main(args)

fun shutdown(reason: String) {
    logger.warn("Shutting down, reason:\n$reason")
    exitProcess(1)
}

fun Application.main() {
    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
            logger.error("Error encountered", cause)
        }
    }

    install(Authentication) {
        basic("auth-basic") {
            realm = "Access to '/' path"
            validate { credentials ->
                try {
                    val user = MongoDBClient.getUser(credentials.name)

                    if (user.password == credentials.password)
                        UserIdPrincipal(credentials.name)
                    else
                        null
                } catch (e: NoSuchElementException) {
                    null
                }
            }
        }

        basic("auth-admin") {
            realm = "Access to '/admin' path"
            validate { credentials ->
                try {
                    val user = MongoDBClient.getUser(credentials.name)

                    if (user.password == credentials.password && user.admin)
                        UserIdPrincipal(credentials.name)
                    else
                        null
                } catch (e: NoSuchElementException) {
                    null
                }
            }
        }
    }

    v1Routes()
}
