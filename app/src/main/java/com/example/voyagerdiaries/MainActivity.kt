package com.example.voyagerdiaries

import Database
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val db = Database()
//        db.printUsersTable()
        val button = findViewById<Button>(R.id.button3);
        val login = findViewById<Button>(R.id.button2);
        button.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        login.setOnClickListener {
            val intent = Intent(this, Reviews::class.java)
            val userName = findViewById<EditText>(R.id.editTextUsernameLogin);
            val password = findViewById<EditText>(R.id.editTextPasswordLogin);
            val db = Database()
            val checkAuthentication = db.authenticateUser(userName.text.toString(), password.text.toString())
            println(checkAuthentication)
            println("checkAuthentication")
            if(checkAuthentication) {
                startActivity(intent)
                Toast.makeText(this, "Authentication successful.", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Invalid credentials.", Toast.LENGTH_SHORT).show()
            }
        }
    }




}