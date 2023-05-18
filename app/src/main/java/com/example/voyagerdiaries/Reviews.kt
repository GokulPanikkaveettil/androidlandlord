package com.example.voyagerdiaries

import Database
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import org.postgresql.util.PSQLException
import java.sql.Connection
import java.sql.DriverManager
import kotlinx.coroutines.withContext


data class Review(val review: String, val fullName: String, val reviewId: Int, val liked: Int)

class ReviewViewHolder(itemView: View, listener: ItemAdapter.onItemClickListener) : RecyclerView.ViewHolder(itemView) {
    val reviewText: TextView = itemView.findViewById(R.id.reviewText);
    val userName: TextView = itemView.findViewById(R.id.reviewedUser);
    var reviewId: Int = 0;
    val likeButton: ImageView = itemView.findViewById(R.id.likeButton);

    init {
        likeButton.setOnClickListener{
            listener.onItemClick(adapterPosition, reviewId)
        }
    }
}

class ItemAdapter(private val reviews: List<Review>) : RecyclerView.Adapter<ReviewViewHolder>() {
    private lateinit var mListener : onItemClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_list_layout, parent, false)
        return ReviewViewHolder(itemView, mListener)
    }
    interface onItemClickListener {
        fun onItemClick(position: Int, reviewId: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.reviewText.text = review.review
        holder.userName.text = review.fullName
        holder.reviewId = review.reviewId

        if (review.liked == 1){
            holder.likeButton.setImageResource(R.drawable.baseline_thumb_up_24);
            holder.likeButton.setTag("unlike")
        }
        else{
            holder.likeButton.setTag("like")
        }
    }

    override fun getItemCount(): Int = reviews.size
}


class Reviews : AppCompatActivity() {
    var reviewList = mutableListOf<Review>();
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reviews)
        val voyagerdiariesPref = this.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
        val userId = voyagerdiariesPref.getString("id", null);

        coroutineScope.launch {
            reviewList = getReview(userId!!)
            val recyclerView = findViewById<RecyclerView>(R.id.allReviews)
            recyclerView.layoutManager = LinearLayoutManager(this@Reviews)
            val itemAdapter = ItemAdapter(reviewList)
            recyclerView.adapter = itemAdapter
            itemAdapter.setOnItemClickListener(object: ItemAdapter.onItemClickListener{
                override fun onItemClick(position: Int, reviewId: Int) {
                    val likebuttonHolder = recyclerView.findViewHolderForAdapterPosition(position)
                    val likedbutton = likebuttonHolder?.itemView?.findViewById<ImageView>(R.id.likeButton);
                    Toast.makeText(this@Reviews, likedbutton?.tag.toString(),Toast.LENGTH_SHORT).show()
                    if (likedbutton?.tag.toString() == "like"){
                        likedbutton?.setImageResource(R.drawable.baseline_thumb_up_24)
                        likedbutton?.setTag("unlike");
                    }
                    else {
                        likedbutton?.setImageResource(R.drawable.baseline_thumb_up_off_alt_24)
                        likedbutton?.setTag("like");
                    }
                    coroutineScope.launch {
                        val liked = likeReview(userId!!, reviewId)
                    }
                }
            })
        }
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationView);
        navbarActions(this, nav);




    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private suspend fun getReview(userId: String, usersReview: Boolean=false): MutableList<Review> = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = Database(this@Reviews)
            db.getAllReview(userId)
        } catch (e: Exception) {
            e.printStackTrace()
            reviewList
        }
    }

    private suspend fun likeReview(userId: String, reviewId: Int): Boolean = withContext(Dispatchers.IO) {
        var connection: Connection? = null
        val user = "zjcjmmse"
        val pass = "gp_LDmHthXvylqUAbb2S2okzyHYDLZj-"
        val url = "jdbc:postgresql://isilo.db.elephantsql.com:5432/zjcjmmse"
        var status = false

        return@withContext try {
            connection = DriverManager.getConnection(url, user, pass)
            val query = "insert into liked_reviews (user_id,review_id) values ($userId,$reviewId) returning id"
            val statement = connection?.createStatement()
            val resultSet = statement?.executeQuery(query)
            true
        } catch (e: PSQLException) {
            if (e.message.toString().contains("duplicate key value violates unique constraint")) {
                val statement = connection?.createStatement()
                val deleteQuery = "delete from liked_reviews where user_id=$userId and review_id=$reviewId returning id"
                statement?.executeQuery(deleteQuery)
            }
            false
        } finally {
            connection?.close()
        }
    }

}