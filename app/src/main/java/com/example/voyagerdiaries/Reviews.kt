package com.example.voyagerdiaries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class Review(val review: String, val fullName: String)

class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val reviewText: TextView = itemView.findViewById(R.id.reviewText);
    val userName: TextView = itemView.findViewById(R.id.reviewedUser);
}

class ItemAdapter(private val reviews: List<Review>) : RecyclerView.Adapter<ReviewViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ReviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.reviewText.text = review.review
        holder.userName.text = review.fullName
    }

    override fun getItemCount(): Int = reviews.size
}


class Reviews : AppCompatActivity() {
    val reviewList = mutableListOf<Review>();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reviews)
        reviewList.add(Review("Good Job", "Sagheer"));
        reviewList.add(Review("Nice Job", "Adam"));
        reviewList.add(Review("Recommended", "bob"));

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ItemAdapter(reviewList)
    }
}