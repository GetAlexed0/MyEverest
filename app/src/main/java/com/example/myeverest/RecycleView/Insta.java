package com.example.myeverest.RecycleView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myeverest.Helpers.CallBack;
import com.example.myeverest.Helpers.DataHandler;
import com.example.myeverest.Helpers.DatabaseHandler;
import com.example.myeverest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static android.app.Activity.RESULT_OK;

public class Insta extends Fragment {

    String username;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private Uri imageUri;
    FloatingActionButton fab;

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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = sharedPreferences.getString("username", "failed");

        View v = getView();
        firestore = FirebaseFirestore.getInstance();
        fab = v.findViewById(R.id.uploadGalleryImage);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

        refreshFeed();
    }

    public void refreshFeed() {
        DatabaseHandler.checkAnswerSubmission(new CallBack<DocumentSnapshot>() {
            @Override
            public void callback(DocumentSnapshot data) throws ExecutionException, InterruptedException {
                int imageNumber = ((Long)data.get("uploadedImages")).intValue();
                Query query;
                if(imageNumber  >= 10) {
                    query = firestore.collection("images").whereGreaterThanOrEqualTo("number",imageNumber - 10);
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

                            Collections.reverse(likelist);
                            Collections.reverse(usernames);
                            Collections.reverse(bitlist);

                            MyAdapter customAdapter = new MyAdapter(likelist, usernames, bitlist);
                            RecyclerView recyclerList = getView().findViewById(R.id.recycleTest);
                            recyclerList.setLayoutManager(new LinearLayoutManager(getContext()));
                            recyclerList.setAdapter(customAdapter);
                            recyclerList.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerList, new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                }

                                @Override
                                public void onLongItemClick(View view, int position) {
                                    int invertedpos = likelist.size() - 1 - position;
                                    doclist.get(invertedpos).getReference().update("likes", FieldValue.increment(1));
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
            uploadPicture(fileInBytes);
        }
    }

    private void uploadPicture(byte[] image) {
        final View v = getView();
        final ProgressDialog pd = new ProgressDialog(v.getContext());
        pd.setTitle("Bild wird hochgeladen...");
        pd.show();
        DatabaseHandler.checkAnswerSubmission(new CallBack<DocumentSnapshot>() {
            @Override
            public void callback(DocumentSnapshot data) throws ExecutionException, InterruptedException {
                int picNum = ((Long) data.get("uploadedImages")).intValue();
                StorageReference riversRef = storageReference.child("images/" + picNum);
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
                                                    DocumentReference imageRef = firestore.collection("images").document(String.valueOf(picNum));
                                                   data.getReference().update("uploadedImages", FieldValue.increment(1));
                                                    Map<String, Object> imageMap = new HashMap<>();
                                                    imageMap.put("url", downloadUri.toString());
                                                    imageMap.put("username", username);
                                                    imageMap.put("likes", 0);
                                                    imageMap.put("number", picNum);
                                                    imageRef.set(imageMap);
                                                    refreshFeed();
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
        }, "images", "metadata");




    }
}