package com.example.myeverest.RecycleView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

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

        View v = getView();
        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        doc_ref = firestore.collection("users").document(fAuth.getCurrentUser().getEmail());

        ListView listView = v.findViewById(R.id.listView);
        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if(document.exists()) {
                        List<String> freunde = (List<String>) document.get("friends");
                        for(int i = 0; i < freunde.size(); i++) {
                            liste.add(freunde.get(i));
                            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, liste);
                            listView.setAdapter(itemsAdapter);
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String position_friend;

                Log.d("Liste_Freunde", liste.get(position));
                position_friend = liste.get(position);

                Bundle args = new Bundle();
                args.putString("username_friend",liste.get(position));

                Fragment friends = new Friends();

                friends.setArguments(args);
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragmentContainerView, friends, "Freunde");
                ft.commit();
            }
        });

}

}