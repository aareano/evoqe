package evoqe.com.evoqe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.JamboreeDetailAdapter;
import evoqe.com.evoqe.objects.ParseProxyObject;

/**
 * Created by Aaron on 1/26/2015.
 */
public class JamboreeDetailActivity extends ActionBarActivity implements JamboreeDetailAdapter.OnClickWeatherListener {

    private ParseProxyObject mJamboree;
    private String TAG = "JamboreeDetailActivity";
    protected static final String JAMBOREE_KEY = "jamboree";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle bundle = this.getIntent().getExtras();
        mJamboree = (ParseProxyObject) bundle.getSerializable(JAMBOREE_KEY);

        setContentView(R.layout.activity_jamboree_detail);
        setUpActionBar();

        RecyclerView recView = (RecyclerView) findViewById(R.id.recycler_view);
        recView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recView.setLayoutManager(llm);
        recView.setAdapter(new JamboreeDetailAdapter(mJamboree, this));

        super.onCreate(savedInstanceState);
    }

    private void setUpActionBar() {
        // As we're using a Toolbar, we should retrieve it and set it
        // to be our ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    /** Start new activity with a WebView to show the weather */
    @Override
    public void onClickWeather() {
        Log.d(TAG, "onClickWeather()");
        Intent intent = new Intent(JamboreeDetailActivity.this, WeatherActivity.class);
        // Pass the Jamboree so that it can be passed back when the user navigates back to this activity
        intent.putExtra(JamboreeDetailActivity.JAMBOREE_KEY, mJamboree);
        startActivity(intent);
    }
}
