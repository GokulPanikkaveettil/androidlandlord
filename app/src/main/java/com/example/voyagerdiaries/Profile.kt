package com.example.voyagerdiaries

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class Profile : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        toggle = ActionBarDrawerToggle(this@Profile, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);


        val profileUpdateView = findViewById<ImageButton>(R.id.profileUpdateView)
        profileUpdateView.setOnClickListener {
            startActivity(Intent(this, ProfileUpdate::class.java))
        }
        val myReviewsList = findViewById<ImageButton>(R.id.myReviewsList)
        myReviewsList.setOnClickListener {
            startActivity(Intent(this, MyProperties::class.java))
        }



        val navView = findViewById<NavigationView>(R.id.navView);

        navView.setNavigationItemSelectedListener {
            when (it.itemId){
                R.id.myproperties-> {
                    val mainIntent = Intent(this@Profile, MyProperties::class.java)
                    startActivity(mainIntent)
                }
                R.id.home-> {
                    val mainIntent = Intent(this@Profile, Properties::class.java)
                    startActivity(mainIntent)
                }
                R.id.add_properties_sidemenu-> {
                    val mainIntent = Intent(this@Profile, CreateProperties::class.java)
                    startActivity(mainIntent)
                }
                R.id.logout_sidemenu-> {
                    val voyagerdiariesPref =
                        this@Profile.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
                    val editor = voyagerdiariesPref.edit()
                    editor.remove("id")
                    editor.remove("firstName")
                    editor.remove("lastName")
                    editor.remove("userName")
                    editor.apply()
                    val mainIntent = Intent(this@Profile, MainActivity::class.java)
                    mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(mainIntent)
                }
            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            true
        }
        return super.onOptionsItemSelected(item)
    }
}