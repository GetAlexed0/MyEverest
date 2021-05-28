package com.example.myeverest

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myeverest.challenges.MapsActivity
import com.example.myeverest.challenges.WalkingChallenge
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        if(firebaseAuth.currentUser != null) {
            val fuser: FirebaseUser = firebaseAuth.currentUser
            fuser.reload()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { (switchActivities())
        }

        val logout: Button = findViewById(R.id.logout_btn)
        logout.setOnClickListener(View.OnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, Login::class.java))
        })

        val mapsButton: Button = findViewById(R.id.maps_btn)
        mapsButton.setOnClickListener(View.OnClickListener {
            val switchActivityIntent = Intent(this, MapsActivity::class.java)
            startActivity(switchActivityIntent)
        })

        val testbtn: Button = findViewById(R.id.test_btn)

        testbtn.setOnClickListener(View.OnClickListener {
            val switchActivityIntent = Intent(this, Userprofile::class.java)
            startActivity(switchActivityIntent)
        })
        val confirmedMailText: TextView = findViewById(R.id.emailconfirmed)
        if(firebaseAuth.currentUser != null) {
            firebaseAuth.currentUser.reload();
        }
        if(firebaseAuth.currentUser != null && firebaseAuth.currentUser.isEmailVerified) {
           confirmedMailText.setText("Email bestätigt")
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        val friendsButton: Button = findViewById(R.id.friends_btn)

        friendsButton.setOnClickListener(View.OnClickListener {
            val switchActivityIntent = Intent(this, WalkingChallenge::class.java)
            startActivity(switchActivityIntent)
        })
    }

    override fun onResume() {
        super.onResume()
        if(firebaseAuth.currentUser != null) {
            firebaseAuth.currentUser.reload();
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, Register::class.java)
        startActivity(switchActivityIntent)
    }
}