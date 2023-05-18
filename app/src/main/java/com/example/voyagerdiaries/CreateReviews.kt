package com.example.voyagerdiaries

import Database
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*

class CreateReviews : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_or_update_reviews);

        val reviewText = findViewById<EditText>(R.id.editTextPostReviews);
        val postReviewButton = findViewById<Button>(R.id.postReview);
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        navbarActions(this, nav);
        val selectedItem = nav.menu.findItem(R.id.add_review)
        selectedItem?.setChecked(true)
        /*
        when postreview button is clicked we check the review text if empty
        and then feed to database class function addReview via coroutine.
         */
        postReviewButton.setOnClickListener {
            if (reviewText.text.isBlank() == true) {
                Toast.makeText(
                    this,
                    "We appreciate your feedback, but we cannot accept empty reviews.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                coroutineScope.launch {
                    addReview(reviewText.text.toString())
                    reviewText.setText("")
                }

                val intent = Intent(this, Reviews::class.java)
                startActivity(intent)

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private suspend fun addReview(review: String): Boolean = withContext(
        Dispatchers.IO
    ) {
        return@withContext try {
            val db = Database(this@CreateReviews)
            db.postUserReviews(review)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}