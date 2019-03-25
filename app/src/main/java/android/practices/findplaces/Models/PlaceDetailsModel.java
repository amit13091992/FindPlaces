package android.practices.findplaces.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Amit on 15-Dec-18.
 */
public class PlaceDetailsModel implements Serializable {

    @SerializedName("status")
    public String status;

    @SerializedName("geometry")
    public Geometry geometry;
    @SerializedName("results")
    public ArrayList<Reviews> reviews = new ArrayList<>();
    @SerializedName("opening_hours")
    public OpeningHours openingHours;
    @SerializedName("address_components")
    public AddressComponents addressComponents;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String placename;
    @SerializedName("place_id")
    @Expose
    private String placeId;
    @SerializedName("rating")
    @Expose
    private String rating;
    @SerializedName("url")
    @Expose
    private String website;
    @SerializedName("vicinity")
    @Expose
    private String vicinity;

    public class locationModel implements Serializable {
        @SerializedName("lat")
        public String lat;
        @SerializedName("lng")
        public String lng;
    }

    private class Reviews implements Serializable {
        @SerializedName("author_name")
        @Expose
        private String author_name;
        @SerializedName("rating")
        @Expose
        private int rating;
        @SerializedName("relative_time_description")
        @Expose
        private String rating_given_time;
        @SerializedName("text")
        @Expose
        private String review_text;
    }

    public class Geometry implements Serializable {
        @SerializedName("location")
        public locationModel locationA;
    }


    private class OpeningHours implements Serializable {
        @SerializedName("weekday_text")
        public ArrayList<String> weekdaysText = new ArrayList<>();
    }

    private class AddressComponents implements Serializable {
        @SerializedName("types")
        public ArrayList<String> types = new ArrayList<>();
        @SerializedName("long_name")
        @Expose
        private String long_name;
        @SerializedName("short_name")
        @Expose
        private String short_name;
    }
}
