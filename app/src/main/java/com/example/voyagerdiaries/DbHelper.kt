import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager

class Database {
    private var connection: Connection? = null
    private val user = "voyageradmin"
    private val pass = "voyageradmin"
    private var url = "jdbc:postgresql://10.0.2.2:5432/voyager_db"
    private var status = false

    init {
        connect()
        println("connection status:$status")
    }

    private fun connect() {
        val thread = Thread {
            try {
                Class.forName("org.postgresql.Driver")
                println(url)
                connection = DriverManager.getConnection(url, user, pass)
                status = true
                println("connected:$status")
            } catch (e: Exception) {
                status = false
                print(e.message)
                e.printStackTrace()
            }
        }
        thread.start()
        try {
            thread.join()
        } catch (e: Exception) {
            e.printStackTrace()
            status = false
        }
    }

    fun addNewUser(firstName: String, lastName: String, userName: String, password: String): Boolean {
        var userAdded = false;
        val thread = Thread {
            val encryptedPassword = MessageDigest.getInstance("SHA-1").digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
            val query = "INSERT INTO users (first_name, last_name, username, password) values ('$firstName', '$lastName', '$userName', '$encryptedPassword')"
            try {
                val statement = connection?.createStatement();
                val resultSet = statement?.executeQuery(query);
                userAdded = true;
            } catch (e: Exception) {
                userAdded = false;
                e.printStackTrace()
            }
        }
        thread.start()
        try {
            thread.join()
        } catch (e: Exception) {
            e.printStackTrace()
            status = false
        }
        return userAdded
    }


    fun authenticateUser(userName: String, password: String): Boolean{
        var authenticationSuccess = false;
        val thread = Thread {
            val encryptedPassword = MessageDigest.getInstance("SHA-1").digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
            val query = "select * from  users where username='$userName' and password='$encryptedPassword'"

            try {
                val statement = connection?.createStatement();
                val resultSet = statement?.executeQuery(query);
                if(resultSet?.next() == true){
                    authenticationSuccess = true
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
        try {
            thread.join()
        } catch (e: Exception) {
            e.printStackTrace()
            status = false
        }
        return authenticationSuccess
    }

}