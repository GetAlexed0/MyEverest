package com.example.myeverest.challenges;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myeverest.Helpers.CallBack;
import com.example.myeverest.Helpers.DatabaseHandler;
import com.example.myeverest.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class ChallengePage extends Fragment {

    String title, username, type;
    ConstraintLayout background;
    TextView titleText, descText, pointText;
    ImageView confirm, cancel;
    Bundle arguments;
    long points = -1;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_complete_challenge, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = sharedPreferences.getString("username", "failed");

        arguments = getArguments();
        type = arguments.getString("type");
        title = arguments.getString("title");

        titleText = v.findViewById(R.id.challengeTitel);
        titleText.setText(title);

        pointText = v.findViewById(R.id.pointOutput);
        descText = v.findViewById(R.id.challengeDescr);
        confirm = v.findViewById(R.id.confirmButton);
        cancel = v.findViewById(R.id.cancelButton);

        background = v.findViewById(R.id.background);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(points != -1) {
                    finishChallenge(false, points);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishChallenge(true, 0);
            }
        });
        DatabaseHandler.checkAnswerSubmission(new CallBack<DocumentSnapshot>() {
            @Override
            public void callback(DocumentSnapshot data) {
                String desc = data.get("description").toString();
                String ctitle = data.get("title").toString();
                points = (long) data.get("points");
                initializeFields(ctitle, desc, (int) points);
                fillInnerFragment(data);
            }
        }, "challenges", title);
    }

    public void initializeFields(String challengeTitel, String challengeDesc, int points) {
        titleText.setText(challengeTitel);
        descText.setText("Challengebeschreibung: " + challengeDesc);
        pointText.setText("Belohnung: \n" + String.valueOf(points) + " Punkte");
    }

    public void fillInnerFragment(DocumentSnapshot doc) {
        Bundle arguments = new Bundle();

        if(type.equals("WALK")) {
            arguments.putDouble("steps", (double) doc.get("steps"));
            Fragment fragment = new Stepcounter();
            fragment.setArguments(arguments);
            switchfragment(fragment, true);
        }

        else if(type.equals("LOCATION")) {
            Fragment fragment = new LocationMap();
            switchfragment(fragment, true);
        }

        else {
            background.setVisibility(View.VISIBLE);
        }
    }

    private void switchfragment (Fragment fragment, boolean inner) {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        if(inner) {
            ft.replace(R.id.fragmentView, fragment, "TAG");
        }
        else {
            ft.replace(R.id.fragmentContainerView, fragment, "TAG");
        }
        ft.commit();
    }

    public void finishChallenge(boolean canceled, long reward) {
        DocumentReference userDoc = firestore.collection("users").document(username);
        DocumentReference challengeDoc = firestore.collection("challenges").document(title);

        if(!canceled) {
            userDoc.update("points", FieldValue.increment(reward));
        }
        userDoc.update("challenges", FieldValue.arrayRemove(title));
        challengeDoc.update("users", FieldValue.arrayRemove(username));

        switchfragment(new ChallengeOverview(), false);

    }

    public void hideImage() {
        cancel.setVisibility(View.INVISIBLE);
    }

}
