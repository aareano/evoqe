
package evoqe.com.evoqe.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.utilities.ImageHelper;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer. See the <a
 * href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction"> design
 * guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private LinearLayout mLayout;
    private RecyclerView mDrawerRecView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    @SuppressWarnings("unused")
    private String TAG = "NavigationDrawerFragment";

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (LinearLayout) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerRecView = (RecyclerView) mLayout.findViewById(R.id.RV_main);

        // subscription count
        setUpSubscriptionCount();

        // set the user information
        ParseUser user = ParseUser.getCurrentUser();
        setText(R.id.TV_user_name, R.string.full_name_key, user);   // name
        setText(R.id.TV_user_school, R.string.school_key, user);    // school
        setText(R.id.TV_user_year, R.string.year_key, user);        // year ("sophomore")

        // user image
        ParseFile picture = user.getParseFile(getString(R.string.user_picture_key));
        ImageView imgView = (ImageView) mLayout.findViewById(R.id.IMG_thumbnail);
        try {
            byte[] byteArray = picture.getData();
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            Bitmap rounded = ImageHelper.getRoundedCornerBitmap(bmp, 150);
            imgView.setImageBitmap(rounded);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        // set adapter
        mDrawerRecView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mDrawerRecView.setLayoutManager(llm);
        DrawerAdapter adapter = new DrawerAdapter(getActivity(),
                        getResources().getStringArray(R.array.drawer_items));
        mDrawerRecView.setAdapter(adapter);

        return mLayout;
    }

    /**
     * A simple helper method to fill find a set a TextView text in this layout
     * @param textViewId - the id of the TextView to change
     * @param keyId - the (String) resource Id of the key to the ParseUser's appropriate value whose text to put in the TextView
     * @param user - the ParseUser that owns all the values accessed by the key.
     */
    public void setText(int textViewId, int keyId, ParseUser user) {
        TextView tv = (TextView) mLayout.findViewById(textViewId);
        String key = getString(keyId);
        tv.setText((CharSequence) user.get(key));
    }

    public void setUpSubscriptionCount() {
        ParseUser user = ParseUser.getCurrentUser();
        ParseRelation<ParseUser> rel = user.getRelation(
                getString(R.string.subscriptions_key));
        rel.getQuery().countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                TextView tv = (TextView) mLayout.findViewById(R.id.TV_subscription_count);
                tv.setText("" + count);
            }
        });
        mLayout.findViewById(R.id.RL_subscription_wrapper).setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            mLayout.findViewById(R.id.RL_subscription_wrapper)
                                    .setBackgroundColor(getResources().getColor(
                                            R.color.grey_light));
                        } else {
                            mLayout.findViewById(R.id.RL_subscription_wrapper)
                                    .setBackgroundColor(getResources().getColor(
                                            R.color.background_floating_material_dark));
                        }
                    }
                }
        );
        mLayout.findViewById(R.id.RL_subscription_wrapper).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectItem(1); // subscriptions
                    }
                }
        );

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        // As we're using a Toolbar, we should retrieve it and set it
        // to be our ActionBar
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.my_awesome_toolbar);
        ((ActionBarActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // Now set the status bar color. || This only takes effect on Lollipop,
        // or when using translucentStatusBar on KitKat.
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.navigation_drawer_open, /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close /* "close drawer" description for accessibility */
                ) {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        if (!isAdded()) {
                            return;
                        }

                        getActivity().supportInvalidateOptionsMenu(); // calls
                                                                      // onPrepareOptionsMenu()
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                        if (!isAdded()) {
                            return;
                        }

                        if (!mUserLearnedDrawer) {
                            // The user manually opened the drawer; store this flag to prevent
                            // auto-showing
                            // the navigation drawer automatically in the future.
                            mUserLearnedDrawer = true;
                            SharedPreferences sp = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity());
                            sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
                        }

                        getActivity().supportInvalidateOptionsMenu(); // calls
                                                                      // onPrepareOptionsMenu()
                    }
                };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach()");
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar(); // empty method
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        /*if (item.getItemId() == R.id.action_example) {
            Toast.makeText(getActivity(), "Example action.", Toast.LENGTH_SHORT).show();
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        // TODO - once styles and such get nailed down, this is where that happens
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }
    
    
    
    
    /**
     * This adapter simply fills in the list that is part of the navigation drawer.
     * @author Aaron
     */
    class DrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final String[] mItems;
        private String TAG = "DrawerAdapter";
        
        public DrawerAdapter(Context context, String[] items) {
            this.mItems = items;
        }

        /** Provide a reference to the type of views that you are using (custom ViewHolder) */
        public class ViewHolder extends RecyclerView.ViewHolder {
            protected TextView vItem;
            protected View view;
            protected int currentItem;

            public ViewHolder(View v) {
                super(v);
                vItem = (TextView) v.findViewById(R.id.TV_item);
                view = v;

                // If a user clicks on a row, send them to the detail activity for that event
                view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            view.setBackgroundColor(getResources().getColor(R.color.grey_light));
                        } else {
                            view.setBackgroundColor(getResources().getColor(R.color.background_floating_material_dark));
                        }
                    }
                });
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectItem(currentItem);
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.list_item_drawer, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            // set the current item
            ((ViewHolder) viewHolder).currentItem = position;
            prepLayout((ViewHolder) viewHolder);
        }

        @Override
        public int getItemCount() {
            return mItems.length;
        }

        /** Does layout editing, like resizing images and stuff */
        public void prepLayout(ViewHolder viewHolder) {
            // add the text
            viewHolder.vItem.setText(mItems[viewHolder.currentItem]);
            viewHolder.vItem.setTextColor(getResources().getColor(R.color.grey_light));
            viewHolder.vItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            Drawable icon = getResources().getDrawable(R.drawable.menu_logo_green); // default
            switch(viewHolder.currentItem) {
                case 0:
                    // use default for icon
                    int rowHeight = 144; // semi-arbitrary row height
                    ViewGroup.LayoutParams params = viewHolder.view.getLayoutParams();
                    if (params == null) {
                        viewHolder.view.setLayoutParams(new ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT, rowHeight)
                        );
                    } else {
                        params.height = rowHeight;
                    }
                    viewHolder.vItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18); // larger text
                    break;
                case 1:
                    icon = getResources().getDrawable(R.drawable.menu_subscription);
                    break;
                case 2:
                    icon = getResources().getDrawable(R.drawable.menu_promotion);
                    break;
                case 3:
                    icon = getResources().getDrawable(R.drawable.menu_help);
                    break;
                default: // should never happen - error
                    Log.e(TAG , "error in PrepLayout, currentItem string is nonsensical");
                    return;
            }
            // add the icon
            double ratio = ((double) icon.getIntrinsicHeight()) / ((double) icon.getIntrinsicWidth()); // (y/x)
            final double width = 48; // based off of navigation drawer layout
            double height = ratio * width;
            icon.setBounds(0, 0, (int) width, (int) height);
            viewHolder.vItem.setCompoundDrawables(icon, null, null, null);
        }
    }
}
