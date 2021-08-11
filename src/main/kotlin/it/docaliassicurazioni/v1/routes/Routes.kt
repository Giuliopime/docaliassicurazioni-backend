package it.docaliassicurazioni.v1.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*

fun Application.v1Routes() {
    routing {
        route("/v1") {
            authenticate("auth-admin") {
                adminUserRoutes()
            }

            authenticate("auth-basic") {
                user()
            }
        }
    }
}
