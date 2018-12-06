package android.practices.findplaces.Activity;

import android.os.Bundle;
import android.practices.findplaces.Network.GPSTracker;
import android.practices.findplaces.R;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ShowLocationOnMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView lblLocation;
    private MapView mapView;
    private GoogleMap map;
    private GPSTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location_on_map);
        mapView = (MapView) findViewById(R.id.idLocationMap);
        lblLocation = (TextView) findViewById(R.id.idLocationText);
        gpsTracker = new GPSTracker(this);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.getMapAsync(this);
        }
        lblLocation.setText(gpsTracker.getLocality(ShowLocationOnMapActivity.this));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getApplicationContext());
        map = googleMap;
        setMapLocation();
    }

    private void setMapLocation() {
        if (map == null) return;

        LatLng currentLocation = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13f));
        map.addMarker(new MarkerOptions().position(currentLocation));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }
}
