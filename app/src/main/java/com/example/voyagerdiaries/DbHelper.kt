import android.content.Context
import com.example.voyagerdiaries.Review
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.postgresql.util.PSQLException
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager


class Database(context: Context) {
    private var connection: Connection? = null
    private val user = "zjcjmmse"
    private val pass = "gp_LDmHthXvylqUAbb2S2okzyHYDLZj-"
    private var url = "jdbc:postgresql://isilo.db.elephantsql.com:5432/zjcjmmse"
    private var status = false
    private var context: Context = context

    init {
        /*
        when Database object is called immediately connect function is called
        and variable connection value is set to Drivermanager.getConnection
         */
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

    fun addNewUser(
        firstName: String,
        lastName: String,
        userName: String,
        password: String
    ): Boolean {
        var userAdded = false;
        /*every password should be encrypted using any hashing algorithm
        here we use SHA-1 algorthm to convert user input to encrypted string.
         */

        val encryptedPassword = MessageDigest.getInstance("SHA-1").digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
        val query =
            "INSERT INTO users (first_name, last_name, username, password) values ('$firstName', '$lastName', '$userName', '$encryptedPassword') returning id"
        try {
            val statement = connection?.createStatement();
            val resultSet = statement?.executeQuery(query);
            userAdded = true;

        } catch (e: Exception) {
            userAdded = false;
            e.printStackTrace()
        } finally {
            connection?.close()
        }
        return userAdded
    }


    fun authenticateUser(userName: String, password: String): Boolean {
        /*
        when a user enter a username and password the password is encrypted
        and we check for an entry in database.
        if the database returns a row we set the username,id,first name and
        last name into sharedpreference which is like a localstorage in web browser
         */
        var authenticationSuccess = false;
        val encryptedPassword = MessageDigest.getInstance("SHA-1").digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
        val query =
            "select * from  users where username='$userName' and password='$encryptedPassword' and is_active=TRUE"

        try {
            val statement = connection?.createStatement();
            val resultSet = statement?.executeQuery(query);
            if (resultSet?.next() == true) {
                authenticationSuccess = true
                val id = resultSet.getInt("id")
                val firstName = resultSet.getString("first_name")
                val lastName = resultSet.getString("last_name")
                val username = resultSet.getString("username")
                val voyagerdiariesPref =
                    context.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
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


    fun postUserReviews(reviews: String): Boolean {
        /*
        we insert the review when a user post from review add screen
        we take the user ID from sharedpreference and insert the review text passed as parameter
         */
        var reviewAdded = true
        val voyagerdiariesPref =
            context.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
        val userId = voyagerdiariesPref.getString("id", null);
        val query =
            "insert into reviews (review, user_id) values ('$reviews','$userId') returning id";
        try {
            val statement = connection?.createStatement();
            val resultSet = statement?.executeQuery(query);

        } catch (e: Exception) {
            e.printStackTrace()
            reviewAdded = false
        } finally {
            connection?.close()
        }
        return reviewAdded
    }

    fun updateProfile(firstName: String, lastName: String) {
        /*
        here we update the user's first name and lastname using update SQL query
        we do not allow user to change username.
         */
        val voyagerdiariesPref =
            context.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
        val userId = voyagerdiariesPref.getString("id", null);
        val query =
            "update users set first_name='$firstName', last_name='$lastName' where id=$userId returning id";
        try {
            val statement = connection?.createStatement();
            val resultSet = statement?.executeQuery(query);
            if (resultSet?.next() == true) {
                val editor = voyagerdiariesPref.edit();
                editor.putString("firstName", firstName);
                editor.putString("lastName", lastName);
                editor.apply()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.close()
        }
    }

    fun getAllReview(userId: String? = null, usersReview: Boolean = false): MutableList<Review> {
        /*
        here we call review,username,id liked and like_count using join queries
        and return a mutable list of Review object
         */
        val reviewList = mutableListOf<Review>();
        var query =
            "select a.review,b.username,a.id from reviews a join users b on a.user_id=b.id where b.is_active=TRUE order by a.id desc;";
        if (userId!!.isNotEmpty()) {
            query =
                "SELECT r.review, u.username, r.id, CASE WHEN l.user_id IS NULL THEN 0 ELSE 1 END AS liked,like_count FROM reviews r LEFT JOIN liked_reviews l ON l.review_id = r.id AND l.user_id = $userId JOIN users u ON r.user_id = u.id where u.is_active=TRUE ORDER BY r.id DESC; "
            if (usersReview) {
                query = query.replace("ORDER BY", "WHERE r.user_id=$userId ORDER BY")
            }
        }
        try {
            val statement = connection?.createStatement();
            val resultSet = statement?.executeQuery(query);
            while (resultSet?.next() == true) {
                reviewList.add(
                    Review(
                        resultSet.getString("review"),
                        resultSet.getString("username"),
                        resultSet.getInt("id"),
                        resultSet.getInt("liked"),
                        resultSet.getInt("like_count")

                    )
                )
            }
            connection?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return reviewList
    }

    fun likeReview(userId: String, reviewId: Int): Boolean {
        var likedReview = false;
        val statement = connection?.createStatement();
        /*
        we have table liked_reviews and user_id and review_id is inserted into the
        table and along with that we also increment or decrement the like_count in reviews table
        based on the outcome of insert query execution.
         */
        try {
            val query =
                "insert into liked_reviews (user_id,review_id) values ($userId,$reviewId) returning id"

            val resultSet = statement?.executeQuery(query);
            statement?.execute("UPDATE reviews SET like_count = like_count + 1 WHERE id =$reviewId returning like_count")
            likedReview = true;
            statement?.close()
        } catch (e: PSQLException) {
            if (e.message.toString().contains("duplicate key value violates unique constraint")) {
                val deleteQuery =
                    "delete from liked_reviews where user_id=$userId and review_id=$reviewId returning id"
                statement?.execute(deleteQuery);
                statement?.execute("UPDATE reviews SET like_count = like_count - 1 WHERE id =$reviewId returning like_count")
                statement?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.close()
        }
        return likedReview
    }

    fun deleteReview(reviewId: Int): Boolean {
        /*
        when a user clicks a delete review button we simple execute the
        delete query and review is deleted based on review_id passed as parameter.
         */
        var deletedReview = false;
        val statement = connection?.createStatement();
        try {
            val query = "delete from reviews where id=$reviewId returning id"
            val resultSet = statement?.executeQuery(query);
            deletedReview = true;

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.close()
        }
        return deletedReview
    }


    fun editReview(reviewId: Int, review: String) {
        /*
        the review passed as parameter is replaced using update query for review with review ID
        that is also passed as parameter.
         */
        val statement = connection?.createStatement();
        try {
            val query = "update reviews set review='$review' where id=$reviewId returning id"
            val resultSet = statement?.executeQuery(query);

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.close()
        }
    }

}