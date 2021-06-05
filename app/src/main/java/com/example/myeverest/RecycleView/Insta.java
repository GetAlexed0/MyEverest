package com.example.myeverest.RecycleView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.myeverest.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.net.InternetDomainName;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Insta extends AppCompatActivity {

    String currentUser;
    ArrayList<String> liste = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insta);
        FirebaseAuth fAuth;
        FirebaseFirestore firestore;
        DocumentReference doc_ref;


        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        doc_ref = firestore.collection("users").document(fAuth.getCurrentUser().getEmail());


        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if(document.exists()) {
                        List<String> freunde = (List<String>) document.get("friends");
                            for(int i = 0; i < freunde.size(); i++) {
                                liste.add(freunde.get(i).toString());
                            }
                                for(int i = 0; i < liste.size(); i++) {
                                    Log.d("Ausgabe", liste.get(i));
                                }
                    }
                }
                else {
                    System.out.println("get failed with "+ task.getException());
                }
            }
        });

        ListView listView = findViewById(R.id.listView);
        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, liste);
        listView.setAdapter(itemsAdapter);

    }
}