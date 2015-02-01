package evoqe.com.evoqe.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
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

import java.util.List;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.SubscriptionAdapater;

public class SubscriptionFragment extends ListFragment {

    private final static String TAG = "SubscriptionFragment";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mLayout;
    
    public static SubscriptionFragment newInstance() {
        return new SubscriptionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
    ViewGroup container, @Nullable
    Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_subscription, container, false);
        return mLayout;
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) mLayout.findViewById(R.id.SRL_main);
        setUpSwipeRefresh();

        // we assume that we're retrieving info from parse, so we start with a loading animation.
        // When parse comes back and gives us info or an error, we then take appropriate action.
        mSwipeRefreshLayout.setRefreshing(true);
        Log.d(TAG, "should be refreshing");

        getCurrentSubscriptions();
        super.onActivityCreated(savedInstanceState);
    }
    
    /**
     * Get a list of my current subscriptions, and from here call
     * getFullSubscriptionList().
     */
    public void getCurrentSubscriptions() {
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
                    if (e == null) {
                        // We now have my subscriptions, let's get full list of possible subscriptions
                        getFullSubscriptionList(mySubs);
                    } else {
                        Log.e(TAG, "error: " + e.toString());
                        nothingRetrieved(true);
                    }
                }
            }); 
        } else { // This should never happen as long as Parse is in order.
            Log.e(TAG, "null relationship between current user and subscriptions");
            nothingRetrieved(true);
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
                mSwipeRefreshLayout.setRefreshing(false); // remove refreshing notifier
                if (objects.size() == 0) {
                    nothingRetrieved(false);
                }
                if (e == null) {
                    retrievalResolution();  // make sure the right things are visible
                    // Adapter inflates list and accounts for subscription logic
                    ArrayAdapter<ParseUser> adapter = new SubscriptionAdapater(getActivity(),
                                         R.layout.list_item_subscription, objects, mySubs);
                    setListAdapter(adapter);
                } else {
                    Log.e(TAG, "error: " + e.toString());
                    nothingRetrieved(true);
                }
            }
        });
    }

    private void setUpSwipeRefresh() {
        Log.d(TAG, "setUpSwipeRefresh()");
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2,
                R.color.green_3, R.color.green_4);
        mSwipeRefreshLayout.setOnRefreshListener (new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCurrentSubscriptions();
            }
        });
    }

    /** Instructs the user that there was an error ("No hosts"), and how to refresh
     * Can be called from any of the parse queries in this class. */
    private void nothingRetrieved(boolean error) {
        Log.i(TAG, "onRetrievalError()");
        if (error) {
            String text = getActivity().getResources().getString(R.string.refresh_error);
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
        }
        mLayout.findViewById(android.R.id.list).setVisibility(View.GONE);
        mLayout.findViewById(R.id.TV_no_hosts).setVisibility(View.VISIBLE);
        mLayout.findViewById(R.id.TV_refresh_instr).setVisibility(View.VISIBLE);
    }

    /** Makes the right things visible and such */
    private void retrievalResolution() {
        Log.i(TAG, "retrievalResolution()");
        mLayout.findViewById(android.R.id.list).setVisibility(View.VISIBLE);
        mLayout.findViewById(R.id.TV_no_hosts).setVisibility(View.GONE);
        mLayout.findViewById(R.id.TV_refresh_instr).setVisibility(View.GONE);
    }
}
