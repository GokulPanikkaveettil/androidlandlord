package com.example.voyagerdiaries

import Database
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = Database()
        db.printUsersTable()
        Toast.makeText(this,"HELLO", Toast.LENGTH_LONG).show()
    }




}