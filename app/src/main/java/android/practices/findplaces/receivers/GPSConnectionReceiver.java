package android.practices.findplaces.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

/**
 * Created by Amit on 16-Mar-19.
 */
public class GPSConnectionReceiver extends BroadcastReceiver {

    public static GPSConnectivityReceiverListener gpsConnectivityReceiverListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (gpsConnectivityReceiverListener != null) {
            gpsConnectivityReceiverListener.onGpsStatusChanged(isGpsEnabled);
        }
    }

    public static boolean isGPSTurnOn(final Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public interface GPSConnectivityReceiverListener {
        void onGpsStatusChanged(boolean isConnected);
    }
}
