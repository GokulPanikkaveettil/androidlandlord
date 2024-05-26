package com.example.pgLandlords

import Database
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*

class ProfileUpdate : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_update)

        val updateFirstName = findViewById<EditText>(R.id.editTextUpdateFirstName);
        val updateLastName = findViewById<EditText>(R.id.editTextUpdateLastName);

        val pgLandlordsPref =
            this.getSharedPreferences("pgLandlordsPref", Context.MODE_PRIVATE)
        val userId = pgLandlordsPref.getString("id", null);
        if (userId == null) {
            val intentMainActivity = Intent(this, MainActivity::class.java)
            startActivity(intentMainActivity)
        }
        val firstName = pgLandlordsPref.getString("firstName", null);
        val lastName = pgLandlordsPref.getString("lastName", null);
        updateFirstName.setText(firstName);
        updateLastName.setText(lastName);
        val updateButton = findViewById<Button>(R.id.updateProfileButton);
        updateButton.setOnClickListener {
            if(updateFirstName.text.isBlank() or updateLastName.text.isBlank()){
                Toast.makeText(this@ProfileUpdate, "firstname and lastname cannot be blank", Toast.LENGTH_SHORT)
                    .show()
            }
            else {
                coroutineScope.launch {
                    updateProfile(updateFirstName.text.toString(), updateLastName.text.toString());
                    val intent = Intent(this@ProfileUpdate, Properties::class.java)
                    startActivity(intent)
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private suspend fun updateProfile(firstName: String, lastName: String): Boolean = withContext(
        Dispatchers.IO
    ) {
        return@withContext try {
            val db = Database(this@ProfileUpdate)
            db.updateProfile(firstName, lastName)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}