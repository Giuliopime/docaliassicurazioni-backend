package it.docaliassicurazioni.v1.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import it.docaliassicurazioni.data.Error
import it.docaliassicurazioni.data.UserSession
import it.docaliassicurazioni.database.MongoDBClient

fun Route.userRoute() {
    get("/user") {
        val email = call.principal<UserSession>()!!.email
        val user = MongoDBClient.getUser(email)
        call.respond(user)
    }
}
