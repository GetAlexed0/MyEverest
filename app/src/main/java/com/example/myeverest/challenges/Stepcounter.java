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
import android.widget.ImageView;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class Stepcounter extends Fragment implements SensorEventListener {
    Sensor stepSensor;
    SensorManager sManager;
    TextView stepText;
    ProgressBar stepBar;
    Button stepButton, resetButton;
    Bundle arguments;


    boolean running = false;
    private int steps = 0;
    private int goalsteps;
    String username;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference userDoc;

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

        //Holt Berechtigungen für Schrittzähler ein
        if(ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED && Build.VERSION.SDK_INT >= 23){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 19);
        }

        //Holt Schrittzahl der Challenge aus dem Bundle des Fragments
        arguments = getArguments();
        goalsteps = arguments.getInt("steps");

        //zieht Nutzername aus Telefonspeicher
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = sharedPreferences.getString("username", "failed");

        userDoc = firestore.collection("users").document(username);
        sManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        stepText = v.findViewById(R.id.stepcounter);
        stepBar = v.findViewById(R.id.stepBar);
        stepBar.setIndeterminate(false);
        stepBar.setSecondaryProgress(100);

        stepText.setText("Schritte: 0 \n von " + goalsteps );

        stepButton = v.findViewById(R.id.step_btn);
        stepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                steps++;
                stepText.setText("Schritte: " + steps + "\n von " + goalsteps );
                //Fügt Schritte dem User in der DB hinzu und aktualisiert die Ansicht
                userDoc.update("steps", FieldValue.increment(1));
                stepBar.setProgress(steps);

                //Sobald Schrittziel erreicht wurde verschwindet der Abbrechen-Button
                if(steps >= goalsteps) {
                    Fragment frag = getFragmentManager().findFragmentById(R.id.fragmentContainerView);
                    ImageView cancel = frag.getView().findViewById(R.id.cancelButton);
                    cancel.setVisibility(View.INVISIBLE);
                }

            }
        });
        //Setzt Schritte auf 0 zurück
        resetButton = v.findViewById(R.id.reset_btn);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                steps = 0;
                stepText.setText("Schritte: " + steps);
                stepBar.setProgress(steps, false);
            }
        });

        stepBar.setMax(goalsteps);
    }

    @Override
    public void onResume() {
        super.onResume();
        running = true;
        stepBar.setIndeterminate(false);

        //Initialisiert den Schrittsensor falls vorhanden
        stepSensor = sManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if(stepSensor == null) {
            Toast.makeText(getActivity(), "Kein Sensor gefunden", Toast.LENGTH_SHORT).show();
        }
        else {
            sManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(getActivity(), "Listener registriert", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        //Stoppt den Schrittsensor
        super.onPause();
        running = false;
        sManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        //Stoppt den Schrittsensor
        super.onStop();
        sManager.unregisterListener(this, stepSensor);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Sofern Schrittsensor läuft werden Schritte registiriert und DB + Ansicht aktualisiert
        if(running) {
            steps++;
            stepText.setText("Schritte: " + steps + "\n von " + goalsteps );
            userDoc.update("steps", FieldValue.increment(1));
            stepBar.setProgress(steps);

            //Versteckt Abbrechen-Button sofern Schrittzahl erreicht
            if(steps >= goalsteps) {
                ImageView cancel = getParentFragment().getView().findViewById(R.id.cancelButton);
                cancel.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}