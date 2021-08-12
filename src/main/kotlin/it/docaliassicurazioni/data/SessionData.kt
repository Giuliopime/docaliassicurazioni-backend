package it.docaliassicurazioni.data

import io.ktor.util.date.*
import kotlinx.serialization.Serializable

@Serializable
data class SessionData(
    val id: String,
    val email: String,
    val creationTimestamp: Long = getTimeMillis()
)
