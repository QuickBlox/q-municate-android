package com.quickblox.q_municate.ui.activities.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.quickblox.q_municate.R;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap googleMap;
    private Location lastLocation;
    private Marker myMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        setUpMapIfNeeded(map);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");

        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
        this.googleMap.moveCamera(center);

        CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
        this.googleMap.animateCamera(zoom);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private void setUpMapIfNeeded(GoogleMap googleMapUpdated) {
        if (googleMap == null) {
            googleMap = googleMapUpdated;
            if (googleMap != null) {
                Log.d(TAG, "setup googleMap");
                googleMap.setMyLocationEnabled(true);
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(50, 36))
                        .title("Marker")
                        .draggable(true)
                        .snippet("Hello")
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {


                        return false;
                    }
                });
                googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {
                        LatLng position = marker.getPosition();
                        Log.d(TAG,  "onMarkerDragStart Lat " + position.latitude + ", Long " + position.longitude);

                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {
                        Log.d(TAG, "Dragging");
                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        LatLng position = marker.getPosition();
                        Log.d(TAG,  "onMarkerDragEnd Lat " + position.latitude + ", Long " + position.longitude);
                    }
                });

            }
        }
    }
}