package evoqe.com.evoqe.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.RestaurantDetailAdapter;
import evoqe.com.evoqe.adapters.RestaurantDetailAdapter.OnRestaurantActionClickListener;
import evoqe.com.evoqe.objects.ParseProxyObject;

/**
 * @author Aaron on 1/27/2015.
 */
public class RestaurantDetailActivity extends ActionBarActivity implements OnRestaurantActionClickListener {

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

    /**
     * Sends user to a new external activity in the local Maps app
     */
    @Override
    public void onClickLocation() {
        // get lat and long
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:<lat>,<long>?q=<lat>,<long>(Label+Name)"));
//        startActivity(intent);
        Toast.makeText(this, "Coming soon", Toast.LENGTH_LONG).show();
    }

    /**
     * Sends user to a new activity with a WebView
     */
    @Override
    public void onClickWebsite() {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_LONG).show();
    }

    /**
     * Sends user to a...?
     */
    @Override
    public void onClickMenu() {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_LONG).show();
    }
}
