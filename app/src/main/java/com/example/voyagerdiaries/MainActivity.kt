package com.example.voyagerdiaries

import Database
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.button3);
        val login = findViewById<Button>(R.id.button2);
        val viewReviews = findViewById<Button>(R.id.viewReviews);
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        nav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.navbar_profile -> {
                    val updateProfile = Intent(this, ProfileUpdate::class.java)
                    startActivity(updateProfile)
                }

                R.id.navbar_home -> {
                    val mainIntent = Intent(this, MainActivity::class.java)
                    startActivity(mainIntent)
                }
            }
            true
        }
        button.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        viewReviews.setOnClickListener {
            val intent = Intent(this, Reviews::class.java)
            startActivity(intent)
        }

        login.setOnClickListener {
            val intent = Intent(this, CreateReviews::class.java)
            val userName = findViewById<EditText>(R.id.editTextUsernameLogin);
            val password = findViewById<EditText>(R.id.editTextPasswordLogin);
            val db = Database(this)
            val checkAuthentication = db.authenticateUser(userName.text.toString().trim(), password.text.toString().trim())
            if(checkAuthentication) {
                startActivity(intent)
                Toast.makeText(this, "Authentication successful....", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Invalid credentials.", Toast.LENGTH_SHORT).show()
            }
        }
    }




}