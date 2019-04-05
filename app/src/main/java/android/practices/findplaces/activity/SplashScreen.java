package android.practices.findplaces.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.practices.findplaces.app.AppController;
import android.practices.findplaces.R;
import android.practices.findplaces.receivers.ConnectivityReceiver;
import android.practices.findplaces.receivers.GPSConnectionReceiver;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
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

@SuppressWarnings("deprecation")
public class SplashScreen extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, ConnectivityReceiver.ConnectivityReceiverListener, GPSConnectionReceiver.GPSConnectivityReceiverListener {

    private static final String TAG = SplashScreen.class.getSimpleName();
    private static final int RC_FINE = 123;
    private static final String[] PERMISSION_REQUIRED =
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
        AppController.getInstance().setConnectivityListener(this);
        AppController.getInstance().setGpsConnectivityListener(this);
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
            Log.e(TAG, " getting here after permission");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    launchMainActivity();
                }
            }, 2000);
        }

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectivityReceiver.isConnected()) {
                    appIconLayout.setVisibility(View.VISIBLE);
                    errorLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    Log.e(TAG, " getting here retry button permission");
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            launchMainActivity();
                        }
                    }, 2000);
                } else {
                    Toast.makeText(SplashScreen.this, getString(R.string.msg_turnon_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });

//        if (!GPSConnectionReceiver.isGPSTurnOn(AppController.getInstance().getApplicationContext())) {
//            showGPSDialog();
//        }
    }

    private void showGPSDialog() {
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme)) // Theme
                .setTitle(R.string.gps_lable_gps) // setTitle
                .setMessage(R.string.gps_lable_warning_message) // setMessage
                .setInverseBackgroundForced(false).setCancelable(true) //
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.cancel();
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);

                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.cancel();
                finish();
            }
        }).setIcon(R.drawable.ic_map_pin).show();
    }

    private void launchMainActivity() {
        Intent intent = new Intent(AppController.getInstance().getApplicationContext(), MainActivity.class);
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
        if (!connectivityReceiver.isConnected()) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            appIconLayout.setVisibility(View.GONE);
        } else if (!hasLocationAndContactsPermissions() && !checkLocationPermission()) {
            requiresTwoPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @AfterPermissionGranted(RC_FINE)
    private void requiresTwoPermission() {
        if (EasyPermissions.hasPermissions(this, PERMISSION_REQUIRED)) {
            // Already have permission, do the thing
            progressBar.setVisibility(View.VISIBLE);
            Log.e(TAG, " getting here after permission requiresTwoPermission()");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    launchMainActivity();
                }
            }, 2000);
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.msg_permission_required),
                    RC_FINE, PERMISSION_REQUIRED);
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
        return EasyPermissions.hasPermissions(this, PERMISSION_REQUIRED);
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

    @Override
    public void onGpsStatusChanged(boolean isConnected) {
        if (!isConnected) {
            showGPSDialog();
        }
    }
}
