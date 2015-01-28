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
 * @author Aaron on 1/27/2015.
 */
public class RestaurantDetailActivity extends ActionBarActivity {

    private ParseProxyObject mRestaurant;
    private String TAG = "RestaurantDetailActivity";
    protected static final String RESTAURANT_KEY = "restaurant";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle bundle = this.getIntent().getExtras();
        mRestaurant = (ParseProxyObject) bundle.getSerializable(RESTAURANT_KEY);

        setContentView(R.layout.activity_restaurant_detail);
        setUpActionBar();

        RecyclerView recView = (RecyclerView) findViewById(R.id.recycler_view);
        recView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recView.setLayoutManager(llm);
        recView.setAdapter(new RestaurantDetailAdapter(mRestaurant, this));

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
}
