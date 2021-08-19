package it.docaliassicurazioni.v1.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import it.docaliassicurazioni.cache.RedisClient
import it.docaliassicurazioni.data.ChangePasswordInfo
import it.docaliassicurazioni.data.Error
import it.docaliassicurazioni.data.SessionID
import it.docaliassicurazioni.database.MongoDBClient
import java.io.File

fun Route.userRoutes() {
    get("/user") {
        val sessionID = call.sessions.get<SessionID>()!!.id
        val sessionData = RedisClient.getSession(sessionID)
        val user = MongoDBClient.getUser(sessionData.email)
        call.respond(HttpStatusCode.OK, user)
    }

    get("/logout") {
        val sessionID = call.sessions.get<SessionID>()!!.id
        RedisClient.deleteSession(sessionID)
        call.respond(HttpStatusCode.OK)
    }

    post("/changePassword") {
        val sessionID = call.sessions.get<SessionID>()!!.id
        val sessionData = RedisClient.getSession(sessionID)
        val user = MongoDBClient.getUser(sessionData.email)

        val passwordInfo = call.receive<ChangePasswordInfo>()

        if (user.password != passwordInfo.old_password) {
            return@post call.respond(
                HttpStatusCode.Unauthorized,
                Error(
                    HttpStatusCode.Unauthorized.description,
                    "The provided old password is incorrect"
                )
            )
        }

        val newPassword = passwordInfo.new_password

        if (newPassword.length < 8 || newPassword.length > 50 || !newPassword.contains(Regex("\\d")) || !newPassword.contains(Regex("[a-zA-Z]"))) {
            return@post call.respond(
                HttpStatusCode.BadRequest,
                Error(
                    HttpStatusCode.BadRequest.description,
                    "The new password must contain at least one number, one letter, and it must be at least 8 characters long."
                )
            )
        }

        user.password = passwordInfo.new_password
        MongoDBClient.updateUser(user)
        call.respond(HttpStatusCode.OK)
    }

    get("/files/{id}") {
        val sessionID = call.sessions.get<SessionID>()!!.id
        val sessionData = RedisClient.getSession(sessionID)
        val user = MongoDBClient.getUser(sessionData.email)

        val fileID = call.parameters["id"]!!
        val fileData = user.files.firstOrNull { it.id == fileID }
            ?: return@get call.respond(
                HttpStatusCode.NotFound,
                Error(
                    HttpStatusCode.NotFound.description,
                    "File with ID $fileID not found."
                )
            )

        val file = File("files/${fileData.id}.${fileData.name.substringAfterLast('.', "txt")}")
        println(file.absolutePath)

        if (!file.exists())
            return@get call.respond(
                HttpStatusCode.NotFound,
                Error(
                    HttpStatusCode.NotFound.description,
                    "File with ID $fileID not found."
                )
            )


        call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, fileData.name)
                .toString()
        )

        call.respondFile(file)
    }
}
