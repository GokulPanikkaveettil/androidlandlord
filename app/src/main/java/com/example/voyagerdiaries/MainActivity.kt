package com.example.voyagerdiaries

import Database
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val voyagerdiariesPref = this.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
        val userId = voyagerdiariesPref.getString("id", null);
        if (userId!!.isNotBlank()){
            val intentMainActivity = Intent(this, Reviews::class.java)
            startActivity(intentMainActivity)
        }
        setContentView(R.layout.activity_main)
        val buttonSignup = findViewById<Button>(R.id.signup);
        val buttonLogin = findViewById<Button>(R.id.login);
        buttonSignup.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        buttonLogin.setOnClickListener {
            val intent = Intent(this, Reviews::class.java)
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