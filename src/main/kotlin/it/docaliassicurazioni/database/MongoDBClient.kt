package it.docaliassicurazioni.database

import it.docaliassicurazioni.Env
import it.docaliassicurazioni.data.User
import org.litote.kmongo.KMongo
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import kotlin.jvm.Throws

object MongoDBClient {
    private val client = KMongo.createClient()
    private val database = client.getDatabase(Env.dbName)

    private val usersCollection = database.getCollection<User>("users")

    @Throws(NoSuchElementException::class)
    fun getUser(email: String) = usersCollection.findOne(User::email eq email)
        ?: throw NoSuchElementException()
}
