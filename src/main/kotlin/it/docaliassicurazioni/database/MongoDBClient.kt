package it.docaliassicurazioni.database

import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.ReturnDocument
import it.docaliassicurazioni.Env
import it.docaliassicurazioni.data.User
import mu.KotlinLogging
import org.litote.kmongo.*

private val logger = KotlinLogging.logger {}

object MongoDBClient {
    private val client = KMongo.createClient()
    private val database = client.getDatabase(Env.dbName)

    private val usersCollection = database.getCollection<User>("users")

    init {
        try {
            getUser(Env.initAdminEmail)
        } catch (e: NoSuchElementException) {
            logger.info("Creating initial admin user")
            createUser(User(
                Env.initAdminEmail,
                Env.initAdminPassword,
                true,
                "initial admin",
                "initial admin",
                "initial_admin"
            ))
        }
    }

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
