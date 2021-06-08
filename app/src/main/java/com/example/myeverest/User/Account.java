package com.example.myeverest.User;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.myeverest.Helpers.DataHandler;
import com.example.myeverest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class Account extends Fragment {

    TextView mUsername, mPrename, mAddress, mBirthdate, mSurname, mEMail;

    TextView currentLvl, progressAbsolute;
    Button mChangeButton;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String vorname, nachname, adresse, geburtsdatum, username;
    ProgressBar lvlBar;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private ImageView profilePic;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    DocumentReference docRef = firestore.collection("users").document(fAuth.getCurrentUser().getEmail());

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = sharedPreferences.getString("username", "failed");
        docRef = firestore.collection("users").document(username);

        mPrename = v.findViewById(R.id.editTextPrename_account);
        mSurname = v.findViewById(R.id.editTextSurname2);
        mAddress = v.findViewById(R.id.editTextTextPostalAddress);
        mBirthdate = v.findViewById(R.id.editTextDate);
        profilePic = v.findViewById(R.id.profilePic);
        mChangeButton = v.findViewById(R.id.setUserAttributes_btn);
        mEMail = v.findViewById(R.id.editTextEmailAddress);
        mUsername = v.findViewById(R.id.username);
        lvlBar = v.findViewById(R.id.lvlbar);
        currentLvl = v.findViewById(R.id.currentlvl);
        progressAbsolute = v.findViewById(R.id.progressabsolute);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        prepareDataForUser();

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


                if(TextUtils.isEmpty(vorname)) {
                    mPrename.setError("Kein Vorname eingegeben");
                    return;
                }

                docRef.update("vorname", vorname);


                if(TextUtils.isEmpty(nachname)) {
                    mSurname.setError("Kein Nachname eingegeben");
                    return;
                }

        docRef.update("nachname", nachname);


                if(TextUtils.isEmpty(adresse)) {
                    mAddress.setError("Keine Adresse eingegeben");
                    return;
        }

        docRef.update("adresse", adresse );

        if(TextUtils.isEmpty(geburtsdatum) && geburtsdatum.length() < 10) {
            mBirthdate.setError("Keingültiges Geburtsdatum eingegeben");
            return;
        }

        docRef.update("geburtsdatum", geburtsdatum);

    }

    private void prepareDataForUser() {

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if(snapshot.exists()) {
                        Log.d("Account", "Current data: " + snapshot.get("vorname"));
                        Log.d("Account", "Current data: " + snapshot.get("nachname"));
                        Log.d("Account", "Current data: " + snapshot.get("adresse"));
                        Log.d("Account", "Current data: " + snapshot.get("geburtsdatum"));
                        Log.d("Account", "Current data: " + snapshot.get("email"));
                        Log.d("Account", "Current data: " + snapshot.get("username"));

                        mPrename.setText(snapshot.get("vorname").toString());
                        mSurname.setText(snapshot.get("nachname").toString());
                        mUsername.setText(snapshot.get("username").toString());
                        if(snapshot.get("adresse") != null) {
                            mAddress.setText(snapshot.get("adresse").toString());
                        }
                        if(snapshot.get("geburtsdatum") != null) {
                            mBirthdate.setText(snapshot.get("geburtsdatum").toString());
                        }
                        mEMail.setText(snapshot.get("email").toString());
                        int test = ((Long) snapshot.get("points")).intValue();
                        currentLvl.setText(String.valueOf(getLevel(test)));
                        lvlBar.setProgress((int) (getProgressToNextLevel(test, true)*100));
                        progressAbsolute.setText(String.valueOf((int) (getProgressToNextLevel(test, false))) + "\n Punkte bis " + String.valueOf(getLevel(test)+1));

                        if(snapshot.get("profilePic") != null) {
                            loadUserImage(getView(), snapshot.get("profilePic").toString());
                        }
                    }
                }
            }
        });
        mEMail.setFocusable(false);
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
            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            float aspectRatio = bmp.getWidth() / (float) bmp.getHeight();
            int width = 700;
            int height = Math.round(width /aspectRatio);
            bmp = Bitmap.createScaledBitmap(bmp, width, height, false);
            bmp.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] fileInBytes = baos.toByteArray();
            profilePic.setImageURI(imageUri);
            uploadPicture(fileInBytes);
        }
    }

    private void uploadPicture(byte[] image) {


        final View v = getView();
        final ProgressDialog pd = new ProgressDialog(v.getContext());
        pd.setTitle("Bild wird hochgeladen...");
        pd.show();

        StorageReference riversRef = storageReference.child("images/" + username);

        riversRef.putBytes(image)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Snackbar.make(getView(), "Bild erfolgreich hinzugefügt", Snackbar.LENGTH_LONG).show();
                        riversRef.getDownloadUrl()
                                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Uri> task) {
                                        if(task.isSuccessful()) {
                                            Uri downloadUri = task.getResult();
                                            docRef.update("profilePic", downloadUri.toString());
                                        }
                                    }
                                });
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

    public void loadUserImage(View v, String imageUrl) {
        /*Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ImageView i = (ImageView) v.findViewById(R.id.profilePic);
                    Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageUrl).getContent());
                    i.setImageBitmap(bitmap);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    thread.start();*/
        new DownloadImageFromInternet((ImageView) v.findViewById(R.id.profilePic)).execute(imageUrl);
    }
    private static int getLevel(int xp) {
        int ret = (int) (-1 + Math.sqrt(1+xp/100));
        return ret;
    }

    private static double getProgressToNextLevel(int xp, boolean relative) {
        double lvl = getLevel(xp);
        double explvlbefore = 100 * Math.pow(lvl, 2) + 200*(lvl);
        double expneeded = 100 * Math.pow(lvl+1, 2) + 200*(lvl+1);
        double differenceBefore = xp - explvlbefore;
        double differenceNext = expneeded - explvlbefore;
        if(!relative) {
            return expneeded - xp;
        }
        System.out.println("Nächstes Level exp: " + expneeded);
        return differenceBefore/differenceNext;
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
                bimage=BitmapFactory.decodeStream(in);
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




