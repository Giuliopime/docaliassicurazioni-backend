package it.docaliassicurazioni

import io.github.cdimascio.dotenv.dotenv

object Env {
    private val dotenv = dotenv()

    val testing
        get () = getBoolean("testing")

    val dbName
        get () = get("db.name")
    val initAdminEmail
        get () = get("init.admin.email")
    val initAdminPassword
        get () = get("init.admin.password")

    val mailersendApiToken
        get () = get("mailersend.api.token")

    private fun get(key: String): String = dotenv[key.replace(".", "_").uppercase()]
        ?: shutdown("Missing .env key: $key")

    private fun getBoolean(key: String): Boolean = get(key).toBooleanStrictOrNull()
        ?: shutdown("Missing .env boolean key: $key")
}
