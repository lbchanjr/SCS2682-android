package com.ut.cinemafinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.ut.cinemafinder.Constants.ERROR_DIALOG_REQUEST;
import static com.ut.cinemafinder.Constants.MAP_REGION_SIZE;
import static com.ut.cinemafinder.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.ut.cinemafinder.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MainActivity";
    private boolean mLocationPermissionGranted = false;

    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private FusedLocationProviderClient mFusedLocationClient;

    private GoogleMap mGoogleMap = null;
    private LatLngBounds mMapBoundary;
    private Location currentLocation;

    private Handler httpGetHandler;

    private ArrayList<Theater> cinemaArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize http asynch handler.
        httpGetHandler = new Handler();

        initMap(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        }
        else {
            getLocationPermission();
        }
    }

    private void setCameraView() {
        // Set parameters for map view window
        double bottom = currentLocation.getLatitude() - MAP_REGION_SIZE;
        double left = currentLocation.getLongitude() - MAP_REGION_SIZE;
        double top = currentLocation.getLatitude() + MAP_REGION_SIZE;
        double right = currentLocation.getLongitude() + MAP_REGION_SIZE;

        mMapBoundary = new LatLngBounds(new LatLng(bottom, left), new LatLng(top, right));

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }

    public void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");


        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Make sure that global location and map services are also enabled.
        if(checkMapServices()) {

            mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        currentLocation = task.getResult();

                        // Do whatever needs to be done upon determining last known location...
                        setCameraView();
                        Log.d(TAG, "onComplete: latitude: " + currentLocation.getLatitude());
                        Log.d(TAG, "onComplete: longitude: " + currentLocation.getLongitude());

                        // Update API call.
                        getReadCineplexTheaterData();
                    }

                }
            });
        }

    }

    private void initMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = findViewById(R.id.map);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }

    public void showQuickInstructions() {

        Toast.makeText(this, "Click 'FIND THEATERS' button to start.", Toast.LENGTH_SHORT).show();

        //getLastKnownLocation();
    }


    private boolean checkMapServices() {
        if (isServicesOK()) {
            return isMapsEnabled();
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static Boolean isLocationEnabled(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
    }

    public boolean isMapsEnabled() {
        //final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        if(!isLocationEnabled(this)) {
            buildAlertMessageNoGps();
            return false;
        }

        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            showQuickInstructions();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        if(requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;

                // Initialize map and the fused location provided.
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                // Ensure that savedInstanceState is set to null.
                initMap(null);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        if(requestCode == PERMISSIONS_REQUEST_ENABLE_GPS) {
            if (mLocationPermissionGranted) {
                showQuickInstructions();
            } else {
                getLocationPermission();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                showQuickInstructions();
            } else {
                getLocationPermission();
            }
        }

        mMapView.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap = map;
        getLastKnownLocation();

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void findButtonClicked(View v) {
        if(mGoogleMap != null) {
            if (checkMapServices()) {
                if (mLocationPermissionGranted) {
                    getLastKnownLocation();
                }
                else {
                    Toast.makeText(this, "Check your app or phone's location services setting and try again.", Toast.LENGTH_LONG).show();
                    getLocationPermission();
                }
            }
        }
        else {
            Toast.makeText(this, "Check your app or phone's location services setting and try again.", Toast.LENGTH_LONG).show();
            getLocationPermission();
        }
    }

    public void getReadCineplexTheaterData() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url("https://www.cineplex.com/api/v1/theatres?language=en-us").build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "URL data available!");

                String urlData = response.body().string();
                Log.d(TAG, urlData);

                try {
                    parseTheaterJSON(urlData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Place UI-related updates here.
                httpGetHandler.post( new Runnable() {
                    public void run() {
                        Log.d(TAG, "Adding movie theater markers to map.");
                        for (Theater t : cinemaArray) {
                            if(mGoogleMap != null) {
                                // Place UI-related updates here.
                                mGoogleMap.addMarker(new MarkerOptions().position(t.coords).title(t.name));
                            }
                        }

                        if(!cinemaArray.isEmpty() && mGoogleMap != null) {
                            Log.d(TAG, "Enabling 'SHOW DETAIL' button.");
                            Button detailBtn = findViewById(R.id.detailBtn);
                            detailBtn.setEnabled(true);

                            Toast.makeText(MainActivity.this, "Tap marker(s) or 'SHOW DETAILS' button for more info.", Toast.LENGTH_SHORT).show();

                        }
                    }
                } );
            }

            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "http GET failure");
                e.printStackTrace();
            }
        });
    }

    private void parseTheaterJSON(String strJSON) throws JSONException {
        JSONObject dataJSON = new JSONObject(strJSON);
        Log.d(TAG, "Parsing JSON data from remote site.");
        cinemaArray.clear();
        JSONArray arrJSON = dataJSON.getJSONArray("data");
        for (int i = 0; i < arrJSON.length(); i++) {
            String name = arrJSON.getJSONObject(i).getString("name");
            String address = arrJSON.getJSONObject(i).getString("address1") + ", " +
                    arrJSON.getJSONObject(i).getString("city") + ", " +
                    arrJSON.getJSONObject(i).getString("provinceCode") + ", " +
                    arrJSON.getJSONObject(i).getString("postalCode");
            String url = "https://www.cineplex.com/Theatre/" + arrJSON.getJSONObject(i).getString("urlSlug");
            LatLng coords = new LatLng(arrJSON.getJSONObject(i).getDouble("latitude"),
                    arrJSON.getJSONObject(i).getDouble("longitude"));

            cinemaArray.add(new Theater(name, address, url, coords));
            // Exit loop if theater distance is more than 25km.
            if(arrJSON.getJSONObject(i).getDouble("distance") > 25000) {
                break;
            }
        }
        Log.d(TAG, "Parsing JSON data complete.");
    }

    public void showDetails(View view) {
        Intent intent = new Intent(this, TheaterActivity.class);

        // Check if there's a need to pass extra data.
        if(!(cinemaArray.isEmpty())) {
            ArrayList<String> extras = new ArrayList<>();
            for (Theater t : cinemaArray) {
                extras.add(t.name);
                extras.add(t.address);
                extras.add(t.url);
                extras.add(t.coords.toString());
            }
            intent.putStringArrayListExtra("theaters_in_string", extras);
        }

        startActivity(intent);
        Log.d(TAG, "Started intent.");
    }

//    private void restartApp() {
//        Intent mStartActivity = new Intent(MainActivity.this, MainActivity.class);
//        int mPendingIntentId = 123456;
//        PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
//        AlarmManager mgr = (AlarmManager)MainActivity.this.getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
//        System.exit(0);
//    }
}
