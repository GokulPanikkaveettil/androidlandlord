package com.example.voyagerdiaries

import Database
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddLandlord : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    lateinit var toggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_landlord)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout);
        toggle = ActionBarDrawerToggle(this@AddLandlord, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


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
        val navView = findViewById<NavigationView>(R.id.navView);

        navView.setNavigationItemSelectedListener {
            when (it.itemId){
                R.id.myprofile-> {
                    val mainIntent = Intent(this@AddLandlord, Profile::class.java)
                    startActivity(mainIntent)
                }
                R.id.home-> {
                    val mainIntent = Intent(this@AddLandlord, Reviews::class.java)
                    startActivity(mainIntent)
                }
                R.id.add_review_sidemenu-> {
                    val mainIntent = Intent(this@AddLandlord, CreateReviews::class.java)
                    startActivity(mainIntent)
                }
                R.id.logout_sidemenu-> {
                    val voyagerdiariesPref =
                        this@AddLandlord.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
                    val editor = voyagerdiariesPref.edit()
                    editor.remove("id")
                    editor.remove("firstName")
                    editor.remove("lastName")
                    editor.remove("userName")
                    editor.apply()
                    val mainIntent = Intent(this@AddLandlord, MainActivity::class.java)
                    mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(mainIntent)
                }
            }
            true
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
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            true
        }
        return super.onOptionsItemSelected(item)
    }
}