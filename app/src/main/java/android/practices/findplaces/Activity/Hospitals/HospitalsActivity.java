package android.practices.findplaces.Activity.Hospitals;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.practices.findplaces.Adapter.HospitalListAdapter;
import android.practices.findplaces.App.ApiClient;
import android.practices.findplaces.App.ApiInterface;
import android.practices.findplaces.Constants.AppConstants;
import android.practices.findplaces.Models.GooglePlacesResponse;
import android.practices.findplaces.Network.GPSTracker;
import android.practices.findplaces.R;
import android.practices.findplaces.receivers.ConnectivityReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HospitalsActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = HospitalsActivity.class.getSimpleName();
    private double placeLatitude, placeLongitude;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout lblNetworkError;
    private Button btnRetry;
    private ConnectivityReceiver connectivityReceiver;
    private ArrayList<GooglePlacesResponse.CustomA> results;
    private ArrayList<LatLng> latLngArrayList;
    private String coOrdinates;
    private HospitalListAdapter hospitalListAdapter;
    private ArrayList<String> placeNameArrayList;
    private ArrayList<String> placeAddressArrayList;
    private String sPlaceName, sPlaceAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospitals);
        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.placeslist_recyclerview);
        progressBar = findViewById(R.id.progressBar);
        lblNetworkError = findViewById(R.id.idErrorLayout);
        btnRetry = findViewById(R.id.idBtnRetry);
        setSupportActionBar(toolbar);
        toolbar.setTitle("nearby Hospitals");
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        }
        //recyclerview
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //set recyclerview divider
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        connectivityReceiver = new ConnectivityReceiver(getApplicationContext());
        //check if internet available or not
        if (!connectivityReceiver.isConnected()) {
            lblNetworkError.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            lblNetworkError.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            // check if GPS enable
            GPSTracker gpsTracker = new GPSTracker(this);
            if (gpsTracker.getIsGPSTrackingEnabled()) {

                double currentLat = gpsTracker.getLatitude();
                double currentLong = gpsTracker.getLongitude();
                coOrdinates = currentLat + "," + currentLong;
                try {
                    URLEncoder.encode(coOrdinates, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(HospitalsActivity.this, getString(R.string.error_fetch_location), Toast.LENGTH_SHORT).show();
            }

            getNearByHospitals();
        }

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectivityReceiver.isConnected()) {
                    lblNetworkError.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(HospitalsActivity.this, getString(R.string.msg_turnon_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView.addOnItemTouchListener(new HospitalListAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new HospitalListAdapter.ClickListener() {
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
                        Intent mapIntent = new Intent(HospitalsActivity.this, ShowHospitalsOnMapsActivity.class);
                        mapIntent.putExtra("latitude", placeLatitude);
                        mapIntent.putExtra("longitude", placeLongitude);
                        mapIntent.putExtra("name", sPlaceName);
                        mapIntent.putExtra("address", sPlaceAddress);
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

    private void getNearByHospitals() {
        progressBar.setVisibility(View.VISIBLE);
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<GooglePlacesResponse.Root> call = apiService.getHospitals(
                coOrdinates, AppConstants.PROXIMITY_RADIUS, "Hospitals", "Hospitals", AppConstants.API_KEY);

        call.enqueue(new Callback<GooglePlacesResponse.Root>() {
            @Override
            public void onResponse(@NonNull Call<GooglePlacesResponse.Root> call, @NonNull Response<GooglePlacesResponse.Root> response) {
                GooglePlacesResponse.Root root = response.body();
                Log.d(TAG, " request url: " + response.raw().request().url());
                latLngArrayList = new ArrayList<>();
                placeNameArrayList = new ArrayList<>();
                placeAddressArrayList = new ArrayList<>();
                if (response.isSuccessful()) {
                    Log.d(TAG, " response: " + response.body());
                    assert root != null;
                    if (root.status.equals("OK")) {
                        progressBar.setVisibility(View.GONE);
                        results = root.customA;
                        if (results.size() != 0) {
                            for (int i = 0; i < results.size(); i++) {
                                hospitalListAdapter = new HospitalListAdapter(getApplicationContext(), latLngArrayList, results);
                                recyclerView.setAdapter(hospitalListAdapter);
                                placeLatitude = Double.parseDouble(results.get(i).geometry.locationA.getLat());
                                placeLongitude = Double.parseDouble(results.get(i).geometry.locationA.getLng());
                                latLngArrayList.add(new LatLng(placeLatitude, placeLongitude));
                                sPlaceName = results.get(i).name;
                                sPlaceAddress = results.get(i).vicinity;
                                placeNameArrayList.add(sPlaceName);
                                placeAddressArrayList.add(sPlaceAddress);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.error_place_search_error), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        lblNetworkError.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), getString(R.string.error_place_search_error), Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
        if (connectivityReceiver.isConnected()) {
            lblNetworkError.setVisibility(View.GONE);
        } else {
            lblNetworkError.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            lblNetworkError.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            lblNetworkError.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

        }
    }
}