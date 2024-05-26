package com.example.pgLandlords

import Database
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.coroutines.*

class GetLandLordDetails : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_reply)
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
    private suspend fun getLandLordDetails(propertyId: Int): String = withContext(
        Dispatchers.IO
    ) {
        return@withContext try {
            val db = Database(this@GetLandLordDetails)
            print("LLL")
            print(propertyId)
            db.getLandLordDetails(propertyId)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}