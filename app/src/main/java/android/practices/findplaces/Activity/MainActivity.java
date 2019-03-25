package android.practices.findplaces.Activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.practices.findplaces.Activity.Places.LocationListActivity;
import android.practices.findplaces.Adapter.MainActivityAdapter;
import android.practices.findplaces.App.AppController;
import android.practices.findplaces.Constants.AppConstants;
import android.practices.findplaces.R;
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

    //ConnectivityReceiver connectivityReceiver;
    //TextView lblNoInternetText;
    private GridView gridView;
    private Spinner spinnerRadius;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        gridView = findViewById(R.id.gridview);
        //lblNoInternetText = (TextView) findViewById(R.id.idNoInternetText);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(AppController.getInstance().getApplicationContext(), R.color.colorPrimary));
        }
//        connectivityReceiver = new ConnectivityReceiver(getApplicationContext());
//        //check if internet available or not
//        if (!connectivityReceiver.isConnected()) {
//            lblNoInternetText.setVisibility(View.VISIBLE);
//            gridView.setVisibility(View.GONE);
//            Snackbar.make(gridView, "You need Internet to use this app.", Snackbar.LENGTH_SHORT).show();
//        } else {
//            lblNoInternetText.setVisibility(View.GONE);
//        }

        MainActivityAdapter adapterViewAndroid = new MainActivityAdapter(MainActivity.this, AppConstants.placeNames, AppConstants.placeThumbnails);
        gridView.setAdapter(adapterViewAndroid);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(MainActivity.this, LocationListActivity.class);
                intent.putExtra("place_type", AppConstants.placeNames[position]);
                intent.putExtra("radius", spinnerRadius.getSelectedItem().toString());
                startActivity(intent);
            }
        });

        spinnerRadius = findViewById(R.id.spinner_radius);
        ArrayList<String> categories = new ArrayList<String>();
        categories.add("0 - 1 KM");
        categories.add("0 - 2 KM");
        categories.add("0 - 3 KM");
        categories.add("0 - 5 KM");
        categories.add("0 - 10 KM");
        categories.add("0 - 20 KM");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        // Drop down layout style - list view with radio button
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
