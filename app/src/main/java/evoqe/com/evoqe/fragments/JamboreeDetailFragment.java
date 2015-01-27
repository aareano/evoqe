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

import com.parse.ParseObject;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.JamboreeDetailAdapter;
import evoqe.com.evoqe.objects.ParseProxyObject;

public class JamboreeDetailFragment extends Fragment {

    private RecyclerView mLayout;
    private ParseProxyObject mJamboree;
    private String TAG = "JamboreeDetailFragment";
    private static final String JAMBOREE_KEY = "jamboree";

    public static JamboreeDetailFragment newInstance(ParseObject jamboree) {
        JamboreeDetailFragment frag = new JamboreeDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(JAMBOREE_KEY, new ParseProxyObject(jamboree));
        frag.setArguments(bundle); // put a Parcelable Jamboree in a bundle
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle bundle = this.getArguments(); // get the Jamboree from the bundle
        mJamboree = (ParseProxyObject) bundle.getSerializable(JAMBOREE_KEY);
        Log.d(TAG, "after bundle, title = " + mJamboree.getString("title"));
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState) {
        mLayout = (RecyclerView) inflater.inflate(R.layout.recyclerview_basic, container, false);
        return mLayout;
    }
    
    @Override
    public void onActivityCreated(@Nullable
    Bundle savedInstanceState) {

        mLayout.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mLayout.setLayoutManager(llm);
        JamboreeDetailAdapter adapter = new JamboreeDetailAdapter(mJamboree, getActivity());
        mLayout.setAdapter(adapter);

        super.onActivityCreated(savedInstanceState);
    }
}
