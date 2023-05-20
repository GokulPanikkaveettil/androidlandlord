package com.example.voyagerdiaries

import Database
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.*

class ReviewReply : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_reply)
        val replyReviewButton = findViewById<Button>(R.id.replyReview);
        val reviewId = intent.getStringExtra("reviewId")
        val replyText = findViewById<EditText>(R.id.editTextPostReviews);
        replyReviewButton.setOnClickListener {
            if (replyText.text.isBlank() == true) {
                Toast.makeText(
                    this,
                    "Reply cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                coroutineScope.launch {
                    val reviewAdded = replyReview(reviewId!!.toInt(), replyText.text.toString())
                    if(reviewAdded) {
                        val intent = Intent(this@ReviewReply, Reviews::class.java)
                        startActivity(intent)
                        replyText.setText("")
                    }
                    else
                    {
                        Toast.makeText(
                            this@ReviewReply,
                            "Reply Add failed. Please modify your input.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }



            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
    private suspend fun replyReview(reviewId: Int,reply: String): Boolean = withContext(
        Dispatchers.IO
    ) {
        return@withContext try {
            val db = Database(this@ReviewReply)
            db.replyUserReviews(reviewId, reply)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}