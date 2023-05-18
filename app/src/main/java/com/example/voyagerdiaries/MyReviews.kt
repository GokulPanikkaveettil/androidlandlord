package com.example.voyagerdiaries

import Database
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*

class MyReviewViewHolder(itemView: View, listener: MyReviewsItemAdapter.onItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    val reviewText: TextView = itemView.findViewById(R.id.reviewText);
    val userName: TextView = itemView.findViewById(R.id.reviewedUser);
    val likeCount: TextView = itemView.findViewById(R.id.like_count);
    var reviewId: Int = 0;
    val likeButton: ImageView = itemView.findViewById(R.id.likeButton);
    val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton);
    val editButton: ImageView = itemView.findViewById(R.id.editButton);

    init {
        likeButton.setOnClickListener {
            listener.onItemClick(adapterPosition, reviewId, "like")
        }
        deleteButton.setOnClickListener {
            listener.onItemClick(adapterPosition, reviewId, "delete")
        }
        editButton.setOnClickListener {
            listener.onItemClick(adapterPosition, reviewId, "edit")
        }
    }
}

class MyReviewsItemAdapter(private val reviews: List<Review>) :
    RecyclerView.Adapter<MyReviewViewHolder>() {
    private lateinit var holderListener: onItemClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_review_list_layout, parent, false)
        return MyReviewViewHolder(itemView, holderListener)
    }

    interface onItemClickListener {
        fun onItemClick(position: Int, reviewId: Int, action: String)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        holderListener = listener
    }

    override fun onBindViewHolder(holder: MyReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.reviewText.text = review.review
        holder.userName.text = review.fullName
        holder.reviewId = review.reviewId
        holder.likeCount.text = review.likeCount.toString()
        if (review.liked == 1) {
            holder.likeButton.setImageResource(R.drawable.baseline_thumb_up_24);
            holder.likeButton.setTag("unlike")
        } else {
            holder.likeButton.setTag("like")
        }
    }

    override fun getItemCount(): Int = reviews.size
}

class MyReviews : AppCompatActivity() {
    var reviewList = mutableListOf<Review>();
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_reviews)

        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationView);
        navbarActions(this, nav);
        val selectedItem = nav.menu.findItem(R.id.navbar_profile)
        selectedItem?.setChecked(true)
        coroutineScope.launch {
            val voyagerdiariesPref =
                this@MyReviews.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
            val userId = voyagerdiariesPref.getString("id", null);
            reviewList = getReview(userId!!)
            val recyclerView = findViewById<RecyclerView>(R.id.myReviews)
            recyclerView.layoutManager = LinearLayoutManager(this@MyReviews)
            val itemAdapter = MyReviewsItemAdapter(reviewList)
            recyclerView.adapter = itemAdapter
            itemAdapter.setOnItemClickListener(object : MyReviewsItemAdapter.onItemClickListener {
                override fun onItemClick(position: Int, reviewId: Int, action: String) {
                    val buttonHolder = recyclerView.findViewHolderForAdapterPosition(position)
                    if (action == "like") {
                        val likedbutton =
                            buttonHolder?.itemView?.findViewById<ImageView>(R.id.likeButton);
                        coroutineScope.launch {
                            likeReview(userId, reviewId)
                        }

                        if (likedbutton?.tag.toString() == "like") {
                            likedbutton?.setImageResource(R.drawable.baseline_thumb_up_24)
                            likedbutton?.setTag("unlike");
                            val like_count = buttonHolder?.itemView?.findViewById<TextView>(R.id.like_count)?.text.toString()
                            buttonHolder?.itemView?.findViewById<TextView>(R.id.like_count)?.text = (like_count.toInt() + 1).toString()
                        } else {
                            likedbutton?.setImageResource(R.drawable.baseline_thumb_up_off_alt_24)
                            likedbutton?.setTag("like");
                            val like_count = buttonHolder?.itemView?.findViewById<TextView>(R.id.like_count)?.text.toString()
                            buttonHolder?.itemView?.findViewById<TextView>(R.id.like_count)?.text = (like_count.toInt() - 1).toString()
                        }
                    } else if (action == "delete") {
                        coroutineScope.launch {
                            deleteReview(reviewId)
                            reviewList.removeAt(position)
                            itemAdapter.notifyItemRemoved(position)
                        }
                    } else if (action == "edit") {
                        val reviewText =
                            buttonHolder?.itemView?.findViewById<TextView>(R.id.reviewText)
                        val editReviewIntent = Intent(this@MyReviews, EditReview::class.java)
                        editReviewIntent.putExtra("review", reviewText?.text.toString())
                        editReviewIntent.putExtra("reviewId", reviewId.toString())
                        startActivity(editReviewIntent)
                    }
                }

            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private suspend fun deleteReview(reviewId: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = Database(this@MyReviews)
            db.deleteReview(reviewId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun likeReview(userId: String, reviewId: Int): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val db = Database(this@MyReviews)
                db.likeReview(userId, reviewId)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    private suspend fun getReview(
        userId: String,
        usersReview: Boolean = true
    ): MutableList<Review> = withContext(
        Dispatchers.IO
    ) {
        return@withContext try {
            val db = Database(this@MyReviews)
            db.getAllReview(userId, usersReview)
        } catch (e: Exception) {
            e.printStackTrace()
            reviewList
        }
    }
}