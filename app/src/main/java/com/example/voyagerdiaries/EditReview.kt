package com.example.voyagerdiaries

import Database
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class EditReview: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_or_update_reviews);
        val intent = intent;
        val extraValue = intent.getStringExtra("review")
        val reviewId = intent.getStringExtra("reviewId")
        val editReview = findViewById<EditText>(R.id.editTextPostReviews)
        editReview.setText(extraValue)
        val postReviewButton = findViewById<Button>(R.id.postReview);
        postReviewButton.setOnClickListener {
            if (editReview.text.isBlank() == true) {
                Toast.makeText(
                    this,
                    "We appreciate your feedback, but we cannot accept empty reviews.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val db = Database(this@EditReview);
                db.editReview(reviewId!!.toInt(), editReview.text.toString())
                startActivity(Intent(this, MyReviews::class.java))
            }
        }

    }
}