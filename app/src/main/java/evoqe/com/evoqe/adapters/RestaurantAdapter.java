package evoqe.com.evoqe.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.Calendar;
import java.util.List;

import evoqe.com.evoqe.R;

public class RestaurantAdapter extends ArrayAdapter<ParseObject> {

    private final String TAG = "RestaurantAdapter";
    private Context context;
    private int layoutResourceId;
    private  List<ParseObject> restaurants;

    public interface RestaurantClickListener {
        public void onRestaurantClicked(String title);
    }

    /**
     * @param context The application context
     * @param resourceId The resource ID of the list item layout to inflate - THIS IS MUST BE R.layout.list_item_restaurant (u.r.e)!
     * @param restaurants A list of ParseObjects in the class 'Restaurants'
     */
    public RestaurantAdapter(Context context, int resourceId, List<ParseObject> restaurants) {
        super(context, resourceId, restaurants);
        
        this.restaurants = restaurants;
        this.context = context;
        this.layoutResourceId = resourceId;
    }
    
    @Override
    public int getCount() {
        return restaurants.size();
    }
    
    /**
     * This handles the construction of every row.
     */
    @Override
    public View getView(final int position, View row, ViewGroup parent) {
        
        // define the row
        if(row == null)
        {   // inflate the layout of the row            
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
        } else {
            // do nothing
        }
        
        // get the Jamboree object
        ParseObject restaurant = restaurants.get(position);
        
        // fill list item's views with Jamboree information
        if (restaurant != null) {
            // name
            TextView title = (TextView) row.findViewById(R.id.TV_name);
            String key = context.getResources().getString(R.string.name);
            title.setText(restaurant.getString(key));
            
            // description
            TextView description = (TextView) row.findViewById(R.id.TV_description);
            key = context.getResources().getString(R.string.details);
            description.setText(restaurant.getString(key));
            
            // open times
            /* Ex. openTimes string:
             * "su 11:00-23:00, mo 11:00-23:00, tu 11:00-23:00, we 11:00-23:00, " +  
             * "th 11:00-23:00, fr 11:00-23:00, sa 11:00-23:00"
             */
            TextView times = (TextView) row.findViewById(R.id.TV_open_times);
            String timeString = getTimesForToday(restaurant);
            times.setText(timeString);

            // TextView list of discounts
            TextView disc = (TextView) row.findViewById(R.id.TV_discounts);
            key = context.getResources().getString(R.string.discounts);
            disc.setText(restaurant.getString(key));
        }
        
        return row;
    }

    /**
     * Extracts and formats the time that this restaurant (restaurant) is open
     * @param restaurant
     * @return e.g. "11AM - 11PM" or "9:30AM - 10PM"
     */
    private String getTimesForToday(ParseObject restaurant) {
        String key = context.getResources().getString(R.string.open_times);
        String openTimes = restaurant.getString(key);
        Calendar now = Calendar.getInstance();
        int day = (now.get(Calendar.DAY_OF_WEEK) - 1); // SUNDAY = 1
        final int dayInterval = 16;
        
        // extract string for today
        String dayString = openTimes.substring(day * dayInterval, (day * dayInterval) + 14);
        String closed = context.getResources().getString(R.string.closed);
        if (dayString.equals(closed)) { // NA-NA == closed all day
            return context.getResources().getString(R.string.closed_message);
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
    /*
    public class DiscountArrayAdapter extends ArrayAdapter<String> {

        private final int resource;
        private final String[] objects;
        
        public DiscountArrayAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
            this.resource = resource;
            this.objects = objects;
        }
        
        /**
         * This handles the construction of every row.
         *
        @Override
        public View getView(final int position, View row, ViewGroup parent) {
            
            // define the row
            if(row == null)
            {   // inflate the layout of the row            
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(resource, parent, false);
                
            } else {
                // do nothing
            }
            TextView tv = (TextView) row.findViewById(android.R.id.text1);
            tv.setText(objects[position]);
            
            return row;
        }
    }
    */
}
