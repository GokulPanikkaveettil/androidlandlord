package com.example.voyagerdiaries

import Database
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*
import kotlinx.coroutines.withContext


    data class Property(val id: Int, val name: String, val user_id: Int, val description: String, val price: Int);
class ReviewViewHolder(itemView: View, listener: ItemAdapter.onItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    val name: TextView = itemView.findViewById(R.id.name);
    val description: TextView = itemView.findViewById(R.id.description);
    val price: TextView = itemView.findViewById(R.id.price);
    val connect: ImageView = itemView.findViewById(R.id.connect);
    var propertyId: Int = 0;
    init {

        connect.setOnClickListener {
            listener.onItemClick(adapterPosition, propertyId, "connect")
        }
    }
}

class ItemAdapter(private val properties: List<Property>, val isAdmin: String) : RecyclerView.Adapter<ReviewViewHolder>() {
    private lateinit var holderListener: onItemClickListener

    // Create the item's view holder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        // Inflate the layout for the item view
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.property_list_layout, parent, false)
        return ReviewViewHolder(itemView, holderListener)
    }

    // Interface for item click listener
    interface onItemClickListener {
        fun onItemClick(position: Int, propertyId: Int, action: String)
    }

    // Configure the item click listener
    fun setOnItemClickListener(listener: onItemClickListener) {
        holderListener = listener
    }

    // Bind data to the view holder
    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val property = properties[position]

        // Set the property text and user name
        holder.name.text = property.name
        holder.description.text = property.description
        holder.price.text = "price: £" + property.price.toString()
        holder.propertyId = property.id
    }

    override fun getItemCount(): Int = properties.size
}



class Properties : AppCompatActivity() {
    var propertyList = mutableListOf<Property>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.properties)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout);
        toggle = ActionBarDrawerToggle(this@Properties, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        val navView = findViewById<NavigationView>(R.id.navView);
        val sharedPref = this@Properties.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)

        val is_admin = sharedPref.getString("isAdmin", "f")
        val isLandlord = sharedPref.getString("isLandlord", "f")


        // Hide the menu item based on the shared preference value
        val menu = navView.menu
        val add_landlord = menu.findItem(R.id.add_landlord)
        val myproperties = menu.findItem(R.id.myproperties)
        val add_properties_sidemenu = menu.findItem(R.id.add_properties_sidemenu)
        add_landlord.isVisible =  is_admin == "t"
        myproperties.isVisible =  isLandlord == "t"
        add_properties_sidemenu.isVisible =  isLandlord == "t"

        navView.setNavigationItemSelectedListener {
            when (it.itemId){
                R.id.myproperties-> {
                    val mainIntent = Intent(this@Properties, MyProperties::class.java)
                    startActivity(mainIntent)
                }
                R.id.add_landlord-> {
                    val mainIntent = Intent(this@Properties, AddLandlord::class.java)
                    startActivity(mainIntent)
                }
                R.id.home-> {
                    val mainIntent = Intent(this@Properties, Properties::class.java)
                    startActivity(mainIntent)
                }
                R.id.add_properties_sidemenu-> {
                    val mainIntent = Intent(this@Properties, CreateProperties::class.java)
                    startActivity(mainIntent)
                }
                R.id.logout_sidemenu-> {
                    val voyagerdiariesPref =
                        this@Properties.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
                    val editor = voyagerdiariesPref.edit()
                    editor.remove("id")
                    editor.remove("firstName")
                    editor.remove("lastName")
                    editor.remove("userName")
                    editor.apply()
                    val mainIntent = Intent(this@Properties, MainActivity::class.java)
                    mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(mainIntent)
                }
            }
            true
        }
        // Get user ID and admin status from shared preferences
        val voyagerdiariesPref = this.getSharedPreferences("voyagerdiariesPref", Context.MODE_PRIVATE)
        val userId = voyagerdiariesPref.getString("id", null)
        val isAdmin = voyagerdiariesPref.getString("isAdmin", null)

        coroutineScope.launch {
            // Retrieve the property list from the Database class
            propertyList = getReview(userId!!)

            // Set up the RecyclerView
            val recyclerView = findViewById<RecyclerView>(R.id.allProperties)
            recyclerView.layoutManager = LinearLayoutManager(this@Properties)
            val itemAdapter = ItemAdapter(propertyList, isAdmin.toString())
            recyclerView.adapter = itemAdapter

            // Set item click listener for the RecyclerView
            itemAdapter.setOnItemClickListener(object : ItemAdapter.onItemClickListener {
                override fun onItemClick(position: Int, propertyId: Int, action: String) {
                    println(action)
                    val buttonHolder = recyclerView.findViewHolderForAdapterPosition(position)
                    if (action == "connect") {
                        // Start the ReviewReply activity to reply to a property
                        println(propertyId)
                        val replyReviewIntent = Intent(this@Properties, GetLandLordDetails::class.java)
                        replyReviewIntent.putExtra("propertyId", propertyId.toString())
                        startActivity(replyReviewIntent)
                    }
                }

            })

        }


    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    // Function to retrieve properties from the Database helper class
    private suspend fun getReview(userId: String, usersReview: Boolean = false): MutableList<Property> = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = Database(this@Properties)
            db.getAllProperty(userId)
        } catch (e: Exception) {
            e.printStackTrace()
            propertyList
        }
    }

    // Function to perform like action on a property

    // Function to perform dislike action on a property

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            true
        }
        return super.onOptionsItemSelected(item)
    }
}
