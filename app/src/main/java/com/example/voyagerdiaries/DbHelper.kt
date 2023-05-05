import android.widget.Toast
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
        val thread = Thread {
            val query = "INSERT INTO users (first_name, last_name, username, password) values ('$firstName', '$lastName', '$userName', '$password')"
            try {
                val statement = connection?.createStatement();
                val resultSet = statement?.executeQuery(query);
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
        return true
    }

}