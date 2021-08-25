package it.docaliassicurazioni.data

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordEmailBody(
    val from: FromData,
    val to: MutableList<ToData>,
    val subject: String,
    val text: String,
    val html: String,
)

@Serializable
data class FromData(
    val email: String,
    val name: String
)

@Serializable
data class ToData(
    val email: String,
    val name: String
)
