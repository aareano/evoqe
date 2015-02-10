package evoqe.com.evoqe.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.objects.ParseProxyObject;
import evoqe.com.evoqe.utilities.DateTimeParser;
import evoqe.com.evoqe.utilities.ImageHelper;

/**
 * Provide views to RecyclerView with data.
 * There will be two cards inflated (three, if we figure out what pictures go in the third one.
 * The first contains Details on the even (title, host, time/date, location, etc).
 * The second holds buttons that allow the user to take certain actions, like email the host, or
 *     save the event to their calendar.
 */
public class JamboreeDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnClickWeatherListener {
        public void onClickWeather();
    }

    private static String TITLE_KEY;
    private static String DETAILS_KEY;
    private static String LOCATION_KEY;
    private static String HOST_KEY;
    private static String OWNER_KEY;
    private static String PRIVACY_KEY;
    private static String START_TIME_KEY;
    private static String END_TIME_KEY;
    private static final String OBJECT_ID_KEY = "objectId";
    private static final String TAG = "JamboreeDetailAdapter";
    private final int NUM_OF_CARDS = 3;
    private static ParseProxyObject mJamboree; // only static so it can be used in enum Card
    private static Context mContext; // only static so it can be used in enum Card

    /** Enum used to take the appropriate layout actions based on the card type. */
    private static enum Card {
        DETAILS {
            /**
             * Get and return the appropriate ViewHolder for this card.
             */
            public ViewHolder_Details getViewHolder(ViewGroup viewGroup) {
                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_jamboree_detail, viewGroup, false);
                return new ViewHolder_Details(v);
            }

            /**
             * Makes edits custom to this card, like calling setText() on TextViews and such.
             */
            public void prepLayout(RecyclerView.ViewHolder vHolder) {
                final ViewHolder_Details viewHolder = (ViewHolder_Details) vHolder;

                // Thumbnail
                loadImageView(viewHolder);
                // Title
                viewHolder.vTitle.setText(mJamboree.getString(TITLE_KEY));
                // Host
                viewHolder.vHost.setText(mJamboree.getString(HOST_KEY));
                // Publicity (e.g. "Public Event") and school (e.g. "Tufts")
                viewHolder.vPrivacy.setText(mJamboree.getString(PRIVACY_KEY));

                // Date (e.g. "Wednesday 4/12/14") and Time (e.g. "4:30pm - 6:30pm")
                String dateString;
                dateString = DateTimeParser.getDay(mJamboree.getDate(START_TIME_KEY));
                dateString += " " + DateTimeParser.getDate(mJamboree.getDate(START_TIME_KEY));
                // multiple day event
                if (DateTimeParser.startsEndsSameDay(mJamboree.getDate(START_TIME_KEY),
                        mJamboree.getDate(END_TIME_KEY)) == false) {
                    dateString += " - " + DateTimeParser.getDay(mJamboree.getDate(END_TIME_KEY));
                    dateString += " " + DateTimeParser.getDate(mJamboree.getDate(END_TIME_KEY));
                }
                // adds " (today)" or " (tomorrow)" or "" as appropriate
                dateString += DateTimeParser.getAnnotation(mJamboree.getDate(START_TIME_KEY));
                viewHolder.vDate.setText(dateString);
                // Time
                final String dateS = dateString; // for sms use only
                final String timeString = DateTimeParser.getFormattedTime(
                        mJamboree.getDate(START_TIME_KEY))
                        + " - " + DateTimeParser.getFormattedTime(
                        mJamboree.getDate(END_TIME_KEY));
                viewHolder.vTime.setText(timeString);

                // Description
                String description = mJamboree.getString(DETAILS_KEY);
                if (description == null || description.equals("")) {
                    description = mContext.getResources().getString(R.string.no_description);
                }
                viewHolder.vDescription.setText(description);
                // Location (e.g. "President's lawn")
                viewHolder.vLocation.setText(mJamboree.getString(LOCATION_KEY));

                // Subscribe to host button
                    // check if user is already subscribed
                final ParseProxyObject host = mJamboree.getParseUser(OWNER_KEY);
                final HashMap<String, String> map = new HashMap<>(); // implicit <String, String>
                map.put(OBJECT_ID_KEY, host.getString(OBJECT_ID_KEY));
                Log.d(TAG, "host ID = " + host.getString(OBJECT_ID_KEY));
                ParseCloud.callFunctionInBackground("isSubscribed", map, // TODO - Cloud Code doesn't work?
                    new FunctionCallback<Boolean>() {
                        @Override
                        public void done(Boolean response, ParseException e) {
                            Log.d(TAG, "RESPONSE to 'isSubscribed' = " + response.toString());
                            if (e == null) { // successfully determined current subscription status!
                                if (response.booleanValue()) {
                                    turnSubButtonOff(viewHolder.vSubscribe);
                                }
                            } else { // failure to determine current subscription status!
                                Log.e(TAG, e.toString());
                            }
                        }
                    });
                viewHolder.vSubscribe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    ParseCloud.callFunctionInBackground("addSubscription", map, // does all relation logic
                        new FunctionCallback<String>() {
                            @Override
                            public void done(String response, ParseException e) {
                                Log.d(TAG, "RESPONSE to 'addSubscription' = " + response + " (Host objectId: " + host.getString(OBJECT_ID_KEY) + ")");
                                if (e == null) { // successfully subscribed!
                                    Toast.makeText(mContext, "Subscription added", Toast.LENGTH_SHORT).show();
                                    turnSubButtonOff(viewHolder.vSubscribe);
                                } else { // failure to subscribe!
                                    Log.e(TAG, e.toString());
                                    Toast.makeText(mContext, "Unable to add subscription", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        },
        ACTIONS {
            /**
             * Get and return the appropriate ViewHolder for this card.
             */
            public ViewHolder_Actions getViewHolder(ViewGroup viewGroup) {
                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_jamboree_action, viewGroup, false);
                return new ViewHolder_Actions(v);
            }

            /**
             * Makes edits custom to this card, like calling setText() on TextViews and such.
             */
            public void prepLayout(RecyclerView.ViewHolder vHolder) {
                final ViewHolder_Actions viewHolder = (ViewHolder_Actions) vHolder;

                setButtonBackground(viewHolder.vWeather, R.drawable.event_weather);
                viewHolder.vWeather.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // callback to JamboreeDetailActivity >> starts new activity with a WebView layout
                        ((OnClickWeatherListener) mContext).onClickWeather();
                    }
                });

                setButtonBackground(viewHolder.vAddToCal, R.drawable.event_add_to_cal);
                viewHolder.vAddToCal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addEventToCalendar();
                    }
                });

                setButtonBackground(viewHolder.vShare, R.drawable.event_share);
                viewHolder.vShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareEvent();
                    }
                });

                setButtonBackground(viewHolder.vEmailHost, R.drawable.event_email_host);
                viewHolder.vEmailHost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] TO = {(mJamboree.getParseUser(OWNER_KEY)).getString("email")};
                        String subject = mJamboree.getString(TITLE_KEY);
                        sendEmail(TO, subject);
                    }
                });

                setButtonBackground(viewHolder.vBuyTickets, R.drawable.event_tickets);
                viewHolder.vBuyTickets.setClickable(false);
                viewHolder.vBuyTickets.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, "Tickets not required for this event", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, REPORT {
            @Override
            public RecyclerView.ViewHolder getViewHolder(ViewGroup viewGroup) {
                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_jamboree_report, viewGroup, false);
                return new ViewHolder_Report(v);
            }

            @Override
            public void prepLayout(RecyclerView.ViewHolder vHolder) {
                ViewHolder_Report viewHolder = (ViewHolder_Report) vHolder;
                viewHolder.vReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] TO = { mContext.getString(R.string.master_email) };
                        String subject = "Report: " + mJamboree.getString(TITLE_KEY);
                        sendEmail(TO, subject);
                    }
                });
            }
        };
        // PHOTOS {
            /**
             * Get and return the appropriate ViewHolder for this card.
             *
            public ViewHolder_3 getViewHolder(ViewGroup viewGroup) {
                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_jamboree_info_3, viewGroup, false);
                return new ViewHolder_3(v);
            }
             */
        // };
        public abstract RecyclerView.ViewHolder getViewHolder(ViewGroup viewGroup);
        public abstract void prepLayout(RecyclerView.ViewHolder viewHolder);
    }

    public JamboreeDetailAdapter(ParseProxyObject jamboree, Context context) {
        mJamboree = jamboree;
        mContext = context;

        // initialize ParseObject Jamboree keys
        TITLE_KEY = mContext.getResources().getString(R.string.title_key);
        DETAILS_KEY = mContext.getResources().getString(R.string.details_key);
        LOCATION_KEY = mContext.getResources().getString(R.string.location_key);
        HOST_KEY = mContext.getResources().getString(R.string.owner_full_name_key);
        OWNER_KEY = mContext.getResources().getString(R.string.owner_key);
        PRIVACY_KEY = mContext.getResources().getString(R.string.privacy_key);
        START_TIME_KEY = mContext.getResources().getString(R.string.start_time_key);
        END_TIME_KEY = mContext.getResources().getString(R.string.end_time_key);
    }

    private static void loadImageView(ViewHolder_Details viewHolder) {
        byte[] byteArray = mJamboree.getParseFile(
                mContext.getString(R.string.jamboree_picture_key));
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        Bitmap rounded = ImageHelper.getRoundedCornerBitmap(bmp); // TODO - put on worker thread
        viewHolder.vThumbnail.setImageBitmap(rounded);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void setButtonBackground(Button button, int drawableResourceId) {
        Drawable icon = mContext.getResources().getDrawable(drawableResourceId);
        double ratio = ((double) icon.getIntrinsicWidth()) / ((double) icon.getIntrinsicHeight()); // (x/y)
        final double height = 88; // based off of navigation drawer layout
        double width = ratio * height;
        Log.d(TAG, "ratio = " + ratio + ", height = 48, width = " + width);
        icon.setBounds(0, 0, (int) width, (int) height);
        button.setCompoundDrawables(null, icon, null, null);
    }

    /**
     * Uses the Calendar Content Provider to start an activity to add the event to the user's calendar
     */
    private static void addEventToCalendar() {
            Calendar startTime = Calendar.getInstance();
            startTime.setTimeInMillis(mJamboree.getDate(START_TIME_KEY).getTime());
            Calendar endTime = Calendar.getInstance();
            endTime.setTimeInMillis(mJamboree.getDate(END_TIME_KEY).getTime());
            Intent calIntent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, mJamboree.getString(TITLE_KEY))
                    .putExtra(CalendarContract.Events.DESCRIPTION, mJamboree.getString(DETAILS_KEY))
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, mJamboree.getString(LOCATION_KEY))
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_TENTATIVE);

            // Verify it resolves
            boolean isIntentSafe = intentIsSafe(calIntent);
        try {
            // Start activity if it is safe
            if (isIntentSafe) {
                mContext.startActivity(calIntent);
                Log.i(TAG, "Started activity to save event to calendar");
            } else {
                Log.i(TAG, "No app available to save event");
                Toast.makeText(mContext, "No app available to save event",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
                Toast.makeText(mContext, "Failed to add event, try again later",
                Toast.LENGTH_SHORT).show();
                e.printStackTrace();
        }
    }

    private static void turnSubButtonOff(Button button) {
        button.setClickable(false);
        button.setBackgroundColor(mContext.getResources().getColor(R.color.grey_light));
        button.setTextColor(mContext.getResources().getColor(R.color.grey_dark));
        button.setText(mContext.getResources().getString(R.string.already_subscribed));
    }

    /**
     * Sends simple text to another application
     */
    private static void shareEvent() {
        String dateString = DateTimeParser.getDay(mJamboree.getDate(START_TIME_KEY));
        dateString += " " + DateTimeParser.getDate(mJamboree.getDate(START_TIME_KEY));
        String timeString = DateTimeParser.getFormattedTime(mJamboree.getDate(START_TIME_KEY));
        String annotation = DateTimeParser.getAnnotation(mJamboree.getDate(START_TIME_KEY));

        // Message to send to another application
        // e.g. "Hot Chocolate Bar at 12:00pm on Friday 1/30/15 (tomorrow), at Meyer's Campus Center."
        String text = mJamboree.getString(TITLE_KEY) +
                " at " + timeString + " on " + dateString + annotation + ", at " + mJamboree.getString(LOCATION_KEY);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        // Verify it resolves
        boolean isIntentSafe = intentIsSafe(sendIntent);
        try {
            // Start activity if it is safe
            if (isIntentSafe) {
                mContext.startActivity(sendIntent);
                Log.i(TAG, "Started activity to send Jamboree information");
            } else {
                Log.i(TAG, "No app available to send Jamboree information");
                Toast.makeText(mContext, "No app available to share event",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Cannot share, try again later",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Sends simple text to another application
     */
    private static void sendEmail(String[] TO, String subject) {
        if ( TO[0] == null || TO[0].equals("")) {
            Toast.makeText(mContext, "No email available", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello,\n\n");

        // Verify it resolves
        boolean isIntentSafe = intentIsSafe(emailIntent);
        try {
            // Start activity if it is safe
            if (isIntentSafe) {
                mContext.startActivity(emailIntent);
                Log.i(TAG, "Started activity to email Jamboree owner");
            } else {
                Log.i(TAG, "No app available to email Jamboree owner");
                Toast.makeText(mContext, "No app available to email with",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Failed to create email, try again later",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private static boolean intentIsSafe(Intent intent) {
        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return activities.size() > 0;
    }


    /* Everything below here is for card construction */

    /**
     * Size of the data set (invoked by the layout manager)
     * This adapter is only doing a single card, so the size is always 1.
     */
    @Override
    public int getItemCount() {
        return NUM_OF_CARDS;
    }

    @Override
    public int getItemViewType(int position) {
        // Unlike ListView adapters, types need not be contiguous
        return position;
    }

    // Create new views (invoked by the layout manager)
    @Override   
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return Card.values()[viewType].getViewHolder(viewGroup);
    }
 
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Card.values()[position].prepLayout(viewHolder);
    }

    /** Provide a reference to the type of views that you are using (custom ViewHolder)
     * This is for the first card - Details */
    public static class ViewHolder_Details extends RecyclerView.ViewHolder {
        protected TextView vTitle;
        protected ImageView vThumbnail;
        protected TextView vHost;
        protected TextView vPrivacy;
        protected TextView vDate;
        protected TextView vTime;
        protected TextView vDescription;
        protected TextView vLocation;
        protected Button   vSubscribe;

        public ViewHolder_Details(View v) {
            super(v);
            vTitle =       (TextView) v.findViewById(R.id.TV_title);
            vThumbnail =   (ImageView) v.findViewById(R.id.thumbnail);
            vHost =        (TextView) v.findViewById(R.id.TV_host);
            vPrivacy =     (TextView) v.findViewById(R.id.TV_publicity_school);
            vDate =        (TextView) v.findViewById(R.id.TV_date);
            vTime =        (TextView) v.findViewById(R.id.TV_time);
            vDescription = (TextView) v.findViewById(R.id.TV_description);
            vLocation =    (TextView) v.findViewById(R.id.TV_location);
            vSubscribe =   (Button) v.findViewById(R.id.BTN_subscribe);
        }
    }

    /** custom ViewHolder for the second card - Actions */
    public static class ViewHolder_Actions extends RecyclerView.ViewHolder {
        protected Button vWeather;
        protected Button vAddToCal;
        protected Button vShare;
        protected Button vEmailHost;
        protected Button vBuyTickets;

        public ViewHolder_Actions(View v) {
            super(v);
            vWeather =    (Button) v.findViewById(R.id.BTN_weather);
            vAddToCal =   (Button) v.findViewById(R.id.BTN_add_to_cal);
            vShare =      (Button) v.findViewById(R.id.BTN_share);
            vEmailHost =  (Button) v.findViewById(R.id.BTN_email_host);
            vBuyTickets = (Button) v.findViewById(R.id.BTN_buy_tickets);
        }
    }

    /** custom ViewHolder for the second card - Actions */
    public static class ViewHolder_Report extends RecyclerView.ViewHolder {
        protected Button vReport;

        public ViewHolder_Report(View v) {
            super(v);
            vReport = (Button) v.findViewById(R.id.BTN_report);
        }
    }
}
