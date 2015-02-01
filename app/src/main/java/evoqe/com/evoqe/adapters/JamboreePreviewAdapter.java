package evoqe.com.evoqe.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;

import java.util.List;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.objects.DateTimeParser;

public class JamboreePreviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<ParseObject> mJamborees;
    private Context mContext;
    private JamboreeClickListener mCallback;
    private static String TITLE_KEY;
    private static String HOST_KEY;
    private static String START_TIME_KEY;
    private static String END_TIME_KEY;
    private static final String TAG = "JamboreePreviewAdapter";
    private int mPosition;

    public interface JamboreeClickListener {
        public void onJamboreeClicked(ParseObject jamboree);
    }

    public JamboreePreviewAdapter(Context context, List<ParseObject> jamborees, Activity activity) {
        Log.i(TAG, jamborees.size() + " jamborees");
        
        mJamborees = jamborees;
        mContext = context;
        mCallback = (JamboreeClickListener) activity;

        // initialize ParseObject Jamboree keys
        TITLE_KEY = mContext.getResources().getString(R.string.title_key);
        HOST_KEY = mContext.getResources().getString(R.string.owner_full_name_key);
        START_TIME_KEY = mContext.getResources().getString(R.string.start_time_key);
        END_TIME_KEY = mContext.getResources().getString(R.string.end_time_key);
    }

    /** Provide a reference to the type of views that you are using (custom ViewHolder) */
    public class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView vTitle;
        protected TextView vHost;
        protected TextView vDate;
        protected TextView vTime;
        protected ImageView vThumbnail;
        protected Button vTextFriend;
        protected View view;
        protected ParseObject currentItem;

        public ViewHolder(View v) {
            super(v);
            vTitle =      (TextView) v.findViewById(R.id.TV_title);
            vThumbnail =  (ImageView) v.findViewById(R.id.IMG_thumbnail);
            vHost =       (TextView) v.findViewById(R.id.TV_host);
            vDate =       (TextView) v.findViewById(R.id.TV_date);
            vTime =       (TextView) v.findViewById(R.id.TV_time);
            vTextFriend = (Button) v.findViewById(R.id.BTN_text_friend);
            view = v;
            // If a user clicks on a row, send them to the detail activity for that event
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.i(TAG, "JamboreePreview onClick, title: '"
                            + currentItem.getString(TITLE_KEY) + "'");
                    mCallback.onJamboreeClicked(currentItem);
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_jamboree_preview, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        // set the current item
        ((ViewHolder) viewHolder).currentItem = mJamborees.get(position);
        prepLayout(viewHolder);
    }

    @Override
    public int getItemCount() {
        return mJamborees.size();
    }

    private void prepLayout(RecyclerView.ViewHolder holder) {
        final ViewHolder viewHolder = (ViewHolder) holder;

        // Title
        viewHolder.vTitle.setText(viewHolder.currentItem.getString(TITLE_KEY));

        // Host
        viewHolder.vHost.setText(viewHolder.currentItem.getString(HOST_KEY));

        // Date
        String dateString;
        dateString = DateTimeParser.getDay(viewHolder.currentItem.getDate(START_TIME_KEY));
        dateString += " " + DateTimeParser.getDate(viewHolder.currentItem.getDate(START_TIME_KEY));
        // multiple day event
        if (DateTimeParser.startsEndsSameDay(viewHolder.currentItem.getDate(START_TIME_KEY),
                viewHolder.currentItem.getDate(END_TIME_KEY)) == false) {
            dateString += " - " + DateTimeParser.getDay(viewHolder.currentItem.getDate(END_TIME_KEY));
            dateString += " " + DateTimeParser.getDate(viewHolder.currentItem.getDate(END_TIME_KEY));
        }
        // adds " (today)" or " (tomorrow)" or "" as appropriate
        dateString += DateTimeParser.getAnnotation(viewHolder.currentItem.getDate(START_TIME_KEY));
        viewHolder.vDate.setText(dateString);
        // Time
        final String dateS = dateString; // for sms use only
        final String timeString = DateTimeParser.getFormattedTime(
                viewHolder.currentItem.getDate(START_TIME_KEY))
                + " - " + DateTimeParser.getFormattedTime(
                viewHolder.currentItem.getDate(END_TIME_KEY));
        viewHolder.vTime.setText(timeString);

        // **** Button onClick listener **** send sms with details about the Jamboree
        viewHolder.vTextFriend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    String sms = "Hey! Check this out - " +
                            viewHolder.currentItem.getString(TITLE_KEY) +
                            " is at " + timeString + " on " + dateS + ".";

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
    }
}
