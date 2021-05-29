package com.example.myeverest.JavaTesting;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DBtests {

    static FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public static void testfun() {
        DocumentReference userRef = firestore.collection("users").document("sgullmann@gmail.com");

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                DocumentSnapshot snap = task.getResult();

                DocumentReference challenge = (DocumentReference) snap.get("ref");
                challenge.update("wurde", "bestanden");
            }
        });
    }
}
