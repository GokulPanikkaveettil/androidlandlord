package com.example.voyagerdiaries

import Database
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class ProfileUpdate : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_update)

        val updateFirstName = findViewById<EditText>(R.id.editTextUpdateFirstName);
        val updateLastName = findViewById<EditText>(R.id.editTextUpdateLastName);

        val voyagerdiariesPref = this.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)

        val firstName = voyagerdiariesPref.getString("firstName", null);
        val lastName = voyagerdiariesPref.getString("lastName", null);
        updateFirstName.setText(firstName);
        updateLastName.setText(lastName);
         val updateButton = findViewById<Button>(R.id.updateProfileButton);
        updateButton.setOnClickListener {
            val db = Database(this);
            db.updateProfile(updateFirstName.text.toString(), updateLastName.text.toString());
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }
}