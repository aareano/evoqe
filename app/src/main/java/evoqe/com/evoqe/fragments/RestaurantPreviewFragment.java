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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.RestaurantPreviewAdapter;

public class RestaurantPreviewFragment extends Fragment {

    private SwipeRefreshLayout mLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout; // actually the same object as mLayout...
    private final String TAG = "RestaurantPreviewFragment";
    
    public static Fragment newInstance() {
        return new RestaurantPreviewFragment();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {
        mLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_restaurant_preview, container, false);
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
        // get the actual data
        getRestaurantList();
        super.onActivityCreated(savedInstanceState);
    }
    
    public void getRestaurantList() {
        // query the parse database for all objects under the class "Restaurant"
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Restaurant");
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                mSwipeRefreshLayout.setRefreshing(false); // remove refreshing notifier
                if (e == null) {
                    // Now use real Restaurant data from Parse
                    RestaurantPreviewAdapter adapter = new RestaurantPreviewAdapter(getActivity(),
                            (ArrayList<ParseObject>) objects);
                    ((RecyclerView) mLayout.findViewById(R.id.recycler_view)).setAdapter(adapter);
                } else {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

    private void setUpSwipeRefresh() {
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2, R.color.green_3, R.color.green_4);
        mSwipeRefreshLayout.setOnRefreshListener (new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRestaurantList();
            }
        });
    }
}