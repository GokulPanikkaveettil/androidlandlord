package com.example.voyagerdiaries

import Database
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val voyagerdiariesPref =
            this.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
        // Retrieve user ID from shared preferences
        val userId = voyagerdiariesPref.getString("id", null);
        // If user ID exists, start AllReviews.kt activity
        if (userId != null) {
            val intentMainActivity = Intent(this, Reviews::class.java)
            startActivity(intentMainActivity)
        }
        setContentView(R.layout.activity_main)
        val buttonSignup = findViewById<Button>(R.id.signup);
        val buttonLogin = findViewById<Button>(R.id.login);
        // Button click listener for Signup button
        buttonSignup.setOnClickListener {
            val intent = Intent(this, UserRegister::class.java)
            startActivity(intent)
        }
        // Button click listener for Login button
        buttonLogin.setOnClickListener {
            val intent = Intent(this, Reviews::class.java)
            val userName = findViewById<EditText>(R.id.editTextUsernameLogin);
            val password = findViewById<EditText>(R.id.editTextPasswordLogin);
            // Launch a coroutine to perform authentication
            coroutineScope.launch {
                val checkAuthentication =
                    authenticate(userName.text.toString().trim(), password.text.toString().trim())
                if (checkAuthentication) {
                    startActivity(intent)
                    // If authentication is successful, start AllReviews.kt activity and show success toast message
                    Toast.makeText(
                        this@MainActivity,
                        "Authentication successful....",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this@MainActivity, "Oops! It seems there was an issue with your login.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }
    }

    override fun onDestroy() {
        //when activity destroy is called we cancel the coroutine scope.
        super.onDestroy()
        coroutineScope.cancel()
    }

    private suspend fun authenticate(userName: String, password: String): Boolean =
        withContext(Dispatchers.IO) {
            val db = Database(this@MainActivity)
            db.authenticateUser(userName, password)
        }
}