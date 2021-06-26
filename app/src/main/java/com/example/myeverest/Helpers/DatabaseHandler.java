package com.example.myeverest.Helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.ExecutionException;

public class DatabaseHandler {

    static FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    //Zieht sich das Firestore-Dokument mit dem Pfad collection/document aus der Datenbank und gibt es nach Abschluss des herunterladens
    // als Callback in Form eines Interfaces zurück
    public static void checkAnswerSubmission(@NonNull CallBack<DocumentSnapshot> finishedCallback, String collection, String document) {
        DocumentReference answerDatabase = firestore.collection(collection).document(document);
        answerDatabase.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        try {
                            finishedCallback.callback(document);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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