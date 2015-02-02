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
import android.widget.FrameLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import evoqe.com.evoqe.utilities.ConnectionDetector;
import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.RestaurantPreviewAdapter;
import evoqe.com.evoqe.objects.ToastWrapper;

public class RestaurantPreviewFragment extends Fragment {

    private FrameLayout mLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private final String TAG = "RestaurantPreviewFragment";
    
    public static Fragment newInstance() {
        return new RestaurantPreviewFragment();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {
        mLayout = (FrameLayout) inflater.inflate(R.layout.fragment_restaurant_preview, container, false);
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
        RestaurantPreviewAdapter adapter = new RestaurantPreviewAdapter(getActivity(),
                new ArrayList<ParseObject>());
        recView.setAdapter(adapter);

        // we assume that we're retrieving info from parse, but that will take non-zero time, so in
        // the meantime, we act like we've received nothing.
        nothingRetrieved(false);

        // get the actual data
        getRestaurantList();
        super.onActivityCreated(savedInstanceState);
    }
    
    public void getRestaurantList() {
        // query the parse database for all objects under the class "Restaurant"
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Restaurant");
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> restaurantList, ParseException e) {
                mSwipeRefreshLayout.setRefreshing(false); // remove refreshing notifier
                if (e == null) {
                    if (restaurantList.size() == 0) {
                        nothingRetrieved(false);
                    }
                    resetVisibilities(); // make sure the right things are visible
                    RestaurantPreviewAdapter adapter = new RestaurantPreviewAdapter(getActivity(),
                            (ArrayList<ParseObject>) restaurantList);
                    ((RecyclerView) mLayout.findViewById(R.id.recycler_view)).setAdapter(adapter);
                } else {
                    Log.e(TAG, e.toString());
                    nothingRetrieved(true);
                }
            }
        });
    }

    private void setUpSwipeRefresh() {
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2,
                R.color.green_3, R.color.green_4);
        mSwipeRefreshLayout.setOnRefreshListener (new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isInternetPresent()) {
                    getRestaurantList();
                } else {
                    nothingRetrieved(false);  // set the layout to show no internet
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
                RestaurantPreviewAdapter adapter = new RestaurantPreviewAdapter(getActivity(),
                        new ArrayList<ParseObject>());
                ((RecyclerView) mLayout.findViewById(R.id.recycler_view)).setAdapter(adapter);
            }
            resetVisibilities();
            mLayout.findViewById(R.id.TV_no_restaurants).setVisibility(View.VISIBLE);
            mLayout.findViewById(R.id.TV_refresh_instr).setVisibility(View.VISIBLE);
            mLayout.findViewById(R.id.SRL_main).bringToFront();
        }
    }

    /** Makes the right things visible and such */
    private void resetVisibilities() {
        Log.i(TAG, "resetVisibilities()");
        mLayout.findViewById(R.id.TV_refresh_instr).setVisibility(View.GONE);
        mLayout.findViewById(R.id.TV_no_restaurants).setVisibility(View.GONE);
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