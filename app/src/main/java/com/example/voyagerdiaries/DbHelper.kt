import android.content.Context
import com.example.voyagerdiaries.Property
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
        password: String,
        gender: String,
        is_landlord: Boolean? = false,
        phoneNumber: String? = "",
    ): Boolean {
        var userAdded = false;
        /*every password should be encrypted using any hashing algorithm
        here we use SHA-1 algorthm to convert user input to encrypted string.
         */

        val encryptedPassword = MessageDigest.getInstance("SHA-1").digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
        val query =
            "INSERT INTO users (first_name, last_name, username, password, gender, is_landlord, phone_number) values ('$firstName', '$lastName', '$userName', '$encryptedPassword', 'nil', '$is_landlord', '$phoneNumber') returning id"
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

    fun checkAndCreateTables(): Boolean{
        /*
        when MainActivity is looded we check if tables exists, if tables exists
        we do nothing if does not exist we create tables.
         */
        try {
            val statement = connection?.createStatement();
            val query = "SELECT table_name\n" +
                    "FROM information_schema.tables\n" +
                    "WHERE table_name = 'users'\n" +
                    "  AND table_schema = 'public';"
            val resultSet = statement?.executeQuery(query);
            var tableName = "";
            if (resultSet?.next() == true) {
                tableName = resultSet.getString("table_name")
            }
            if(tableName == ""){
                println("table does not exist")
                val usertablequery = "CREATE TABLE users ( id serial PRIMARY KEY, first_name character varying(50) NOT NULL, last_name character varying(50), username character varying(50) NOT NULL, password character varying(50) NOT NULL, is_active boolean DEFAULT true, is_admin boolean DEFAULT false, gender character varying(20) );"
                statement?.executeUpdate(usertablequery);
                statement?.executeUpdate("ALTER TABLE users ADD CONSTRAINT users_username_key UNIQUE (username);")
                statement?.executeQuery("insert into users values (1, 'admin', 'admin', 'admin', 'd033e22ae348aeb5660fc2140aec35850c4da997', true, true, 'Male') returning id;")
                val reviewTable = "CREATE TABLE reviews ( id serial PRIMARY KEY, user_id integer NOT NULL, review text NOT NULL, like_count integer DEFAULT 0, admin_reply character varying(255) DEFAULT '', dislike_count integer DEFAULT 0 );"
                statement?.executeUpdate(reviewTable);
                statement?.executeUpdate("ALTER TABLE reviews ADD CONSTRAINT reviews_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);")
                val likeReviewsTable = "CREATE TABLE liked_reviews ( id serial PRIMARY KEY, user_id integer NOT NULL, review_id integer NOT NULL );"
                statement?.executeUpdate(likeReviewsTable);
                statement?.executeUpdate("ALTER TABLE liked_reviews ADD CONSTRAINT unique_user_review UNIQUE (user_id, review_id);")
                val dislikeReviewsTable = "CREATE TABLE disliked_reviews ( id serial PRIMARY KEY, user_id integer NOT NULL, review_id integer NOT NULL );"
                statement?.executeUpdate(dislikeReviewsTable);
                statement?.executeUpdate("ALTER TABLE disliked_reviews ADD CONSTRAINT unique_user_review_dislike UNIQUE (user_id, review_id);")



            }else{
                println("table exist")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }


    fun authenticateUser(userName: String, password: String): List<Boolean> {
        /*
        when a user enter a username and password the password is encrypted
        and we check for an entry in database.
        if the database returns a row we set the username,id,first name and
        last name into sharedpreference which is like a localstorage in web browser
         */
        var authenticationSuccess = false;
        var loggedAsAdmin = false;
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
                val isAdmin = resultSet.getString("is_admin")
                loggedAsAdmin = resultSet.getBoolean("is_admin")
                val loggedAsLandlord = resultSet.getString("is_landlord")
                val voyagerdiariesPref =
                    context.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
                val editor = voyagerdiariesPref.edit()
                editor.putString("id", id.toString())
                editor.putString("firstName", firstName)
                editor.putString("userName", username)
                editor.putString("lastName", lastName)
                editor.putString("isAdmin", isAdmin)
                editor.putString("isLandlord", loggedAsLandlord)
                editor.apply()
                connection?.close()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return listOf(authenticationSuccess, loggedAsAdmin)
    }


    fun postProperty(name: String, description: String, price: Int): Boolean {
        /*
        we insert the review when a user post from review add screen
        we take the user ID from sharedpreference and insert the review text passed as parameter
         */
        var reviewAdded = true
        val voyagerdiariesPref =
            context.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
        val userId = voyagerdiariesPref.getString("id", null);
        val query =
            "insert into properties (name, user_id, description, price) values ('$name','$userId', '$description', '$price') returning id";
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

    fun getLandLordDetails(reviewId: Int): String {
        /*
        we update the review field admin_reply with reply posted by admin
        we take the reviewId as parameter.
         */
        var phone_number: String = ""

        val query =
            "select a.phone_number from users a join properties p on a.id=p.user_id where p.id='$reviewId'";
        print(query)
        try {
            val statement = connection?.createStatement();
            val resultSet = statement?.executeQuery(query);

            while (resultSet?.next() == true) {
                phone_number = resultSet.getString("phone_number")
            }
            connection?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        print(phone_number)
        print("::::::")
        return phone_number
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

    fun getAllProperty(userId: String? = null, usersReview: Boolean = false): MutableList<Property> {
        /*
        here we call id, name, user_id, description, price
         */
        val propertyList = mutableListOf<Property>();
        val propertyidList = mutableListOf<Int>()
        var query =
            "SELECT id, name, user_id, description, price FROM properties order by id desc";
        if (usersReview){
            query =
                "SELECT id, name, user_id, description, price FROM properties where user_id='$userId' order by desc";
        }
        println(query)

        try {
            val statement = connection?.createStatement();
            val resultSet = statement?.executeQuery(query);
            while (resultSet?.next() == true) {
                if(resultSet.getInt("id") !in propertyidList) {
                    propertyList.add(
                        Property(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("description"),
                            resultSet.getInt("price"),
                            )
                    )
                    propertyidList.add(resultSet.getInt("id"))
                }
            }
            connection?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return propertyList
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

    fun dislikeReview(userId: String, reviewId: Int): Boolean {
        var dislikedReview = false
        val statement = connection?.createStatement()

        try {
            val query = "INSERT INTO disliked_reviews (user_id, review_id) VALUES ($userId, $reviewId) RETURNING id"
            val resultSet = statement?.executeQuery(query)
            statement?.execute("UPDATE reviews SET dislike_count = dislike_count + 1 WHERE id = $reviewId RETURNING dislike_count")
            dislikedReview = true
            statement?.close()
        } catch (e: PSQLException) {
            if (e.message.toString().contains("duplicate key value violates unique constraint")) {
                val deleteQuery = "DELETE FROM disliked_reviews WHERE user_id = $userId AND review_id = $reviewId RETURNING id"
                statement?.execute(deleteQuery)
                statement?.execute("UPDATE reviews SET dislike_count = dislike_count - 1 WHERE id = $reviewId RETURNING dislike_count")
                dislikedReview = true
                statement?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.close()
        }

        return dislikedReview
    }


    fun deleteProperty(reviewId: Int): Boolean {
        /*
        when a user clicks a delete review button we simple execute the
        delete query and review is deleted based on review_id passed as parameter.
         */
        var deletedReview = false;
        val statement = connection?.createStatement();
        try {
            val query = "delete from properties where id=$reviewId returning id"
            val resultSet = statement?.executeQuery(query);
            deletedReview = true;

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.close()
        }
        return deletedReview
    }


    fun editProperty(reviewId: Int, name: String, description: String, price: Int) {
        /*
        the review passed as parameter is replaced using update query for review with review ID
        that is also passed as parameter.
         */
        val statement = connection?.createStatement();
        try {
            val query = "update properties set name='$name', description='$description', price='$price' where id=$reviewId returning id"
            val resultSet = statement?.executeQuery(query);

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.close()
        }
    }

}