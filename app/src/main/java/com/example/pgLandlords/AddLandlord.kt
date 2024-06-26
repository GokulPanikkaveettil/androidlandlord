package com.example.pgLandlords

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
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        val navView = findViewById<NavigationView>(R.id.navView);


        val button = findViewById<Button>(R.id.signup);
        val firstName = findViewById<EditText>(R.id.editTextFirstName);
        val lastName = findViewById<EditText>(R.id.editTextLastName);
        val userName = findViewById<EditText>(R.id.editTextUsername);
        val password = findViewById<EditText>(R.id.editTextPassword);
        val phoneNumber = findViewById<EditText>(R.id.phoneNumber);
        button.setOnClickListener {
                coroutineScope.launch {
                    val userAdded = addNewLandlord(
                        firstName.text.toString().trim(), lastName.text.toString().trim(),
                        userName.text.toString().trim(), password.text.toString().trim(), phoneNumber.text.toString().trim()
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

        navView.setNavigationItemSelectedListener {
            when (it.itemId){
                R.id.myproperties-> {
                    val mainIntent = Intent(this@AddLandlord, MyProperties::class.java)
                    startActivity(mainIntent)
                }
                R.id.home-> {
                    val mainIntent = Intent(this@AddLandlord, Properties::class.java)
                    startActivity(mainIntent)
                }
                R.id.add_properties_sidemenu-> {
                    val mainIntent = Intent(this@AddLandlord, CreateProperties::class.java)
                    startActivity(mainIntent)
                }
                R.id.add_landlord-> {
                    val mainIntent = Intent(this@AddLandlord, AddLandlord::class.java)
                    startActivity(mainIntent)
                }
                R.id.logout_sidemenu-> {
                    val pgLandlordsPref =
                        this@AddLandlord.getSharedPreferences("pgLandlordsPref", Context.MODE_PRIVATE)
                    val editor = pgLandlordsPref.edit()
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

    private suspend fun addNewLandlord(firstName: String,lastName: String,userName: String,password: String,
                                       phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = Database(this@AddLandlord)
            db.addNewUser(firstName, lastName, userName, password, "", true, phoneNumber)
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