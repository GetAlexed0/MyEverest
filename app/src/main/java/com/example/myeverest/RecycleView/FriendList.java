package com.example.myeverest.RecycleView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myeverest.Helpers.CallBack;
import com.example.myeverest.Helpers.DataHandler;
import com.example.myeverest.Helpers.DatabaseHandler;
import com.example.myeverest.R;
import com.example.myeverest.User.Friend_Account;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FriendList extends Fragment {

    ArrayList<String> liste = new ArrayList<String>();
    FloatingActionButton fab;
    String username;
    DocumentReference doc_ref;
    TextView friendInput;
    static FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    int counter;
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friendlist, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        super.onCreate(savedInstanceState);

        //Entnahme des Nutzernamens aus der Shared Preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = sharedPreferences.getString("username", "failed");

        //Befüllung der View
        View v = getView();
        fab = v.findViewById(R.id.addFriendButton);
        friendInput = v.findViewById(R.id.input_AddFriend);
        doc_ref = firestore.collection("users").document(username);

        //Aktualisiert Freunde
        refreshFriends();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabOnClickListener(friendInput);
            }
        });
    }

    //
    public void fillRecyclerView(List<Bitmap> bitlist, List<String> usernames) {
        MyAdapter customAdapter = new MyAdapter(usernames, bitlist);
        RecyclerView listView = getView().findViewById(R.id.recycleTest);
        //Erstellen des LayoutManagers und des Adapters
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.setAdapter(customAdapter);

        listView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), listView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle args = new Bundle();
                //Setzt NUtzernamen ins Bundle (Liste)
                args.putString("username_friend", usernames.get(position));
                Fragment friend = new Friend_Account();
                friend.setArguments(args);
                //Wenn man bei Freunden ist, wird das Freunde-Fragment geöffnet
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragmentContainerView, friend, "Tag");
                ft.commit();

            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

    }

    public void createBitmapList(@NonNull CallBack<List<Bitmap>> finishedCallback, List<String> usernames) {
        List<Bitmap> bitlist = new ArrayList<>();
        for(int i = 0; i < usernames.size(); i++) {
            DatabaseHandler.checkAnswerSubmission(new CallBack<DocumentSnapshot>() {
                @Override
                public void callback(DocumentSnapshot data) throws ExecutionException, InterruptedException {
                    if(data.get("profilePic") != null) {
                        //wenn Bild vorhanden, wird dieses in die Liste hinzugefügt
                        Bitmap image = new DataHandler.myTask().execute(data.get("profilePic").toString()).get();
                        bitlist.add(image);

                    }

                    else {
                        //Andernfalls wird ein Default Bild eingesetzt
                        bitlist.add(BitmapFactory.decodeResource(getResources(), R.drawable.test));
                    }
                    //Stimmen die Namen der Nutzer überein wird das Callback beendet
                    if(data.get("username").toString().equals(usernames.get(usernames.size()-1))) {
                        finishedCallback.callback(bitlist);
                    }
                }
            }, "users", usernames.get(i)); //Speicherung des Wertes an der Stelle in der Liste
        }
    }


    public void fabOnClickListener(TextView tv) {
        String newFriend = tv.getText().toString();
        if(!newFriend.isEmpty()) {
            DocumentReference friendDoc = firestore.collection("users").document(newFriend);
            friendDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            //wenn das Snapshot existiert, sollen die Freunde aus der Liste in der Datenbank entnommen werden
                            //neuer Freund wird hinzugefügt
                            doc_ref.update("friends", FieldValue.arrayUnion(newFriend));
                            refreshFriends();
                            tv.setText("");
                        } else {
                            tv.setError("Der angegebene User existiert nicht, hast du dich vertippt? \nBeachte Groß-/Kleinschreibung!");
                        }
                    }
                }
            });
        }
        else {
            tv.setError("Du hast nichts eingegeben");
        }
    }

    public void refreshFriends() {
        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    //aktuelle Freundesliste wird aus der Datenbank entnommen
                    if (document.exists()) {
                        List<String> freunde = (List<String>) document.get("friends");
                        if (freunde != null) {
                            List<Bitmap> bits = new ArrayList<>();
                            createBitmapList(new CallBack<List<Bitmap>>() {
                                @Override
                                public void callback(List<Bitmap> data) {
                                    fillRecyclerView(data, freunde);
                                }
                            }, freunde);
                        }
                    } else {
                        //Andernfalls wird die Exeption gerufen
                        System.out.println("get failed with " + task.getException());
                    }
                }
            }
        });

    }

}