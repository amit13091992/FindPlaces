package android.practices.findplaces.App;

import android.practices.findplaces.Models.PlaceDetailsModel;
import android.practices.findplaces.Models.PlacesResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Amit on 30-Nov-18.
 */
public interface ApiInterface {

    @POST("nearbysearch/json?")
    Call<PlacesResponseModel.Root> getPlaces(@Query("location") String lat_long, @Query("radius") int radius,
                                             @Query("types") String placetype, @Query("keyword") String keyword,
                                             @Query("key") String api_key);


    @GET("details/json?")
    Call<PlaceDetailsModel> getPlaceDetails(@Query("placeid") String placeId, @Query("key") String apiKey);
}
