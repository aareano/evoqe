package evoqe.com.evoqe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.parse.ParseObject;
import com.parse.ParseUser;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.adapters.JamboreePreviewAdapter.JamboreeClickListener;
import evoqe.com.evoqe.adapters.RestaurantPreviewAdapter.RestaurantClickListener;
import evoqe.com.evoqe.adapters.SubscriptionAdapter;
import evoqe.com.evoqe.fragments.HelpFragment;
import evoqe.com.evoqe.fragments.NavigationDrawerFragment;
import evoqe.com.evoqe.fragments.PromotionFragment;
import evoqe.com.evoqe.fragments.SubscriptionFragment;
import evoqe.com.evoqe.fragments.TabFragment;
import evoqe.com.evoqe.objects.ParseProxyObject;

/* TODO - only load 15 Hosts at a time (low priority)
 * TODO - sometimes activity context from fragments is null. weird.
 * TODO - images (circle)
 * TODO - styles
 * TODO - change nav drawer ListViews to RecyclerViews (low priority)
 * TODO - Login page
 *
 * Issues:
 *     Initial subscription check for JamboreeDetailActivity - Cloud code doesn't work?
 */

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        JamboreeClickListener, RestaurantClickListener,
        SubscriptionAdapter.SubscriptionUpdateListener {

    private final String TAG = "MainActivity";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.i(TAG, "position " + position + " selected on drawer");

        if (position == 0) {  // home
            replaceFragment(R.id.container, (Fragment) TabFragment.newInstance(), "TabFragment");
        } else if (position == 1) { // subscriptions
            replaceFragment(R.id.container, (Fragment) SubscriptionFragment.newInstance(), "SubscriptionFragment");
        } else if (position == 2) { // promotions
            replaceFragment(R.id.container, (Fragment) PromotionFragment.newInstance(), "PromotionFragment");
        } else if (position == 3) { // help
            replaceFragment(R.id.container, (Fragment) HelpFragment.newInstance(), "HelpFragment");
        }
    }

    public void replaceFragment(int resourceLayoutId, Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) { // only replace the fragment if we're
            fragmentManager.beginTransaction()                // navigating away from the current one
                    .replace(resourceLayoutId, fragment, tag)
                    .commit();
        }
    }

    public void restoreActionBar() {
        // for global styles and stuff, not critical.
        /*
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
        */
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onJamboreeClicked(ParseObject jamboree) {
        Intent intent = new Intent(MainActivity.this, JamboreeDetailActivity.class);
        intent.putExtra(JamboreeDetailActivity.JAMBOREE_KEY, new ParseProxyObject(jamboree));
        startActivity(intent);
    }

    @Override
    public void onRestaurantClicked(ParseObject restaurant) {
        Intent intent = new Intent(MainActivity.this, RestaurantDetailActivity.class);
        intent.putExtra(RestaurantDetailActivity.RESTAURANT_KEY, new ParseProxyObject(restaurant));
        startActivity(intent);
    }

    @Override
    public void updateSubscriptionCount() {
        mNavigationDrawerFragment.setText(R.id.TV_subscription_count, R.string.subscriptions_key,
                ParseUser.getCurrentUser());   // subscription count
    }

    /** Receives onClick events from BTN_goto_subscriptions when no events to show */
    public void onSubscriptionButtonClick(View view) {
        Log.d(TAG, "button clicked");
        onNavigationDrawerItemSelected(1); // TODO - should go through the nav drawer to be proper?
    }
}
