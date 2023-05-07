package com.example.voyagerdiaries

import Database
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreateReviews: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_reviews);

        val reviewText = findViewById<EditText>(R.id.editTextPostReviews);
        val postReviewButton = findViewById<Button>(R.id.postReview);
        postReviewButton.setOnClickListener {
            if (reviewText.text.isBlank() == true) {
                Toast.makeText(this, "We appreciate your feedback, but we cannot accept empty reviews.", Toast.LENGTH_SHORT).show()
            }
            else {
                val db = Database(this);
                db.postUserReviews(reviewText.text.toString());
                reviewText.setText("")

            }
        }
    }
}