package android.practices.findplaces.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.practices.findplaces.Models.GooglePlacesResponse;
import android.practices.findplaces.R;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


/**
 * Created by Amit on 30-Nov-18.
 */

public class HospitalListAdapter extends RecyclerView.Adapter<HospitalListAdapter.MyViewHolder> {
    Context context;
    ArrayList<LatLng> latLngArrayList;
    private ArrayList<GooglePlacesResponse.CustomA> stLstStores;


    public HospitalListAdapter(Context context, ArrayList<LatLng> latLng, ArrayList<GooglePlacesResponse.CustomA> stLstStores) {
        this.context = context;
        this.latLngArrayList = latLng;
        this.stLstStores = stLstStores;
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
        holder.res_rating.setText("Location: " + stLstStores.get(holder.getAdapterPosition()).geometry);
        holder.view1.setTag(context);
    }

    @Override
    public int getItemCount() {
        return stLstStores.size();

    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public GoogleMap map;
        TextView res_name;
        TextView res_address;
        TextView res_rating;
        TextView res_location;
        View view1;

        public MyViewHolder(View view, int viewType) {
            super(view);
            view1 = view;
            this.res_name = (TextView) itemView.findViewById(R.id.idName);
            this.res_rating = (TextView) itemView.findViewById(R.id.idRating);
            this.res_address = (TextView) itemView.findViewById(R.id.idAddress);
            this.res_location = (TextView) itemView.findViewById(R.id.idLocation);
        }
    }

}

