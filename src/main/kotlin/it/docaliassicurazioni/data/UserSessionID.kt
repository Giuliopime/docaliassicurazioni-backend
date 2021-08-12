package it.docaliassicurazioni.data

import io.ktor.auth.*
import io.ktor.util.date.*
import kotlinx.serialization.Serializable

@Serializable
data class UserSessionID(
    val id: String
): Principal
