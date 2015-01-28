package evoqe.com.evoqe.fragments;

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
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.RestaurantPreviewAdapter;

public class RestaurantPreviewFragment extends Fragment {
    
    private RecyclerView mLayout;
    private final String TAG = "RestaurantPreviewFragment";
    
    public static Fragment newInstance() {
        return new RestaurantPreviewFragment();
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
        RestaurantPreviewAdapter adapter = new RestaurantPreviewAdapter(getActivity(),
                new ArrayList<ParseObject>());
        mLayout.setAdapter(adapter);
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
                if (e == null) {
                    // Now use real Restaurant data from Parse
                    RestaurantPreviewAdapter adapter = new RestaurantPreviewAdapter(getActivity(),
                            (ArrayList<ParseObject>) objects);
                    mLayout.setAdapter(adapter);
                } else {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }
}