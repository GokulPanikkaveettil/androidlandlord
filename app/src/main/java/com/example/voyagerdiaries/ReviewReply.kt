package com.example.voyagerdiaries

import Database
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*

class ReviewReply : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_reply)
        val propertyId = intent.getStringExtra("propertyId")
        println("KK")
        println(propertyId)
        val phone = findViewById<TextView>(R.id.phone);
        coroutineScope.launch {
            val fetchedphone = getLandLordDetails(propertyId!!.toInt())
            phone.setText("Contact Landlord :" + fetchedphone)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
    private suspend fun getLandLordDetails(reviewId: Int): String = withContext(
        Dispatchers.IO
    ) {
        return@withContext try {
            val db = Database(this@ReviewReply)
            print("LLL")
            print(reviewId)
            db.getLandLordDetails(reviewId)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}