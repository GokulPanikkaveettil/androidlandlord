package com.example.voyagerdiaries

import android.content.Context
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

fun navbarActions(context: Context, nav: BottomNavigationView) {
    nav.setOnItemSelectedListener {
        when (it.itemId) {
            R.id.navbar_profile -> {
                val updateProfile = Intent(context, Profile::class.java)
                context.startActivity(updateProfile)
            }

            R.id.navbar_home -> {
                val mainIntent = Intent(context, Reviews::class.java)
                context.startActivity(mainIntent)
            }

            R.id.add_review -> {
                val mainIntent = Intent(context, CreateReviews::class.java)
                context.startActivity(mainIntent)
            }

            R.id.logout -> {
                val voyagerdiariesPref =
                    context.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
                val editor = voyagerdiariesPref.edit()
                editor.remove("id")
                editor.remove("firstName")
                editor.remove("lastName")
                editor.remove("userName")
                editor.apply()
                val mainIntent = Intent(context, MainActivity::class.java)
                mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(mainIntent)
            }
        }
        true
    }
}


