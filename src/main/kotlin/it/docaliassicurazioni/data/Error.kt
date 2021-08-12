package it.docaliassicurazioni.data

import kotlinx.serialization.Serializable

@Serializable
data class Error(
    val code: String,
    val message: String
)
