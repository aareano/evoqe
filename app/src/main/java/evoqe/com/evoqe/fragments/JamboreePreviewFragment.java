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
                // now we have the params to query Jamborees by
                // get the list of users to whom the current user is subscribed
                ParseUser me = ParseUser.getCurrentUser();
                ParseRelation<ParseUser> rel = me.getRelation("subscriptions");

                rel.getQuery().findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> mySubscriptions, ParseException e) {
                        // Now we have the params and the users from whom to pull events, so let's query Parse based on that
                        ParseQuery<ParseObject> query = setUpQuery(mySubscriptions, params);
                        if (query == null) {
                            nothingRetrieved(true);
                            return;
                        }
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                mSwipeRefreshLayout.setRefreshing(false); // remove refreshing notifier
                                if (e == null) {
                                    Log.d(TAG, "jamboree size = " + objects.size());
                                    if (objects.size() == 0) {
                                        nothingRetrieved(false);
                                    } else {
                                        retrievalResolution(); // make sure the right things are visible
                                        JamboreePreviewAdapter adapter = new JamboreePreviewAdapter(getActivity(), objects, mActivity);
                                        ((RecyclerView) mLayout.findViewById(R.id.recycler_view)).setAdapter(adapter);
                                    }
                                } else {
                                    Log.e(TAG, "error: " + e.toString());
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * Constructs a ParseQuery<ParseObject> based on params pulled from the cloud code and the
     * subscriptions of the current user.
     * @param mySubscriptions List<ParseUser> of Users the current user to subscribed to
     * @param params HashMap<String, String> params to retrieve Jamborees based off of
     * @return the query to run
     */
    private ParseQuery<ParseObject> setUpQuery(List<ParseUser> mySubscriptions,
                                               HashMap<String, String> params) {
        if (params == null) {
            return null;
        }
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
        Long time = new Date().getTime();
        int hoursBeforeNow = 12; // 12 hours before now
        Date date = new Date(time + (hoursBeforeNow * 60 * 60 * 1000));
        query.whereGreaterThan("startTime", date);

        // get the next 7 days' events - doesn't work? or don't need it?
//        time = new Date().getTime();
//        int daysFromNow = 7; // 7 days from now
//        date = new Date(time + daysFromNow * (24 * 60 * 60 * 1000));
//        query.whereLessThanOrEqualTo("startTime", date);

        return query;
    }

    private void setUpSwipeRefresh() {
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2,
                R.color.green_3, R.color.green_4);
        mSwipeRefreshLayout.setOnRefreshListener (new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getEventList();
            }
        });
    }

    /** Instructs the user that there was an error ("No events"), and how to refresh
     * Can be called from any of the parse queries in this class.
     * This is called if there actually are no events too
     * @param error - whether there was an error
     */
    private void nothingRetrieved(boolean error) { // TODO - the textviews etc don't show up.
        Log.i(TAG, "nothingRetrieved()");
        if (error) {
            String text = getActivity().getResources().getString(R.string.refresh_error);
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
        }
        mLayout.findViewById(R.id.TV_refresh_instr).setVisibility(View.VISIBLE);
        mLayout.findViewById(R.id.TV_no_events).setVisibility(View.VISIBLE);
        mLayout.findViewById(R.id.BTN_goto_subscriptions).setVisibility(View.VISIBLE);
        mLayout.findViewById(R.id.SRL_main).bringToFront();
//        mLayout.findViewById(R.id.BTN_goto_subscr).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    /** Makes the right things visible and such */
    private void retrievalResolution() {
        Log.i(TAG, "retrievalResolution()");
        mLayout.findViewById(R.id.TV_refresh_instr).setVisibility(View.INVISIBLE);
        mLayout.findViewById(R.id.TV_no_events).setVisibility(View.INVISIBLE);
        mLayout.findViewById(R.id.BTN_goto_subscriptions).setVisibility(View.INVISIBLE);
    }
}