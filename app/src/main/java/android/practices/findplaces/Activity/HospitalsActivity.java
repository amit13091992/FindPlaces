package android.practices.findplaces.Activity;

import android.Manifest;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HospitalsActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = HospitalsActivity.class.getSimpleName();
    private static final int RC_FINE = 123;
    private static final String[] LOCATION_FINE_CORSE =
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private String coOrdinates;
    private HospitalListAdapter hospitalListAdapter;
    private GPSTracker gpsTracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospitals);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.placeslist_recyclerview);
        setSupportActionBar(toolbar);
        toolbar.setTitle("nearby Hospitals");
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        }
        // check if GPS enable
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(hospitalListAdapter);
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
        fetchCurrentLocation();
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<GooglePlacesResponse.Root> call = apiService.getHospitals(
                coOrdinates, AppConstants.PROXIMITY_RADIUS, "Hospitals", "Hospitals", AppConstants.API_KEY);

        call.enqueue(new Callback<GooglePlacesResponse.Root>() {
            @Override
            public void onResponse(@NonNull Call<GooglePlacesResponse.Root> call, @NonNull Response<GooglePlacesResponse.Root> response) {
                GooglePlacesResponse.Root root = (GooglePlacesResponse.Root) response.body();
                Log.d(TAG, " request url: " + response.raw().request().url());
                if (response.isSuccessful()) {
                    Log.d(TAG, " response: " + response.body());
                    assert root != null;
                    if (root.status.equals("OK")) {
                        ArrayList<GooglePlacesResponse.CustomA> results = root.customA;
                        for (int i = 0; i < results.size(); i++) {
                            hospitalListAdapter = new HospitalListAdapter(results, getApplicationContext());
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No matches found near you, Please Retry!!!", Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() != 200) {
                    Toast.makeText(getApplicationContext(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull Throwable t) {
                // Log error here since request failed
                call.cancel();
            }
        });

    }

    private boolean hasLocationAndContactsPermissions() {
        return EasyPermissions.hasPermissions(this, LOCATION_FINE_CORSE);
    }

    @AfterPermissionGranted(RC_FINE)
    public void fetchCurrentLocation() {
        gpsTracker = new GPSTracker(this);
        if (gpsTracker.getIsGPSTrackingEnabled() && checkLocationPermission() && hasLocationAndContactsPermissions()) {
            double currentLat = gpsTracker.getLatitude();
            double currentLong = gpsTracker.getLongitude();
            getNearByHospitals();
            coOrdinates = currentLat + "," + currentLong;
            try {
                URLEncoder.encode(coOrdinates, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
//            gpsTracker.showSettingsAlert();
            // Ask for both permissions
            EasyPermissions.requestPermissions(
                    this, "Permissions needed ...",
                    RC_FINE,
                    LOCATION_FINE_CORSE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
}