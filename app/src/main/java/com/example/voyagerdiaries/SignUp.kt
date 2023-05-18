package com.example.voyagerdiaries

import Database
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class SignUp : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);


        val button = findViewById<Button>(R.id.signup);
        val firstName = findViewById<EditText>(R.id.editTextFirstName);
        val lastName = findViewById<EditText>(R.id.editTextLastName);
        val userName = findViewById<EditText>(R.id.editTextUsername);
        val password = findViewById<EditText>(R.id.editTextPassword);
        button.setOnClickListener {
            val formValid = validateInput(
                firstName.text.toString(),
                lastName.text.toString(),
                userName.text.toString(),
                password.text.toString()
            )
            if (formValid) {
                coroutineScope.launch {
                    val userAdded = addNewUser(
                        firstName.text.toString().trim(), lastName.text.toString().trim(),
                        userName.text.toString().trim(), password.text.toString().trim()
                    )
                    if (userAdded == true) {
                        Toast.makeText(
                            this@SignUp,
                            "User Created successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val mainActivityIntent = Intent(this@SignUp, MainActivity::class.java)
                        startActivity(mainActivityIntent)
                    } else {
                        Toast.makeText(
                            this@SignUp,
                            "Unable to create account..",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private suspend fun addNewUser(firstName: String,lastName: String,userName: String,password: String
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = Database(this@SignUp)
            db.addNewUser(firstName, lastName, userName, password)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun validateInput(firstName: String,lastName: String,userName: String,password: String): Boolean {
        if (firstName.length <= 0) {
            Toast.makeText(this, "First Name cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (userName.length <= 0) {
            Toast.makeText(this, "User Name cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length <= 0) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}