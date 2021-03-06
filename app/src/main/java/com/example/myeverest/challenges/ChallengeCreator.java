package com.example.myeverest.challenges;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.myeverest.Helpers.CallBack;
import com.example.myeverest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ChallengeCreator extends Fragment {

    TextView text, descriptionInput, pointInput, titleInput, textView;
    Button button;
    Slider seekBar;
    Bundle arguments;
    String type, username;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_challenge_creator, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        descriptionInput = v.findViewById(R.id.description_input);
        titleInput = v.findViewById(R.id.title_input);
        pointInput = v.findViewById(R.id.points_input);

        //zieht Nutzername aus dem Telefonspeicher
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = sharedPreferences.getString("username", "failed");

        button = v.findViewById(R.id.create_walking_btn);
        seekBar = v.findViewById(R.id.steps_seekBar);

        //zieht mitgegebene Paramenter aus dem Fragment
        arguments = getArguments();
        type = arguments.getString("type");

        //zeigt Leiste zum einstellen der Schrittzahl sofern es sich um eine Schrittchallenge handelt
        if(type == "WALK")
        {
            seekBar.setVisibility(View.VISIBLE);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //erstellt challenge sofern ben??tigte Felder ausgef??llt sind
                if(checkFilledFields()) {
                    createChallenge();
                }
            }
        });

        seekBar.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull @NotNull Slider slider, float value, boolean fromUser) {
                //text.setText(String.valueOf(seekBar.getValue()));
            }
        });
    }


    //schaut ob alle ben??tigten Textfelder bef??llt sind und gibt ggfs Fehler aus
    public boolean checkFilledFields() {
        boolean ret = true;
        if(titleInput.getText().toString().isEmpty()) {
            titleInput.setError("Titel darf nicht leer sein");
            ret = false;
        }

        if(descriptionInput.getText().toString().isEmpty()) {
            descriptionInput.setError("Beschreibung darf nicht leer sein");
            ret = false;
        }

        if(pointInput.getText().toString().isEmpty()) {
            pointInput.setError("Willst du keine Punkte? :)");
            ret = false;
        }

        return ret;
    }

    public void createChallenge() {
        String challengetitle = titleInput.getText().toString().trim();
        DocumentReference challenge = firestore.collection("challenges").document(challengetitle);
        challenge.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if(doc.exists()) {
                                //Wenn Challenge schon existiert
                                titleInput.setError("Der Titel ist leider vergeben, versuch es doch mal mit einem anderen :)");
                            }

                            else {
                                String desc = descriptionInput.getText().toString().trim();
                                int points = Integer.parseInt(pointInput.getText().toString().trim());

                                //Erstellen und bef??llen einer Map f??r Firestore
                                Map<String, Object> challengeMap = new HashMap<>();
                                challengeMap.put("title", challengetitle);
                                challengeMap.put("description", desc);
                                challengeMap.put("points", points);
                                challengeMap.put("creator", username);
                                challengeMap.put("users", Arrays.asList(username));
                                challengeMap.put("type", type);

                                if(type == "WALK") {
                                    challengeMap.put("steps", seekBar.getValue());
                                }
                                //??bertr??gt challengeMap in DB
                                challenge.set(challengeMap);

                                DocumentReference createdBy = firestore.collection("users").document(username);
                                //f??gt challenge dem User hinzu
                                createdBy.update("challenges", FieldValue.arrayUnion(challengetitle));

                                //??ffnet erstellte Challenge und gibt Challengeart und den Titel mit
                                Fragment individualFragment = new ChallengePage();
                                Bundle individualArguments = new Bundle();
                                individualArguments.putString("type", type);
                                individualArguments.putString("title", challengetitle);
                                individualFragment.setArguments(individualArguments);
                                switchFragments(individualFragment);

                            }
                        }
                    }
                });
    }


    //wechselt fragmentContainerView zu mitgegebenem Fragment
    private void switchFragments(Fragment fragment) {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainerView, fragment, "TAG");
        ft.commit();
    }
}