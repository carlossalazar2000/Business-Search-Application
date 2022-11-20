package com.example.finalproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment {

    private OnMapReadyCallback callback = new OnMapReadyCallback() { //When map is called
        @Override
        public void onMapReady(GoogleMap googleMap) {
            //Global variable from SearchActivity that checks if user made a search before viewing map
            //Only if user makes a search or views favorites can they see the map
            if (SearchActivity.searchConducted == 1 || SearchActivity.searchConducted == 2) {
                YelpData data = SearchActivity.getDataInstance (); //Get Yelp data instance
                //Get lat and long data from business and place into variables
                double latitude = Double.parseDouble(data.getLat());
                double longitude = Double.parseDouble(data.getLong());
                //Make a new LatLnhg location based on business coords
                LatLng location = new LatLng(latitude, longitude);

                //Add a marker and place camera at business location on map
                googleMap.addMarker(new MarkerOptions().position(location).title(data.getName()));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(12));
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}