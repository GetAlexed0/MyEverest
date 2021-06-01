package com.example.myeverest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class Account extends Fragment {

    EditText mPrename, mAddress, mBirthdate, mSurname;
    Button mChangeButton;
    FirebaseAuth fAuth;
    FirebaseFirestore firestore;
    String vorname;
    String nachname;
    String adresse;
    String geburtsdatum;

    private ImageView profilePic;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();

        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        mPrename = v.findViewById(R.id.editTextPrename2);
        mSurname = v.findViewById(R.id.editTextSurname2);
        mAddress = v.findViewById(R.id.editTextTextPostalAddress);
        mBirthdate = v.findViewById(R.id.editTextDate);
        profilePic = v.findViewById(R.id.profilePic);
        mChangeButton = v.findViewById(R.id.setUserAttributes_btn);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                choosePicture();
            }
        });

        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeData();
            }
        });
    }
    public void changeData() {


        vorname = mPrename.getText().toString().trim();
        nachname = mSurname.getText().toString().trim();
        adresse = mAddress.getText().toString().trim();
        geburtsdatum = mBirthdate.getText().toString().trim();

        DocumentReference docRef = firestore.collection("users").document(fAuth.getCurrentUser().getEmail());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot snapshot,
                                @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Log.w("Account", "Listen failed.", error);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("Account", "Current data: " + snapshot.get("vorname"));
                    //rest hinzufügen
                } else {
                    Log.d("Account", "Current data: null");
                }
            }
        });
 //null prüfen
        docRef.update("vorname", vorname);
        docRef.update("nachname", nachname);
        docRef.update("adresse", adresse );
        docRef.update("geburtsdatum", geburtsdatum);

        }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            profilePic.setImageURI(imageUri);
            uploadPicture();

        }
    }

    private void uploadPicture() {

        final View v = getView();
        final ProgressDialog pd = new ProgressDialog(v.getContext());
        pd.setTitle("Bild wird hochgeladen...");
        pd.show();

        final String randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/" + randomKey);

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Snackbar.make(getView(), "Bild erfolgreich hinzugefügt", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(v.getContext(), "Fehlgeschlagen", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull @NotNull UploadTask.TaskSnapshot snapshot) {
                        double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        pd.setMessage("Stand: " + (int) progressPercent + "%" );
                    }
                });


    }
}


