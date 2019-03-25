package android.practices.findplaces.Constants;

import android.practices.findplaces.R;

/**
 * Created by Amit on 30-Nov-18.
 */
public class AppConstants {

    /**
     * Place your API_KEY here ...
     */
    public static final String API_KEY = "";

    public static final String TAG = "gplaces";

    public static final String GOOGLE_PLACES_URL = "https://maps.googleapis.com/maps/api/place/";
    //public static final String GOOGLE_DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json?origin=";

    public static final String RESULTS = "results";
    public static final String STATUS = "status";

    public static final String OK = "OK";
    public static final String ZERO_RESULTS = "ZERO_RESULTS";
    public static final String REQUEST_DENIED = "REQUEST_DENIED";
    public static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";

    //Key for nearby places json from google
    public static final String GEOMETRY = "geometry";
    public static final String LOCATION = "location";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";
    public static final String ICON = "icon";
    public static final String SUPERMARKET_ID = "id";
    public static final String NAME = "name";
    public static final String PLACE_ID = "place_id";
    public static final String REFERENCE = "reference";
    public static final String VICINITY = "vicinity";
    public static final String PLACE_NAME = "place_name";

    // remember to change the browser api key
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final int PROXIMITY_RADIUS = 2000;
    // The minimum distance to change Updates in meters
    public static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    public static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute
    // Timeout limit for making call for direction API.
    public static final int MIN_TIMEOUT_LIMIT = 5000; // unit in miliseconds

    public static final String[] placeNames = {"Hospitals", "Hotel", "ATM", "School", "Salon", "Gas Station"};

    public static final int[] placeThumbnails = {
            R.drawable.ic_hospital, R.drawable.ic_hotel,
            R.drawable.ic_atm, R.drawable.ic_school,
            R.drawable.ic_salon, R.drawable.ic_gas_station};
}
