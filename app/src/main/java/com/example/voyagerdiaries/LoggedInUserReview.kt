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
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

class MyReviewViewHolder(itemView: View, listener: MyReviewsItemAdapter.onItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    val name: TextView = itemView.findViewById(R.id.name);
    val description: TextView = itemView.findViewById(R.id.description);
    val price: TextView = itemView.findViewById(R.id.price);
    var propertyId: Int = 0;
    val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton);
    val editButton: ImageView = itemView.findViewById(R.id.editButton);

    init {
        deleteButton.setOnClickListener {
            listener.onItemClick(adapterPosition, propertyId, "delete")
        }
        editButton.setOnClickListener {
            listener.onItemClick(adapterPosition, propertyId, "edit")
        }
    }
}

class MyReviewsItemAdapter(private val reviews: List<Property>) :
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
        holder.name.text = review.name
        holder.description.text = review.description
        holder.price.text = review.price.toString()
        holder.propertyId = review.id
    }

    override fun getItemCount(): Int = reviews.size
}

class MyReviews : AppCompatActivity() {
    var reviewList = mutableListOf<Property>();
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_reviews)

        // Set up the bottom navigation view
        coroutineScope.launch {
            val voyagerdiariesPref =
                this@MyReviews.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
            val userId = voyagerdiariesPref.getString("id", null);
            // Retrieve the user's reviews from the Database
            reviewList = getReview(userId!!)
            val recyclerView = findViewById<RecyclerView>(R.id.myReviews)
            recyclerView.layoutManager = LinearLayoutManager(this@MyReviews)
            if (reviewList.size < 1){
                Toast.makeText(
                    this@MyReviews,
                    "Currently, there are no reviews available in this section.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            // configure the adapter for the RecyclerView
            val itemAdapter = MyReviewsItemAdapter(reviewList)
            recyclerView.adapter = itemAdapter

            // Set item click listener for the RecyclerView
            itemAdapter.setOnItemClickListener(object : MyReviewsItemAdapter.onItemClickListener {
                override fun onItemClick(position: Int, reviewId: Int, action: String) {
                    val buttonHolder = recyclerView.findViewHolderForAdapterPosition(position)
                    //Like  review asynchronously
                    if (action == "delete") {
                        // Delete the review asynchronously
                        coroutineScope.launch {
                            deleteProperty(reviewId)
                            reviewList.removeAt(position)
                            itemAdapter.notifyItemRemoved(position)
                        }
                    } else if (action == "edit") {
                        println(reviewId)
                        println("here is review")
                        val name =
                            buttonHolder?.itemView?.findViewById<TextView>(R.id.name)
                        val description =
                            buttonHolder?.itemView?.findViewById<TextView>(R.id.description)
                        val price =
                            buttonHolder?.itemView?.findViewById<TextView>(R.id.price)
                        // Start the EditReview activity to edit the review
                        val editReviewIntent = Intent(this@MyReviews, UpdateReview::class.java)
                        editReviewIntent.putExtra("name", name?.text.toString())
                        editReviewIntent.putExtra("description", description?.text.toString())
                        editReviewIntent.putExtra("propertyId", reviewId)
                        editReviewIntent.putExtra("price", price?.text.toString())
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

    private suspend fun deleteProperty(reviewId: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = Database(this@MyReviews)
            db.deleteProperty(reviewId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun dislikeReview(userId: String, reviewId: Int): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val db = Database(this@MyReviews)
                db.dislikeReview(userId, reviewId)
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
    ): MutableList<Property> = withContext(
        Dispatchers.IO
    ) {
        return@withContext try {
            val db = Database(this@MyReviews)
            db.getAllProperty(userId, usersReview)
        } catch (e: Exception) {
            e.printStackTrace()
            reviewList
        }
    }
}