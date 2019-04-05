package android.practices.findplaces.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.practices.findplaces.model.PlacesResponseModel;
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

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


/**
 * Created by Amit on 30-Nov-18.
 */

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<LatLng> latLngArrayList;
    private ArrayList<PlacesResponseModel.ResultsResponse> stLstStores;


    public LocationListAdapter(Context context, ArrayList<LatLng> latLng, ArrayList<PlacesResponseModel.ResultsResponse> stLstStores) {
        this.context = context;
        this.latLngArrayList = latLng;
        this.stLstStores = stLstStores;
    }

    @NonNull
    @Override
    public LocationListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final LocationListAdapter.MyViewHolder holder, int position) {
        Log.i("adapter_position", holder.getAdapterPosition() + "");
        holder.res_name.setText("Place: " + stLstStores.get(position).name);
        holder.res_address.setText("Address: " + stLstStores.get(position).vicinity);
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
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent motionEvent) {
            View child = rv.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(motionEvent)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent motionEvent) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView res_name;
        TextView res_address;
        View view1;

        MyViewHolder(View view) {
            super(view);
            this.view1 = view;
            this.res_name = (TextView) itemView.findViewById(R.id.idName);
            this.res_address = (TextView) itemView.findViewById(R.id.idAddress);
        }
    }

}

