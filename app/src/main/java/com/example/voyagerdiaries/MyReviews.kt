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

class MyReviewViewHolder(itemView: View, listener: MyReviewsItemAdapter.onItemClickListener) : RecyclerView.ViewHolder(itemView) {
    val reviewText: TextView = itemView.findViewById(R.id.reviewText);
    val userName: TextView = itemView.findViewById(R.id.reviewedUser);
    var reviewId: Int = 0;
    val likeButton: ImageView = itemView.findViewById(R.id.likeButton);
    val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton);
    val editButton: ImageView = itemView.findViewById(R.id.editButton);

    init {
        likeButton.setOnClickListener{
            listener.onItemClick(adapterPosition, reviewId, "like")
        }
        deleteButton.setOnClickListener{
            listener.onItemClick(adapterPosition, reviewId, "delete")
        }
        editButton.setOnClickListener{
            listener.onItemClick(adapterPosition, reviewId, "edit")
        }
    }
}
class MyReviewsItemAdapter(private val reviews: List<Review>) : RecyclerView.Adapter<MyReviewViewHolder>() {
    private lateinit var rListener : onItemClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_review_list_layout, parent, false)
        return MyReviewViewHolder(itemView, rListener)
    }
    interface onItemClickListener {
        fun onItemClick(position: Int, reviewId: Int, action: String)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        rListener = listener
    }

    override fun onBindViewHolder(holder: MyReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.reviewText.text = review.review
        holder.userName.text = review.fullName
        holder.reviewId = review.reviewId
        if (review.liked == 1){
            holder.likeButton.setImageResource(R.drawable.baseline_thumb_up_24);
        }
    }

    override fun getItemCount(): Int = reviews.size
}
class MyReviews : AppCompatActivity() {
    var reviewList = mutableListOf<Review>();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_reviews)

        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationView);
        navbarActions(this, nav);
        val selectedItem = nav.menu.findItem(R.id.navbar_profile)
        selectedItem?.setChecked(true)
        val db = Database(this);
        val voyagerdiariesPref = this.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
        val userId = voyagerdiariesPref.getString("id", null);
        reviewList = db.getAllReview(userId, true);
        val recyclerView = findViewById<RecyclerView>(R.id.myReviews)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val itemAdapter = MyReviewsItemAdapter(reviewList)
        recyclerView.adapter = itemAdapter
        itemAdapter.setOnItemClickListener(object: MyReviewsItemAdapter.onItemClickListener{
            override fun onItemClick(position: Int, reviewId: Int, action: String) {
                val buttonHolder = recyclerView.findViewHolderForAdapterPosition(position)
                if (action == "liked") {
                    val likedbutton =
                        buttonHolder?.itemView?.findViewById<ImageView>(R.id.likeButton);
                    val db2 = Database(this@MyReviews)
                    val liked = db2.likeReview(userId!!, reviewId)
                    if (liked) {
                        likedbutton?.setImageResource(R.drawable.baseline_thumb_up_24);
                    } else {
                        likedbutton?.setImageResource(R.drawable.baseline_thumb_up_off_alt_24);
                    }
                }
                else if (action == "delete"){
                    val db2 = Database(this@MyReviews)
                    val delete = db2.deleteReview(reviewId)
                    if (delete) {
                        reviewList.removeAt(position)
                            println(reviewList)
                        itemAdapter.notifyItemRemoved(position)

                    }
                }

                else if (action == "edit"){
                    val reviewText = buttonHolder?.itemView?.findViewById<TextView>(R.id.reviewText)
                    val editReviewIntent = Intent(this@MyReviews, EditReview::class.java)
                    editReviewIntent.putExtra("review", reviewText?.text.toString())
                    editReviewIntent.putExtra("reviewId", reviewId.toString())
                    startActivity(editReviewIntent)
                }
            }

        })
    }
}