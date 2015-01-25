package evoqe.com.evoqe.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.SubscriptionAdapater;

// TODO: ADD SUBSCRIBER RELATIONSHIP STUFF

public class SubscriptionFragment extends ListFragment {

    private final static String TAG = "SubscriptionFragment";
    private View mLayout;
    /** Whether any changes were made to the current user's subscription list */
    // private Boolean isDirty = false; // TODO - save changes only when there are some
    /** Used in onPause() */
    private Context activityContext;
    
    public static SubscriptionFragment newInstance() {
        return new SubscriptionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
    ViewGroup container, @Nullable
    Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.vertical_listview, container, false);
        return mLayout;
    }
    
    @Override
    public void onActivityCreated(@Nullable
    Bundle savedInstanceState) {
        getCurrentSubsciptions(); // TODO put in a loading animation
        super.onActivityCreated(savedInstanceState);
    }
    
    /**
     * Get a list of my current subscriptions, and from here call
     * getFullSubscriptionList().
     */
    public void getCurrentSubsciptions() {
        // The current user is actually saved to disc at this point
        ParseUser me = ParseUser.getCurrentUser();
        // Get the relationship between current user (me) and my subscriptions
        String subKey = getActivity().getResources().getString(R.string.subscriptions_key);
        // A relation between current user and other users (one-to-many?)
        ParseRelation<ParseUser> relation = me.getRelation(subKey);
        if (relation != null) {
            ParseQuery<ParseUser> relQuery = relation.getQuery();
            relQuery.findInBackground(new FindCallback<ParseUser>() {

                @Override
                public void done(List<ParseUser> mySubs, ParseException e) {
                    // We now have my subscriptions, let's get full list of possible subscriptions
                    getFullSubscriptionList(mySubs);
                }
            }); 
        } else { // This should never happen as long as Parse is in order.
            Log.e(TAG, "null relationship between current user and subscriptions");
            String error = getActivity().getResources().getString(R.string.basic_error);
            Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            // TODO send user to tab fragments
        }
    }
    
    /**
     * Get a list of all possible subscriptions.
     * These are the "User" class on Parse with property "isPublicHost = true"
     * This method also creates and set the adapter for the list
     * @param mySubs - A list of the current user's subscriptions (other users)
     */
    public void getFullSubscriptionList(final List<ParseUser> mySubs) {
        // query the parse database for users that are public hosts
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("isPublicHost", true);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    // Adapter inflates list and accounts for subscription logic
                    ArrayAdapter<ParseUser> adapter = new SubscriptionAdapater(getActivity(),
                                         R.layout.list_item_subscription, objects, mySubs);
                    setListAdapter(adapter);
                } else {
                    Log.e(TAG, "error: " + e.toString());
                }
            }
        });
    }
        
    /** Once this fragment is paused (onPause), subscription
    * changes are saved to Parse. If successfully saved, nothing happens, on failure
    * a toast informs the user
    */
    @Override
    public void onPause() {
        // Push any changes to Parse
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i(TAG, "Subscription changes (if any) were saved");
                } else {
                    Toast.makeText(activityContext, "Changes could not be saved", 
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Subscription changes not saved || error: " 
                            + e.toString());
                }
            }
        });
        super.onPause();
    }
}
