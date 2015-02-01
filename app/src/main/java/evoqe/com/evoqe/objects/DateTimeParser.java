package evoqe.com.evoqe.objects;

import android.content.Context;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import evoqe.com.evoqe.R;

/**
 * This class is for parsing date objects into formatted strings
 */
public class DateTimeParser {

    private static String TAG = "DateTimeParser";

    /***********************************************************************************************
     *        Everything below here is for parsing Restaurant times (until the Jamboree bit)
     **********************************************************************************************/

    /**
     * Extracts the hour and minute for both open and close times of the restaurant TODAY (according to Calendar class)
     * REQUIRED: The time must follow the format "DD HH:MM, " for each day (the last can be just "DD HH:MM")
     * @param restaurant
     * @param context
     * @param day - zero based (0 <= day <= 6)
     * @return ArrayList<Integer> holds {openHour, openMinute, closeHour, closeMinute}
     */
    public static ArrayList<Integer> getTimesForToday(ParseObject restaurant, Context context, int day) {
        if (restaurant == null || day >= 6 || day <= 0) {
            return null;
        }
        ParseProxyObject proxyRestaurant = new ParseProxyObject(restaurant);
        return getTimesForDay(proxyRestaurant, context, day);
    }
    /**
     * Extracts the hour and minute for both open and close times of the restaurant TODAY (according to Calendar class)
     * REQUIRED: The time must follow the format "DD HH:MM, " for each day (the last can be just "DD HH:MM")
     * @param restaurant
     * @param context
     * @param day - zero based (0 <= day <= 6)
     * @return ArrayList<Integer> holds {openHour, openMinute, closeHour, closeMinute}
     */
    public static ArrayList<Integer> getTimesForDay(ParseProxyObject restaurant, Context context, int day) {
        if (restaurant == null || day >= 6 || day <= 0) {
            return null;
        }
        String openTimes, dayString, closed, openHour, openMinute, closeHour, closeMinute;
        String OPEN_TIMES_KEY = context.getResources().getString(R.string.open_times_key);
        ArrayList<Integer> times = new ArrayList<>();

        /* Ex. openTimes string:
         * "su 11:00-23:00, mo 11:00-23:00, tu 11:00-23:00, we 11:00-23:00, " +
         * "th 11:00-23:00, fr 11:00-23:00, sa NA:NA-NA:NA"
         */
        openTimes = restaurant.getString(OPEN_TIMES_KEY);

        // extract string for today
        final int dayInterval = 16; // The times for each day take up 16 characters of the string
        dayString = openTimes.substring(day * dayInterval, (day * dayInterval) + 14);

        // check if restaurant is closed all day
        closed = context.getResources().getString(R.string.closed);
        if (dayString.substring(3).equals(closed)) { // NA:NA-NA:NA == closed all day
            return null;
        }

        // get open/close hour/minute
        openHour = dayString.substring(3, 5);  // e.g. "su 11:00-23:00"
        openMinute = dayString.substring(6, 8);  // e.g. "su 11:00-23:00"
        closeHour = dayString.substring(9, 11);  // e.g. "su 11:00-23:00"
        closeMinute = dayString.substring(12, 14);  // e.g. "su 11:00-23:00"

        times.add(Integer.parseInt(openHour));
        times.add(Integer.parseInt(openMinute));
        times.add(Integer.parseInt(closeHour));
        times.add(Integer.parseInt(closeMinute));

        return times;
    }

    /**
     * @param times - an ArrayList<Integer> of the numberic hours/minutes of open and close
     * @return either the correctly formatted string, or null if restaurant is closed, or "Unable to fetch times"
     */
    public static String getPresentableString(ArrayList<Integer> times) {
        String timeString;
        if (times == null) { // restaurant is closed
            return null;
        }
        try { // format times to e.g. "11:15AM" or "6PM"
            String openFull = DateTimeParser.formatString(times.get(0), times.get(1));
            String closeFull = DateTimeParser.formatString(times.get(2), times.get(3));
            timeString = openFull + " - " + closeFull;
        } catch (NumberFormatException e) {
            e.fillInStackTrace();
            timeString = "Unable to fetch times";
        }
        return timeString;
    }

    /**
     * Concatenates hour and minute into properly formatted String, adds AM/PM
     * @param hour - the hour of the day as an int
     * @param minute - the minute of the hour as an int
     * @return e.g. 14:00 becomes 2PM, 14:30 becomes 2:30PM.
     */
    public static String formatString(int hour, int minute) {
        int newHour = hour; // preserve original hour for AM/PM logic
        // convert from 24hr to 12hr
        if (hour > 12) {
            newHour %= 12;
        }
        // add minutes if not 0, then AM/PM
        String time = (minute == 0) ? String.valueOf(newHour) : newHour + ":" + minute;
        time += (hour < 12 || hour > 23) ? "AM" : "PM";
        return time;
    }

    /***********************************************************************************************
     *                 Everything below here is for parsing Jamboree times
     **********************************************************************************************/

    /** Enum used to convert ints to Strings (e.g. 2 becomes "Tuesday") */
    private enum Day {
        SUNDAY {
            public String getDay() {
                return "Sunday";
            }
        },
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
        };

        public abstract String getDay();
    }

    public static String getDay(Date date) {
        if (date != null) {
            Calendar cal = dateToCalendar(date);
            String day = Day.values()[cal.get(Calendar.DAY_OF_WEEK) - 1].getDay();
            return day;
        } else {
            return null;
        }
    }

    /**
     * @param date
     * @return a String representation of date, e.g. "2/26/15" */
    public static String getDate(Date date) {
        if (date != null) {
            Calendar cal = dateToCalendar(date);
            String dateRep = "" + Integer.valueOf(cal.get(Calendar.MONTH) + 1) + "/"
                    + Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            return dateRep;
        } else {
            return null;
        }
    }

    /**
     * @param startDate
     * @param endDate
     * @return a boolean indicating whether or not startDate and endDate are on the same DAY_OF_YEAR
     * if either startDate or endDate are null, returns TRUE
     */
    public static boolean startsEndsSameDay(Date startDate, Date endDate) {
        if (startDate != null && endDate != null) {
            Calendar startCal = dateToCalendar(startDate);
            Calendar endCal = dateToCalendar(endDate);
            return (startCal.get(Calendar.DAY_OF_YEAR) == endCal.get(Calendar.DAY_OF_YEAR));
        } else {
            return true;
        }
    }

    /**
     * @param date
     * @return " (today)" or " (tomorrow)" or "" as appropriate, or null if date is null
     */
    public static String getAnnotation(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = dateToCalendar(date);
        // return " (today)" or " (tomorrow)" or ""
        if (cal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
            return " (today)";
        } else if (cal.get(Calendar.DAY_OF_YEAR) == (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + 1)) {
            return " (tomorrow)";
        } else {
            return "";
        }
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
     * @param date
     * @return The string representation of the time indicated by date (e.g. "11:45am")
     * OR "No times available" if either param is null.
     */
    public static String getFormattedTime(Date date) {
        Calendar cal = dateToCalendar(date);
        if (cal == null) {
            return "No time data available";
        }
        // '?' operator used to get e.g. 11:00 instead of 11:0
        String timeString = ((cal.get(Calendar.HOUR) == 0) ? "12" : cal.get(Calendar.HOUR)) + ":" +
                ((cal.get(Calendar.MINUTE) == 0) ? "00" : cal.get(Calendar.MINUTE));

        // add "am" or "pm" to the time
        if (cal.get(Calendar.AM_PM) == Calendar.AM) {
            timeString += "am";
        } else {
            timeString += "pm";
        }
        return timeString;
    }
}