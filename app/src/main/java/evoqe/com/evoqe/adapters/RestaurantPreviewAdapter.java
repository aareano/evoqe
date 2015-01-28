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

public class RestaurantPreviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String NAME_KEY;
    private String DETAILS_KEY;
    private String DISCOUNTS_KEY;
    private String OPEN_TIMES_KEY;
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
        OPEN_TIMES_KEY = mContext.getResources().getString(R.string.open_times_key);
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
        String timeString = getTimesForToday(viewHolder.currentItem);
        viewHolder.vOpenTimes.setText(timeString);

        // TextView list of discounts
        viewHolder.vDiscounts.setText(viewHolder.currentItem.getString(DISCOUNTS_KEY));
    }

    /**
     * Extracts and formats the time that this restaurant (restaurant) is open
     * @param restaurant
     * @return e.g. "11AM - 11PM" or "9:30AM - 10PM"
     */
    private String getTimesForToday(ParseObject restaurant) {
        /* Ex. openTimes string:
         * "su 11:00-23:00, mo 11:00-23:00, tu 11:00-23:00, we 11:00-23:00, " +
         * "th 11:00-23:00, fr 11:00-23:00, sa 11:00-23:00"
         */
        String openTimes = restaurant.getString(OPEN_TIMES_KEY);
        Calendar now = Calendar.getInstance();
        int day = (now.get(Calendar.DAY_OF_WEEK) - 1); // SUNDAY = 1
        final int dayInterval = 16;
        
        // extract string for today
        String dayString = openTimes.substring(day * dayInterval, (day * dayInterval) + 14);
        String closed = mContext.getResources().getString(R.string.closed);
        if (dayString.equals(closed)) { // NA-NA == closed all day
            return mContext.getResources().getString(R.string.closed_message);
        }
        
        // get open and close substrings and format them
        String open = dayString.substring(3, 8);
        String close = dayString.substring(9);
        open = formatString(open);
        close = formatString(close);
        return open + " - " + close;
    }
    
    /**
     * Trims a string in 24hr format (e.g. "14:00") if necessary and adds AM/PM
     * @param time - a string in 24hr format (e.g. "14:00")
     * @return e.g. 14:00 becomes 2PM, 14:30 becomes 2:30PM. On error, returns:
     *         "??".
     */
    private String formatString(String time) { 
        // remove leading 0's (e.g. 1:00 instead of 01:00)
        if (time.charAt(0) == '0') {
            time = time.substring(1);
        }
        
        // divine the hour
        int colon = time.indexOf(':');
        int hour = -1;
        if (colon == 1) { // 1 digit hour (e.g. 9:00)
            hour = Integer.parseInt(time.substring(0, 1));
        } else if (colon == 2) { // 2 digit hour (e.g. 11:00)
            hour = Integer.parseInt(time.substring(0, 2));
        } else if (hour == -1) { // we have a problem
            Log.e(TAG, "erroneous time string: \"" + time + "\"");
            return "??";
        }
        
        // remove trailing 0's (e.g. 11:00 becomes 11, 11:30 becomes 11:30)
        if (time.substring(time.length() - 2).equals("00")) {
            if (colon == 1) { // e.g. 9:00
                time = time.substring(0, 1);
            } else if (colon == 2) { // e.g. 11:00
                time = time.substring(0, 2);
            }
        }
        // add AM/PM
        if (hour < 12) {
            time = time + "AM";
        } else {
            if (hour != 12) { hour = hour - 12; } // back to 12hr format
            time = hour + time.substring(2) + "PM";
        }
        return time;
    }
}
