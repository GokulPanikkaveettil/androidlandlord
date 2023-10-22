package com.example.voyagerdiaries

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

class CreateReviews : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_or_update_reviews);
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout);
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);

        val reviewText = findViewById<EditText>(R.id.editTextPostReviews);
        val postReviewButton = findViewById<Button>(R.id.postReview);
        /*
        when postreview button is clicked we check the review text if empty
        and then feed to database class function addReview via coroutine.
         */
        postReviewButton.setOnClickListener {
            if (reviewText.text.isBlank() == true) {
                Toast.makeText(
                    this,
                    "We appreciate your feedback, but we cannot accept empty reviews.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                coroutineScope.launch {
                    val reviewAdded = addReview(reviewText.text.toString())
                    if(reviewAdded) {
                        val intent = Intent(this@CreateReviews, Reviews::class.java)
                        startActivity(intent)
                        reviewText.setText("")
                    }
                    else
                    {
                        Toast.makeText(
                            this@CreateReviews,
                            "Review Add failed. Please modify your input.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }



            }
        }
        val navView = findViewById<NavigationView>(R.id.navView);

        navView.setNavigationItemSelectedListener {
            when (it.itemId){
                R.id.myprofile-> {
                    val mainIntent = Intent(this@CreateReviews, Profile::class.java)
                    startActivity(mainIntent)
                }
                R.id.home-> {
                    val mainIntent = Intent(this@CreateReviews, Reviews::class.java)
                    startActivity(mainIntent)
                }
                R.id.add_review_sidemenu-> {
                    val mainIntent = Intent(this@CreateReviews, CreateReviews::class.java)
                    startActivity(mainIntent)
                }
                R.id.logout_sidemenu-> {
                    val voyagerdiariesPref =
                        this@CreateReviews.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
                    val editor = voyagerdiariesPref.edit()
                    editor.remove("id")
                    editor.remove("firstName")
                    editor.remove("lastName")
                    editor.remove("userName")
                    editor.apply()
                    val mainIntent = Intent(this@CreateReviews, MainActivity::class.java)
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

    private suspend fun addReview(review: String): Boolean = withContext(
        Dispatchers.IO
    ) {
        return@withContext try {
            val db = Database(this@CreateReviews)
            db.postUserReviews(review)
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