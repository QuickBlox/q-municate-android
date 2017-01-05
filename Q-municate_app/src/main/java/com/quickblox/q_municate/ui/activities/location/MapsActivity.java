package com.quickblox.q_municate.ui.activities.location;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

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
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;

import java.util.ArrayList;

public class MapsActivity extends BaseLoggableActivity implements LocationListener, OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();
    public static final String EXTRA_LOCATION = "qb_locations";

    private GoogleMap googleMap;
    private LatLng lastPosition = new LatLng(50, 36);
    private Marker myMarker;

    public static void startForResult(Activity activity, int code) {
        Intent intent = new Intent(activity, MapsActivity.class);
        activity.startActivityForResult(intent, code);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    private void sendLocation(LatLng position) {
        ArrayList<Double> location = new ArrayList<>(2);
        location.add(position.latitude);
        location.add(position.longitude);
        Intent result = new Intent();
        result.putExtra(EXTRA_LOCATION, location);
        setResult(RESULT_OK, result);
    }


    private void setUpMapIfNeeded(GoogleMap googleMapUpdated) {
        if (googleMap == null) {
            googleMap = googleMapUpdated;
            if (googleMap != null) {
                Log.d(TAG, "setup googleMap");
                googleMap.setMyLocationEnabled(true);
                googleMap.addMarker(new MarkerOptions()
                        .position(lastPosition)
                        .title("Marker")
                        .draggable(true)
                        .snippet("Hello")
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        Log.d(TAG,  "onMarkerClick" );
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
                        lastPosition = position;
                        Log.d(TAG,  "onMarkerDragEnd Lat " + position.latitude + ", Long " + position.longitude);
                    }
                });

            }
        }
    }
    public void onClickSendLocation(View view) {
        switch (view.getId()) {
            case R.id.check_in_button:
                sendLocation(lastPosition);
                Log.d(TAG,  "onClickSendLocation Lat " + lastPosition.latitude + ", Long " + lastPosition.longitude);
                finish();
                break;
        }
    }
}