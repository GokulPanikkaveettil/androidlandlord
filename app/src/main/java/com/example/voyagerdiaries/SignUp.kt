package com.example.voyagerdiaries

import Database
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.sql.DriverManager

class SignUp : AppCompatActivity() {
    private val user = "voyageradmin"
    private val pass = "voyageradmin"
    private var url = "jdbc:postgresql://10.0.2.2:5432/voyager_db"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);


        val button = findViewById<Button>(R.id.signup);
        val firstName = findViewById<EditText>(R.id.editTextFirstName);
        val lastName = findViewById<EditText>(R.id.editTextLastName);
        val userName = findViewById<EditText>(R.id.editTextUsername);
        val password = findViewById<EditText>(R.id.editTextPassword);
        button.setOnClickListener {
            val db = Database()
            val userAdded = db.addNewUser(firstName.text.toString(), lastName.text.toString(),
                userName.text.toString(), password.text.toString())
            if (userAdded == true){
                Toast.makeText(this, "User Created successfully.", Toast.LENGTH_SHORT).show()
                val mainActivityIntent = Intent(this, MainActivity::class.java)
                startActivity(mainActivityIntent)
            }
        }
    }
}