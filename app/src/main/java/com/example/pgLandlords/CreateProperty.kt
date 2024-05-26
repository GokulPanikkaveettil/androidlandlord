package com.example.pgLandlords

import Database
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*

class CreateProperties : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_or_update_properties);
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout);
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);

        val name = findViewById<EditText>(R.id.name);
        val description = findViewById<EditText>(R.id.description);
        val price = findViewById<EditText>(R.id.price);
        val addPropertyButton = findViewById<Button>(R.id.addProperty);
        /*
        when postproperty button is clicked we check the property text if empty
        and then feed to database class function addProperty via coroutine.
         */
        addPropertyButton.setOnClickListener {
            if (name.text.isBlank() == true) {
                Toast.makeText(
                    this,
                    "We cannot accept empty name.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                coroutineScope.launch {
                    val propertyAdded = addProperty(name.text.toString(), description.text.toString(), price.text.toString())
                    if(propertyAdded) {
                        val intent = Intent(this@CreateProperties, Properties::class.java)
                        startActivity(intent)
                        name.setText("")
                    }
                    else
                    {
                        Toast.makeText(
                            this@CreateProperties,
                            "Property Add failed. Please modify your input.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }



            }
        }
        val navView = findViewById<NavigationView>(R.id.navView);

        navView.setNavigationItemSelectedListener {
            when (it.itemId){
                R.id.myproperties-> {
                    val mainIntent = Intent(this@CreateProperties, MyProperties::class.java)
                    startActivity(mainIntent)
                }
                R.id.home-> {
                    val mainIntent = Intent(this@CreateProperties, Properties::class.java)
                    startActivity(mainIntent)
                }
                R.id.add_landlord-> {
                    val mainIntent = Intent(this@CreateProperties, AddLandlord::class.java)
                    startActivity(mainIntent)
                }
                R.id.add_properties_sidemenu-> {
                    val mainIntent = Intent(this@CreateProperties, CreateProperties::class.java)
                    startActivity(mainIntent)
                }
                R.id.logout_sidemenu-> {
                    val pgLandlordsPref =
                        this@CreateProperties.getSharedPreferences("pgLandlordsPref", Context.MODE_PRIVATE)
                    val editor = pgLandlordsPref.edit()
                    editor.remove("id")
                    editor.remove("firstName")
                    editor.remove("lastName")
                    editor.remove("userName")
                    editor.apply()
                    val mainIntent = Intent(this@CreateProperties, MainActivity::class.java)
                    mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(mainIntent)
                }
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private suspend fun addProperty(name: String, description: String, price: String): Boolean = withContext(
        Dispatchers.IO
    ) {
        return@withContext try {
            val db = Database(this@CreateProperties)
            db.postProperty(name, description, price.toInt())
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            true
        }
        return super.onOptionsItemSelected(item)
    }
}