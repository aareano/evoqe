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
import evoqe.com.evoqe.adapters.JamboreeInfoAdapter;
import evoqe.com.evoqe.objects.ParseProxyObject;

public class JamboreeInfoFragment extends Fragment {

    private RelativeLayout mLayout;
    private ParseProxyObject mJamboree;
    private String TAG = "JamboreeInfoFragment";
    private static final String JAMBOREE_KEY = "jamboree";

    public static JamboreeInfoFragment newInstance(ParseObject jamboree) {
        JamboreeInfoFragment frag = new JamboreeInfoFragment();
        Bundle bundle = new Bundle();
        // bundle.putParcelable("jamboree", new Jamboree(jamboree));
        bundle.putSerializable(JAMBOREE_KEY, new ParseProxyObject(jamboree));
        frag.setArguments(bundle); // put a Parcelable Jamboree in a bundle
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle bundle = this.getArguments(); // get the Jamboree from the bundle
        // Jamboree jam = (Jamboree) bundle.getParcelable(JAMBOREE_KEY);
        mJamboree = (ParseProxyObject) bundle.getSerializable(JAMBOREE_KEY);
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
        JamboreeInfoAdapter adapter = new JamboreeInfoAdapter(mJamboree, getActivity());
        recList.setAdapter(adapter);

        super.onActivityCreated(savedInstanceState);
    }
}
