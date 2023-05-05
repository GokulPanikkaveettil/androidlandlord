package com.example.voyagerdiaries

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SignUp : AppCompatActivity() {
    private val user = "voyageradmin"
    private val pass = "voyageradmin"
    private var url = "jdbc:postgresql://10.0.2.2:5432/voyager_db"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);


        val button = findViewById<Button>(R.id.signup);
        val firstName = findViewById<EditText>(R.id.editTextFirstName);
        button.setOnClickListener {
            firstName.setText("");

        }
    }
}