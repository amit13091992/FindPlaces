package android.practices.findplaces.activity.Places;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.practices.findplaces.app.ApiClient;
import android.practices.findplaces.app.ApiInterface;
import android.practices.findplaces.app.AppController;
import android.practices.findplaces.constants.AppConstants;
import android.practices.findplaces.model.PlaceDetailsModel;
import android.practices.findplaces.network.HttpConnection;
import android.practices.findplaces.network.PathJSONParser;
import android.practices.findplaces.R;
import android.practices.findplaces.receivers.ConnectivityReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Amit on 09-Dec-18.
 */
public class LocationMapViewActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, ConnectivityReceiver.ConnectivityReceiverListener {
    private static final String TAG = LocationMapViewActivity.class.getSimpleName();
    private static double LATITUDE = 0.00;
    private static double LONGITUDE = 0.00;
    private static String PLACE_NAME;
    private static String PLACE_ADDRESS;
    private static String PLACE_ID;
    double currentLatitude;
    double currentLongitude;

    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private MapView mMapView;
    private ConnectivityReceiver connectivityReceiver;
    private LinearLayout lblNetworkError;
    private SupportMapFragment supportMapFragment;
    private Button btnRetry;
    //private FrameLayout mapLayout;
    private String sCurrentLocation;
    private LatLng currentLocation;
    private LatLng CURRENT_LATLNG;
    private LatLng DEST_LATLNG;
    private ProgressBar progressBar;
    private TextView placename, placeAddress;
    private LinearLayout layoutBottomSheet;
    private BottomSheetBehavior sheetBehavior;
    private ImageView imgDirections, imgPlaceDetails;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_details);
        lblNetworkError = findViewById(R.id.idErrorLayout);
        btnRetry = findViewById(R.id.idBtnRetry);
        mMapView = (MapView) findViewById(R.id.mapView);
        //mapLayout = findViewById(R.id.mapLayout);
        progressBar = findViewById(R.id.progressBar);
        placename = findViewById(R.id.idPlaceValue);
        placeAddress = findViewById(R.id.idAddressValue);
        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        imgDirections = findViewById(R.id.idImgDirection);
        imgPlaceDetails = findViewById(R.id.idImgPlaceDetails);

        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        //btnBottomSheet.setText("Close Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        //btnBottomSheet.setText("Expand Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        /**
         * Check device is connected to Internet OR not.
         */
        connectivityReceiver = new ConnectivityReceiver(AppController.getInstance().getApplicationContext());
        //check if internet available or not
        if (!connectivityReceiver.isConnected()) {
            lblNetworkError.setVisibility(View.VISIBLE);
            //mapLayout.setVisibility(View.GONE);
            Toast.makeText(LocationMapViewActivity.this, getString(R.string.msg_turnon_internet), Toast.LENGTH_SHORT).show();
        } else {
            lblNetworkError.setVisibility(View.GONE);
            //mapLayout.setVisibility(View.VISIBLE);
            //loadMapIntoFragment();
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();
            mMapView.getMapAsync(this);
            showMap();


            /**
             * Getting values from activity intent.
             */
            LATITUDE = getIntent().getDoubleExtra("latitude", 0.00);
            LONGITUDE = getIntent().getDoubleExtra("longitude", 0.00);
            PLACE_NAME = getIntent().getStringExtra("name");
            PLACE_ADDRESS = getIntent().getStringExtra("address");
            PLACE_ID = getIntent().getStringExtra("placeId");
            currentLatitude = getIntent().getDoubleExtra("curLat", 0.0);
            currentLongitude = getIntent().getDoubleExtra("curLong", 0.0);

            placename.setText(PLACE_NAME);
            placeAddress.setText(PLACE_ADDRESS);

            imgDirections.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e(TAG, " current location onclick " + currentLatitude + ", " + currentLongitude);
                    if (currentLatitude != 0.0 && currentLongitude != 0.0) {
                        CURRENT_LATLNG = new LatLng(currentLatitude, currentLongitude);
                        DEST_LATLNG = new LatLng(LATITUDE, LONGITUDE);
                        drawMarker(DEST_LATLNG, "dest");
                        drawMarker(CURRENT_LATLNG, "source");
                        String url = getMapsApiDirectionsUrl();
                        ReadTask downloadTask = new ReadTask();
                        downloadTask.execute(url);
                        Log.e(TAG, " function working.");
                        Log.e(TAG, "cur- " + CURRENT_LATLNG + " dest: " + DEST_LATLNG);
                    } else {
                        Toast.makeText(AppController.getInstance().getApplicationContext(), "Please set your addresses", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        imgPlaceDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LocationMapViewActivity.this, "Coming Soon.", Toast.LENGTH_SHORT).show();
            }
        });

//       /* //get the bundle
//        Bundle args = getIntent().getBundleExtra("BUNDLE");
//        //noinspection unchecked
//        placeResultArray = (ArrayList<PlacesResponseModel.ResultsResponse>) args.getSerializable("placeResultArray");
//        args.size();*/
//        //if (currentLatitude != 0.0 && destLatitude != 0.0) {
//
////        } else {
////            Toast.makeText(AppController.getInstance().getApplicationContext(), "Please set your addresses", Toast.LENGTH_SHORT).show();
////        }

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectivityReceiver.isConnected()) {
                    //mapLayout.setVisibility(View.VISIBLE);
                    lblNetworkError.setVisibility(View.GONE);
                    //loadMapIntoFragment();
                } else {
                    lblNetworkError.setVisibility(View.VISIBLE);
                    //mapLayout.setVisibility(View.GONE);
                    Toast.makeText(LocationMapViewActivity.this, getString(R.string.msg_turnon_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void showMap() {
        try {
            initializeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeMap() {
        MapsInitializer.initialize(AppController.getInstance().getApplicationContext());
        // Check if we were successful in obtaining the map.
        if (googleMap != null) {
            setUpMap();
        }
    }

    private void setUpMap() {
        googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }


//    private void loadMapIntoFragment() {
//        supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment));
//        assert supportMapFragment != null;
//        supportMapFragment.getMapAsync(this);
//
//        getPlaceDetailsFromServer();
//    }

    public void onMapReady(GoogleMap map) {
        Log.e(TAG, " OnMapready() called.");
        this.googleMap = map;
        double mLat = LATITUDE;
        double mLon = LONGITUDE;
        CURRENT_LATLNG = new LatLng(mLat, mLon);
        Log.e(TAG, " current location OnMapready " + mLat + ", " + mLat);
    }

    private void getPlaceDetailsFromServer() {
        progressBar.setVisibility(View.VISIBLE);

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<PlaceDetailsModel> call = apiService.getPlaceDetails(PLACE_ID, AppConstants.API_KEY);
        call.enqueue(new Callback<PlaceDetailsModel>() {
            @Override
            public void onResponse(@NonNull Call<PlaceDetailsModel> call, @NonNull Response<PlaceDetailsModel> response) {
                PlaceDetailsModel placeDetailsModel = response.body();
                Log.d(TAG, " request url: " + response.raw().request().url());
                Log.d(TAG, " request : " + call.request().body());

                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    assert placeDetailsModel != null;
                    if (placeDetailsModel.status.equals(AppConstants.OK)) {
                        Log.v(TAG, " response: " + response.body());
                    } else if (placeDetailsModel.status.equals(AppConstants.OVER_QUERY_LIMIT)) {
                        try {
                            Thread.sleep(5000);
                            PlaceDetailsModel placeDetailsModel1 = response.body();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (response.errorBody() != null) {
                        Log.v(TAG, " response error: " + response.errorBody());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlaceDetailsModel> call, @NonNull Throwable t) {
                t.printStackTrace();
                Log.v(TAG, " response error: " + call.request().body());
            }
        });
    }

    @Override
    public void onResume() {
        //  Log.d("Case", "onResume");
        super.onResume();
        mMapView.onResume();
        if (connectivityReceiver.isConnected()) {
            //mapLayout.setVisibility(View.VISIBLE);
            lblNetworkError.setVisibility(View.GONE);
            //loadMapIntoFragment();
        } else {
            lblNetworkError.setVisibility(View.VISIBLE);
            //mapLayout.setVisibility(View.GONE);
            Toast.makeText(LocationMapViewActivity.this, getString(R.string.msg_turnon_internet), Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    public void onPause() {
//        // Log.d("Case", "onPause");
//        super.onPause();
//        mMapView.onPause();
//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.stopAutoManage(this);
//            mGoogleApiClient.disconnect();
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        // Log.d("Case", "onDestroy");
//        super.onDestroy();
//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.stopAutoManage(this);
//            mGoogleApiClient.disconnect();
//        }
//        mMapView.onDestroy();
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        if (mGoogleApiClient != null)
//            mGoogleApiClient.connect();
//    }
//
//    @Override
//    public void onStop() {
//        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }
//        super.onStop();
//    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            lblNetworkError.setVisibility(View.GONE);
        } else {
            lblNetworkError.setVisibility(View.VISIBLE);
        }
    }


    //TODO implement map functionality
    private void drawMarker(LatLng latLng, String imageType) {
        Log.e(TAG, "Draw marker called. ");
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        if (imageType.equals("dest")) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin));
        } else {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin));
        }
        Marker marker = googleMap.addMarker(markerOptions);
        marker.showInfoWindow();
    }

    private String getMapsApiDirectionsUrl() {
        String str_origin = "origin=" + CURRENT_LATLNG.latitude + "," + CURRENT_LATLNG.longitude;
        // Destination of route
        String str_dest = "destination=" + DEST_LATLNG.latitude + "," + DEST_LATLNG.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (location == null) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                } else {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    Log.e(TAG, " current location onconnected " + currentLatitude + ", " + currentLongitude);
                    onLocationChanged(location);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            if (googleMap != null)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
        } else {
            try {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (mLastLocation != null) {
                    Log.e(TAG, " current location locationlistener " + currentLatitude + ", " + currentLongitude);
                    currentLatitude = mLastLocation.getLatitude();
                    currentLongitude = mLastLocation.getLongitude();
                    LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                    if (googleMap != null)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                //Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points;
            PolylineOptions polyLineOptions = null;
            if (routes != null) {
                // traversing through routes
                for (int i = 0; i < routes.size(); i++) {
                    points = new ArrayList<>();
                    polyLineOptions = new PolylineOptions();
                    List<HashMap<String, String>> path = routes.get(i);
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);
                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);
                        points.add(position);
                    }
                    polyLineOptions.addAll(points);
                    polyLineOptions.width(12);
                    polyLineOptions.color(ContextCompat.getColor(AppController.getInstance().getApplicationContext(), R.color.colorAccent));
                }
                if (googleMap != null)
                    googleMap.addPolyline(polyLineOptions);
            }
        }
    }


//    private void buildGoogleApiClient() {
//        mGoogleApiClient = new GoogleApiClient.Builder(AppController.getInstance().getApplicationContext())
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API).addApi(Places.GEO_DATA_API).enableAutoManage(this, 0 /* clientId */, this)
//                .addApi(Places.PLACE_DETECTION_API)
//                .addApi(AppIndex.API).build();
//        createLocationRequest();
//    }

//    private void createLocationRequest() {
//        // Create the LocationRequest object
//        int UPDATE_INTERVAL = 10000;
//        int FATEST_INTERVAL = 5000;
//        int DISPLACEMENT = 50;
//        mLocationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(UPDATE_INTERVAL)        // 10 seconds, in milliseconds
//                .setFastestInterval(FATEST_INTERVAL).setSmallestDisplacement(DISPLACEMENT); // 1 second, in milliseconds
//    }
//
//    private boolean checkPlayServices() {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getApplicationContext());
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (apiAvailability.isUserResolvableError(resultCode)) {
//                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
//                        .show();
//            } else {
//                // Log.i(TAG, "This device is not supported. Google Play Services not installed!");
//                Toast.makeText(AppController.getInstance().getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
//                finish();
//            }
//            return false;
//        }
//        return true;
//    }

}