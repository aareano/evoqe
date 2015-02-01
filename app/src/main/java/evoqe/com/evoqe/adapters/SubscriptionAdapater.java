package evoqe.com.evoqe.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.objects.ToastWrapper;

/* NOTE: 
 * Users subscribe to other users who have the propertie 
 *      "isPublicHost == true"
 * When a user subscribes to another user, two relations are made on parse.
 *      The subscribing user gains a "subscription" relationship and the 
 *      subscribed to user gains a "subscriber" relationship. 
 * On Parse, the "Host" class is depreciated; here the var name "host" is
 *      used to indicate a user who can be subscribed to.
 */

/**
 * This class inflates the layout for each row and holds an OnClickListener
 * for the checkbox on each row. When one is checked or unchecked, the 
 * image is immediately changed; all changes are saved to parse when fragment is paused
 * NOTE: "Host" here simply means a "User" who can be subscribed to. 
 * @author Aaron
 */
public class SubscriptionAdapater extends ArrayAdapter<ParseUser> {

    /** 
     * A list of Hosts whose associated users it is possible to subscribe to 
     */
    private List<ParseUser> mHosts;
    /** 
     * A list of Users to which the current user is subscribed 
     */
    private List<ParseUser> mMySubs;
    private Context mContext;
    private int mLayoutResourceId;
    private String TAG = "SubscriptionAdapter";

    public interface SubscriptionUpdateListener {
        public void updateSubscriptionCount();
    }

    /**
     * (The only) constructor for the HostAdapter class
     * @param context - The activity context
     * @param layoutResourceId - The id of the row layout
     * @param hosts - A list of users it is possible to subscribe to
     * @param mySubs - A list of my current subscriptions
     */
    public SubscriptionAdapater(Context context, int layoutResourceId, List<ParseUser> hosts, 
            List<ParseUser> mySubs) {
        super(context, layoutResourceId);
        
        Log.i(TAG , "Inflating subscription list with " + hosts.size() + 
                " host objects, and " + mySubs.size() + " subscriptions.");
        
        this.mHosts = hosts;
        this.mContext = context;
        this.mLayoutResourceId = layoutResourceId;
        this.mMySubs = mySubs;
    }
    
    @Override
    public int getCount() {
        return mHosts.size();
    }

    /**
     * This handles the construction of every row. 
     * The checkboxes are accounted for here.
     */
    @Override
    public View getView(final int position, View row, ViewGroup parent) {
        
        // define the row
        if (row == null)
        {   // inflate the layout of the row            
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
            
        } else {
            // do nothing
        }
        
        // get the specific User object for this row
        ParseUser host = mHosts.get(position);
        
        // fill list item's views with Host information
        if (host != null) {
            // *** title *** //
            TextView title = (TextView) row.findViewById(R.id.TV_name);
            String nameKey = mContext.getResources().getString(R.string.full_name_key);
            String currHostName = host.getString(nameKey);
            if (currHostName == null || currHostName.equals("")) {
                currHostName = mContext.getResources().getString(R.string.basic_error);
            }
            title.setText(currHostName);
            
            // image... TODO
            
            // *** check box *** //
            // NOTE: checkbox image changed once Parse is up to date on the change
            checkBoxAndSubscrLogic(row, host); // also handles changes in subscription
        }
        return row;
    }
    
    /**
     * This changes the check box image immediately, then saves the change 
     * to the instance of the user on disc. Once this fragment is paused (onPause), 
     * changes are saved to Parse. If successfully saved, nothing happens, on failure
     * a toast informs the user.
     * @param row - the row in question
     * @param host - the ParseUser in question
     */
    private void checkBoxAndSubscrLogic(View row, final ParseUser host) {
        CheckBox checkBox = (CheckBox) row.findViewById(R.id.check_box);
        
        // is the user/host on this row is already a current subscription?
        // loop through m(y)Subscriptions looking a match
        int size = mMySubs.size();
        for (int i = 0; i < size; i++) {
            // objectId match
            String hostId = host.getObjectId(); // this row
            String aUserId = mMySubs.get(i).getObjectId(); // subscription list
            
            // change checkbox image based on equality
            if (hostId != null && aUserId != null && hostId.equals(aUserId)) {
                checkBox.setChecked(true);
                checkBox.setButtonDrawable(R.drawable.ic_menu_compass); // TODO - get a proper image
                break;  // match! exit for loop
            } else {
                checkBox.setChecked(false);
                checkBox.setButtonDrawable(R.drawable.create_contact); // TODO - get a proper image
            }
        }
        
        /* The checkbox/subscription logic
         * NOTE: changes are pushed to Parse in onPause()
         * TODO save to Preferences in the interim
         */
        setUpCheckBoxResponses(checkBox, host);
    }

    private void setUpCheckBoxResponses(CheckBox checkBox, final ParseUser host) {
        checkBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View checkBox) {
                ParseUser me = ParseUser.getCurrentUser();
                String subKey = mContext.getResources().getString(R.string.subscriptions_key);
                ParseRelation<ParseObject> rel = me.getRelation(subKey);

                // was the box just checked or unchecked?
                final boolean checked = ((CheckBox) checkBox).isChecked();
                if (checked) { // was just checked, so add subscription
                    mMySubs.add(host);
                    ((CheckBox) checkBox).setButtonDrawable(R.drawable.ic_menu_compass);
                } else { // was just unchecked, so remove subscription
                    int size = mMySubs.size();
                    for (int i = 0; i < size; i++) { // loop through list to find and destroy
                        if (mMySubs.get(i).getString("username").equals(host.getString("username"))) {
                            mMySubs.remove(i);
                            break;
                        }
                    }
                    ((CheckBox) checkBox).setButtonDrawable(R.drawable.create_contact);
                }
                saveChanges(host, checked);
            }
        });
    }

    private void saveChanges(final ParseUser host, final boolean checked) {
        String funcName;
        if (checked) {
            funcName = "addSubscription";
        } else {
            funcName = "removeSubscription";
        }
        // do the actual adding or remove on Parse
        HashMap<String, String> map = new HashMap<>(); // implicit <String, String>
        map.put("objectId", host.getObjectId());
        ParseCloud.callFunctionInBackground(funcName, map, // does all relation logic
                new FunctionCallback<String>() {
                    @Override
                    public void done(String response, ParseException e) {
                        Log.d(TAG, "RESPONSE: " + response
                                + " (Host objectId: " + host.getObjectId() + ")");
                        if (e == null) { // success!
                            // update subscription count in NavigationDrawer
                            ((SubscriptionUpdateListener) mContext).updateSubscriptionCount();
                            if (checked) { // was added
                                ToastWrapper.makeText(mContext, "Subscription added",
                                        Toast.LENGTH_SHORT).show();
                            } else { // was removed
                                ToastWrapper.makeText(mContext, "Subscription removed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else { // failure!
                            Log.e(TAG, e.toString());
                            if (checked) { // was NOT added
                                ToastWrapper.makeText(mContext, "Unable to add subscription",
                                        Toast.LENGTH_SHORT).show();
                            } else {  // was NOT removed
                                ToastWrapper.makeText(mContext, "Unable to remove subscription",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }
}
