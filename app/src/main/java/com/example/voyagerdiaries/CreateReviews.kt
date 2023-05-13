package com.example.voyagerdiaries

import Database
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class CreateReviews: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_or_update_reviews);

        val reviewText = findViewById<EditText>(R.id.editTextPostReviews);
        val postReviewButton = findViewById<Button>(R.id.postReview);
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        navbarActions(this, nav);
        val selectedItem = nav.menu.findItem(R.id.add_review)
        selectedItem?.setChecked(true)
        postReviewButton.setOnClickListener {
            if (reviewText.text.isBlank() == true) {
                Toast.makeText(this, "We appreciate your feedback, but we cannot accept empty reviews.", Toast.LENGTH_SHORT).show()
            }
            else {
                val db = Database(this);
                db.postUserReviews(reviewText.text.toString());
                reviewText.setText("")
                val intent = Intent(this, Reviews::class.java)
                startActivity(intent)

            }
        }
    }
}