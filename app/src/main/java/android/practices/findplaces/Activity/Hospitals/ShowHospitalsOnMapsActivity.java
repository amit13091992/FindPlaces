package android.practices.findplaces.Activity.Hospitals;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.practices.findplaces.R;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * Created by Amit on 09-Dec-18.
 */
public class ShowHospitalsOnMapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = ShowHospitalsOnMapsActivity.class.getSimpleName();
    private static double LATITUDE = 0.00;
    private static double LONGITUDE = 0.00;
    private static String PLACE_NAME;
    private static String PLACE_ADDRESS;

    private GoogleMap mMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_hospitals_on_maps);

        LATITUDE = getIntent().getDoubleExtra("latitude", 0.00);
        LONGITUDE = getIntent().getDoubleExtra("longitude", 0.00);
        PLACE_NAME = getIntent().getStringExtra("name");
        PLACE_ADDRESS = getIntent().getStringExtra("address");

        SupportMapFragment map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment));
        assert map != null;
        map.getMapAsync(this);
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        double mLat = LATITUDE;
        double mLon = LONGITUDE;
        LatLng mylocation = new LatLng(mLat, mLon);
        mMap.addMarker(new MarkerOptions().position(mylocation).snippet(PLACE_ADDRESS).title(PLACE_NAME)).showInfoWindow();
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
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 15));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}