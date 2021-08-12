package it.docaliassicurazioni.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val email: String,
    var password: String,
    val admin: Boolean,
    val name: String,
    val surname: String,
    val id_code: String
)
