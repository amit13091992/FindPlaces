package android.practices.findplaces.activity.Places;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.practices.findplaces.R;
import android.practices.findplaces.adapter.LocationListAdapter;
import android.practices.findplaces.app.ApiClient;
import android.practices.findplaces.app.ApiInterface;
import android.practices.findplaces.app.AppController;
import android.practices.findplaces.constants.AppConstants;
import android.practices.findplaces.model.PlacesResponseModel;
import android.practices.findplaces.network.GPSTracker;
import android.practices.findplaces.receivers.ConnectivityReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("deprecation")
public class LocationListActivity extends AppCompatActivity /*implements EasyPermissions.PermissionCallbacks , ConnectivityReceiver.ConnectivityReceiverListener, GPSConnectionReceiver.GPSConnectivityReceiverListener */ {

    private static final String TAG = LocationListActivity.class.getSimpleName();
    private static final int RC_FINE = 123;
    private static final String[] PERMISSION_REQUIRED =
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private double placeLatitude, placeLongitude, curLat, curLong;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout lblNetworkError;
    private Button btnRetry;
    private ConnectivityReceiver connectivityReceiver;
    private ArrayList<PlacesResponseModel.ResultsResponse> results;
    private ArrayList<LatLng> latLngArrayList;
    private GPSTracker gpsTracker;
    private String coOrdinates;
    private LocationListAdapter locationListAdapter;
    private ArrayList<String> placeNameArrayList;
    private ArrayList<String> placeAddressArrayList;
    private String sPlaceName, sPlaceAddress;
    private String placeType;
    private String placeId;
    private String radius;
    private int REQUEST_CODE_FOR_LOCATION = 3;
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.placeslist_recyclerview);
        progressBar = findViewById(R.id.progressBar);
        lblNetworkError = findViewById(R.id.idErrorLayout);
        btnRetry = findViewById(R.id.idBtnRetry);
        placeType = getIntent().getStringExtra("place_type");
        radius = getIntent().getStringExtra("radius");

        toolbar.setTitle("nearby " + placeType);
        setSupportActionBar(toolbar);
        //this line shows back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(AppController.getInstance().getApplicationContext(), R.color.colorPrimaryDark));
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_FOR_LOCATION);
        } else {
            getLocationOfUser(true);
        }

        connectivityReceiver = new ConnectivityReceiver(LocationListActivity.this);
        //check if internet available or not
        if (!connectivityReceiver.isConnected()) {
            lblNetworkError.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_FOR_LOCATION);
        } else {
            lblNetworkError.setVisibility(View.GONE);
            getLocationOfUser(true);
        }

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectivityReceiver.isConnected()) {
                    lblNetworkError.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    getNearByPlacesList();
                } else {
                    Toast.makeText(LocationListActivity.this, getString(R.string.msg_turnon_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView.addOnItemTouchListener(new LocationListAdapter.RecyclerTouchListener(AppController.getInstance().getApplicationContext(), recyclerView, new LocationListAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (latLngArrayList.size() != 0) {
                    latLngArrayList.get(position);
                    placeLatitude = latLngArrayList.get(position).latitude;
                    placeLongitude = latLngArrayList.get(position).longitude;

                    if (placeNameArrayList.size() != 0 && placeAddressArrayList.size() != 0) {
                        placeNameArrayList.get(position);
                        placeAddressArrayList.get(position);
                        sPlaceName = placeNameArrayList.get(position);
                        sPlaceAddress = placeAddressArrayList.get(position);

                        Log.i(TAG, " Place Location: " + placeLatitude + " & " + placeLongitude);
                        Intent mapIntent = new Intent(LocationListActivity.this, LocationMapViewActivity.class);
                        mapIntent.putExtra("latitude", placeLatitude);
                        mapIntent.putExtra("longitude", placeLongitude);
                        mapIntent.putExtra("name", sPlaceName);
                        mapIntent.putExtra("address", sPlaceAddress);
                        mapIntent.putExtra("placeId", placeId);
                        mapIntent.putExtra("curLat", curLat);
                        mapIntent.putExtra("curLong", curLong);

                        /*Bundle args = new Bundle();
                        args.putSerializable("placeResultArray", (Serializable) results);
                        mapIntent.putExtra("BUNDLE", args);*/

                        startActivity(mapIntent);
                    }
                } else {
                    Log.e(TAG, " error");
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_FOR_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationOfUser(true);
            } else {
                // Permission was denied or request was cancelled
                Toast.makeText(LocationListActivity.this, "Location Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocationOfUser(boolean result) {
        if (result) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i("permission return ", "---> failed");
                return;
            }
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                                                           @Override
                                                                           public void onSuccess(Location location) {
                                                                               if (location != null) {
                                                                                   LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                                                                   Geocoder geocoder;
                                                                                   List<Address> addresses;
                                                                                   curLat =
                                                                                           location.getLatitude();
                                                                                   curLong =
                                                                                           location.getLongitude();
                                                                                   coOrdinates = curLat + "," + curLong;
                                                                                   Log.i("Current: ",
                                                                                           "lat: " + curLat + " long: " + curLong);
                                                                                   Log.i("Current latlong: ",
                                                                                           latLng.toString());
//                                                                                   try {
//                                                                                       URLEncoder.encode(coOrdinates, "utf-8");
//                                                                                   } catch (UnsupportedEncodingException e) {
//                                                                                       e.printStackTrace();
//                                                                                       Log.e(
//                                                                                               "FusedLocationError: ", " error");
//                                                                                   }

                                                                                   getNearByPlacesList();
//                        geocoder = new Geocoder(LocationListActivity.this, Locale.getDefault());
//                        try {
//                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//                            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                            String city = addresses.get(0).getLocality();
//                            String state = addresses.get(0).getAdminArea();
//                            String country = addresses.get(0).getCountryName();
//                            String postalCode = addresses.get(0).getPostalCode();
//                            String knownName = addresses.get(0).getFeatureName();
//                            if (state != null) {
//                                PreferenceConnector.writeString(this, getString(R.string.state), state);
//                            }
//                            CommonMethods.infoLog("location details " + address + " " + city + " " + state + " " + country + " " + postalCode + " " + knownName);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            CommonMethods.infoLog("location details exception " + e.toString());
//                        }
                                                                               }
                                                                           }
                                                                       }
            ).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("location exception ", e.toString());
                    lblNetworkError.setVisibility(View.VISIBLE);
                }
            });
        } else {
            requestPermissionForAccessLocation(this);
        }
    }

    private void requestPermissionForAccessLocation(LocationListActivity locationListActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_FOR_LOCATION);
        }
    }

    private void getNearByPlacesList() {
        Log.d(TAG, "Method call:---> fetching places from server");
        Log.d(TAG, "Method call:---> place type: " + placeType);
        progressBar.setVisibility(View.VISIBLE);

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<PlacesResponseModel.Root> call;
        if (placeType.equalsIgnoreCase("ATM")) {
            call = apiService.getPlaces(
                    coOrdinates, Integer.parseInt(radius), "atm", "atm", AppConstants.API_KEY);
        } else if (placeType.equalsIgnoreCase("School")) {
            call = apiService.getPlaces(
                    coOrdinates, Integer.parseInt(radius), "School", "School", AppConstants.API_KEY);
        } else if (placeType.equalsIgnoreCase("Salon")) {
            call = apiService.getPlaces(
                    coOrdinates, Integer.parseInt(radius), "salon", "salon", AppConstants.API_KEY);
        } else if (placeType.equalsIgnoreCase("Gas Station")) {
            call = apiService.getPlaces(
                    coOrdinates, Integer.parseInt(radius), "gas_station", "gas_station", AppConstants.API_KEY);
        } else {
            call = apiService.getPlaces(
                    coOrdinates, Integer.parseInt(radius), placeType, placeType, AppConstants.API_KEY);
        }
        Log.e(TAG, "place type: " + placeType);

        call.enqueue(new Callback<PlacesResponseModel.Root>() {
            @Override
            public void onResponse(@NonNull Call<PlacesResponseModel.Root> call, @NonNull Response<PlacesResponseModel.Root> response) {
                PlacesResponseModel.Root root = response.body();
                Log.d(TAG, " request url: " + response.raw().request().url());
                latLngArrayList = new ArrayList<>();
                placeNameArrayList = new ArrayList<>();
                placeAddressArrayList = new ArrayList<>();
                if (response.isSuccessful()) {
                    Log.d(TAG, " response: " + response.body());
                    assert root != null;
                    if (root.status.equals(AppConstants.OK)) {
                        progressBar.setVisibility(View.GONE);
                        results = root.resultsResponse;
                        if (results.size() != 0) {
                            for (int i = 0; i < results.size(); i++) {
                                locationListAdapter = new LocationListAdapter(AppController.getInstance().getApplicationContext(), latLngArrayList, results);
                                recyclerView.setAdapter(locationListAdapter);
                                placeLatitude = Double.parseDouble(results.get(i).geometry.locationA.getLat());
                                placeLongitude = Double.parseDouble(results.get(i).geometry.locationA.getLng());
                                latLngArrayList.add(new LatLng(placeLatitude, placeLongitude));
                                sPlaceName = results.get(i).name;
                                sPlaceAddress = results.get(i).vicinity;
                                placeId = results.get(i).place_id;
                                placeNameArrayList.add(sPlaceName);
                                placeAddressArrayList.add(sPlaceAddress);
                            }
                        } else {
                            Toast.makeText(AppController.getInstance().getApplicationContext(), getString(R.string.error_place_search_error), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        lblNetworkError.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AppController.getInstance().getApplicationContext(), getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() != 200) {
                    progressBar.setVisibility(View.GONE);
                    lblNetworkError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull Throwable t) {
                // Log error here since request failed
                progressBar.setVisibility(View.GONE);
                lblNetworkError.setVisibility(View.VISIBLE);
                t.printStackTrace();
                call.cancel();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}