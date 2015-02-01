package evoqe.com.evoqe.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.objects.ToastWrapper;

/**
 * @author Aaron on 1/31/2015.
 */
public class HelpFragment extends Fragment{

    private final static String TAG = "HelpFragment";
    private View mLayout;

    public static HelpFragment newInstance() {
        return new HelpFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
                             Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_help, container, false);
        return mLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mLayout.findViewById(R.id.BTN_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastWrapper.makeText(getActivity(), "There there, it will be alright",
                        Toast.LENGTH_SHORT).show();
            }
        });
        super.onActivityCreated(savedInstanceState);
    }

}
