package it.docaliassicurazioni.data

data class User(
    val email: String,
    val password: String,
    val admin: Boolean,
    val name: String,
    val surname: String,
    val id_code: String
)
