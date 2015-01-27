package evoqe.com.evoqe.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.ViewPagerAdapter;
import evoqe.com.evoqe.tabs.SlidingTabLayout;


public class TabFragment extends Fragment {
    
    private View mLayout;
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingLayout;

    public static TabFragment newInstance() {
        TabFragment frag = new TabFragment();
        return frag;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
    ViewGroup container, @Nullable
    Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_tab, container, false);
        return mLayout;
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        initializePager();
        super.onActivityCreated(savedInstanceState);
    }
    
    /**
     * Set up the ViewPager and sliding tab indicator
     */
    public void initializePager() {
        mViewPager = (ViewPager) mLayout.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), (Activity) getActivity()));
        mSlidingLayout = (SlidingTabLayout) mLayout.findViewById(R.id.sliding_tabs);
        mSlidingLayout.setViewPager(mViewPager);
    }
}
