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
import android.widget.RelativeLayout;

import com.parse.ParseObject;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.JamboreeInfoAdapter_1;
import evoqe.com.evoqe.objects.Jamboree;

public class JamboreeInfoFragment extends Fragment {

    private RelativeLayout mLayout;
    private ParseObject mJamboree;
    private String TAG = "JamboreeInfoFragment";

    public static JamboreeInfoFragment newInstance(ParseObject jamboree) {
        JamboreeInfoFragment frag = new JamboreeInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("jamboree", new Jamboree(jamboree));
        frag.setArguments(bundle); // put a Parcelable Jamboree in a bundle
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle bundle = this.getArguments(); // get the Jamboree from the bundle
        Jamboree jam = (Jamboree) bundle.getParcelable("jamboree");
        mJamboree = jam.getJamboree(); // get the actual (non-parcelable) jamboree
        Log.d(TAG, "after bundle, title = " + mJamboree.getString("title"));
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_jamboree_info, container, false);
        return mLayout;
    }
    
    @Override
    public void onActivityCreated(@Nullable
    Bundle savedInstanceState) {
        
        RecyclerView recList = (RecyclerView) mLayout.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        JamboreeInfoAdapter_1 adapter = new JamboreeInfoAdapter_1(mJamboree, getActivity());
        recList.setAdapter(adapter);

        super.onActivityCreated(savedInstanceState);
    }
}
