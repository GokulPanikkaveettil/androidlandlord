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
    data class Property(val id: Int, val name: String, val user_id: Int, val description: String, val price: Int);
class ReviewViewHolder(itemView: View, listener: ItemAdapter.onItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    val name: TextView = itemView.findViewById(R.id.name);
    val description: TextView = itemView.findViewById(R.id.description);
    val price: TextView = itemView.findViewById(R.id.price);
}

class ItemAdapter(private val reviews: List<Property>, val isAdmin: String) : RecyclerView.Adapter<ReviewViewHolder>() {
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
        holder.name.text = review.name
        holder.description.text = review.description
        holder.price.text = "price: £" + review.price.toString()
    }

    override fun getItemCount(): Int = reviews.size
}



class Reviews : AppCompatActivity() {
    var propertyList = mutableListOf<Property>()
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
                R.id.myproperties-> {
                    val mainIntent = Intent(this@Reviews, MyReviews::class.java)
                    startActivity(mainIntent)
                }
                R.id.add_landlord-> {
                    val mainIntent = Intent(this@Reviews, AddLandlord::class.java)
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
            propertyList = getReview(userId!!)

            // Set up the RecyclerView
            val recyclerView = findViewById<RecyclerView>(R.id.allReviews)
            recyclerView.layoutManager = LinearLayoutManager(this@Reviews)
            val itemAdapter = ItemAdapter(propertyList, isAdmin.toString())
            recyclerView.adapter = itemAdapter

            // Set item click listener for the RecyclerView
            itemAdapter.setOnItemClickListener(object : ItemAdapter.onItemClickListener {
                override fun onItemClick(position: Int, reviewId: Int, action: String) {
                    val buttonHolder = recyclerView.findViewHolderForAdapterPosition(position)
                    val likeCountItem = buttonHolder?.itemView?.findViewById<TextView>(R.id.like_count)
                    //Like  review asynchronously
                    if (action == "like") {
                        val likedbutton =
                            buttonHolder?.itemView?.findViewById<ImageView>(R.id.likeButton);
                        coroutineScope.launch {
                            likeReview(userId, reviewId)
                        }


                        // Update the like button image and count based on the current state
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
                    }
                    else if (action == "edit") {
                        val reviewText =
                            buttonHolder?.itemView?.findViewById<TextView>(R.id.reviewText)
                        // Start the EditReview activity to edit the review
                        val editReviewIntent = Intent(this@Reviews, UpdateReview::class.java)
                        editReviewIntent.putExtra("review", reviewText?.text.toString())
                        editReviewIntent.putExtra("reviewId", reviewId.toString())
                        startActivity(editReviewIntent)
                    }

                    else if (action == "dislike") {
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
                }

            })

        }


    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    // Function to retrieve reviews from the Database helper class
    private suspend fun getReview(userId: String, usersReview: Boolean = false): MutableList<Property> = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = Database(this@Reviews)
            db.getAllProperty(userId)
        } catch (e: Exception) {
            e.printStackTrace()
            propertyList
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
