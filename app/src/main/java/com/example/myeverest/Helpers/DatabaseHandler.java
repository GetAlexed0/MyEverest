package com.example.myeverest.Helpers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {

    static FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    public static void checkAnswerSubmission(@NonNull CallBack<DocumentSnapshot> finishedCallback, String collection, String document) {
        DocumentReference answerDatabase = firestore.collection(collection).document(document);
        answerDatabase.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        finishedCallback.callback(document);
                        Log.d("Testcallback", "In der if");
                    } else {
                        Log.d("Testcallback", "In der else");
                    }
                }
                else {
                    Log.d("Testcallback", "Fehler", task.getException());
                }
            }
        });
    }
}
