package evoqe.com.evoqe.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.objects.DateTimeParser;
import evoqe.com.evoqe.objects.ParseProxyObject;

/**
 * @author Aaron on 1/28/2015.
 */
public class RestaurantDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected static String NAME_KEY;
    protected static String DISCOUNTS_KEY;
    protected static String DETAILS_KEY;
    private static ParseProxyObject mRestaurant;
    private static Context mContext;
    private int NUM_OF_CARDS = 3;

    public interface OnRestaurantActionClickListener {
        public void onClickLocation();
        public void onClickWebsite();
        public void onClickMenu();
    }

    public RestaurantDetailAdapter(ParseProxyObject restaurant, Context context) {
        mRestaurant = restaurant;
        mContext = context;

        NAME_KEY = mContext.getResources().getString(R.string.name_key);
        DISCOUNTS_KEY = mContext.getResources().getString(R.string.discounts_key);
        DETAILS_KEY = mContext.getResources().getString(R.string.details_key);
    }

    /** Enum used to take the appropriate layout actions based on the card type. */
    private static enum Card {
        DETAILS {
            /**
             * Get and return the appropriate ViewHolder for this card.
             */
            public ViewHolder_Details getViewHolder(ViewGroup viewGroup) {
                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_restaurant_detail, viewGroup, false);
                return new ViewHolder_Details(v);
            }

            /**
             * Makes edits custom to this card, like calling setText() on TextViews and such.
             */
            public void prepLayout(RecyclerView.ViewHolder vHolder) {
                final ViewHolder_Details viewHolder = (ViewHolder_Details) vHolder;

                // Name
                viewHolder.vName.setText(mRestaurant.getString(NAME_KEY));

                // Details
                viewHolder.vDetails.setText(mRestaurant.getString(DETAILS_KEY));

                // Open-Close times table
                setUpTable(viewHolder);
            }
        },
        DISCOUNTS {
            /**
             * Get and return the appropriate ViewHolder for this card.
             */
            public ViewHolder_Discounts getViewHolder(ViewGroup viewGroup) {
                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_restaurant_discounts, viewGroup, false);
                return new ViewHolder_Discounts(v);
            }
            /**
             * Makes edits custom to this card, like calling setText() on TextViews and such.
             */
            public void prepLayout(RecyclerView.ViewHolder vHolder) {
                final ViewHolder_Discounts viewHolder = (ViewHolder_Discounts) vHolder;

                // Header -    defaults to "Discounts"
                // Discounts - defaults to "- None at this time"
                // Footnote -  defaults to ""
                String disc = mRestaurant.getString(DISCOUNTS_KEY);
                if (disc == null || disc.equals("")) {
                    // no discounts, do nothing
                } else {
                    viewHolder.vDiscounts.setText("- " + mRestaurant.getString(DISCOUNTS_KEY) + "*"); // TODO - format?
                    viewHolder.vFootnote.setText(mContext.getResources().getString(R.string.discount_footnote));
                }
            }
        },
        ACTIONS {
            /**
             * Get and return the appropriate ViewHolder for this card.
             */
            public ViewHolder_Actions getViewHolder(ViewGroup viewGroup) {
                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_restaurant_actions, viewGroup, false);
                return new ViewHolder_Actions(v);
            }
            /**
             * Makes edits custom to this card, like calling setText() on TextViews and such.
             */
            public void prepLayout(RecyclerView.ViewHolder vHolder) {
                final ViewHolder_Actions viewHolder = (ViewHolder_Actions) vHolder;

                Drawable icon = mContext.getResources().getDrawable(R.drawable.logo);
                // add the icon, text is already there
                icon.setBounds(0, 0, 48, 48);
                viewHolder.vLocation.setCompoundDrawables(null, icon, null, null);
                viewHolder.vLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // callback to send user to Maps app
                        ((OnRestaurantActionClickListener) mContext).onClickLocation();
                    }
                });

                icon.setBounds(0, 0, 48, 48);
                viewHolder.vWebsite.setCompoundDrawables(null, icon, null, null);
                viewHolder.vWebsite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // callback to start new WebView activity
                        ((OnRestaurantActionClickListener) mContext).onClickWebsite();
                    }
                });

                icon.setBounds(0, 0, 48, 48);
                viewHolder.vMenu.setCompoundDrawables(null, icon, null, null);
                viewHolder.vMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // callback to start new ...?
                        ((OnRestaurantActionClickListener) mContext).onClickMenu();
                    }
                });
            }
        };

        /*PHOTOS {
            *//**
             * Get and return the appropriate ViewHolder for this card.
             *//*
            public ViewHolder_Photos getViewHolder(ViewGroup viewGroup) {
                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_restaurant_photos, viewGroup, false);
                return new ViewHolder_Photos(v);
            }
            *//**
             * Makes edits custom to this card, like calling setText() on TextViews and such.
             *//*
            public void prepLayout(RecyclerView.ViewHolder vHolder) {
                final ViewHolder_Photos viewHolder = (ViewHolder_Photos) vHolder;
            }
        };*/

        public abstract RecyclerView.ViewHolder getViewHolder(ViewGroup viewGroup);
        public abstract void prepLayout(RecyclerView.ViewHolder viewHolder);
    }

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

    /** Enum used to fill each days' cells in the table with the appropriate times */
    private enum TableDay {
        MONDAY {
            public void fillTable(ViewHolder_Details viewHolder, String open, String close) {
                viewHolder.vOpenMon.setText(open);
                viewHolder.vCloseMon.setText(close);
            }
        },
        TUESDAY {
            public void fillTable(ViewHolder_Details viewHolder, String open, String close) {
                viewHolder.vOpenTue.setText(open);
                viewHolder.vCloseTue.setText(close);
            }
        },
        WEDNESDAY {
            public void fillTable(ViewHolder_Details viewHolder, String open, String close) {;
                viewHolder.vOpenWed.setText(open);
                viewHolder.vCloseWed.setText(close);
            }
        },
        THURSDAY {
            public void fillTable(ViewHolder_Details viewHolder, String open, String close) {
                viewHolder.vOpenThr.setText(open);
                viewHolder.vCloseThr.setText(close);
            }
        },
        FRIDAY {
            public void fillTable(ViewHolder_Details viewHolder, String open, String close) {
                viewHolder.vOpenFri.setText(open);
                viewHolder.vCloseFri.setText(close);
            }
        },
        SATURDAY {
            public void fillTable(ViewHolder_Details viewHolder, String open, String close) {
                viewHolder.vOpenSat.setText(open);
                viewHolder.vCloseSat.setText(close);
            }
        },
        SUNDAY {
            public void fillTable(ViewHolder_Details viewHolder, String open, String close) {
                viewHolder.vOpenSun.setText(open);
                viewHolder.vCloseSun.setText(close);
            }
        };

        // THIS WORKS! returns the right day
        public abstract void fillTable(ViewHolder_Details viewHolder, String open, String close);
    }

    /** Fills the table with open/close times for mRestaurant */
    private static void setUpTable(ViewHolder_Details viewHolder) {
        for (int day = 0; day < 7; day++) {
            ArrayList<Integer> times = DateTimeParser.getTimesForDay(mRestaurant, mContext, day);
            String open, close;
            if (times != null) {
                open = DateTimeParser.formatString(times.get(0), times.get(1));
                open = open.substring(0, open.length() - 1).toLowerCase(Locale.US); // make "11:45a"
                close = DateTimeParser.formatString(times.get(2), times.get(3));
                close = close.substring(0, close.length() - 1).toLowerCase(Locale.US);;
            } else {
                open = "CL";
                close = "CL";
            }
            TableDay.values()[day].fillTable(viewHolder, open, close);
        }

    }

    /** Provide a reference to the type of views that you are using (custom ViewHolder)
     * This is for the first card - Details */
    public static class ViewHolder_Details extends RecyclerView.ViewHolder {
        protected final ImageView vThumbnail;
        protected final TextView vName;
        protected final TextView vDetails;

        protected final TextView vOpenSun;
        protected final TextView vOpenMon;
        protected final TextView vOpenTue;
        protected final TextView vOpenWed;
        protected final TextView vOpenThr;
        protected final TextView vOpenFri;
        protected final TextView vOpenSat;

        protected final TextView vCloseSun;
        protected final TextView vCloseMon;
        protected final TextView vCloseTue;
        protected final TextView vCloseWed;
        protected final TextView vCloseThr;
        protected final TextView vCloseFri;
        protected final TextView vCloseSat;

        protected Button vCall;

        public ViewHolder_Details (View v) {
            super(v);
            vThumbnail = (ImageView) v.findViewById(R.id.IMG_thumbnail);
            vName =    (TextView) v.findViewById(R.id.TV_name);
            vDetails = (TextView) v.findViewById(R.id.TV_details);

            vOpenSun = (TextView) v.findViewById(R.id.TV_sunday_open);
            vOpenMon = (TextView) v.findViewById(R.id.TV_monday_open);
            vOpenTue = (TextView) v.findViewById(R.id.TV_tuesday_open);
            vOpenWed = (TextView) v.findViewById(R.id.TV_wednesday_open);
            vOpenThr = (TextView) v.findViewById(R.id.TV_thursday_open);
            vOpenFri = (TextView) v.findViewById(R.id.TV_friday_open);
            vOpenSat = (TextView) v.findViewById(R.id.TV_saturday_open);

            vCloseSun = (TextView) v.findViewById(R.id.TV_sunday_close);
            vCloseMon = (TextView) v.findViewById(R.id.TV_monday_close);
            vCloseTue = (TextView) v.findViewById(R.id.TV_tuesday_close);
            vCloseWed = (TextView) v.findViewById(R.id.TV_wednesday_close);
            vCloseThr = (TextView) v.findViewById(R.id.TV_thursday_close);
            vCloseFri = (TextView) v.findViewById(R.id.TV_friday_close);
            vCloseSat = (TextView) v.findViewById(R.id.TV_saturday_close);

            vCall =   (Button) v.findViewById(R.id.BTN_call);
        }
    }

    /** This is for the second card - Discounts */
    public static class ViewHolder_Discounts extends RecyclerView.ViewHolder {
        protected final TextView vDiscountsHeader;
        protected final TextView vDiscounts;
        protected final TextView vFootnote;

        public ViewHolder_Discounts (View v) {
            super(v);
            vDiscountsHeader = (TextView) v.findViewById(R.id.TV_discount_header);
            vDiscounts =       (TextView) v.findViewById(R.id.TV_discounts);
            vFootnote =        (TextView) v.findViewById(R.id.TV_discount_footnote);
        }
    }

    /** This is for the third card - Actions */
    public static class ViewHolder_Actions extends RecyclerView.ViewHolder {
        protected final Button vLocation;
        protected final Button vWebsite;
        protected final Button vMenu;

        public ViewHolder_Actions (View v) {
            super(v);
            vLocation = (Button) v.findViewById(R.id.BTN_location);
            vWebsite =  (Button) v.findViewById(R.id.BTN_website);
            vMenu =     (Button) v.findViewById(R.id.BTN_menu);
        }
    }
}
