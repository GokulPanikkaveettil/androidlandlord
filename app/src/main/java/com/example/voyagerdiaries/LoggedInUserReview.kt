package com.example.voyagerdiaries

import Database
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

class MyPropertyViewHolder(itemView: View, listener: MyPropertiesItemAdapter.onItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    val name: TextView = itemView.findViewById(R.id.name);
    val description: TextView = itemView.findViewById(R.id.description);
    val price: TextView = itemView.findViewById(R.id.price);
    var propertyId: Int = 0;
    val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton);
    val editButton: ImageView = itemView.findViewById(R.id.editButton);

    init {
        deleteButton.setOnClickListener {
            listener.onItemClick(adapterPosition, propertyId, "delete")
        }
        editButton.setOnClickListener {
            listener.onItemClick(adapterPosition, propertyId, "edit")
        }
    }
}

class MyPropertiesItemAdapter(private val propertys: List<Property>) :
    RecyclerView.Adapter<MyPropertyViewHolder>() {
    private lateinit var holderListener: onItemClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPropertyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_property_list_layout, parent, false)
        return MyPropertyViewHolder(itemView, holderListener)
    }

    interface onItemClickListener {
        fun onItemClick(position: Int, propertyId: Int, action: String)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        holderListener = listener
    }

    override fun onBindViewHolder(holder: MyPropertyViewHolder, position: Int) {
        val property = propertys[position]
        holder.name.text = property.name
        holder.description.text = property.description
        holder.price.text = property.price.toString()
        holder.propertyId = property.id
    }

    override fun getItemCount(): Int = propertys.size
}

class MyProperties : AppCompatActivity() {
    var propertyList = mutableListOf<Property>();
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_propertys)

        // Set up the bottom navigation view
        coroutineScope.launch {
            val voyagerdiariesPref =
                this@MyProperties.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
            val userId = voyagerdiariesPref.getString("id", null);
            // Retrieve the user's propertys from the Database
            propertyList = getProperty(userId!!)
            val recyclerView = findViewById<RecyclerView>(R.id.MyProperties)
            recyclerView.layoutManager = LinearLayoutManager(this@MyProperties)
            if (propertyList.size < 1){
                Toast.makeText(
                    this@MyProperties,
                    "Currently, there are no propertys available in this section.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            // configure the adapter for the RecyclerView
            val itemAdapter = MyPropertiesItemAdapter(propertyList)
            recyclerView.adapter = itemAdapter

            // Set item click listener for the RecyclerView
            itemAdapter.setOnItemClickListener(object : MyPropertiesItemAdapter.onItemClickListener {
                override fun onItemClick(position: Int, propertyId: Int, action: String) {
                    val buttonHolder = recyclerView.findViewHolderForAdapterPosition(position)
                    //Like  property asynchronously
                    if (action == "delete") {
                        // Delete the property asynchronously
                        coroutineScope.launch {
                            deleteProperty(propertyId)
                            propertyList.removeAt(position)
                            itemAdapter.notifyItemRemoved(position)
                        }
                    } else if (action == "edit") {
                        println(propertyId)
                        println("here is property")
                        val name =
                            buttonHolder?.itemView?.findViewById<TextView>(R.id.name)
                        val description =
                            buttonHolder?.itemView?.findViewById<TextView>(R.id.description)
                        val price =
                            buttonHolder?.itemView?.findViewById<TextView>(R.id.price)
                        // Start the EditProperty activity to edit the property
                        val editPropertyIntent = Intent(this@MyProperties, UpdateProperty::class.java)
                        editPropertyIntent.putExtra("name", name?.text.toString())
                        editPropertyIntent.putExtra("description", description?.text.toString())
                        editPropertyIntent.putExtra("propertyId", propertyId)
                        editPropertyIntent.putExtra("price", price?.text.toString())
                        startActivity(editPropertyIntent)
                    }
                }

            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private suspend fun deleteProperty(propertyId: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = Database(this@MyProperties)
            db.deleteProperty(propertyId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun getProperty(
        userId: String,
        usersProperty: Boolean = true
    ): MutableList<Property> = withContext(
        Dispatchers.IO
    ) {
        return@withContext try {
            val db = Database(this@MyProperties)
            db.getAllProperty(userId, usersProperty)
        } catch (e: Exception) {
            e.printStackTrace()
            propertyList
        }
    }
}