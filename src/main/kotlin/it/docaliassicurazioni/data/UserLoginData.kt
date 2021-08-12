package it.docaliassicurazioni.data

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginData(
    val email: String,
    val password: String
)
