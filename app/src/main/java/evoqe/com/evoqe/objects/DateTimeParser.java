package evoqe.com.evoqe.objects;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This class is for parsing date objects into formatted strings
 */
public class DateTimeParser {

    private static String TAG = "DateTimeParser";

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

    /**
     * @param areCaps - Whether or not the result should be returned with all caps.
     * @param startDate
     * @param endDate
     * @return The string representation of the day(s) which startCal and endCal
     * span (e.g. "Thursday" or "Wednesday - Thursday")
     * OR "No date available" if either param is null. Will also add "today" or "tomorrow"
     * if appropriate (e.g. "Tuesday (today)" or "Sunday (tomorrow)").
     */
    public static String getDays(Date startDate, Date endDate, boolean areCaps) {
        Calendar startCal = dateToCalendar(startDate);
        Calendar endCal = dateToCalendar(endDate);
        Log.d(TAG, "startCal::::::: " + startCal.toString());
        Log.d(TAG, "\n\nendCal::::::: " + endCal.toString());

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
     *
     * @param date - The date whose time is to be represented in the returned calendar.
     * @return The appropriate Calendar instance or null if the incoming date was null.
     */
    private static Calendar dateToCalendar(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(date.getTime());
            return cal;
        } else {
            return null;
        }
    }

    /**
     * Puts together a String representation of the time spanned by the Jamboree.
     *
     * @param ampmAreCaps - true if the returned string should be e.g. 4:30PM ... as opposed to
     *                    4:30pm
     * @param startDate
     * @param endDate
     * @return The string representation of the time which startCal and endCal
     * span (e.g. "11:00am - 1:45pm")
     * OR "No times available" if either param is null.
     */
    public static String getTimes(Date startDate, Date endDate, boolean ampmAreCaps) {
        Calendar startCal = dateToCalendar(startDate);
        Calendar endCal = dateToCalendar(endDate);

        if (startCal == null || endCal == null) {
            return "No times available";
        }
        String startTime = formatTime(startCal, ampmAreCaps);
        String endTime = formatTime(endCal, ampmAreCaps);

        return startTime + " - " + endTime;
    }

    /**
     * @param cal         - the calendar whose time to use
     * @param ampmAreCaps - true if the returned string should be e.g. "4:30PM", as opposed to
     *                    "4:30pm"
     * @return - The formatted string (e.g. "4:00PM")
     */
    private static String formatTime(Calendar cal, boolean ampmAreCaps) {
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
}