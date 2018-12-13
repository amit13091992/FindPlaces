package android.practices.findplaces.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.practices.findplaces.R;
import android.practices.findplaces.receivers.ConnectivityReceiver;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class SplashScreen extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = SplashScreen.class.getSimpleName();
    private static final int RC_FINE = 123;
    private static final String[] LOCATION_FINE_CORSE =
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    LinearLayout appIconLayout;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private Button btnRetry;
    private ConnectivityReceiver connectivityReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        progressBar = findViewById(R.id.progressBar);
        errorLayout = findViewById(R.id.idErrorLayout);
        btnRetry = findViewById(R.id.idBtnRetry);
        appIconLayout = (LinearLayout) findViewById(R.id.idAppIconHolder);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        connectivityReceiver = new ConnectivityReceiver(this);
        //check if internet available or not
        if (!connectivityReceiver.isConnected()) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            appIconLayout.setVisibility(View.GONE);
        } else if (!hasLocationAndContactsPermissions() && !checkLocationPermission()) {
            requiresTwoPermission();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    launchMainActivity();
                }
            }, 3000);
        }
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectivityReceiver.isConnected()) {
                    appIconLayout.setVisibility(View.VISIBLE);
                    errorLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            launchMainActivity();
                        }
                    }, 3000);
                } else {
                    Toast.makeText(SplashScreen.this, getString(R.string.msg_turnon_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void launchMainActivity() {
        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (connectivityReceiver.isConnected()) {
            errorLayout.setVisibility(View.GONE);
        } else {
            errorLayout.setVisibility(View.VISIBLE);
        }
    }

    @AfterPermissionGranted(RC_FINE)
    private void requiresTwoPermission() {
        if (EasyPermissions.hasPermissions(this, LOCATION_FINE_CORSE)) {
            // Already have permission, do the thing
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.msg_permission_required),
                    RC_FINE, LOCATION_FINE_CORSE);
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

    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasLocationAndContactsPermissions() {
        return EasyPermissions.hasPermissions(this, LOCATION_FINE_CORSE);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }
}
