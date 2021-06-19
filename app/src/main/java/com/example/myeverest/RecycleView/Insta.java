package com.example.myeverest.RecycleView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.myeverest.Helpers.CallBack;
import com.example.myeverest.Helpers.CustomAdapter;
import com.example.myeverest.Helpers.DataHandler;
import com.example.myeverest.Helpers.DatabaseHandler;
import com.example.myeverest.R;
import com.example.myeverest.User.Account;
import com.example.myeverest.User.Friends;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.net.InternetDomainName;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Insta extends Fragment {

    ArrayList<String> liste = new ArrayList<String>();
    String username;
    static FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    int counter;
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
        FirebaseAuth fAuth;
        DocumentReference doc_ref;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = sharedPreferences.getString("username", "failed");

        View v = getView();
        doc_ref = firestore.collection("users").document(username);

        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if(document.exists()) {
                        List<String> freunde = (List<String>) document.get("friends");

                        List<Bitmap> bits = new ArrayList<>();
                        createBitmapList(new CallBack<List<Bitmap>>() {
                            @Override
                            public void callback(List<Bitmap> data) {
                                fillRecyclerView(data, freunde);
                            }
                        }, freunde);
                        for(int i = 0; i < freunde.size(); i++) {
                            counter = i;
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


}

    public void fillRecyclerView(List<Bitmap> bitlist, List<String> usernames) {
        MyAdapter customAdapter = new MyAdapter(usernames, bitlist);
        RecyclerView listView = getView().findViewById(R.id.recycleTest);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.setAdapter(customAdapter);

        listView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), listView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle args = new Bundle();
                args.putString("username_friend", liste.get(position));
                Fragment friend = new Friends();
                friend.setArguments(args);
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
            checkAnswerSubmission(new CallBack<DocumentSnapshot>() {
                @Override
                public void callback(DocumentSnapshot data) throws ExecutionException, InterruptedException {
                    if(data.get("profilePic") != null) {
                      Bitmap image = new myTask().execute(data.get("profilePic").toString()).get();
                      bitlist.add(image);

                    }

                    else {
                        bitlist.add(BitmapFactory.decodeResource(getResources(), R.drawable.test));
                    }

                    if(data.get("username").toString().equals(usernames.get(usernames.size()-1))) {
                        finishedCallback.callback(bitlist);
                    }
                }
            }, "users", usernames.get(i));
        }
    }
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

    private class myTask extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                return bitmap;
            } catch (IOException e) {
                // Log exception
                return bitmap;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            //do stuff

        }
    }

}