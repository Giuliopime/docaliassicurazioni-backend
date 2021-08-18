package it.docaliassicurazioni.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val email: String,
    var password: String,
    val admin: Boolean,
    val name: String,
    val surname: String,
    val id_code: String,
    val files: MutableList<FileInfo> = mutableListOf(),
    val redirect: String? = null,
)

@Serializable
data class FileInfo(
    val id: String,
    var name: String
)

@Serializable
data class RenameFileInfo(
    val new_name: String
)
