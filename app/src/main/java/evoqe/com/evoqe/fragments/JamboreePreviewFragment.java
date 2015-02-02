package evoqe.com.evoqe.fragments;


import android.app.Activity;
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
import android.widget.FrameLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.JamboreePreviewAdapter;
import evoqe.com.evoqe.objects.ToastWrapper;
import evoqe.com.evoqe.utilities.ConnectionDetector;

public class JamboreePreviewFragment extends Fragment {
    
    private final String TAG = "JamboreePreviewFragment";
    private FrameLayout mLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout; // actually the same object as mLayout...
    
    /** To be passed to the adapter for callback puposes */
    private Activity mActivity;
    
    public static Fragment newInstance(Activity activity) {
        JamboreePreviewFragment frag = new JamboreePreviewFragment();
        frag.mActivity = activity;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {      
        mLayout = (FrameLayout) inflater.inflate(R.layout.fragment_jamboree_preview, container, false);
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
        JamboreePreviewAdapter adapter = new JamboreePreviewAdapter(getActivity(), new ArrayList<ParseObject>(), mActivity);
        recView.setAdapter(adapter);

        // we assume that we're retrieving info from parse, but that will take non-zero time, so in
        // the meantime, we act like we've received nothing.
        nothingRetrieved(false);

        getEventList();
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Retrieves a List<ParseObject> of "Jamboree"s from Parse and populates the adapter with it.
     */
    public void getEventList() {
        // Get the params to use for fetching Jamborees
        ParseCloud.callFunctionInBackground("getPublicJamboreeQueryParameters", new HashMap<String, Object>(),
                new FunctionCallback<HashMap<String, String>>() {
            @Override
            public void done(final HashMap<String, String> params, ParseException e) {
                if (e == null) {
                    // We have most of the params, but now get the current users subscriptions
                    ParseUser me = ParseUser.getCurrentUser();
                    ParseRelation<ParseUser> rel = me.getRelation("subscriptions");
                    rel.getQuery().findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> mySubscriptions, ParseException e) {
                            if (e == null) {
                                if (params == null) {
                                    Log.e(TAG, "getPublicJamboreeQueryParameters returned null!");
                                    nothingRetrieved(true);
                                    return;
                                } else {
                                    // Now we have the params and the users from whom to pull events, so let's query Parse based on that
                                    ParseQuery<ParseObject> query = setUpQuery(mySubscriptions, params);
                                    query.findInBackground(new FindCallback<ParseObject>() {
                                        @Override
                                        public void done(List<ParseObject> objects, ParseException e) {
                                            mSwipeRefreshLayout.setRefreshing(false); // remove refreshing notifier
                                            if (e == null) {
                                                if (objects.size() == 0) {
                                                    nothingRetrieved(false);
                                                } else {
                                                    resetVisibilities(); // make sure the right things are visible
                                                    JamboreePreviewAdapter adapter = new JamboreePreviewAdapter(getActivity(), objects, mActivity);
                                                    ((RecyclerView) mLayout.findViewById(R.id.recycler_view)).setAdapter(adapter);
                                                }
                                            } else {
                                                Log.e(TAG, e.toString());
                                                nothingRetrieved(true);
                                            }
                                        }
                                    });
                                }
                            } else {
                                Log.e(TAG, e.toString());
                                nothingRetrieved(true);
                            }
                        }
                    });
                } else {
                    Log.e(TAG, e.toString());
                    nothingRetrieved(true);
                }

            }
        });
    }

    /**
     * Constructs a ParseQuery<ParseObject> based on params pulled from the cloud code and the
     * subscriptions of the current user.
     * @param mySubscriptions List<ParseUser> of Users the current user to subscribed to
     * @param params HashMap<String, String> params to retrieve Jamborees based off of, u.r.e. for this to be null!
     * @return the query to run
     */
    private ParseQuery<ParseObject> setUpQuery(List<ParseUser> mySubscriptions,
                                               HashMap<String, String> params) {
        // ...example params...
        /* "sortBy": "startTime",
         * "initialRetrievalCount": 50,
         * "cutOffTimeDayBeforeInHours": 4, TODO - what?
         * "greaterThanAttribute": "startTime", // attr for time restriction on query TODO - what?
         * "privacy": "PUBLIC",
         * "cachePolicy": "kPFCachePolicyCacheThenNetwork" TODO - what?
         */
//        Log.d(TAG, "params = " + params.toString());
        ParseQuery<ParseObject> query = new ParseQuery<>("Jamboree"); // implicit <ParseObject>
        // Only get events that the user has subscribed to
        query.whereContainedIn("owner", mySubscriptions);

        query.orderByDescending(params.get("sortBy").toString());
//        String count = params.get("initialRetrievalCount"); // TODO - maps tries to cast Integer to String. can't do it.
//        Log.d(TAG, "count = " + count);
//        query.setLimit(Integer.parseInt(count));
        query.whereEqualTo("privacy", params.get("privacy"));

        // get events after 12 hours ago
//        Long time = new Date().getTime();
//        int hoursBeforeNow = 12; // 12 hours before now
//        Date date = new Date(time + (hoursBeforeNow * 60 * 60 * 1000));
//        query.whereGreaterThan("startTime", date);

        // get the next 7 days' events - doesn't work? or don't need it?
        Long time = new Date().getTime();
        int daysFromNow = 7; // 7 days from now
        Date date = new Date(time + daysFromNow * (24 * 60 * 60 * 1000));
        query.whereLessThanOrEqualTo("startTime", date);

        return query;
    }

    private void setUpSwipeRefresh() {
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2,
                R.color.green_3, R.color.green_4);
        mSwipeRefreshLayout.setOnRefreshListener (new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isInternetPresent()) {
                    getEventList();
                } else {
                    nothingRetrieved(false);  // now set it to show no internet
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    /** Instructs the user that there was an error ("No events"), and how to refresh
     * Can be called from any of the parse queries in this class.
     * This is called if there actually are no events too
     * @param error - whether there was an error
     */
    private void nothingRetrieved(boolean error) {
        Log.i(TAG, "nothingRetrieved(" + error + ")");

        int childCount = ((RecyclerView) mLayout.findViewById(R.id.recycler_view)).getChildCount();
        boolean areChildren = childCount > 0;

        // retrieval error
        if (error) {
            if (areChildren) {
                String text;
                if (isInternetPresent() == false) { // counted as an error by parse
                    text = getActivity().getResources().getString(R.string.no_connection);
                } else {
                    text = getActivity().getResources().getString(R.string.refresh_error);
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
                String text = getActivity().getResources().getString(R.string.no_connection);
                ToastWrapper.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            } else {
                resetVisibilities();
                mLayout.findViewById(R.id.TV_no_connection).setVisibility(View.VISIBLE);
            }
        // simply no hosts to show
        } else {
            if (areChildren) {
                // remove all children by setting an empty adapter
                JamboreePreviewAdapter adapter = new JamboreePreviewAdapter(getActivity(),
                        new ArrayList<ParseObject>(), mActivity);
                ((RecyclerView) mLayout.findViewById(R.id.recycler_view)).setAdapter(adapter);
            }
            resetVisibilities();
            mLayout.findViewById(R.id.TV_no_events).setVisibility(View.VISIBLE);
            mLayout.findViewById(R.id.TV_refresh_instr).setVisibility(View.VISIBLE);
            mLayout.findViewById(R.id.BTN_goto_subscriptions).setVisibility(View.VISIBLE);
            mLayout.findViewById(R.id.SRL_main).bringToFront();
            mLayout.findViewById(R.id.BTN_goto_subscriptions).bringToFront();
        }
    }

    /** Makes the right things visible and such */
    private void resetVisibilities() {
        Log.i(TAG, "resetVisibilities()");
        mLayout.findViewById(R.id.TV_refresh_instr).setVisibility(View.GONE);
        mLayout.findViewById(R.id.TV_no_events).setVisibility(View.GONE);
        mLayout.findViewById(R.id.BTN_goto_subscriptions).setVisibility(View.GONE);
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
