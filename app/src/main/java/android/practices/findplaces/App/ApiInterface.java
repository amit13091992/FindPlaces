package android.practices.findplaces.App;

import android.practices.findplaces.Models.GooglePlacesResponse;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Amit on 30-Nov-18.
 */
public interface ApiInterface {

    @POST("json?")
    Call<GooglePlacesResponse.Root> getHospitals(@Query("location") String lat_long, @Query("radius") int radius,
                                                 @Query("types") String placetype, @Query("keyword") String keyword,
                                                 @Query("key") String api_key);


}
