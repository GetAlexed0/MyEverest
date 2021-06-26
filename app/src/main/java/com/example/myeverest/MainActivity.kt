package com.example.myeverest

import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.ui.AppBarConfiguration
import com.example.myeverest.RecycleView.FriendList
import com.example.myeverest.RecycleView.Insta
import com.example.myeverest.User.Account
import com.example.myeverest.challenges.ChallengeOverview
import com.example.myeverest.challenges.LocationMap
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*


class MainActivity : AppCompatActivity() {

    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var mailConfirmed: TextView;
    lateinit var userName: String;


    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //mailConfirmed = findViewById(R.id.emailconfirmed)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        //Regelt wechsel zwischen Fragmenten über die Fußleiste
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {

                R.id.challenge_page -> {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                    switchFragments(ChallengeOverview());
                    true
                }
                R.id.user_page -> {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                    switchFragments(Account())
                    true
                }
                R.id.map_page -> {
                    //Fragment öffnen
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                    switchFragments(LocationMap())

                    true
                }
                R.id.friends_page -> {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                    switchFragments(FriendList())
                    true
                }
                R.id.insta -> {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                    switchFragments(Insta())
                    true
                }
                else -> false
            }
        }
    }

    //checkt ob der User nach neuladen angemeldet ist
    override fun onResume() {
        super.onResume()
        if(firebaseAuth.currentUser != null) {
            firebaseAuth.currentUser.reload();
            if(firebaseAuth.currentUser.isEmailVerified) {

            }

        }
    }

    //wechselt fragmentContainerView zu mitgegebenem fragment
    private fun switchFragments(fragment: Fragment) {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(R.id.fragmentContainerView.toString())
        transaction.commit()
    }


}

