package com.example.myeverest;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ChallengeOverview extends Fragment {
// WICHTIG: Username variabel lassen damit wechsel möglich sind!
    ListView challengeList;
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

        challengeList = v.findViewById(R.id.challenge_List);

        ArrayList<Object> arrayList = new ArrayList<>();

        arrayList.add("Test");
        arrayList.add("Test2");

        challengeList.setAdapter(new ArrayAdapter(v.getContext(), android.R.layout.simple_list_item_1, arrayList));
    }
}