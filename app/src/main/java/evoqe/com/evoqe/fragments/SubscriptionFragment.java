package evoqe.com.evoqe.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import evoqe.com.evoqe.utilities.ConnectionDetector;
import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.SubscriptionAdapter;
import evoqe.com.evoqe.objects.ToastWrapper;

public class SubscriptionFragment extends Fragment {

    private final static String TAG = "SubscriptionFragment";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mLayout;
    /** A list of the current user's subscriptions (other users) */
    private List<ParseUser> mMySubList;


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

        RecyclerView recView = (RecyclerView) mLayout.findViewById(R.id.recycler_view);
        recView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recView.setLayoutManager(llm);

        // set an empty temporary adapter
        SubscriptionAdapter adapter = new SubscriptionAdapter(getActivity(), new ArrayList<ParseUser>(), new ArrayList<ParseUser>());
        recView.setAdapter(adapter);

        // we assume that we're retrieving info from parse, but that will take non-zero time, so in
        // the meantime, we assume we've received nothing.
        nothingRetrieved(false);

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
        String subKey = getString(R.string.subscriptions_key);
        // A relation between current user and other users (one-to-many?)
        ParseRelation<ParseUser> relation = me.getRelation(subKey);
        if (relation != null) {
            ParseQuery<ParseUser> relQuery = relation.getQuery();
            relQuery.findInBackground(new FindCallback<ParseUser>() {

                @Override
                public void done(List<ParseUser> mySubs, ParseException e) {
                    if (e == null) {
                        // We now have my subscriptions, let's get full list of possible subscriptions
                        mMySubList = mySubs;
                        getFullSubscriptionList();
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
     */
    public void getFullSubscriptionList() {
        // query the parse database for users that are public hosts
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("isPublicHost", true);
        query.orderByAscending(getString(R.string.full_name_key));
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> allSubs, ParseException e) {
                mSwipeRefreshLayout.setRefreshing(false); // remove refreshing notifier
                if (allSubs.size() == 0) {
                    nothingRetrieved(false);
                }
                if (e == null) {
                    resetVisibilities();  // make sure the right things are visible
                    // Adapter inflates list and accounts for subscription logic
                    SubscriptionAdapter adapter = new SubscriptionAdapter(getActivity(),
                                         allSubs, mMySubList);
                    ((RecyclerView) mLayout.findViewById(R.id.recycler_view)).setAdapter(adapter);
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
                if (isInternetPresent()) {
                    getCurrentSubscriptions();
                } else {
                    nothingRetrieved(false);  // now set it to show no internet
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    /** Instructs the user that there was an error ("No hosts"), and how to refresh
     * Can be called from any of the parse queries in this class. */
    private void nothingRetrieved(boolean error) {
        Log.i(TAG, "nothingRetrieved()");

        int childCount = ((RecyclerView) mLayout.findViewById(R.id.recycler_view)).getChildCount();
        boolean areChildren = childCount > 0;

        // retrieval error
        if (error) {
            if (areChildren) {
                String text;
                if (isInternetPresent() == false) { // counted as an error by parse
                    text = getString(R.string.no_connection);
                } else {
                    text = getString(R.string.refresh_error);
                }
                ToastWrapper.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            } else {
                if (isInternetPresent() == false) { // counted as an error by parse
                    resetVisibilities();
                    mLayout.findViewById(R.id.TV_no_connection).setVisibility(View.VISIBLE);
                } else {
                    resetVisibilities();
                    mLayout.findViewById(R.id.TV_refresh_instr).setVisibility(View.VISIBLE);
                    mLayout.findViewById(R.id.TV_retrieval_error).setVisibility(View.VISIBLE);
                }
            }
        // there is no internet connection
        } else if (isInternetPresent() == false) {
                if (areChildren) {
                    String text = getString(R.string.no_connection);
                    ToastWrapper.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                } else {
                    resetVisibilities();
                    mLayout.findViewById(R.id.TV_no_connection).setVisibility(View.VISIBLE);
                }
        // simply no hosts to show
        } else {
            if (areChildren) {
                // remove all children by setting an empty adapter
                SubscriptionAdapter adapter = new SubscriptionAdapter(getActivity(),
                        new ArrayList<ParseUser>(), new ArrayList<ParseUser>());
                ((RecyclerView) mLayout.findViewById(R.id.recycler_view)).setAdapter(adapter);
            }
            resetVisibilities();
            mLayout.findViewById(R.id.TV_no_hosts).setVisibility(View.VISIBLE);
            mLayout.findViewById(R.id.TV_refresh_instr).setVisibility(View.VISIBLE);
            mLayout.findViewById(R.id.TV_no_connection).setVisibility(View.GONE);
            mLayout.findViewById(R.id.SRL_main).bringToFront();
        }
    }

    /** Makes the right things visible and such */
    private void resetVisibilities() {
        Log.i(TAG, "resetVisibilities()");

        mLayout.findViewById(R.id.TV_no_hosts).setVisibility(View.GONE);
        mLayout.findViewById(R.id.TV_refresh_instr).setVisibility(View.GONE);
        mLayout.findViewById(R.id.TV_no_connection).setVisibility(View.GONE);
        mLayout.findViewById(R.id.TV_retrieval_error).setVisibility(View.GONE);
    }

    /** @return a boolean as to whether the internet is present or not */
    private boolean isInternetPresent() {
        // creating connection detector class instance
        ConnectionDetector cd = new ConnectionDetector(getActivity().getApplicationContext());
        boolean isInternetPresent = cd.isConnectingToInternet();
        return isInternetPresent;
    }
}
