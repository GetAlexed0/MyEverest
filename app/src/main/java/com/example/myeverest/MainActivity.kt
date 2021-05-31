package com.example.myeverest

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.ui.AppBarConfiguration
import com.example.myeverest.challenges.Maps
import com.example.myeverest.ui.gallery.GalleryFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var mailConfirmed: TextView;


    private lateinit var appBarConfiguration: AppBarConfiguration

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mailConfirmed = findViewById(R.id.emailconfirmed)
        if(firebaseAuth.currentUser != null) {
            val fuser: FirebaseUser = firebaseAuth.currentUser
            fuser.reload()
            if(fuser.isEmailVerified) {
                mailConfirmed.setText("Email bestätigt")
            }
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {

                R.id.challenge_page -> {
                    switchFragments(ChallengeOverview());
                    true
                }
                R.id.user_page -> {
                    switchFragments(Account())
                    true
                }
                R.id.map_page -> {
                    //Fragment öffnen
                    switchFragments(Maps())
                    true
                }
                R.id.friends_page -> {
                    val switchActivityIntent = Intent(this, Friends::class.java)
                    startActivity(switchActivityIntent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(firebaseAuth.currentUser != null) {
            firebaseAuth.currentUser.reload();
            if(firebaseAuth.currentUser.isEmailVerified) {
                mailConfirmed.setText("Email bestätigt")
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    private fun switchFragments(fragment: Fragment) {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}