package com.quickblox.q_municate.ui.activities.location;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.utils.helpers.SystemPermissionHelper;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.ui.kit.chatmessage.adapter.utils.LocationUtils;

import butterknife.Bind;
import butterknife.OnClick;

public class MapsActivity extends BaseLoggableActivity
        implements
        OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String EXTRA_LOCATION_DATA = "location_data";

    private boolean permissionDenied = false;

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;

    private LatLng latLng;
    private Marker currLocationMarker;
    private boolean isLocationServiceOn = true;
    private String location;

    public static void startMapForResult(Context context, String location) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(EXTRA_LOCATION_DATA, location);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Bind(R.id.map_textview)
    TextView sendTextView;

    @Bind(R.id.map_framelayout)
    FrameLayout sendLocationPanel;

    @OnClick(R.id.map_framelayout)
    void sendLocationButtonClicked() {
        sendLocation(latLng);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        title = getString(R.string.location_title);

        initView();
        setUpActionBarWithUpButton();
        mapFragment.getMapAsync(this);
    }

    private void initView() {
        if (getIntent().getExtras() != null) {
            location = getIntent().getExtras().getString(EXTRA_LOCATION_DATA);
            sendLocationPanel.setVisibility(View.GONE);
            setLocationButtonPosition(true);
        } else {
            setLocationButtonPosition(false);
        }
    }

    private void setLocationButtonPosition(boolean toBottom) {
        View locationButton = ((View) findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        if (toBottom) {
            params.setMargins(0, 0, 30, 30);
        } else {
            params.setMargins(0, 0, 180, 180);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    private LatLng getReceivedLocation() {
        if (location == null) {
            return null;
        }
        Log.d(TAG, "getReceivedLocation");
        Pair<Double, Double> latLngPair = LocationUtils.getLatLngFromJson(location);

        return new LatLng(latLngPair.first, latLngPair.second);
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            SystemPermissionHelper.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            return true;
        }
        return false;
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (checkPermission()) {
            return;
        }
        if (googleMap != null) {
            // Access to the location has been granted to the app.
            googleMap.setMyLocationEnabled(true);
            buildGoogleApiClient();

            googleApiClient.connect();
        }
    }

    private void cameraUpdate(double latitude, double longitude) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17);
        googleMap.animateCamera(cameraUpdate);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Log.d(TAG, "MyLocation button clicked");
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (SystemPermissionHelper.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        Toast.makeText(this, R.string.permission_required_toast, Toast.LENGTH_SHORT).show();
        finish();
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "MyLocation onConnected");
        if (checkPermission()) {
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (mLastLocation != null) {

            if (location == null) {
                latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            } else {
                isLocationServiceOn = false;
                latLng = getReceivedLocation();
            }

            currLocationMarker = googleMap.addMarker(buildMarkerOptions(latLng));
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    isLocationServiceOn = false;
                    LatLng position = marker.getPosition();
                    Log.d(TAG, "onMarkerDragStart Lat " + position.latitude + ", Long " + position.longitude);
                    sendTextView.setText(R.string.send_selected_location);
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                    Log.d(TAG, "Dragging");
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    latLng = marker.getPosition();
                    Log.d(TAG, "onMarkerDragEnd Lat " + latLng.latitude + ", Long " + latLng.longitude);
                    cameraUpdate(latLng.latitude, latLng.longitude);
                }
            });
        }
        cameraUpdate(latLng.latitude, latLng.longitude);
        buildRequestLocation();
    }

    private void buildRequestLocation() {
        if (checkPermission()) {
            return;
        }
        LocationRequest locationRequest = new LocationRequest();
//        in seconds
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //locationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "MyLocation onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "MyLocation onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!isLocationServiceOn) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            return;
        }
        //place marker at current position
        if (currLocationMarker != null) {
            currLocationMarker.remove();
        }
        latLng = new LatLng(location.getLatitude(), location.getLongitude());

        currLocationMarker = googleMap.addMarker(buildMarkerOptions(latLng));

        Log.d(TAG, "onLocationChanged");

        cameraUpdate(latLng.latitude, latLng.longitude);

    }

    private MarkerOptions buildMarkerOptions(LatLng latLng) {
        return new MarkerOptions()
                .position(latLng)
                .title("Current Position")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
    }

    private void sendLocation(LatLng position) {
        Bundle extras = new Bundle();
        extras.putDouble(ConstsCore.EXTRA_LOCATION_LATITUDE, position.latitude);
        extras.putDouble(ConstsCore.EXTRA_LOCATION_LONGITUDE, position.longitude);

        Intent result = new Intent();
        result.putExtras(extras);
        setResult(RESULT_OK, result);
        finish();
    }
}