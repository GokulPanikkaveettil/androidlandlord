package com.example.voyagerdiaries

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        navbarActions(this, nav);
        val selectedItem = nav.menu.findItem(R.id.navbar_profile)
        selectedItem?.setChecked(true)


        val profileUpdateView = findViewById<ImageButton>(R.id.profileUpdateView)
        profileUpdateView.setOnClickListener {
            startActivity(Intent(this, ProfileUpdate::class.java))
        }
        val myReviewsList = findViewById<ImageButton>(R.id.myReviewsList)
        myReviewsList.setOnClickListener {
            startActivity(Intent(this, MyReviews::class.java))
        }
    }
}