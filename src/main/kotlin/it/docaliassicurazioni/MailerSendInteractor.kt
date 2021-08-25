package it.docaliassicurazioni

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import it.docaliassicurazioni.data.ForgotPasswordEmailBody
import it.docaliassicurazioni.data.FromData
import it.docaliassicurazioni.data.ToData
import it.docaliassicurazioni.data.User

object MailerSendInteractor {
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    suspend fun sendForgottenPasswordEmail(user: User) {
        client.post<Unit>("https://api.mailersend.com/v1/email") {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${Env.mailersendApiToken}")
            }

            contentType(ContentType.Application.Json)
            body = ForgotPasswordEmailBody(
                FromData("noreply@docaliassicurazioni.it", "Recupero password Docali&Dona assicurazioni"),
                mutableListOf(ToData(user.email, "${user.surname} ${user.name}")),
                "Recupero password portale documenti",
                "La tua password attuale risulta ${user.password}.\nEffettua il login su https://portale.docaliassicurazioni.it/login e successivamente cambia la password qui https://portale.docaliassicurazioni.it/cambiaPassword oppure elimina questa mail.",
                "<p>La tua password attuale risulta <i>${user.password}</i> .</p><p>Effettua il login su https://portale.docaliassicurazioni.it/login e <b>successivamente cambia la password qui https://portale.docaliassicurazioni.it/cambiaPassword oppure elimina questa mail.</b></p>"
            )
        }
    }
}
