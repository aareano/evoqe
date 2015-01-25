package evoqe.com.evoqe.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.ParseObject;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * See User.java notes
 *--
 * Right now this class is exclusively for passing a Jamboree object from one fragment to another.
 */
public class Jamboree implements Parcelable {
    private ParseObject mJamboree;

    /** Enum used to convert ints to Strings (e.g. 2 becomes "Tuesday") */
    private enum Day {
        MONDAY {
            public String getDay() {
                return "Monday";
            }
        },
        TUESDAY {
            public String getDay() {
                return "Tuesday";
            }
        },
        WEDNESDAY {
            public String getDay() {
                return "Wednesday";
            }
        },
        THURSDAY {
            public String getDay() {
                return "Thursday";
            }
        },
        FRIDAY {
            public String getDay() {
                return "Friday";
            }
        },
        SATURDAY {
            public String getDay() {
                return "Saturday";
            }
        },
        SUNDAY {
            public String getDay() {
                return "Sunday";
            }
        };

        public abstract String getDay();
    }

    public Jamboree(ParseObject jamboree) {
        mJamboree = jamboree;
    }
    public Jamboree(ParseProxyObject jamboree) {
        mJamboree = jamboree;
    }

    public ParseObject getJamboree() {
        return mJamboree;
    }

    public void setJamboree(ParseObject jamboree) {
        mJamboree = jamboree;
    }

    /* everything below here until the Parcelable code is for parsing start/end times and dates */

    /**
     * @param areCaps - Whether or not the result should be returned with all caps
     * @return The string representation of the day(s) which startCal and endCal
     *         span (e.g. "Thursday" or "Wednesday - Thursday")
     *         OR "No date available" if either param is null. Will also add "today" or "tomorrow"
     *         if appropriate (e.g. "Tuesday (today)" or "Sunday (tomorrow)").
     */
    public String getDays(boolean areCaps) {
        Calendar startCal = dateToCalendar((Date) mJamboree.get("startTime")); // have to do funky casting because of using ParseProxyObject
        Calendar endCal = dateToCalendar((Date) mJamboree.get("endTime"));

        // error handling
        if (startCal == null || endCal == null) {
            return "No date available";
        }
        // gets the string representation of the day (e.g. "Thursday")
        String dateString = Day.values()[startCal.get(Calendar.DAY_OF_WEEK) - 1].getDay();

        // TODO: there are some problems here. but they seems to be coming from erroneous data stored in Parse
        if (startCal.get(Calendar.DAY_OF_WEEK) == endCal.get(Calendar.DAY_OF_WEEK)) {
            return dateString;
        } else {    // multi-day
            dateString = dateString + " - " + Day.values()[endCal.get(Calendar.DAY_OF_WEEK) - 1].getDay();
        }

        // add "today" if appropriate
        if (startCal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
            dateString = dateString + " (today)";
        // add "tomorrow" if appropriate
        } else if (startCal.get(Calendar.DAY_OF_YEAR) == (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + 1)) {
            dateString = dateString + " (tomorrow)";
        }
        if (areCaps) {
            dateString.toUpperCase(Locale.US);
        }
        return dateString;
    }

    /**
     * Converts a Date to a Calendar representing the same time.
     * @param date - The date whose time is to be represented in the returned calendar.
     * @return The appropriate Calendar instance or null if the incoming date was null.
     */
    private Calendar dateToCalendar(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(date.getTime());
            return cal;
        } else {
            return null;
        }
    }

    /** Puts together a String representation of the time spanned by the Jamboree.
     * @param ampmAreCaps - true if the returned string should be e.g. 4:30PM ... as opposed to 4:30pm
     * @return The string representation of the time which startCal and endCal
     *         span (e.g. "11:00am - 1:45pm")
     *         OR "No times available" if either param is null.
     */
    public String getTimes(boolean ampmAreCaps) {
        Calendar startCal = dateToCalendar(mJamboree.getDate("startTime"));
        Calendar endCal = dateToCalendar(mJamboree.getDate("endTime"));

        if (startCal == null || endCal == null) {
            return "No times available";
        }
        String startTime = formatTime(startCal, ampmAreCaps);
        String endTime = formatTime(endCal, ampmAreCaps);

        return startTime + " - " + endTime;
    }

    /**
     * @param cal - the calendar whose time to use
     * @param ampmAreCaps - true if the returned string should be e.g. "4:30PM", as opposed to "4:30pm"
     * @return - The formatted string (e.g. "4:00PM")
     */
    private String formatTime(Calendar cal, boolean ampmAreCaps) {
        // '?' operator used to get e.g. 11:00 instead of 11:0
        String timeString = cal.get(Calendar.HOUR) + ":" +
                ((cal.get(Calendar.MINUTE) == 0) ? "00" : cal.get(Calendar.MINUTE));
        // add "am" or "pm" to the time
        if (cal.get(Calendar.AM_PM) == Calendar.AM) {
            timeString = timeString + "am";
        } else {
            timeString = timeString + "pm";
        }
        if (ampmAreCaps) {
            timeString.toUpperCase(Locale.US);
        }
        return timeString;
    }


    /***********************************************************************************************
     *                  Everything below here is for implementing Parcelable
     **********************************************************************************************/

    // Constructor that takes a Parcel and gives you an object populated with its values
    // at this point the public constructor has already been called, I think?
    protected Jamboree(Parcel in) {
        mJamboree = (ParseObject) in.readValue(ParseObject.class.getClassLoader());
    }

    /** IGNORE THIS METHOD
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(mJamboree);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Jamboree> CREATOR = new Parcelable.Creator<Jamboree>() {
        /**
         * Create a new instance of the Parcelable class, instantiating it
         * from the given Parcel whose data had previously been written by
         * {@link android.os.Parcelable#writeToParcel Parcelable.writeToParcel()}.
         *
         * @param source The Parcel to read the object's data from.
         * @return Returns a new instance of the Parcelable class.
         */
        @Override
        public Jamboree createFromParcel(Parcel source) {
            return new Jamboree(source);
        }

        /**
         * Create a new array of the Parcelable class.
         *
         * @param size Size of the array.
         * @return Returns an array of the Parcelable class, with every entry
         * initialized to null.
         */
        @Override
        public Jamboree[] newArray(int size) {
            return new Jamboree[0];
        }
    };
}
