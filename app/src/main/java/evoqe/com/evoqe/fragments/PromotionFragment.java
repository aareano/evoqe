package evoqe.com.evoqe.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import evoqe.com.evoqe.R;

/**
 * @author Aaron on 1/31/2015.
 */
public class PromotionFragment extends Fragment {

    @SuppressWarnings("UnusedDeclaration")
    private final static String TAG = "PromotionFragment";
    private View mLayout;

    public static PromotionFragment newInstance() {
        return new PromotionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
    ViewGroup container, @Nullable
                             Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_promotion, container, false);
        return mLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // TODO
        super.onActivityCreated(savedInstanceState);
    }

}
