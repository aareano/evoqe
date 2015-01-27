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
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.JamboreePreviewAdapter;

public class JamboreePreviewFragment extends Fragment {
    
    private final String TAG = "EventListFragment";
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
        // query the parse database for all objects under the class "Jamboree"
        ParseQuery<ParseObject> query = new ParseQuery<>("Jamboree"); // implicit <ParseObject>
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
}
