package it.docaliassicurazioni.data

import io.ktor.auth.*
import kotlinx.serialization.Serializable

@Serializable
data class SessionID(
    val id: String
): Principal
