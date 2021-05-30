package com.example.myeverest.challenges;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myeverest.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment {
    static MarkerOptions options;
    static Location location;

    public static MarkerOptions getOptions() {
        return options;
    }

    public static void setLocation(Location location2) {
        location = location2;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //View initialisieren
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        //Mapfragment initialisieren
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.gmapfragment);

        //Async map
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                //Wenn map geladen wurde
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        //wenn map ausgewählt wird
                        //initialisiere MarkerOptions
                        MarkerOptions markerOptions = new MarkerOptions();

/*
                        LatLng home = new LatLng(49.48143872888664, 8.474383085521366);
*/


                        markerOptions.position(latLng);
                        markerOptions.title(latLng.latitude + " : " + latLng.longitude);

/*
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(home).zoom(10).build();
*/

                        //marker entfernen
                        googleMap.clear();
                        //Zum zoomen markieren
                        //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        googleMap.addMarker(markerOptions);

                        Location location = new Location(LocationManager.GPS_PROVIDER);
                        location.setLatitude(latLng.latitude);
                        location.setLongitude(latLng.longitude);

                        options = markerOptions;

                    }
                });
                LatLng home = new LatLng(Maps.getLastknown().getLatitude(), Maps.getLastknown().getLongitude());
                googleMap.addMarker(new MarkerOptions()
                        .position(home)
                        .title("Mein Zuhause"));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(home).zoom(10).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        });

        return view;
    }
}