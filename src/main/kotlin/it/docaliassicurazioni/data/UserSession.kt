package it.docaliassicurazioni.data

import io.ktor.auth.*
import io.ktor.util.date.*
import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val id: String,
    val email: String,
    val creationTimestamp: Long = getTimeMillis()
): Principal
