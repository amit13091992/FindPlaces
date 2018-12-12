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
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HospitalsActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = HospitalsActivity.class.getSimpleName();
    double placeLatitude, placeLongitude;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    LinearLayout errorLayout;
    Button btnRetry;
    TextView lblNoInternetText;
    ConnectivityReceiver connectivityReceiver;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ArrayList<GooglePlacesResponse.CustomA> results;
    ArrayList<LatLng> latLngArrayList;
    private String coOrdinates;
    private HospitalListAdapter hospitalListAdapter;
    private RecyclerView.RecyclerListener mRecycleListener = new RecyclerView.RecyclerListener() {
        @Override
        public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
            HospitalListAdapter.MyViewHolder mapHolder = (HospitalListAdapter.MyViewHolder) holder;
            if (mapHolder.map != null) {
                mapHolder.map.clear();
                mapHolder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospitals);
        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.placeslist_recyclerview);
        progressBar = findViewById(R.id.progressBar);
        errorLayout = findViewById(R.id.idErrorLayout);
        btnRetry = findViewById(R.id.idBtnRetry);
        lblNoInternetText = findViewById(R.id.idNoInternetText);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        setSupportActionBar(toolbar);
        toolbar.setTitle("nearby Hospitals");
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        }
        connectivityReceiver = new ConnectivityReceiver(getApplicationContext());
        //recyclerview
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setRecyclerListener(mRecycleListener);
        //check if internet available or not
        if (!connectivityReceiver.isConnected()) {
            lblNoInternetText.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            lblNoInternetText.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            // check if GPS enable
            GPSTracker gpsTracker = new GPSTracker(this);
            if (gpsTracker.getIsGPSTrackingEnabled()) {

                double currentLat = gpsTracker.getLatitude();
                double currentLong = gpsTracker.getLongitude();
                coOrdinates = currentLat + "," + currentLong;
                try {
                    URLEncoder.encode(coOrdinates, "utf-8");
                    getNearByHospitals();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(HospitalsActivity.this, "Unable to fetch current Locations, Try Again !!!", Toast.LENGTH_SHORT).show();
            }
        }

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectivityReceiver.isConnected()) {
                    errorLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    getNearByHospitals();
                } else {
                    Toast.makeText(HospitalsActivity.this, "Please Turn on Internet and Try Again !!!", Toast.LENGTH_SHORT).show();
                }

            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (connectivityReceiver.isConnected()) {
                    getNearByHospitals();
                    mSwipeRefreshLayout.setRefreshing(true);
                } else {
                    lblNoInternetText.setVisibility(View.VISIBLE);
                    Toast.makeText(HospitalsActivity.this, "Please Turn on Internet and Try Again !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView.addOnItemTouchListener(new HospitalListAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new HospitalListAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.i(TAG, " Place Location: " + placeLatitude + " & " + placeLongitude);
                Intent mapIntent = new Intent(HospitalsActivity.this, ShowHospitalsOnMapsActivity.class);
                startActivity(mapIntent);
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
                if (response.isSuccessful()) {
                    Log.d(TAG, " response: " + response.body());
                    assert root != null;
                    if (root.status.equals("OK")) {
                        progressBar.setVisibility(View.GONE);
                        results = root.customA;
                        for (int i = 0; i < results.size(); i++) {
                            placeLatitude = Double.parseDouble(results.get(i).geometry.locationA.lat);
                            placeLongitude = Double.parseDouble(results.get(i).geometry.locationA.lng);
                            latLngArrayList.add(new LatLng(placeLatitude, placeLongitude));
                            hospitalListAdapter = new HospitalListAdapter(getApplicationContext(), latLngArrayList, results);
                            recyclerView.setAdapter(hospitalListAdapter);
                        }

                    } else {
                        errorLayout.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "No matches found near you, Please Retry!!!", Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() != 200) {
                    progressBar.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull Throwable t) {
                // Log error here since request failed
                progressBar.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
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
            lblNoInternetText.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            lblNoInternetText.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            lblNoInternetText.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            lblNoInternetText.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }
}