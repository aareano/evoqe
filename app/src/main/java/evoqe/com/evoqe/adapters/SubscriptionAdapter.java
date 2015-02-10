package evoqe.com.evoqe.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.objects.AsyncDrawable;
import evoqe.com.evoqe.objects.BitmapWorkerTask;
import evoqe.com.evoqe.objects.ToastWrapper;

/* NOTE: 
 * Users subscribe to other users who have the propertie 
 *      "isPublicHost == true"
 * When a user subscribes to another user, two relations are made on parse.
 *      The subscribing user gains a "subscription" relationship and the 
 *      subscribed to user gains a "subscriber" relationship. 
 * On Parse, the "Host" class is depreciated; here the var name "host" is
 *      used to indicate a user who can be subscribed to.
 */

/**
 * This class inflates the layout for each row and holds an OnClickListener
 * for the checkbox on each row. When one is checked or unchecked, the 
 * image is immediately changed; all changes are saved to parse when fragment is paused
 * NOTE: "Host" here simply means a "User" who can be subscribed to. 
 * @author Aaron
 */
public class SubscriptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /** 
     * A list of Hosts whose associated users it is possible to subscribe to 
     */
    private List<ParseUser> mHosts;
    /** 
     * A list of Users to which the current user is subscribed 
     */
    private List<ParseUser> mMySubs;
    private Context mContext;
    private String TAG = "SubscriptionAdapter";
    private SubscriptionUpdateListener mFragCallback;

    public interface SubscriptionUpdateListener {
        public void updateSubscriptionCount();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_subscription, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        // set the current item
        ((ViewHolder) viewHolder).currentItem = mHosts.get(position);
        prepLayout((ViewHolder) viewHolder);
    }

    @Override
    public int getItemCount() {
        return mHosts.size();
    }

    /**
     * (The only) constructor for the HostAdapter class
     * @param context - The activity context
     * @param hosts - A list of users it is possible to subscribe to
     * @param mySubs - A list of my current subscriptions
     */
    public SubscriptionAdapter(Context context, List<ParseUser> hosts, List<ParseUser> mySubs) {
        
        Log.i(TAG , "Inflating subscription list with " + hosts.size() + 
                " host objects, and " + mySubs.size() + " subscriptions.");
        
        this.mHosts = hosts;
        this.mContext = context;
        this.mMySubs = mySubs;
    }

    /**
     * This handles the construction of every row. 
     * The checkboxes are accounted for here.
     * @param viewHolder
     */
    public void prepLayout(ViewHolder viewHolder) {
        
        // fill list item's views with Host information
        if (viewHolder.currentItem != null) {
            // *** name *** //
            String nameKey = mContext.getResources().getString(R.string.full_name_key);
            String currHostName = viewHolder.currentItem.getString(nameKey);
            if (currHostName == null || currHostName.equals("")) { // make hosts with no name disappear
                ViewGroup.LayoutParams params = viewHolder.view.getLayoutParams();
                params.height = 0;
                viewHolder.view.setLayoutParams(params);
            }
            viewHolder.vName.setText(currHostName);

            // Thumbnail
            loadImageView(viewHolder);

            // *** check box *** //
            // NOTE: checkbox image is changed once Parse is up to date on the change
            checkBoxAndSubscrLogic(viewHolder); // also handles changes in subscription

        } else if (viewHolder.currentItem == null) { // make empty items have height zero
            ViewGroup.LayoutParams params = viewHolder.view.getLayoutParams();
            params.height = 0;
            viewHolder.view.setLayoutParams(params);
        }
    }
    
    /**
     * This changes the check box image immediately, then saves the change 
     * to the instance of the user on disc. Once this fragment is paused (onPause), 
     * changes are saved to Parse. If successfully saved, nothing happens, on failure
     * a toast informs the user.
     */
    private void checkBoxAndSubscrLogic(ViewHolder viewHolder) {
        
        // is the user/host on this row is already a current subscription?
        // loop through m(y)Subscriptions looking a match
        int size = mMySubs.size();
        for (int i = 0; i < size; i++) {
            // objectId match
            String hostId = viewHolder.currentItem.getObjectId(); // this row
            String aUserId = mMySubs.get(i).getObjectId(); // subscription list
            
            // change checkbox image based on equality
            if (hostId != null && aUserId != null && hostId.equals(aUserId)) {
                viewHolder.vCheckBox.setChecked(true);
                setCheckBoxImage(viewHolder, R.drawable.subscription_added);
//                viewHolder.vCheckBox.setButtonDrawable(R.drawable.subscription_added);
                break;  // match! exit for loop
            } else {
                viewHolder.vCheckBox.setChecked(false);
                setCheckBoxImage(viewHolder, R.drawable.subscription_add);
//                viewHolder.vCheckBox.setButtonDrawable(R.drawable.subscription_add);
            }
        }
        
        /* The checkbox/subscription logic
         * NOTE: changes are immediately pushed to Parse */
        setUpCheckBoxResponses(viewHolder);
    }

    private void setUpCheckBoxResponses(final ViewHolder viewHolder) {
        viewHolder.vCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View checkBox) {
                ParseUser me = ParseUser.getCurrentUser();
                String subKey = mContext.getResources().getString(R.string.subscriptions_key);
                ParseRelation<ParseObject> rel = me.getRelation(subKey);

                // was the box just checked or unchecked?
                final boolean checked = ((CheckBox) checkBox).isChecked();
                if (checked) { // was just checked, so add subscription
                    mMySubs.add(viewHolder.currentItem);
                    setCheckBoxImage(viewHolder, R.drawable.subscription_added);
                } else { // was just unchecked, so remove subscription
                    int size = mMySubs.size();
                    for (int i = 0; i < size; i++) { // loop through list to find and destroy
                        if (mMySubs.get(i).getString("username").equals(viewHolder.currentItem.getString("username"))) {
                            mMySubs.remove(i);
                            break;
                        }
                    }
                    setCheckBoxImage(viewHolder, R.drawable.subscription_add);
                }
                saveChanges(viewHolder, checked);
            }
        });
    }

    private void saveChanges(final ViewHolder viewHolder, final boolean checked) {
        String funcName;
        if (checked) {
            funcName = "addSubscription";
        } else {
            funcName = "removeSubscription";
        }
        // do the actual adding or remove on Parse
        HashMap<String, String> map = new HashMap<>(); // implicit <String, String>
        map.put("objectId", viewHolder.currentItem.getObjectId());
        ParseCloud.callFunctionInBackground(funcName, map, // does all relation logic
                new FunctionCallback<String>() {
                    @Override
                    public void done(String response, ParseException e) {
                        Log.d(TAG, "RESPONSE: " + response
                                + " (Host objectId: " + viewHolder.currentItem.getObjectId() + ")");
                        if (e == null) { // success!
                            // update subscription count in NavigationDrawer
                            ((SubscriptionUpdateListener) mContext).updateSubscriptionCount();
                            if (checked) { // was added
                                ToastWrapper.makeText(mContext, "Subscription added",
                                        Toast.LENGTH_SHORT).show();
                            } else { // was removed
                                ToastWrapper.makeText(mContext, "Subscription removed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else { // failure!
                            Log.e(TAG, e.toString());
                            if (checked) { // was NOT added
                                ToastWrapper.makeText(mContext, "Unable to add subscription",
                                        Toast.LENGTH_SHORT).show();
                            } else {  // was NOT removed
                                ToastWrapper.makeText(mContext, "Unable to remove subscription",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    public void loadImageView(ViewHolder viewHolder) {
        ParseFile picFile = viewHolder.currentItem.getParseFile(
                mContext.getString(R.string.user_picture_key));
        Bitmap placeHolderBitmap = null;

        if (picFile == null) {
            return;
        }

        //  getData() throws ParseException
        if (BitmapWorkerTask.cancelPotentialWork(viewHolder.vThumbnail, picFile)) {
            final int height = viewHolder.vThumbnail.getHeight();
            final int width = viewHolder.vThumbnail.getWidth();
            final BitmapWorkerTask task = new BitmapWorkerTask(viewHolder.vThumbnail, mContext,
                    width, height);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mContext.getResources(), placeHolderBitmap, task);
            viewHolder.vThumbnail.setImageDrawable(asyncDrawable);
            task.execute(picFile);
        }
    }

    /**
     * Sets the checkbox image. Is a separate method to allow for resizing.
     * @param viewHolder
     * @param drawableResourceId
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setCheckBoxImage(ViewHolder viewHolder, int drawableResourceId) {
        Drawable icon = mContext.getResources().getDrawable(drawableResourceId);
        int width = 20;
        int height = 20;
        icon.setBounds(0, 0, width, height);
        viewHolder.vCheckBox.setBackground(icon);
        viewHolder.vCheckBox.setButtonDrawable(null);
    }

    /** Provide a reference to the type of views that you are using (custom ViewHolder) */
    public class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected ImageView vThumbnail;
        protected CheckBox vCheckBox;
        protected ParseUser currentItem;
        protected View view;

        public ViewHolder(View v) {
            super(v);
            vName =      (TextView) v.findViewById(R.id.TV_name);
            vThumbnail = (ImageView) v.findViewById(R.id.IMG_thumbnail);
            vCheckBox =  (CheckBox) v.findViewById(R.id.CB_main);
            view = v;
        }
    }
}
