package android.practices.findplaces.Activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.practices.findplaces.Activity.Hospitals.LocationListActivity;
import android.practices.findplaces.Adapter.MainActivityAdapter;
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
import android.widget.GridView;
import android.widget.Toast;

/**
 * Created by Amit on 22-Nov-18.
 */
public class MainActivity extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce = false;
    private GridView gridView;

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
            window.setStatusBarColor(ContextCompat.getColor(AppController.getInstance().getApplicationContext() R.color.colorPrimary));
        }
        MainActivityAdapter adapterViewAndroid = new MainActivityAdapter(MainActivity.this, AppConstants.placeNames, AppConstants.placeThumbnails);
        gridView.setAdapter(adapterViewAndroid);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(MainActivity.this, LocationListActivity.class);
                intent.putExtra("place_type", AppConstants.placeNames[position]);
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
