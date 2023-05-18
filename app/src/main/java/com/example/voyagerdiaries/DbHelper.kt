import android.content.Context
import com.example.voyagerdiaries.Review
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.postgresql.util.PSQLException
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager


class Database (context: Context){
    private var connection: Connection? = null
    private val user = "zjcjmmse"
    private val pass = "gp_LDmHthXvylqUAbb2S2okzyHYDLZj-"
    private var url = "jdbc:postgresql://isilo.db.elephantsql.com:5432/zjcjmmse"
    private var status = false
    private var context: Context = context
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        connect()
        println("connection status:$status")
    }

    private fun connect() {
        try {
            Class.forName("org.postgresql.Driver")
            connection = DriverManager.getConnection(url, user, pass)
            status = true
        } catch (e: Exception) {
            status = false
            print(e.message)
            e.printStackTrace()
        }
    }

    fun addNewUser(firstName: String, lastName: String, userName: String, password: String): Boolean {
        var userAdded = false;
        val thread = Thread {
            val encryptedPassword = MessageDigest.getInstance("SHA-1").digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
            val query = "INSERT INTO users (first_name, last_name, username, password) values ('$firstName', '$lastName', '$userName', '$encryptedPassword') returning id"
            try {
                val statement = connection?.createStatement();
                val resultSet = statement?.executeQuery(query);
                userAdded = true;
                connection?.close()
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
        }
        return userAdded
    }


    fun authenticateUser(userName: String, password: String): Boolean{
        var authenticationSuccess = false;
            val encryptedPassword = MessageDigest.getInstance("SHA-1").digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
            val query = "select * from  users where username='$userName' and password='$encryptedPassword'"

            try {
                val statement = connection?.createStatement();
                val resultSet = statement?.executeQuery(query);
                if(resultSet?.next() == true){
                    authenticationSuccess = true
                    val id = resultSet.getInt("id")
                    val firstName = resultSet.getString("first_name")
                    val lastName = resultSet.getString("last_name")
                    val username = resultSet.getString("username")
                    val voyagerdiariesPref = context.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
                    val editor = voyagerdiariesPref.edit()
                    editor.putString("id", id.toString())
                    editor.putString("firstName", firstName)
                    editor.putString("userName", username)
                    editor.putString("lastName", lastName)
                    editor.apply()
                    connection?.close()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        return authenticationSuccess
    }


    fun postUserReviews(reviews: String){
            val voyagerdiariesPref = context.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
            val userId = voyagerdiariesPref.getString("id", null);
            val query = "insert into reviews (review, user_id) values ('$reviews','$userId') returning id";
            try {
                val statement = connection?.createStatement();
                val resultSet = statement?.executeQuery(query);
                connection?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
    }

    fun updateProfile(firstName: String, lastName: String){
            val voyagerdiariesPref = context.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
            val userId = voyagerdiariesPref.getString("id", null);
            val query = "update users set first_name='$firstName', last_name='$lastName' where id=$userId returning id";
            try {
                val statement = connection?.createStatement();
                val resultSet = statement?.executeQuery(query);
                if(resultSet?.next() == true){
                    val editor = voyagerdiariesPref.edit();
                    editor.putString("firstName", firstName);
                    editor.putString("lastName", lastName);
                    editor.apply()
                }
                connection?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
    }

    fun getAllReview(userId: String? = null, usersReview: Boolean = false): MutableList<Review>{
        val reviewList = mutableListOf<Review>();
            var query = "select a.review,b.username,a.id from reviews a join users b on a.user_id=b.id order by a.id desc;";
            if (userId!!.isNotEmpty()){
                query = "SELECT r.review, u.username, r.id, CASE WHEN l.user_id IS NULL THEN 0 ELSE 1 END AS liked FROM reviews r LEFT JOIN liked_reviews l ON l.review_id = r.id AND l.user_id = $userId JOIN users u ON r.user_id = u.id ORDER BY r.id DESC; "
                if(usersReview){
                    query = query.replace("ORDER BY", "WHERE r.user_id=$userId ORDER BY")
                }
            }
            try {
                val statement = connection?.createStatement();
                val resultSet = statement?.executeQuery(query);
                while (resultSet?.next() == true) {
                    reviewList.add(Review(resultSet.getString("review"), resultSet.getString("username"), resultSet.getInt("id"), resultSet.getInt("liked")))
                }
                connection?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        return reviewList
    }

    fun likeReview(userId: String, reviewId: Int): Boolean{
        var likedReview = false;
            val statement = connection?.createStatement();
            try {
                val query =
                    "insert into liked_reviews (user_id,review_id) values ($userId,$reviewId) returning id"

                val resultSet = statement?.executeQuery(query);
                likedReview = true;
                connection?.close()
            } catch (e: PSQLException){
                if(e.message.toString().contains("duplicate key value violates unique constraint")){
                    val deleteQuery = "delete from liked_reviews where user_id=$userId and review_id=$reviewId returning id"
                    statement?.executeQuery(deleteQuery)
                }
            }

            catch (e: Exception) {
                e.printStackTrace()
            }
        return likedReview
    }

    fun deleteReview(reviewId: Int): Boolean{
        var deletedReview = false;
            val statement = connection?.createStatement();
            try {
                val query = "delete from reviews where id=$reviewId returning id"
                val resultSet = statement?.executeQuery(query);
                deletedReview = true;
                connection?.close()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        return deletedReview
    }


    fun editReview(reviewId: Int, review: String){
            val statement = connection?.createStatement();
            try {
                val query = "update reviews set review='$review' where id=$reviewId returning id"
                val resultSet = statement?.executeQuery(query);
                connection?.close()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
    }

}