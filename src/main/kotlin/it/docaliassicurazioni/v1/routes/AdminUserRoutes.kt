package it.docaliassicurazioni.v1.routes

import io.ktor.auth.*
import io.ktor.routing.*

fun Route.adminUserRoutes() {
    get("/admin/users") {

    }

    route("/admin/user/{email}") {
        get {

        }

        post {

        }

        put {

        }

        delete {

        }
    }
}
