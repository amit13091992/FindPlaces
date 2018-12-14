package android.practices.findplaces.Activity.Hospitals;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.practices.findplaces.Network.GPSTracker;
import android.practices.findplaces.Network.HttpConnection;
import android.practices.findplaces.Network.PathJSONParser;
import android.practices.findplaces.R;
import android.practices.findplaces.receivers.ConnectivityReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Amit on 09-Dec-18.
 */
public class LocationDetailsViewActivity extends FragmentActivity implements OnMapReadyCallback, ConnectivityReceiver.ConnectivityReceiverListener {
    private static final String TAG = LocationDetailsViewActivity.class.getSimpleName();
    private static double LATITUDE = 0.00;
    private static double LONGITUDE = 0.00;
    private static String PLACE_NAME;
    private static String PLACE_ADDRESS;

    private GoogleMap googleMap;
    private ConnectivityReceiver connectivityReceiver;
    private LinearLayout lblNetworkError;
    private SupportMapFragment supportMapFragment;
    private Button btnRetry;
    private FrameLayout mapLayout;
    private String sCurrentLocation;
    /*    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener =
                new GoogleMap.OnMyLocationClickListener() {
                    @Override
                    public void onMyLocationClick(@NonNull Location location) {
                        googleMap.setMinZoomPreference(15);
                        CircleOptions circleOptions = new CircleOptions();
                        circleOptions.center(new LatLng(location.getLatitude(),
                                location.getLongitude()));
                        circleOptions.radius(200);
                        circleOptions.fillColor(Color.RED);
                        circleOptions.strokeWidth(6);
                        googleMap.addCircle(circleOptions);
                    }
                };*/
    private LatLng currentLocation;
    private LatLng placeLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_hospitals_on_maps);
        lblNetworkError = findViewById(R.id.idErrorLayout);
        btnRetry = findViewById(R.id.idBtnRetry);
        mapLayout = findViewById(R.id.mapLayout);

        /**
         * Check device is connected to Internet OR not.
         */
        connectivityReceiver = new ConnectivityReceiver(getApplicationContext());
        //check if internet available or not
        if (!connectivityReceiver.isConnected()) {
            lblNetworkError.setVisibility(View.VISIBLE);
            mapLayout.setVisibility(View.GONE);
            Toast.makeText(LocationDetailsViewActivity.this, getString(R.string.msg_turnon_internet), Toast.LENGTH_SHORT).show();
        } else {
            lblNetworkError.setVisibility(View.GONE);
            mapLayout.setVisibility(View.VISIBLE);
            loadMapIntoFragment();
        }

        /**
         * Getting values from activity intent.
         */
        LATITUDE = getIntent().getDoubleExtra("latitude", 0.00);
        LONGITUDE = getIntent().getDoubleExtra("longitude", 0.00);
        PLACE_NAME = getIntent().getStringExtra("name");
        PLACE_ADDRESS = getIntent().getStringExtra("address");

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectivityReceiver.isConnected()) {
                    mapLayout.setVisibility(View.VISIBLE);
                    lblNetworkError.setVisibility(View.GONE);
                    loadMapIntoFragment();
                } else {
                    lblNetworkError.setVisibility(View.VISIBLE);
                    mapLayout.setVisibility(View.GONE);
                    Toast.makeText(LocationDetailsViewActivity.this, getString(R.string.msg_turnon_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMapIntoFragment() {
        supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment));
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(this);
    }

    private void fetchCurrentLocation() {
        GPSTracker gpsTracker = new GPSTracker(this);
        if (gpsTracker.getIsGPSTrackingEnabled()) {

            double currentLat = gpsTracker.getLatitude();
            double currentLong = gpsTracker.getLongitude();
            sCurrentLocation = currentLat + "," + currentLong;
            currentLocation = new LatLng(currentLat, currentLong);
            try {
                URLEncoder.encode(sCurrentLocation, "utf-8");
                Toast.makeText(LocationDetailsViewActivity.this, sCurrentLocation, Toast.LENGTH_SHORT).show();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(LocationDetailsViewActivity.this, getString(R.string.error_fetch_location), Toast.LENGTH_SHORT).show();
        }
    }

    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        double mLat = LATITUDE;
        double mLon = LONGITUDE;
        placeLocation = new LatLng(mLat, mLon);
        this.googleMap.addMarker(new MarkerOptions().position(placeLocation).
                snippet(PLACE_ADDRESS).title(PLACE_NAME)).showInfoWindow();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        this.googleMap.setMyLocationEnabled(true);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLocation, 15));
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                googleMap.setMinZoomPreference(10);
                googleMap.getMaxZoomLevel();
                fetchCurrentLocation();
                /*googleMap.addPolyline(new PolylineOptions()
                        .add(placeLocation, currentLocation)
                        .width(10)
                        .color(R.color.colorAccent));*/
                String url = getMapsApiDirectionsUrl();
                ReadTask downloadTask = new ReadTask();
                downloadTask.execute(url);

                return false;
            }
        });
    }


    private String getMapsApiDirectionsUrl() {
        String str_origin = "origin=" + currentLocation.latitude + "," + currentLocation.longitude;
        // Destination of route
        String str_dest = "destination=" + placeLocation.latitude + "," + placeLocation.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
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
                    polyLineOptions.color(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                }
                if (googleMap != null&&polyLineOptions!=null)
                    googleMap.addPolyline(polyLineOptions);
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (connectivityReceiver.isConnected()) {
            mapLayout.setVisibility(View.VISIBLE);
            lblNetworkError.setVisibility(View.GONE);
            loadMapIntoFragment();
        } else {
            lblNetworkError.setVisibility(View.VISIBLE);
            mapLayout.setVisibility(View.GONE);
            Toast.makeText(LocationDetailsViewActivity.this, getString(R.string.msg_turnon_internet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            lblNetworkError.setVisibility(View.GONE);
        } else {
            lblNetworkError.setVisibility(View.VISIBLE);
        }
    }
}