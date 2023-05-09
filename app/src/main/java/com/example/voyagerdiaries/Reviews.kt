package com.example.voyagerdiaries

import Database
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView


data class Review(val review: String, val fullName: String, val reviewId: Int)

class ReviewViewHolder(itemView: View, listener: ItemAdapter.onItemClickListener) : RecyclerView.ViewHolder(itemView) {
    val reviewText: TextView = itemView.findViewById(R.id.reviewText);
    val userName: TextView = itemView.findViewById(R.id.reviewedUser);
    var reviewId: Int = 0;
    val likeButton: Button = itemView.findViewById(R.id.likeButton);

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
            .inflate(R.layout.item_layout, parent, false)
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
    }

    override fun getItemCount(): Int = reviews.size
}


class Reviews : AppCompatActivity() {
    var reviewList = mutableListOf<Review>();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reviews)
        val db = Database(this);
        reviewList = db.getAllReview();
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationView);
        navbarActions(this, nav);

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val itemAdapter = ItemAdapter(reviewList)
        recyclerView.adapter = itemAdapter
        itemAdapter.setOnItemClickListener(object: ItemAdapter.onItemClickListener{
            override fun onItemClick(position: Int, reviewId: Int) {
                val likebuttonHolder = recyclerView.findViewHolderForAdapterPosition(position)
                val likedbutton = likebuttonHolder?.itemView?.findViewById<Button>(R.id.likeButton);
                likedbutton?.setText("Liked");
                Toast.makeText(this@Reviews, "you clicked review $reviewId",Toast.LENGTH_SHORT).show()
            }
        })


    }
}