package evoqe.com.evoqe.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Calendar;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.objects.DateTimeParser;

public class RestaurantPreviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String NAME_KEY;
    private String DETAILS_KEY;
    private String DISCOUNTS_KEY;
    private final String TAG = "RestaurantPreviewAdapter";
    private Context mContext;
    private ArrayList<ParseObject> mRestaurants;

    public interface RestaurantClickListener {
        public void onRestaurantClicked(ParseObject restaurant);
    }

    /**
     * @param context The application context
     * @param restaurants A list of ParseObjects in the class 'Restaurants'
     */
    public RestaurantPreviewAdapter(Context context, ArrayList<ParseObject> restaurants) {
        mRestaurants = restaurants;
        mContext = context;

        NAME_KEY = mContext.getResources().getString(R.string.name_key);
        DETAILS_KEY = mContext.getResources().getString(R.string.details_key);
        DISCOUNTS_KEY = mContext.getResources().getString(R.string.discounts_key);
    }

    /** Provide a reference to the type of views that you are using (custom ViewHolder) */
    public class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView vTitle;
        protected TextView vName;
        protected TextView vDetails;
        protected TextView vOpenTimes;
        protected TextView vDiscounts;
        protected ImageView vThumbnail;
        protected View view;
        protected ParseObject currentItem;

        public ViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.TV_title);
            vName =  (TextView) v.findViewById(R.id.TV_name);
            vDetails = (TextView) v.findViewById(R.id.TV_details);
            vOpenTimes = (TextView) v.findViewById(R.id.TV_open_times);
            vDiscounts = (TextView) v.findViewById(R.id.TV_discounts);
            vThumbnail = (ImageView) v.findViewById(R.id.IMG_thumbnail);

            view = v;
            // If a user clicks on a row, send them to the detail activity for that event
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "RestaurantPreview onClick, title: '"
                            + currentItem.getString(NAME_KEY) + "'");
                    ((RestaurantClickListener) mContext).onRestaurantClicked(currentItem);
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_restaurant_preview, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        // set the current item
        ((ViewHolder) viewHolder).currentItem = mRestaurants.get(position);
        prepLayout(viewHolder);
    }

    @Override
    public int getItemCount() {
        return mRestaurants.size();
    }

    private void prepLayout(RecyclerView.ViewHolder holder) {
        final ViewHolder viewHolder = (ViewHolder) holder;

        // name
        viewHolder.vName.setText(viewHolder.currentItem.getString(NAME_KEY));

        // description
        viewHolder.vDetails.setText(viewHolder.currentItem.getString(DETAILS_KEY));

        // open times
        Calendar now = Calendar.getInstance();
        int day = (now.get(Calendar.DAY_OF_WEEK) - 1); // SUNDAY = 1
        ArrayList<Integer> times = DateTimeParser.getTimesForToday(viewHolder.currentItem, mContext, day);
        String timeString = DateTimeParser.getPresentableString(times);
        String closedMessage = mContext.getResources().getString(R.string.closed_message);
        viewHolder.vOpenTimes.setText((timeString == null) ? closedMessage : timeString);

        // discounts
        viewHolder.vDiscounts.setText("- " + viewHolder.currentItem.getString(DISCOUNTS_KEY));
    }
}
