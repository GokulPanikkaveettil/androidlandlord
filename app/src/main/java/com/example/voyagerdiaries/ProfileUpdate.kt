package com.example.voyagerdiaries

import Database
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileUpdate : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_update)

        val updateFirstName = findViewById<EditText>(R.id.editTextUpdateFirstName);
        val updateLastName = findViewById<EditText>(R.id.editTextUpdateLastName);
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val selectedItem = nav.menu.findItem(R.id.navbar_profile)
        selectedItem?.setChecked(true)
        navbarActions(this, nav);

        val voyagerdiariesPref = this.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
        val userId = voyagerdiariesPref.getString("id", null);
        if (userId == null){
            val intentMainActivity = Intent(this, MainActivity::class.java)
            startActivity(intentMainActivity)
        }
        val firstName = voyagerdiariesPref.getString("firstName", null);
        val lastName = voyagerdiariesPref.getString("lastName", null);
        updateFirstName.setText(firstName);
        updateLastName.setText(lastName);
        val updateButton = findViewById<Button>(R.id.updateProfileButton);
        updateButton.setOnClickListener {
            val db = Database(this);
            db.updateProfile(updateFirstName.text.toString(), updateLastName.text.toString());
            val intent = Intent(this, Reviews::class.java)
            startActivity(intent)
        }


    }
}