package evoqe.com.evoqe.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private RecyclerView mLayout;
    
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
        mLayout = (RecyclerView) inflater.inflate(R.layout.recyclerview_basic, container, false);
        return mLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        mLayout.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mLayout.setLayoutManager(llm);

        // set an empty temporary adapter
        JamboreePreviewAdapter adapter = new JamboreePreviewAdapter(getActivity(), new ArrayList<ParseObject>(), mActivity);
        mLayout.setAdapter(adapter);

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

                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e == null) {
                                    JamboreePreviewAdapter adapter = new JamboreePreviewAdapter(getActivity(), objects, mActivity);
                                    mLayout.setAdapter(adapter);
                                    Log.i(TAG, "Jamboree Preview List adapter has been applied");

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
        // ...example params...
        /* "sortBy": "startTime",
         * "initialRetrievalCount": 50,
         * "cutOffTimeDayBeforeInHours": 4, TODO - what?
         * "greaterThanAttribute": "startTime", // attr for time restriction on query TODO - what?
         * "privacy": "PUBLIC",
         * "cachePolicy": "kPFCachePolicyCacheThenNetwork" TODO - what?
         */
        Log.d(TAG, "map = " + params.toString());
        ParseQuery<ParseObject> query = new ParseQuery<>("Jamboree"); // implicit <ParseObject>
        // Only get events that the user has subscribed to
        query.whereContainedIn("owner", mySubscriptions);

        query.orderByDescending(params.get("sortBy").toString());
//                String count = map.get("initialRetrievalCount").toString(); // TODO - casting is weird
//                Log.d(TAG, "count = " + count);
//                query.setLimit(Integer.parseInt(map.get("initialRetrievalCount")));
        query.whereEqualTo("privacy", params.get("privacy"));

        // get events after 24 hours ago
        Long time = new Date().getTime();
        int daysBeforeNow = 1; // 1 days from now
        Date date = new Date(time + daysBeforeNow * (24 * 60 * 60 * 1000));
        query.whereGreaterThan("startTime", date);

        // get events before 7 days from now
        time = new Date().getTime();
        int daysFromNow = 7; // 7 days from now
        date = new Date(time + daysFromNow * (24 * 60 * 60 * 1000));
        query.whereLessThanOrEqualTo("startTime", date);
        Log.d(TAG, "query = " + query.toString());

        return query;
    }
}
