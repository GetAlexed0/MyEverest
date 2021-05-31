package com.example.myeverest;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.example.myeverest.challenges.Maps;
import com.example.myeverest.challenges.ChallengeCreator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ChallengeOverview extends Fragment {
// WICHTIG: Username variabel lassen damit wechsel möglich sind! Am besten per Parameter mitgeben
    FloatingActionButton createButton;

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

        createButton = v.findViewById(R.id.floatingActionButton);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v);
            }
        });
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
}