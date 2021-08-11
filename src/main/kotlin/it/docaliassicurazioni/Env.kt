package it.docaliassicurazioni

import io.github.cdimascio.dotenv.dotenv

object Env {
    private val dotenv = dotenv()

    val dbName = get("db.name")

    private fun get(key: String): String {
        val value = dotenv[key.replace(".", "_").uppercase()]

        if (value == null)
            shutdown("Missing .env key: $key")

        return value
    }
}
