package it.docaliassicurazioni.v1.routes

import io.ktor.auth.*
import io.ktor.routing.*

fun Route.user() {
    get("/user/{email}") {

    }
}
