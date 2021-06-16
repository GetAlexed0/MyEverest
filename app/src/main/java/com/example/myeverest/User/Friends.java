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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myeverest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class Friends extends Fragment {

    TextView mUsername, mPrename, mSurname;
    String username;
    ImageView mProfilePic;
    private ImageView profilePic;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
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

       Bundle args = getArguments();
       username = args.getString("username_friend");

       docRef = firestore.collection("users").document(username);

        mProfilePic = v.findViewById(R.id.profilePic_friends);
        mUsername = v.findViewById(R.id.textViewUsername_friends);
        mPrename = v.findViewById(R.id.textViewPrename_friends);
        mSurname = v.findViewById(R.id.textViewSurname_friends);

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
                        Log.d("Friends_Profile", "Current data: " + snapshot.get("vorname"));
                        Log.d("Friends_Profile", "Current data: " + snapshot.get("nachname"));
                        Log.d("Friends_Profile", "Current data: " + snapshot.get("username"));

                        mPrename.setText(snapshot.get("vorname").toString());
                        mSurname.setText(snapshot.get("nachname").toString());
                        mUsername.setText(snapshot.get("username").toString());

                        if(snapshot.get("profilePic") != null) {
                            loadUserImage(getView(), snapshot.get("profilePic").toString());
                        }
                    }
                }
            }
        });
    }

    public void loadUserImage(View v, String imageUrl) {

        new DownloadImageFromInternet((ImageView) v.findViewById(R.id.profilePic_friends)).execute(imageUrl);
    }


    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {

        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView=imageView;
            Toast.makeText(getActivity().getApplicationContext(), "Please wait, it may take a few minute...",Toast.LENGTH_SHORT).show();
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
