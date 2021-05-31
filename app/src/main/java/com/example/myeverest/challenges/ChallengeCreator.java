package com.example.myeverest.challenges;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myeverest.R;
import com.google.android.material.slider.Slider;

import org.jetbrains.annotations.NotNull;

public class ChallengeCreator extends Fragment {

    TextView text;
    Button button;
    Slider seekBar;
    Bundle arguments;

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

        text = v.findViewById(R.id.stepcounter_textview);
        button = v.findViewById(R.id.create_walking_btn);
        seekBar = v.findViewById(R.id.steps_seekBar);

        text.setText(String.valueOf(seekBar.getValue()));

        arguments = getArguments();
        String test = arguments.getString("type");
        text.setText(test);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), text.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        /*seekBar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull @NotNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull @NotNull Slider slider) {
                text.setText(String.valueOf(seekBar.getValue()));
            }

            @Override
            public void on
        });*/

        seekBar.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull @NotNull Slider slider, float value, boolean fromUser) {
                text.setText(String.valueOf(seekBar.getValue()));
            }
        });
    }
}