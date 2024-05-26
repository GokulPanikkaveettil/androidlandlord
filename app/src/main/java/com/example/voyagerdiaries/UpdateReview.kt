package com.example.voyagerdiaries

import Database
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*


class UpdateProperty : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_or_update_properties);
        val intent = intent;
        val propertyId = intent.getIntExtra("propertyId", 0)
        val name = intent.getStringExtra("name")
        val description = intent.getStringExtra("description")
        val price = intent.getStringExtra("price")
        val editname = findViewById<EditText>(R.id.name)
        val editdescription = findViewById<EditText>(R.id.description)
        val editprice = findViewById<EditText>(R.id.price)
        editname.setText(name)
        editdescription.setText(description)
        editprice.setText(price)

        val postpropertyButton = findViewById<Button>(R.id.addProperty);
        postpropertyButton.setOnClickListener {
            println(editname.text.toString())
            println(editdescription.text.toString())
            println(editprice.text.toString())
            println("<<<<<<")
            println(propertyId)
            if (editname.text.isBlank() == true) {
                Toast.makeText(
                    this,
                    "we cannot accept empty name.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                coroutineScope.launch {
                    getProperty(propertyId!!.toInt(), editname.text.toString(),
                        editdescription.text.toString(), editprice.text.toString())
                }
                startActivity(Intent(this, MyProperties::class.java))
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private suspend fun getProperty(propertyId: Int, name: String, description: String, price: String): Boolean = withContext(
        Dispatchers.IO
    ) {
        return@withContext try {
            val db = Database(this@UpdateProperty)
            db.editProperty(propertyId, name, description, price.toInt())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}