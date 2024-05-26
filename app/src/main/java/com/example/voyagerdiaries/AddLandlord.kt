package com.example.voyagerdiaries

import Database
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddLandlord : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_landlord)

        val button = findViewById<Button>(R.id.signup);
        val firstName = findViewById<EditText>(R.id.editTextFirstName);
        val lastName = findViewById<EditText>(R.id.editTextLastName);
        val userName = findViewById<EditText>(R.id.editTextUsername);
        val password = findViewById<EditText>(R.id.editTextPassword);
        button.setOnClickListener {
                coroutineScope.launch {
                    val userAdded = addNewLandlord(
                        firstName.text.toString().trim(), lastName.text.toString().trim(),
                        userName.text.toString().trim(), password.text.toString().trim()
                    )
                    if (userAdded == true) {
                        Toast.makeText(
                            this@AddLandlord,
                            "User Created successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val mainActivityIntent = Intent(this@AddLandlord, MainActivity::class.java)
                        startActivity(mainActivityIntent)
                    } else {
                        Toast.makeText(
                            this@AddLandlord,
                            "Unable to create account..",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private suspend fun addNewLandlord(firstName: String,lastName: String,userName: String,password: String
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = Database(this@AddLandlord)
            db.addNewUser(firstName, lastName, userName, password, "")
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}