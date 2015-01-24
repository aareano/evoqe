package evoqe.com.evoqe.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.JamboreeAdapter;

public class JamboreeListFragment extends ListFragment {
    
    private final String TAG = "EventListFragment";
    
    /** To be passed to the adapter for callback puposes */
    private Activity mActivity;
    
    public static Fragment newInstance(Activity activity) {
        JamboreeListFragment frag = new JamboreeListFragment();
        frag.mActivity = activity;
        return frag;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {      
        View v = inflater.inflate(R.layout.vertical_listview, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getEventList();
        super.onActivityCreated(savedInstanceState);
    }
    
    public void getEventList() {
        // query the parse database for all objects under the class "Jamboree"
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Jamboree");
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    // Filling an adapter the Jamborees from Parse
                    ArrayAdapter<ParseObject> adapter = new JamboreeAdapter(getActivity(), 
                                         R.layout.list_item_jamboree, objects, mActivity);
                    
                    // logKeys(objects, "Jamboree");
                    
                    // Actually apply the new data to the ListView
                    setListAdapter(adapter);
                    Log.i(TAG, "Event list adapter has been applied");
                
                } else {
                    Log.e(TAG, "error: " + e.toString());
                }
            }
        });
    }
    
    /**
     * Prints a list of the keys of a list of objects with key-value attributes 
     * @param objects
     * @param className
     */
    public void logKeys(List<ParseObject> objects, String className) {
        Set<String> keys = objects.get(0).keySet();
        Iterator<String> iterKeys = keys.iterator();
        while (iterKeys.hasNext()) {
            String next = iterKeys.next();
            Log.d(TAG, className + " key: '" + next + "'");
        }
    }
}
