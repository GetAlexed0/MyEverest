package com.example.myeverest

import android.os.Bundle
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
                    switchFragments(LocationMap())

                    true
                }
                R.id.friends_page -> {
                    switchFragments(FriendList())
                    true
                }
                R.id.logout_bar -> {
                    switchFragments(Insta())
                    /*firebaseAuth.signOut();
                    val switchActivityIntent = Intent(this, Login::class.java)
                    startActivity(switchActivityIntent);*/
                    true
                }
                else -> false
            }
        }

        /*firestore.collection("users").whereEqualTo("email", "sgullmann@gmail.com").get().addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("TAG", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents: ", exception)
            }*/

        /*firestore.collection("users")
            .whereEqualTo("email", "sgullmann@gmail.com")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        Toast.makeText(this, document["nachname"].toString(), Toast.LENGTH_SHORT)
                        Log.d("TAG", document["nachname"].toString())
                    }
                } else {
                    Log.d("TAG", "Error getting documents: ", task.exception)
                }
            }*/
    }

    override fun onResume() {
        super.onResume()
        if(firebaseAuth.currentUser != null) {
            firebaseAuth.currentUser.reload();
            if(firebaseAuth.currentUser.isEmailVerified) {
                //mailConfirmed.setText("Email bestätigt")
            }

        }
    }

    private fun switchFragments(fragment: Fragment) {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(R.id.fragmentContainerView.toString())
        transaction.commit()
    }
}

