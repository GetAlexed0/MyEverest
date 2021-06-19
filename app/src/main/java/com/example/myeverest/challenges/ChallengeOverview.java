package com.example.myeverest.challenges;

import android.content.SharedPreferences;
import android.content.res.ObbInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.R.layout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myeverest.Helpers.CallBack;
import com.example.myeverest.Helpers.CustomAdapter;
import com.example.myeverest.Helpers.DataHandler;
import com.example.myeverest.R;
import com.example.myeverest.challenges.Maps;
import com.example.myeverest.challenges.ChallengeCreator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ChallengeOverview extends Fragment {
// WICHTIG: Username variabel lassen damit wechsel möglich sind! Am besten per Parameter mitgeben
    FloatingActionButton createButton;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    ListView listView;
    DocumentReference userDoc;
    String username;
    TextView titleInput;
    Button joinButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_challenge_overview, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();

        listView = v.findViewById(R.id.listview);
        createButton = v.findViewById(R.id.floatingActionButton);
        joinButton = v.findViewById(R.id.joinChallengeButton);
        titleInput = v.findViewById(R.id.joinChallenge);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = sharedPreferences.getString("username", "failed");

        userDoc = firestore.collection("users").document(username);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v);
            }
        });

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!titleInput.getText().toString().isEmpty()) {
                    joinChallenge();
                }
                else {
                    titleInput.setError("Dieses Feld solltest du ausfüllen");
                }
            }
        });

        refreshChallenges();
    }

    private void joinChallenge() {
        String challengeTitle = titleInput.getText().toString();
        DocumentReference challengeDoc = firestore.collection("challenges").document(challengeTitle);
        challengeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if(snapshot.exists()) {
                        List<String> list = (List) snapshot.get("users");
                        if(list.contains(username)) {
                            titleInput.setError("Du bist bereits in dieser Challenge angemeldet");
                            return;
                        }
                        challengeDoc.update("users", FieldValue.arrayUnion(username));
                        userDoc.update("challenges", FieldValue.arrayUnion(challengeTitle));
                        refreshChallenges();
                        titleInput.setText("");
                    }

                    else {
                        titleInput.setError("Die angegebene Challenge existiert nicht, hast du dich vertippt? \nBeachte Groß-/Kleinschreibung!");
                    }
                }
            }
        });
    }

    public void refreshChallenges() {
        checkAnswerSubmission(new CallBack<List<DocumentSnapshot>>() {
            @Override
            public void callback(List<DocumentSnapshot> data) {
                String[] list = new String[data.size()];

                for(int i = 0; i < data.size(); i++) {
                    Log.d("Joar2", data.get(i).get("title").toString());
                    list[i] = data.get(i).get("title").toString();
                }
                ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(getContext(), layout.simple_list_item_1, list);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String type = data.get(position).get("type").toString();
                        if(!type.isEmpty()) {
                            Fragment challengePage = new ChallengePage();
                            Bundle arguments = new Bundle();
                            arguments.putString("type", type);
                            arguments.putString("title", data.get(position).get("title").toString());
                            challengePage.setArguments(arguments);
                            switchFragments(challengePage);
                            }
                    }
                });
            }
        }, username);
    }

    private void showMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.locationchallenge:
                        switchFragments(new Maps());
                        return true;
                    case R.id.stepchallenge:
                        Fragment stepFragment = new ChallengeCreator();
                        Bundle stepArguments = new Bundle();
                        stepArguments.putString("type", "WALK");
                        stepFragment.setArguments(stepArguments);
                        switchFragments(stepFragment);
                        return true;
                    case R.id.individualchallenge:
                        Fragment individualFragment = new ChallengeCreator();
                        Bundle individualArguments = new Bundle();
                        individualArguments.putString("type", "PERSONAL");
                        individualFragment.setArguments(individualArguments);
                        switchFragments(individualFragment);
                        return true;



                    default:
                        return false;
                }
            }
        });
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.show();
    }

    private void switchFragments(Fragment fragment) {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainerView, fragment, "TAG");
        ft.commit();
    }


    private void checkAnswerSubmission(@NonNull CallBack<List<DocumentSnapshot>> finishedCallback, String getting) {

        List<DocumentSnapshot> list = new ArrayList<>();
        CollectionReference answerDatabase = firestore.collection("challenges");
        answerDatabase.whereArrayContains("users", getting).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        list.add(document);
                    }
                    try {
                        finishedCallback.callback(list);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}