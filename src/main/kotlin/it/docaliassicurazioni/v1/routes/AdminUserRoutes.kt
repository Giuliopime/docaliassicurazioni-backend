package it.docaliassicurazioni.v1.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import it.docaliassicurazioni.data.Error
import it.docaliassicurazioni.data.User
import it.docaliassicurazioni.database.MongoDBClient

fun Route.adminUserRoutes() {
    get("/admin/users") {
        call.respond(MongoDBClient.getAllUsers())
    }

    route("/admin/user/{email}") {
        get {
            val email = call.parameters["email"]!!
            val user = MongoDBClient.getUser(email)
            call.respond(user)
        }

        post {
            val email = call.parameters["email"]!!
            val userUpdated = call.receive<User>()
            if (email != userUpdated.email) {
                call.respond(HttpStatusCode.BadRequest, Error(HttpStatusCode.BadRequest.toString(), "The email parameter and the email property in the request body don't match."))
                return@post
            }

            MongoDBClient.updateUser(userUpdated)
            call.respond(HttpStatusCode.OK)
        }

        put {
            val email = call.parameters["email"]!!
            val newUser = call.receive<User>()
            if (email != newUser.email) {
                call.respond(HttpStatusCode.BadRequest, Error(HttpStatusCode.BadRequest.toString(), "The email parameter and the email property in the request body don't match."))
                return@put
            }

            MongoDBClient.createUser(newUser)
            call.respond(HttpStatusCode.OK)
        }

        delete {
            val email = call.parameters["email"]!!
            MongoDBClient.deleteUser(email)
            call.respond(HttpStatusCode.OK)
        }
    }
}
