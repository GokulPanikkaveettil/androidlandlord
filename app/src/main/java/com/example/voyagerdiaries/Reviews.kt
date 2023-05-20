package com.example.voyagerdiaries

import Database
import android.content.Context
import android.content.Intent
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


    data class Review(val review: String, val fullName: String, val reviewId: Int, val liked: Int, val likeCount: Int, val adminReply: String?)

class ReviewViewHolder(itemView: View, listener: ItemAdapter.onItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    val reviewText: TextView = itemView.findViewById(R.id.reviewText);
    val userName: TextView = itemView.findViewById(R.id.reviewedUser);
    val likeCount: TextView = itemView.findViewById(R.id.like_count);
    var reviewId: Int = 0;
    val likeButton: ImageView = itemView.findViewById(R.id.likeButton);
    val replyButton: ImageView = itemView.findViewById(R.id.reply);

    init {
        likeButton.setOnClickListener {
            listener.onItemClick(adapterPosition, reviewId, "like")
        }

        replyButton.setOnClickListener {
            listener.onItemClick(adapterPosition, reviewId, "reply")
        }
    }
}

class ItemAdapter(private val reviews: List<Review>, val isAdmin:String) : RecyclerView.Adapter<ReviewViewHolder>() {
    private lateinit var holderListener: onItemClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_list_layout, parent, false)
        return ReviewViewHolder(itemView, holderListener)
    }

    interface onItemClickListener {
        fun onItemClick(position: Int, reviewId: Int, action: String)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        holderListener = listener
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.reviewText.text = review.review
        holder.userName.text = review.fullName
        holder.reviewId = review.reviewId
        holder.likeCount.text = review.likeCount.toString()
        if(isAdmin == "f"){
            holder.replyButton.visibility = View.GONE
        }

        if (review.liked == 1) {
            holder.likeButton.setImageResource(R.drawable.baseline_thumb_up_24);
            holder.likeButton.setTag("unlike")
        } else {
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
        val voyagerdiariesPref =
            this.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
        val userId = voyagerdiariesPref.getString("id", null);
        val isAdmin = voyagerdiariesPref.getString("isAdmin", null);

        coroutineScope.launch {
            reviewList = getReview(userId!!)
            val recyclerView = findViewById<RecyclerView>(R.id.allReviews)
            recyclerView.layoutManager = LinearLayoutManager(this@Reviews)
            val itemAdapter = ItemAdapter(reviewList, isAdmin.toString())
            recyclerView.adapter = itemAdapter
            itemAdapter.setOnItemClickListener(object : ItemAdapter.onItemClickListener {
                override fun onItemClick(position: Int, reviewId: Int, action: String) {
                    if (action == "like") {
                        val likebuttonHolder =
                            recyclerView.findViewHolderForAdapterPosition(position)
                        val likeCountItem =
                            likebuttonHolder?.itemView?.findViewById<TextView>(R.id.like_count)
                        val likedbutton =
                            likebuttonHolder?.itemView?.findViewById<ImageView>(R.id.likeButton);
                        if (likedbutton?.tag.toString() == "like") {
                            likedbutton?.setImageResource(R.drawable.baseline_thumb_up_24)
                            likedbutton?.setTag("unlike");
                            val like_count = likeCountItem?.text.toString()
                            likeCountItem?.text = (like_count.toInt() + 1).toString()
                        } else {
                            likedbutton?.setImageResource(R.drawable.baseline_thumb_up_off_alt_24)
                            likedbutton?.setTag("like");
                            val like_count = likeCountItem?.text.toString()
                            likeCountItem?.text = (like_count.toInt() - 1).toString()
                        }
                        coroutineScope.launch {
                            val liked = likeReview(userId!!, reviewId)
                        }
                    }
                    if (action == "reply") {
                        Toast.makeText(this@Reviews, "Reply", Toast.LENGTH_SHORT).show()
                        val replyReviewIntent = Intent(this@Reviews, ReviewReply::class.java)
                        replyReviewIntent.putExtra("reviewId", reviewId.toString())
                        startActivity(replyReviewIntent)
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

    private suspend fun getReview(
        userId: String,
        usersReview: Boolean = false
    ): MutableList<Review> = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = Database(this@Reviews)
            db.getAllReview(userId)
        } catch (e: Exception) {
            e.printStackTrace()
            reviewList
        }
    }

    private suspend fun likeReview(userId: String, reviewId: Int): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val db = Database(this@Reviews)
                db.likeReview(userId, reviewId)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

}