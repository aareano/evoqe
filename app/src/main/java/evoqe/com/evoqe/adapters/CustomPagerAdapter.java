package evoqe.com.evoqe.adapters;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import evoqe.com.evoqe.fragments.ErrorFragment;
import evoqe.com.evoqe.fragments.JamboreeListFragment;
import evoqe.com.evoqe.fragments.RestaurantListFragment;

public class CustomPagerAdapter extends FragmentPagerAdapter {
    /** List of fragments to fill the pages */
    @SuppressWarnings("unused")
    private final String TAG = "CustomPagerAdapter";
    
    /** To be passed to the fragments' list adapter for callback purposes */ 
    private Activity mActivity;
    
    public CustomPagerAdapter(FragmentManager fragmentManager, Activity activity) {
        super(fragmentManager);
        mActivity = activity;
    }
    
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) { // Event list
            return "Public Events";
        } else if (position == 1) { // Munchie list
            return "Munchies";
        } else { // should never happen
            return ":D";
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) { // Event list
            return JamboreeListFragment.newInstance(mActivity);
        } else if (position == 1) { // Restaurant list
            return RestaurantListFragment.newInstance();
        } else { // should never happen
            return ErrorFragment.newInstance();
        }
    }

    /**
     * @return the number of pages to display
     */
    @Override
    public int getCount() {
        return 2;
    }
}