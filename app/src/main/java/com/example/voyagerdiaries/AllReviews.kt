package com.example.voyagerdiaries

import Database
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*
import kotlinx.coroutines.withContext


    data class Review(val review: String, val fullName: String, val reviewId: Int, val liked: Int, val likeCount: Int, val adminReply: String?, val dislikeCount: Int, val disliked: Int)

class ReviewViewHolder(itemView: View, listener: ItemAdapter.onItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    val reviewText: TextView = itemView.findViewById(R.id.reviewText);
    val userName: TextView = itemView.findViewById(R.id.reviewedUser);
    val likeCount: TextView = itemView.findViewById(R.id.like_count);
    val dislikeCount: TextView = itemView.findViewById(R.id.dislike_count);
    var reviewId: Int = 0;
    val likeButton: ImageView = itemView.findViewById(R.id.likeButton);
    val dislikeButton: ImageView = itemView.findViewById(R.id.dislikeButton);
    val replyButton: ImageView = itemView.findViewById(R.id.reply);

    init {
        likeButton.setOnClickListener {
            listener.onItemClick(adapterPosition, reviewId, "like")
        }

        dislikeButton.setOnClickListener {
            listener.onItemClick(adapterPosition, reviewId, "dislike")
        }

        replyButton.setOnClickListener {
            listener.onItemClick(adapterPosition, reviewId, "reply")
        }
    }
}

class ItemAdapter(private val reviews: List<Review>, val isAdmin: String) : RecyclerView.Adapter<ReviewViewHolder>() {
    private lateinit var holderListener: onItemClickListener

    // Create the item's view holder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        // Inflate the layout for the item view
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.review_list_layout, parent, false)
        return ReviewViewHolder(itemView, holderListener)
    }

    // Interface for item click listener
    interface onItemClickListener {
        fun onItemClick(position: Int, reviewId: Int, action: String)
    }

    // Configure the item click listener
    fun setOnItemClickListener(listener: onItemClickListener) {
        holderListener = listener
    }

    // Bind data to the view holder
    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        // Set the review text and user name
        holder.reviewText.text = review.review
        holder.userName.text = review.fullName
        holder.reviewId = review.reviewId
        holder.likeCount.text = review.likeCount.toString()
        holder.dislikeCount.text = review.dislikeCount.toString()

        // If the user is not an admin, check and hide the respond button.
        if (isAdmin == "f") {
            holder.replyButton.visibility = View.GONE
        }

        // Based on the review's liked status, set the like button's image and tag.
        if (review.liked == 1) {
            holder.likeButton.setImageResource(R.drawable.baseline_thumb_up_24)
            holder.likeButton.setTag("unlike")
        } else {
            holder.likeButton.setTag("like")
        }
        if (review.disliked == 1) {
            holder.dislikeButton.setImageResource(R.drawable.baseline_thumb_down_24)
            holder.dislikeButton.setTag("undislike")
        } else {
            holder.dislikeButton.setTag("dislike")
        }
    }

    override fun getItemCount(): Int = reviews.size
}



class Reviews : AppCompatActivity() {
    var reviewList = mutableListOf<Review>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reviews)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout);
        toggle = ActionBarDrawerToggle(this@Reviews, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        val navView = findViewById<NavigationView>(R.id.navView);

        navView.setNavigationItemSelectedListener {
            when (it.itemId){
                R.id.myprofile-> {
                    val mainIntent = Intent(this@Reviews, Profile::class.java)
                    startActivity(mainIntent)
                }
                R.id.home-> {
                    val mainIntent = Intent(this@Reviews, Reviews::class.java)
                    startActivity(mainIntent)
                }
                R.id.add_review_sidemenu-> {
                    val mainIntent = Intent(this@Reviews, CreateReviews::class.java)
                    startActivity(mainIntent)
                }
                R.id.logout_sidemenu-> {
                    val voyagerdiariesPref =
                        this@Reviews.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
                    val editor = voyagerdiariesPref.edit()
                    editor.remove("id")
                    editor.remove("firstName")
                    editor.remove("lastName")
                    editor.remove("userName")
                    editor.apply()
                    val mainIntent = Intent(this@Reviews, MainActivity::class.java)
                    mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(mainIntent)
                }
            }
            true
        }
        // Get user ID and admin status from shared preferences
        val voyagerdiariesPref = this.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
        val userId = voyagerdiariesPref.getString("id", null)
        val isAdmin = voyagerdiariesPref.getString("isAdmin", null)

        coroutineScope.launch {
            // Retrieve the review list from the Database class
            reviewList = getReview(userId!!)

            // Set up the RecyclerView
            val recyclerView = findViewById<RecyclerView>(R.id.allReviews)
            recyclerView.layoutManager = LinearLayoutManager(this@Reviews)
            val itemAdapter = ItemAdapter(reviewList, isAdmin.toString())
            recyclerView.adapter = itemAdapter

            // Set item click listener for the RecyclerView
            itemAdapter.setOnItemClickListener(object : ItemAdapter.onItemClickListener {
                override fun onItemClick(position: Int, reviewId: Int, action: String) {
                    if (action == "like") {
                        // Perform like action
                        val likebuttonHolder = recyclerView.findViewHolderForAdapterPosition(position)
                        val likeCountItem = likebuttonHolder?.itemView?.findViewById<TextView>(R.id.like_count)
                        val likedbutton = likebuttonHolder?.itemView?.findViewById<ImageView>(R.id.likeButton)

                        if (likedbutton?.tag.toString() == "like") {
                            likedbutton?.setImageResource(R.drawable.baseline_thumb_up_24)
                            likedbutton?.setTag("unlike")
                            val like_count = likeCountItem?.text.toString()
                            likeCountItem?.text = (like_count.toInt() + 1).toString()
                        } else {
                            likedbutton?.setImageResource(R.drawable.baseline_thumb_up_off_alt_24)
                            likedbutton?.setTag("like")
                            val like_count = likeCountItem?.text.toString()
                            likeCountItem?.text = (like_count.toInt() - 1).toString()
                        }

                        // Perform like action on the review using coruotines asyncronously
                        coroutineScope.launch {
                            val liked = likeReview(userId!!, reviewId)
                        }
                    }

                    if (action == "dislike") {
                        // Perform like action
                        val dislikebuttonHolder = recyclerView.findViewHolderForAdapterPosition(position)
                        val dislikeCountItem = dislikebuttonHolder?.itemView?.findViewById<TextView>(R.id.dislike_count)
                        val dislikedbutton = dislikebuttonHolder?.itemView?.findViewById<ImageView>(R.id.dislikeButton)

                        if (dislikedbutton?.tag.toString() == "dislike") {
                            dislikedbutton?.setImageResource(R.drawable.baseline_thumb_down_24)
                            dislikedbutton?.setTag("undislike")
                            val dislike_count = dislikeCountItem?.text.toString()
                            dislikeCountItem?.text = (dislike_count.toInt() + 1).toString()
                        } else {
                            dislikedbutton?.setImageResource(R.drawable.baseline_thumb_down_off_alt_24)
                            dislikedbutton?.setTag("dislike")
                            val dislike_count = dislikeCountItem?.text.toString()
                            dislikeCountItem?.text = (dislike_count.toInt() - 1).toString()
                        }

                        // Perform dislike action on the review using coruotines asyncronously
                        coroutineScope.launch {
                            val disliked = dislikeReview(userId!!, reviewId)
                        }
                    }

                    if (action == "reply") {
                        // Start the ReviewReply activity to reply to a review
                        val replyReviewIntent = Intent(this@Reviews, ReviewReply::class.java)
                        replyReviewIntent.putExtra("reviewId", reviewId.toString())
                        startActivity(replyReviewIntent)
                    }
                }
            })
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    // Function to retrieve reviews from the Database helper class
    private suspend fun getReview(userId: String, usersReview: Boolean = false): MutableList<Review> = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = Database(this@Reviews)
            db.getAllReview(userId)
        } catch (e: Exception) {
            e.printStackTrace()
            reviewList
        }
    }

    // Function to perform like action on a review
    private suspend fun likeReview(userId: String, reviewId: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = Database(this@Reviews)
            db.likeReview(userId, reviewId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Function to perform dislike action on a review
    private suspend fun dislikeReview(userId: String, reviewId: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = Database(this@Reviews)
            db.dislikeReview(userId, reviewId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            true
        }
        return super.onOptionsItemSelected(item)
    }
}
