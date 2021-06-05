package com.example.myeverest;

import android.content.res.ObbInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.R.layout;
import android.widget.Toast;

import com.example.myeverest.Helpers.CallBack;
import com.example.myeverest.Helpers.CustomAdapter;
import com.example.myeverest.challenges.Maps;
import com.example.myeverest.challenges.ChallengeCreator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChallengeOverview extends Fragment {
// WICHTIG: Username variabel lassen damit wechsel möglich sind! Am besten per Parameter mitgeben
    FloatingActionButton createButton;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    ListView listView;
    DocumentSnapshot user;

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
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v);
            }
        });

        checkAnswerSubmission(new CallBack<List<DocumentSnapshot>>() {
            @Override
            public void callback(List<DocumentSnapshot> data) {
                user = data.get(0);
                Log.d("Fehler", user.get("username").toString());
            }
        },"leni@live.de");
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

    /*public void createList() {
        checkAnswerSubmission(new CallBack<List<DocumentSnapshot>>() {
            @Override
            public void callback(List<DocumentSnapshot> data) {
                DocumentSnapshot doc = data.get(0);
                Map<String, Object> map = doc.getData().get("challenges");

               for(Map.Entry<String, Object> entry : map.entrySet()) {
                   if(entry.getKey().equals("challenges")) {

                   }
               }
                ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(getContext(), layout.simple_list_item_1, list);
                listView.setAdapter(adapter);
            }
        });
    }*/


    private void setUser(String username) {
        checkAnswerSubmission(new CallBack<List<DocumentSnapshot>>() {
            @Override
            public void callback(List<DocumentSnapshot> data) {
                user = data.get(0);
            }
        }, username);
    }

    private void checkAnswerSubmission(@NonNull CallBack<List<DocumentSnapshot>> finishedCallback, String getting) {

        List<DocumentSnapshot> list = new ArrayList<>();
        DocumentReference answerDatabase = firestore.collection("users").document(getting);
        answerDatabase.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        list.add(document);
                        finishedCallback.callback(list);
                        Log.d("Testcallback", "In der if");
                    } else {
                        Log.d("Testcallback", "In der else");
                    }
                }
                else {
                    Log.d("Testcallback", "Penis");
                }
            }
        });
    }
}