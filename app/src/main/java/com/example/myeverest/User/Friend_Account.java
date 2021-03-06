package com.example.myeverest.User;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myeverest.Helpers.CallBack;
import com.example.myeverest.Helpers.DatabaseHandler;
import com.example.myeverest.MainActivity;
import com.example.myeverest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.List;

public class Friend_Account extends Fragment {

    TextView mUsername, mPrename, mSurname;
    String username, loggedUser;
    ListView listView;
    ImageView mProfilePic;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference docRef;


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.friends_main, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();

        //Entnahme der Freunde des aktuellen Nutzers aus Bundle
        Bundle args = getArguments();
        username = args.getString("username_friend");

        //Entnahme des aktuelles NUtzers
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        loggedUser = sharedPreferences.getString("username", "failed");

        //Collection Instanz mit Nutzernamen
        docRef = firestore.collection("users").document(username);

        listView = v.findViewById(R.id.listView_friends);
        mProfilePic = v.findViewById(R.id.profilePic_friends);
        mUsername = v.findViewById(R.id.friend_username);
        mPrename = v.findViewById(R.id.friend_prename);
        mSurname = v.findViewById(R.id.friend_surname);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        prepareDataForUser();
    }

    private void prepareDataForUser() {

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if(snapshot.exists()) {

                        //Speicherung der Daten in Logcat
                        Log.d("Friends_Profile", "Current data: " + snapshot.get("vorname"));
                        Log.d("Friends_Profile", "Current data: " + snapshot.get("nachname"));
                        Log.d("Friends_Profile", "Current data: " + snapshot.get("username"));

                        //Felder bef??llen mit Daten aus der Datenbank
                        mPrename.setText(snapshot.get("vorname").toString());
                        mSurname.setText(snapshot.get("nachname").toString());
                        mUsername.setText(snapshot.get("username").toString());

                        if(snapshot.get("profilePic") != null) {
                            loadUserImage(getView(), snapshot.get("profilePic").toString());
                        }

                        refreshFriends();
                    }
                }
            }
        });
    }

    public void loadUserImage(View v, String imageUrl) {

        //Bei vorhandenem Bild wird das Bild aus URL entnommen
        new DownloadImageFromInternet((ImageView) v.findViewById(R.id.profilePic_friends)).execute(imageUrl);
    }

    public void refreshFriends() {
        DatabaseHandler.checkAnswerSubmission(new CallBack<DocumentSnapshot>() {
            @Override
            public void callback(DocumentSnapshot data) {
                //Liste mit Challenges erstellen
                List<String> list = (List<String>) data.get("challenges");
                if(list != null) {
                    //Bei bef??llter Liste neuen Adapter erstellen mit Bezug auf den Nutzer und dessen Challenges
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, list);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String challenge = list.get(position);
                            if (!challenge.isEmpty()) {
                                firestore.collection("users").document(loggedUser).update("challenges", FieldValue.arrayUnion(challenge));
                                firestore.collection("challenges").document(challenge).update("users", FieldValue.arrayUnion(loggedUser));
                            }
                        }
                    });
                }
            }
        }, "users", username);
    }


    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {

        ImageView imageView;
        //Bild wird aus URL entnommen und in eine Bitmap umgewandelt
        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView=imageView;
        }
        protected Bitmap doInBackground(String... urls) {
            String imageURL=urls[0];
            Bitmap bimage=null;
            try {
                InputStream in=new java.net.URL(imageURL).openStream();
                bimage= BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}
