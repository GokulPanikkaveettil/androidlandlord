package com.example.voyagerdiaries

import Database
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*


class EditReview : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
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
                coroutineScope.launch {
                    getReview(reviewId!!.toInt(), editReview.text.toString())
                }
                startActivity(Intent(this, MyReviews::class.java))
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private suspend fun getReview(reviewId: Int, review: String): Boolean = withContext(
        Dispatchers.IO
    ) {
        return@withContext try {
            val db = Database(this@EditReview)
            db.editReview(reviewId, review)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}