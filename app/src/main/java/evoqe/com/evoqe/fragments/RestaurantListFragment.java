package evoqe.com.evoqe.fragments;

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
import evoqe.com.evoqe.adapters.RestaurantAdapter;

public class RestaurantListFragment extends ListFragment {
    
    private final String TAG = "MuchieListFragment";
    
    public static Fragment newInstance() {
        return new RestaurantListFragment();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {      
        View v = inflater.inflate(R.layout.vertical_listview, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
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
                    // Filling an adapter the Jamborees from Parse
                    ArrayAdapter<ParseObject> adapter = new RestaurantAdapter(getActivity(),
                                         R.layout.list_item_restaurant, objects);
                    
                    // logKeys(objects, "Restaurant");
                    
                    // Actually apply the new data to the ListView
                    setListAdapter(adapter);
                
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