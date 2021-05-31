package com.example.myeverest.challenges;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.myeverest.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class Maps extends Fragment implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    Button setButton, showButton;
    TextInputEditText coordLong;
    GoogleMap gMap;
    LatLng position;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private static Location lastknown;

    public Maps() {
        // Required empty public constructor
    }

    public static Location getLastknown() {
        return lastknown;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();

        coordLong = v.findViewById(R.id.inputCoordinates);
        getLastKnownLocation();
        MapsFragment.setLocation(getLastKnownLocation());

        if(lastknown == null) {
            lastknown = new Location("");
            lastknown.setLatitude(49.47438163723466);
            lastknown.setLongitude(8.534547877930759);
        }

        coordLong.setText(String.valueOf(lastknown.getLatitude() + " : " + lastknown.getLongitude()));
        //Mapfragment initialisieren
        Fragment fragment = new MapsFragment();
        setButton = v.findViewById(R.id.setLocation_btn);
        showButton = v.findViewById(R.id.showLocation_btn);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MarkerOptions marker = MapsFragment.getOptions();
                if(marker != null) {
                    lastknown.setLongitude(marker.getPosition().longitude);
                    lastknown.setLatitude(marker.getPosition().latitude);
                }
                coordLong.setText(String.valueOf(lastknown.getLatitude()) + " : " + String.valueOf(lastknown.getLongitude()));
            }
        });

        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //Fragment öffnen
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment).commit();




    }

    private Location getLastKnownLocation() {
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                return getLastknown();
            }
        }

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Permissions fehlen");

            return null;
        }
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location:
                bestLocation = l;
            }
        }
        lastknown = bestLocation;
        return bestLocation;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        position = new LatLng(lastknown.getLatitude(), lastknown.getLongitude());
        gMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Aktueller Standort"));
        gMap.moveCamera(CameraUpdateFactory.newLatLng(position));
    }
}