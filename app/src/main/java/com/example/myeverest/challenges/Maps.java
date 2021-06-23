package com.example.myeverest.challenges;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.preference.PreferenceManager;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Maps extends Fragment implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    TextView titleInput, pointInput, descriptionInput;
    Button setButton, showButton;
    static TextInputEditText coordLong;
    GoogleMap gMap;
    LatLng position;
    String username;
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = sharedPreferences.getString("username", "failed");
        coordLong = v.findViewById(R.id.inputCoordinates);
        titleInput = v.findViewById(R.id.title_input);
        pointInput = v.findViewById(R.id.points_input);
        getLastKnownLocation();
        MapsFragment.setLocation(getLastKnownLocation());

        if(lastknown == null) {
            lastknown = new Location("");
            lastknown.setLatitude(49.4743816);
            lastknown.setLongitude(8.5345478);
        }

        coordLong.setText(String.valueOf(lastknown.getLatitude() + " : " + lastknown.getLongitude()));
        //Mapfragment initialisieren
        Fragment fragment = new MapsFragment();
        setButton = v.findViewById(R.id.create_location_btn);
        showButton = v.findViewById(R.id.showLocation_btn);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!titleInput.getText().toString().isEmpty()) {
                    if (!pointInput.getText().toString().isEmpty()) {
                        MarkerOptions marker = MapsFragment.getOptions();
                        if (marker != null) {
                            lastknown.setLongitude(marker.getPosition().longitude);
                            lastknown.setLatitude(marker.getPosition().latitude);
                            createChallenge(lastknown);
                        } else {
                            createChallenge(lastknown);
                        }
                    }
                    else {
                        pointInput.setError("Willst du keine Punkte?");
                    }
                }
                else {
                    titleInput.setError("Du musst schon einen Titel eingeben ;)");
                }
            }
        });

        //Fragment öffnen
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment).commit();




    }

    public static void updateCoordinateText() {
        MarkerOptions marker = MapsFragment.getOptions();
        if(marker != null) {
            lastknown.setLongitude(marker.getPosition().longitude);
            lastknown.setLatitude(marker.getPosition().latitude);
            coordLong.setText(String.valueOf(lastknown.getLatitude() + " : " + lastknown.getLongitude()));
        }
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

    public static double round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    public void createChallenge(Location loc) {
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
                        int points = Integer.parseInt(pointInput.getText().toString().trim());
                        Map<String, Object> challengeMap = new HashMap<>();
                        challengeMap.put("title", challengetitle);
                        challengeMap.put("description", "");
                        challengeMap.put("points", points);
                        challengeMap.put("creator", username);
                        challengeMap.put("challengetype", "LOCATION");
                        challengeMap.put("users", Arrays.asList(username));
                        challengeMap.put("type", "LOCATION");
                        GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                        challengeMap.put("position", point);

                        challenge.set(challengeMap);

                        DocumentReference createdBy = firestore.collection("users").document(username);
                        createdBy.update("challenges", FieldValue.arrayUnion(challengetitle));

                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.fragmentContainerView, new ChallengeOverview(), "TAG");
                        ft.commit();

                    }
                }
            }
        });
    }

}