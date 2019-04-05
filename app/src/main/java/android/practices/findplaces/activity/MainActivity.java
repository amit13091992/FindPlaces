package android.practices.findplaces.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.practices.findplaces.R;
import android.practices.findplaces.activity.Places.LocationListActivity;
import android.practices.findplaces.adapter.MainActivityAdapter;
import android.practices.findplaces.constants.AppConstants;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Amit on 22-Nov-18.
 */
public class MainActivity extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce = false;
    private GridView gridView;
    private Spinner spinnerRadius;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        gridView = findViewById(R.id.gridview);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        }

        spinnerRadius = findViewById(R.id.spinner_radius);
        ArrayList<String> categories = new ArrayList<String>();
        categories.add("0 - 1 KM");
        categories.add("0 - 2 KM");
        categories.add("0 - 3 KM");
        categories.add("0 - 5 KM");
        categories.add("0 - 10 KM");
        categories.add("0 - 20 KM");
        String radiusString = null;

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        // Drop down layout style - list view with radio button
        if (categories.contains("0 - 1 KM")) {
            radiusString = "1";
        } else if (categories.contains("0 - 2 KM")) {
            radiusString = "2";
        } else if (categories.contains("0 - 3 KM")) {
            radiusString = "3";
        } else if (categories.contains("0 - 4 KM")) {
            radiusString = "4";
        } else if (categories.contains("0 - 5 KM")) {
            radiusString = "5";
        } else if (categories.contains("0 - 10 KM")) {
            radiusString = "10";
        } else if (categories.contains("0 - 20 KM")) {
            radiusString = "20";
        }
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinnerRadius.setAdapter(dataAdapter);
        spinnerRadius.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();
                // Showing selected spinner item
                //Toast.makeText(parent.getContext(), "Selected: " + item + " position: " + position, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        MainActivityAdapter adapterViewAndroid = new MainActivityAdapter(MainActivity.this, AppConstants.placeNames, AppConstants.placeThumbnails);
        gridView.setAdapter(adapterViewAndroid);
        final String finalRadiusString = radiusString;
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(MainActivity.this, LocationListActivity.class);
                intent.putExtra("place_type", AppConstants.placeNames[position]);
                intent.putExtra("radius", finalRadiusString);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.msg_exit_application), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

}
