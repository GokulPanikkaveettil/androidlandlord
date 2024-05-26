package com.example.voyagerdiaries

import Database
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*


class UpdateReview : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_or_update_reviews);
        val intent = intent;
        val propertyId = intent.getIntExtra("propertyId", 0)
        val name = intent.getStringExtra("name")
        val description = intent.getStringExtra("description")
        val price = intent.getStringExtra("price")
        val editname = findViewById<EditText>(R.id.name)
        val editdescription = findViewById<EditText>(R.id.description)
        val editprice = findViewById<EditText>(R.id.price)
        editname.setText(name)
        editdescription.setText(description)
        editprice.setText(price)

        val postReviewButton = findViewById<Button>(R.id.addProperty);
        postReviewButton.setOnClickListener {
            println(editname.text.toString())
            println(editdescription.text.toString())
            println(editprice.text.toString())
            println("<<<<<<")
            println(propertyId)
            if (editname.text.isBlank() == true) {
                Toast.makeText(
                    this,
                    "we cannot accept empty name.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                coroutineScope.launch {
                    getReview(propertyId!!.toInt(), editname.text.toString(),
                        editdescription.text.toString(), editprice.text.toString())
                }
                startActivity(Intent(this, MyReviews::class.java))
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private suspend fun getReview(reviewId: Int, name: String, description: String, price: String): Boolean = withContext(
        Dispatchers.IO
    ) {
        return@withContext try {
            val db = Database(this@UpdateReview)
            db.editProperty(reviewId, name, description, price.toInt())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}