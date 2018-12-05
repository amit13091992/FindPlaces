package android.practices.findplaces.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.practices.findplaces.Models.GooglePlacesResponse;
import android.practices.findplaces.R;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Amit on 30-Nov-18.
 */

public class HospitalListAdapter extends RecyclerView.Adapter<HospitalListAdapter.MyViewHolder> {
    Context context;
    private ArrayList<GooglePlacesResponse.CustomA> stLstStores;

    public HospitalListAdapter(ArrayList<GooglePlacesResponse.CustomA> stLstStores, Context context) {
        this.stLstStores = stLstStores;
        this.context = context;
    }

    @NonNull
    @Override
    public HospitalListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_listitem, parent, false);

        return new MyViewHolder(itemView, viewType);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final HospitalListAdapter.MyViewHolder holder, int position) {
        Log.i("adapter_position", holder.getAdapterPosition() + "");
        holder.res_name.setText("Place: " + stLstStores.get(holder.getAdapterPosition()).name);
        holder.res_address.setText("Address: " + stLstStores.get(holder.getAdapterPosition()).vicinity);
        holder.res_rating.setText("Rating: " + stLstStores.get(holder.getAdapterPosition()).rating);
        Log.i("photos array: ", stLstStores.get(holder.getAdapterPosition()).photos + "");
    }

    @Override
    public int getItemCount() {
        return stLstStores.size();

    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView res_name;
        TextView res_address;
        TextView res_rating;
        TextView res_phone;
        TextView res_distance;
        TextView current_location;
        ImageView res_image;
        //int view_type;

        public MyViewHolder(View view, int viewType) {
            super(view);
            this.res_name = (TextView) itemView.findViewById(R.id.idName);
            this.res_rating = (TextView) itemView.findViewById(R.id.idRating);
            this.res_address = (TextView) itemView.findViewById(R.id.idAddress);
            this.res_phone = (TextView) itemView.findViewById(R.id.idPhone);
            this.res_distance = (TextView) itemView.findViewById(R.id.idDistance);
            this.res_image = (ImageView) itemView.findViewById(R.id.idImage);

        }
    }
}

