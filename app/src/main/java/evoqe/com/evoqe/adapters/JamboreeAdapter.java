package evoqe.com.evoqe.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;

import java.util.List;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.objects.DateTimeParser;

public class JamboreeAdapter extends ArrayAdapter<ParseObject>{

    public interface JamboreeClickListener {
        public void onJamboreeClicked(ParseObject jamboree);
    }
    
    private List<ParseObject> jamborees;
    private Context mContext;
    private int layoutResourceId;
    private JamboreeClickListener mCallback;

    private static String TITLE_KEY;
    private static String HOST_KEY;

    private final String TAG = "JamboreeAdapter";
    
    public JamboreeAdapter(Context context, int layoutResourceId, List<ParseObject> jamborees, Activity activity) {
        super(context, layoutResourceId);
        Log.i(TAG, jamborees.size() + " jamborees");
        
        this.jamborees = jamborees;
        this.mContext = context;
        this.layoutResourceId = layoutResourceId;
        this.mCallback = (JamboreeClickListener) activity;

        // initialize ParseObject Jamboree keys
        TITLE_KEY = mContext.getResources().getString(R.string.title_key);
        HOST_KEY = mContext.getResources().getString(R.string.owner_full_name_key);
    }
    
    @Override
    public int getCount() {
        return jamborees.size();
    }

    /**
     * This handles the construction of every row.
     */
    @Override
    public View getView(final int position, View row, ViewGroup parent) {
        
        // define the row
        if(row == null)
        {   // inflate the layout of the row            
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
        } else {
            // do nothing
        }
        // get the jamboree ParseObject object
        final ParseObject jamboree = jamborees.get(position);
        
        // set an listener for the entire row. onClick >> send the user to an info page for that Jamboree
        row.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "rowJamboreeClick - '" + jamboree.getString(TITLE_KEY) + "'");
                mCallback.onJamboreeClicked(jamboree);
            }
        });
        
        // fill list item's views with Jamboree information
        if (jamboree != null) {
            // title
            TextView title = (TextView) row.findViewById(R.id.TV_title);
            title.setText(jamboree.getString(TITLE_KEY));
            
            // host
            TextView hostName = (TextView) row.findViewById(R.id.TV_host);
            hostName.setText("Hosted by: " + jamboree.getString(HOST_KEY));

            // *** Date and time ***
            TextView date = (TextView) row.findViewById(R.id.TV_date);
            TextView time = (TextView) row.findViewById(R.id.TV_time);
            final String dateString = DateTimeParser.getDays(jamboree.getDate("startTime"),
                    jamboree.getDate("endTime"), false);
            final String timeString = DateTimeParser.getDays(jamboree.getDate("startTime"),
                    jamboree.getDate("endTime"), false);
            date.setText(dateString);
            time.setText(timeString);
            
            // **** Button onClick listeners ****
            Button textAFriend = (Button) row.findViewById(R.id.BTN_text_friend);
            textAFriend.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    try {
                        Log.i(TAG, "Setting up intent to send text.");
                        String sms = "Hey! Check this out - " +
                        		jamborees.get(position).getString(TITLE_KEY) +
                        		" is at " + timeString + " on " + dateString + ".";
                        
                        // set up intent
                        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                        smsIntent.setType("vnd.android-dir/mms-sms");
                        smsIntent.setData(Uri.parse("smsto:"));
                        smsIntent.putExtra("sms_body", sms); 
                        
                        // Verify it resolves
                        PackageManager packageManager = mContext.getPackageManager();
                        List<ResolveInfo> activities = packageManager.queryIntentActivities(smsIntent, 0);
                        boolean isIntentSafe = activities.size() > 0;
                        
                        // Start activity if it is safe
                        if (isIntentSafe) {
                            mContext.startActivity(smsIntent);
                            Log.i(TAG, "Started activity to send text");
                        } else {
                            Log.i(TAG, "Not able to send text");
                            Toast.makeText(mContext, "No available app to send text", 
                                    Toast.LENGTH_LONG).show();
                        }
                    
                    } catch (Exception e) {
                        Toast.makeText(mContext,
                                "Text failed, please try again later!",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
        } else {
            // Problem: no event in the list where there should be one
            Log.e(TAG, "null ParseObject in jamboree query response");
            Toast.makeText(mContext, "Error, that's our bad.",
                    Toast.LENGTH_LONG).show();
        }
        return row;
    }
}
