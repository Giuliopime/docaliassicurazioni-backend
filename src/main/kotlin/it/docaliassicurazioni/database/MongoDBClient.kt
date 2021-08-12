package it.docaliassicurazioni.database

import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.ReturnDocument
import it.docaliassicurazioni.Env
import it.docaliassicurazioni.data.User
import org.litote.kmongo.*
import kotlin.jvm.Throws

object MongoDBClient {
    private val client = KMongo.createClient()
    private val database = client.getDatabase(Env.dbName)

    private val usersCollection = database.getCollection<User>("users")


    @Throws(NoSuchElementException::class)
    fun getUser(email: String) = usersCollection.findOne(User::email eq email)
        ?: throw NoSuchElementException()


    fun getAllUsers(): List<User> = usersCollection.find().toList()


    @Throws(NoSuchElementException::class)
    fun updateUser(user: User): User = usersCollection.findOneAndReplace(
        User::email eq user.email,
        user,
        FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER)
    ) ?: throw NoSuchElementException()


    fun createUser(user: User) {
        usersCollection.save(user)
    }


    @Throws(NoSuchElementException::class)
    fun deleteUser(email: String) {
        val deleted = usersCollection.deleteOne(User::email eq email).deletedCount > 0
        if (!deleted)
            throw NoSuchElementException()
    }
}
