package android.practices.findplaces.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.practices.findplaces.Adapter.HospitalListAdapter;
import android.practices.findplaces.Constants.AppConstants;
import android.practices.findplaces.Models.GooglePlacesResponse;
import android.practices.findplaces.Network.ApiClient;
import android.practices.findplaces.Network.ApiInterface;
import android.practices.findplaces.Network.GPSTracker;
import android.practices.findplaces.R;
import android.practices.findplaces.receivers.ConnectivityReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HospitalsActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = HospitalsActivity.class.getSimpleName();
    private static final int RC_FINE = 123;
    private static final String[] LOCATION_FINE_CORSE =
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    double currentLat, currentLong;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    LinearLayout errorLayout;
    Button btnRetry;
    TextView lblNoInternetText;
    ConnectivityReceiver connectivityReceiver;
    SwipeRefreshLayout mSwipeRefreshLayout;
    GooglePlacesResponse.CustomA location;
    private String coOrdinates;
    private HospitalListAdapter hospitalListAdapter;
    private GPSTracker gpsTracker;

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
        //check if internet available or not
        if (!connectivityReceiver.isConnected()) {
            lblNoInternetText.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            lblNoInternetText.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            // check if GPS enable
            gpsTracker = new GPSTracker(this);
            if (gpsTracker.getIsGPSTrackingEnabled() && checkLocationPermission() && hasLocationAndContactsPermissions()) {
                currentLat = gpsTracker.getLatitude();
                currentLong = gpsTracker.getLongitude();
                coOrdinates = currentLat + "," + currentLong;
                try {
                    URLEncoder.encode(coOrdinates, "utf-8");
                    getNearByHospitals();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                EasyPermissions.requestPermissions(
                        this, "Permissions needed ...",
                        RC_FINE,
                        LOCATION_FINE_CORSE);
            }
        }

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                getNearByHospitals();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNearByHospitals();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        recyclerView.addOnItemTouchListener(new HospitalListAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new HospitalListAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String latitude = location.geometry.locationA.lat;
                String longitude = location.geometry.locationA.lng;
                Intent mapIntent = new Intent(HospitalsActivity.this, ShowLocationOnMapActivity.class);
                mapIntent.putExtra("latitude", latitude);
                mapIntent.putExtra("longitude", longitude);
                startActivity(mapIntent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
                if (response.isSuccessful()) {
                    Log.d(TAG, " response: " + response.body());
                    assert root != null;
                    if (root.status.equals("OK")) {
                        progressBar.setVisibility(View.GONE);
                        ArrayList<GooglePlacesResponse.CustomA> results = root.customA;
                        for (int i = 0; i < results.size(); i++) {
                            hospitalListAdapter = new HospitalListAdapter(results, getApplicationContext());
                        }
                        LinearLayoutManager llm = new LinearLayoutManager(HospitalsActivity.this);
                        recyclerView.setLayoutManager(llm);
                        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
                        recyclerView.setAdapter(hospitalListAdapter);
                    } else {
                        errorLayout.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        //Toast.makeText(getApplicationContext(), "No matches found near you, Please Retry!!!", Toast.LENGTH_SHORT).show();
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
                errorLayout.setVisibility(View.VISIBLE);
                call.cancel();
            }
        });

    }

    private boolean hasLocationAndContactsPermissions() {
        return EasyPermissions.hasPermissions(this, LOCATION_FINE_CORSE);
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
        } else {
            lblNoInternetText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted for Location:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            lblNoInternetText.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setRefreshing(true);
        } else {
            lblNoInternetText.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }
}