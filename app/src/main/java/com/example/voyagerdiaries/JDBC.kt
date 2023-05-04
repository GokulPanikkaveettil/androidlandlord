import java.sql.DriverManager


// the model class
data class User(val id: Int, val name: String)

fun main() {

    val jdbcUrl = "jdbc:postgresql://localhost:5432/voyager_db"
    val connection = DriverManager
        .getConnection(jdbcUrl, "voyageradmin", "voyageradmin")
    println(connection.isValid(0))
    val query = connection.prepareStatement("SELECT * FROM users")
    val result = query.executeQuery()
    val users = mutableListOf<User>()

    while (result.next()) {
        val id = result.getInt("id")
        val name = result.getString("first_name")
        users.add(User(id, name))
    }
    println(users)
}

