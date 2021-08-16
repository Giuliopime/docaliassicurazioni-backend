package it.docaliassicurazioni.v1.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import it.docaliassicurazioni.cache.RedisClient
import it.docaliassicurazioni.data.*
import it.docaliassicurazioni.database.MongoDBClient
import java.io.File
import java.util.*
import kotlin.NoSuchElementException

fun Route.adminUserRoutes() {
    get("/admin/users") {
        call.respond(MongoDBClient.getAllUsers())
    }

    route("/admin/user/{email}") {
        get {
            val email = call.parameters["email"]!!
            val user = MongoDBClient.getUser(email)
            call.respond(HttpStatusCode.OK, user)
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

            try {
                MongoDBClient.getUser(email)
                return@put call.respond(
                    HttpStatusCode.MethodNotAllowed,
                    Error(
                        HttpStatusCode.MethodNotAllowed.description,
                        "A user with that email already exists"
                    )
                )
            } catch (ignored: NoSuchElementException) {}

            MongoDBClient.createUser(newUser)
            call.respond(HttpStatusCode.OK)
        }

        delete {
            val email = call.parameters["email"]!!
            MongoDBClient.deleteUser(email)
            call.respond(HttpStatusCode.OK)
        }

        route("/files") {
            get("/{id}") {
                val email = call.parameters["email"]!!
                val user = MongoDBClient.getUser(email)

                val fileID = call.parameters["id"]!!
                val fileData = user.files.firstOrNull { it.id == fileID }
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        Error(
                            HttpStatusCode.NotFound.description,
                            "File with ID $fileID not found for user $email."
                        )
                    )

                val file = File("files/${fileData.id}.${fileData.name.substringAfterLast('.', "txt")}")

                if (!file.exists())
                    return@get call.respond(
                        HttpStatusCode.NotFound,
                        Error(
                            HttpStatusCode.NotFound.description,
                            "File with ID $fileID not found for user $email."
                        )
                    )

                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, fileData.name)
                        .toString()
                )
                call.respondFile(file)
            }

            post("/{id}") {
                val newFileInfo = call.receive<RenameFileInfo>()

                val email = call.parameters["email"]!!
                val user = MongoDBClient.getUser(email)

                val fileID = call.parameters["id"]!!
                val fileData = user.files.firstOrNull { it.id == fileID }
                    ?: return@post call.respond(
                        HttpStatusCode.NotFound,
                        Error(
                            HttpStatusCode.NotFound.description,
                            "File with ID $fileID not found for user $email."
                        )
                    )

                fileData.name = newFileInfo.new_name
                MongoDBClient.updateUser(user)

                call.respond(HttpStatusCode.OK)
            }

            put {
                val email = call.parameters["email"]!!
                val user = MongoDBClient.getUser(email)

                val multipartData = call.receiveMultipart()

                multipartData.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val fileName = part.originalFileName as String
                        val fileID = UUID.randomUUID().toString()
                        var fileBytes = part.streamProvider().readBytes()
                        File("files/${fileID}.${fileName.substringAfterLast('.', "txt")}").writeBytes(fileBytes)

                        user.files.add(FileInfo(
                            fileID,
                            fileName
                        ))
                    }
                }

                MongoDBClient.updateUser(user)
                call.respond(HttpStatusCode.OK)
            }

            delete("/{id}") {
                val email = call.parameters["email"]!!
                val user = MongoDBClient.getUser(email)

                val fileID = call.parameters["id"]!!
                val fileIndex = user.files.indexOfFirst { it.id == fileID }
                if (fileIndex == -1)
                    return@delete call.respond(
                        HttpStatusCode.NotFound,
                        Error(
                            HttpStatusCode.NotFound.description,
                            "File with ID $fileID not found for user $email."
                        )
                    )

                val file = File("files/${fileID}.${user.files[fileIndex].name.substringAfterLast('.', "txt")}")
                if (file.exists())
                    file.delete()

                user.files.removeAt(fileIndex)
                MongoDBClient.updateUser(user)

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
