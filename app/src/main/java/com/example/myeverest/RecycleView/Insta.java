package com.example.myeverest.RecycleView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myeverest.Helpers.CallBack;
import com.example.myeverest.Helpers.DataHandler;
import com.example.myeverest.Helpers.DatabaseHandler;
import com.example.myeverest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Insta extends Fragment {

    String username;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_insta, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        super.onCreate(savedInstanceState);
        FirebaseFirestore firestore;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = sharedPreferences.getString("username", "failed");

        View v = getView();
        firestore = FirebaseFirestore.getInstance();


        DatabaseHandler.checkAnswerSubmission(new CallBack<DocumentSnapshot>() {
            @Override
            public void callback(DocumentSnapshot data) throws ExecutionException, InterruptedException {
                int imageNumber = ((Long)data.get("uploadedImages")).intValue();
                Query query;
                if(imageNumber  >= 10) {
                    query = firestore.collection("images").whereGreaterThanOrEqualTo("number",imageNumber - (imageNumber - 10));
                }
                else {
                    query = firestore.collection("images").whereGreaterThanOrEqualTo("number",0);
                }

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            List<String> usernames = new ArrayList<>();
                            List<Bitmap> bitlist = new ArrayList<>();
                            List<Integer> likelist = new ArrayList<>();
                            List<DocumentSnapshot> doclist = new ArrayList<>();
                            for(DocumentSnapshot doc : task.getResult()) {
                                usernames.add(doc.get("username").toString());
                                likelist.add(((Long) doc.get("likes")).intValue());
                                Bitmap image = null;
                                try {
                                    image = new DataHandler.myTask().execute(doc.get("url").toString()).get();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                bitlist.add(image);

                                doclist.add(doc);

                            }
                            MyAdapter customAdapter = new MyAdapter(likelist, usernames, bitlist);
                            RecyclerView recyclerList = getView().findViewById(R.id.recycleTest);
                            recyclerList.setLayoutManager(new LinearLayoutManager(getContext()));
                            recyclerList.setAdapter(customAdapter);
                            recyclerList.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerList, new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    Log.d("Ausgabe", customAdapter.getStringAtPosition(position));
                                }

                                @Override
                                public void onLongItemClick(View view, int position) {
                                    doclist.get(position).getReference().update("likes", FieldValue.increment(1));
                                    customAdapter.setLikesAtPosition(position, likelist.get(position)+1);
                                    customAdapter.notifyItemChanged(position);
                                }
                            }));
                        }

                        else {
                            Log.d("Datenbankfehler", task.getException().getMessage());
                        }
                    }
                });
            }
        }, "images", "metadata");
    }
}