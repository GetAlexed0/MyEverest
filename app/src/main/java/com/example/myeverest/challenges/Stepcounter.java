package com.example.myeverest.challenges;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myeverest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class Stepcounter extends Fragment implements SensorEventListener {
    Sensor stepSensor;
    SensorManager sManager;
    TextView stepText;
    ProgressBar stepBar;
    Button stepButton;
    Button resetButton;


    boolean running = false;
    private long steps = 0;
    String challengeID, username;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference userDoc;
    DocumentReference challengeDoc;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stepcounter, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        if(ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED && Build.VERSION.SDK_INT >= 23){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 19);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = sharedPreferences.getString("username", "failed");
        userDoc = firestore.collection("users").document(username);
        //challengeDoc = firestore.collection("challenges").document(challengeID);
        sManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        stepText = v.findViewById(R.id.stepcounter);
        stepBar = v.findViewById(R.id.stepBar);
        stepBar.setIndeterminate(false);
        stepBar.setSecondaryProgress(100);

        stepButton = v.findViewById(R.id.step_btn);
        stepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                steps++;
                stepText.setText("Schritte: " + steps);
                stepBar.setProgress((int) steps);
            }
        });

        resetButton = v.findViewById(R.id.reset_btn);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                steps = 0;
                stepText.setText("Schritte: " + steps);
                stepBar.setProgress((int) steps, false);
            }
        });

        stepBar.setMax(300);
    }

    @Override
    public void onResume() {
        super.onResume();
        running = true;
        stepSensor = sManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepBar.setIndeterminate(false);

        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if(document.exists()) {
                        if(document.get("steps") != null) {
                            Object i = document.get("steps");
                            steps = (long) i;
                            stepText.setText("Schritte: " + steps);
                            stepBar.setProgress((int) steps);
                        }

                        else {
                            userDoc.update("steps", 0);
                            steps = 0;
                        }
                    }

                    else {
                        System.out.println("Document nicht existent");
                        System.out.println(currentUser.getEmail());
                    }
                }
                else {
                    System.out.println("get failed with "+ task.getException());
                }
            }
        });
        if(stepSensor == null) {
            Toast.makeText(getActivity(), "No Sensor detected", Toast.LENGTH_SHORT).show();
        }
        else {
            sManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(getActivity(), "Listener registered", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        running = false;
        sManager.unregisterListener(this);
        userDoc.update("steps", steps);
    }

    @Override
    public void onStop() {
        super.onStop();
        sManager.unregisterListener(this, stepSensor);
        userDoc.update("steps", steps);


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(running) {
            steps++;
            stepText.setText("Schritte: " + steps);
            stepBar.setProgress((int) steps);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void finishChallenge() {
        //TODO
    }
}