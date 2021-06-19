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
import android.widget.ListView;

import com.example.myeverest.Helpers.CustomAdapter;
import com.example.myeverest.R;
import com.example.myeverest.User.Friends;
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

public class Insta extends Fragment {

    ArrayList<String> liste = new ArrayList<String>();
    String username;
    int likes = 0;

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
        FirebaseFirestore firestore;
        DocumentReference doc_ref;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = sharedPreferences.getString("username", "failed");

        View v = getView();
        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        doc_ref = firestore.collection("users").document(username);

        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if(document.exists()) {
                        List<String> freunde = (List<String>) document.get("friends");


                        for(int i = 0; i < freunde.size(); i++) {
                            liste.add(freunde.get(i));
                        }
                        //ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, freunde);
                        List<Bitmap> bitlist = new ArrayList<Bitmap>();
                        for(int i = 0; i < liste.size(); i++) {
                            bitlist.add(BitmapFactory.decodeResource(getResources(), R.drawable.test));
                        }

                        MyAdapter customAdapter = new MyAdapter(liste, bitlist);
                        RecyclerView listView = v.findViewById(R.id.recycleTest);
                        listView.setLayoutManager(new LinearLayoutManager(getContext()));
                        listView.setAdapter(customAdapter);

                        listView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), listView, new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Log.d("Ausgabe", customAdapter.getStringAtPosition(position));
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }
                        }));

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

    public static Bitmap createImage(int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        return bitmap;
    }


    public void createLikes(List<Bitmap> bitmapList, List<String> usernames) {


        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = sharedPreferences.getString("username", "failed");
        docRef = firestore.collection("users").document(username);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if(snapshot.exists()) {
                        Log.d("Account", "Current data: " + snapshot.get("username"));
                        Log.d("Account", "Current data: " + snapshot.get("like"));

                         likes = ((Long) snapshot.get("like")).intValue();

                    }
                }
            }
        });

        MyAdapter customAdapter = new MyAdapter(usernames,bitmapList);
        RecyclerView recyclerList = getView().findViewById(R.id.recycleTest);
        recyclerList.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerList.setAdapter(customAdapter);

        recyclerList.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                }

            @Override
            public void onLongItemClick(View view, int position) {
                likes = likes +1;
                docRef.update("like", likes);
            }
        }));


    }



}