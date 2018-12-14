package android.practices.findplaces.Adapter;

import android.content.Context;
import android.practices.findplaces.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Amit on 29-Nov-18.
 */
public class MainActivityAdapter extends BaseAdapter {

    private final String[] placeNames;
    private final int[] placeThumbnails;
    private Context mContext;

    public MainActivityAdapter(Context context, String[] gridViewString, int[] gridViewImageId) {
        mContext = context;
        this.placeThumbnails = gridViewImageId;
        this.placeNames = gridViewString;
    }

    @Override
    public int getCount() {
        return placeNames.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View gridViewAndroid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            gridViewAndroid = new View(mContext);
            gridViewAndroid = inflater.inflate(R.layout.main_activity_gridview_items, null);
            TextView placeType = (TextView) gridViewAndroid.findViewById(R.id.idPlaceName);
            ImageView placeIcon = (ImageView) gridViewAndroid.findViewById(R.id.idPlaceIcon);
            placeType.setText(placeNames[i]);
            placeIcon.setImageResource(placeThumbnails[i]);
        } else {
            gridViewAndroid = (View) convertView;
        }

        return gridViewAndroid;
    }
}
