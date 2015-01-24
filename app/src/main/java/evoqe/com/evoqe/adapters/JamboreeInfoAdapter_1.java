package evoqe.com.evoqe.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.HashMap;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.objects.Jamboree;

/**
 * Provide views to RecyclerView with data.
 */
public class JamboreeInfoAdapter_1 extends RecyclerView.Adapter<JamboreeInfoAdapter_1.ViewHolder> {
    
    private static final String TAG = "JamboreeInfoAdapter_1";
    private ParseObject mJamboree;
    private Context mContext;
 
    public JamboreeInfoAdapter_1(ParseObject jamboree, Context context) {
        mJamboree = jamboree;
        mContext = context;
    }

    /**
     * Size of the dataset (invoked by the layout manager)
     * This adapter is only doing a single card, so the size is always 1.
     */
    @Override
    public int getItemCount() {
        return 1;
    }
    
    /** Provide a reference to the type of views that you are using (custom ViewHolder) */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView vTitle;
        protected ImageView vThumbnail;
        protected TextView vHost;
        protected TextView vPublicitySchool;
        protected TextView vDate;
        protected TextView vTime;
        protected TextView vDescription;
        protected TextView vLocation;
        protected Button vSubscribe;
        
        public ViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.TV_title);
            vThumbnail = (ImageView) v.findViewById(R.id.thumbnail);
            vHost = (TextView) v.findViewById(R.id.TV_host);
            vPublicitySchool = (TextView) v.findViewById(R.id.TV_publicity_school);
            vDate = (TextView) v.findViewById(R.id.TV_date);
            vTime = (TextView) v.findViewById(R.id.TV_time);
            vDescription = (TextView) v.findViewById(R.id.TV_description);
            vLocation = (TextView) v.findViewById(R.id.TV_location);
            vSubscribe = (Button) v.findViewById(R.id.BTN_subscribe);
        }
    }
 
    // Create new views (invoked by the layout manager)
    @Override   
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_jamboree_info, viewGroup, false);
        return new ViewHolder(v);
    }
 
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Title
        String titleKey = mContext.getResources().getString(R.string.title);
        viewHolder.vTitle.setText(mJamboree.getString(titleKey));

        // Host
        String hostKey = mContext.getResources().getString(R.string.owner_full_name);
        viewHolder.vHost.setText(mJamboree.getString(hostKey));

        // Publicity (e.g. "Public Event") and school (e.g. "Tufts")
        // TODO

        // Date (e.g. "Wednesday 4/12/14") and Time (e.g. "4:30pm - 6:30pm")
        String dateKey = mContext.getResources().getString(R.string.start_time);
        String timeKey = mContext.getResources().getString(R.string.title);
        // make a new Jamboree class object to handle date/time formatting
        Jamboree jam = new Jamboree(mJamboree);
        final String dateString = jam.getDays(false);
        final String timeString = jam.getTimes(false);
        viewHolder.vDate.setText(dateString);
        viewHolder.vTime.setText(timeString);

        // Description
        String descriptionKey = mContext.getResources().getString(R.string.description);
        String description = mJamboree.getString(descriptionKey);
        if (description == null || description.equals("")) {
            description = mContext.getResources().getString(R.string.no_description);
        }
        viewHolder.vDescription.setText(description);

        // Location (e.g. "President's lawn")
        String locationKey = mContext.getResources().getString(R.string.location);
        viewHolder.vLocation.setText(mJamboree.getString(locationKey));

        // Subscribe to host button
        viewHolder.vSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ParseUser host = ((ParseUser) mJamboree.get("owner")); // get host user
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("objectId", host.getObjectId());
                ParseCloud.callFunctionInBackground("addSubscription", map, // does all relation logic
                        new FunctionCallback<String>() {

                    @Override
                    public void done(String response, ParseException e) {
                        Log.d(TAG, "RESPONSE: " + response + " (Host objectId: " + host.getObjectId() + ")");
                        if (e == null) { // success!
                            Toast.makeText(mContext, "Subscription added", Toast.LENGTH_SHORT).show();
                        } else { // failure!
                            Log.e(TAG, e.toString());
                            Toast.makeText(mContext, "Unable to add subscription", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
