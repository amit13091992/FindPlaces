package android.practices.findplaces.App;

import android.app.Application;
import android.practices.findplaces.Network.Volley.VolleySingleton;

import com.android.volley.RequestQueue;

/**
 * Created by Amit on 12-Dec-18.
 */
public class CustomApplication extends Application {
    private RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        requestQueue = VolleySingleton.getInstance(getApplicationContext()).getRequestQueue();
    }

    public RequestQueue getVolleyRequestQueue() {
        return requestQueue;
    }
}
