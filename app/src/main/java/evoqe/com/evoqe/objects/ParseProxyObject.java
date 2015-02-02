package evoqe.com.evoqe.objects;

// By Jamie Chapman, @chappers57
// License: open, do as you wish, just don't blame me if stuff breaks ;-)

// Aaron Bowen 1/25/14 - added getParseObject(), getDate()

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class ParseProxyObject implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String TAG = "ParseProxyObject";
    private final String OBJECT_ID_KEY = "objectId";
    private HashMap<String, Object> values = new HashMap<String, Object>();

    public HashMap<String, Object> getValues() {
        return values;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }

    public ParseProxyObject(ParseObject object) {

        // Loop the keys in the ParseObject
        for(String key : object.keySet()) {
            @SuppressWarnings("rawtypes")
            Class classType = object.get(key).getClass();
            if(classType == byte[].class || classType == String.class ||
                    classType == Integer.class || classType == Boolean.class || classType == Date.class) {
                values.put(key, object.get(key));
            } else if(classType == ParseUser.class) {
                // I think the reason we can't store ParseUsers directly is
                // because they are not serializable
                ParseProxyObject parseUserObject = new ParseProxyObject(object.getParseUser(key));
                values.put(key, parseUserObject);
            } else if(classType == ParseFile.class) {
                ParseFile file = (ParseFile) object.get(key);
                try {
                    values.put(key, file.getData());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
            // You might want to add more conditions here, for embedded ParseObject, ParseFile, etc.
            // If you do, make changes to getParseObject() method below
        }
    }
    // Every ParseObject has an objectId, this needs to be added separately
    values.put(OBJECT_ID_KEY, object.getObjectId());
}

    public String getString(String key) {
        if(has(key)) {
            return (String) values.get(key);
        } else {
            return "";
        }
    }

    public int getInt(String key) {
        if(has(key)) {
            return (Integer)values.get(key);
        } else {
            return 0;
        }
    }

    public Boolean getBoolean(String key) {
        if(has(key)) {
            return (Boolean)values.get(key);
        } else {
            return false;
        }
    }

    public byte[] getBytes(String key) {
        if(has(key)) {
            return (byte[])values.get(key);
        } else {
            return new byte[0];
        }
    }

    public Date getDate(String key) {
        if(has(key)) {
            return (Date)values.get(key);
        } else {
            return new Date();
        }
    }

    public ParseProxyObject getParseUser(String key) {
        if(has(key)) {
            return (ParseProxyObject) values.get(key);
        } else {
            return null;
        }
    }

    public byte[] getParseFile(String key) {
        if(has(key)) {
            return (byte[]) values.get(key);
        } else {
            return null;
        }
    }

    public Boolean has(String key) {
        return values.containsKey(key);
    }

    /**
     * NOTE: all ParseUsers are returned as ParseObjects!
     * @param className
     * @return
     */
    public ParseObject getParseObject(String className) {
        // Create empty ParseObject
        ParseObject object = ParseObject.createWithoutData(className, (String) values.get(OBJECT_ID_KEY));

        // Loop the keys in the HashMap
        for (String key : values.keySet()) {
            Class classType = values.get(key).getClass();
            if (classType == byte[].class || classType == String.class ||
                    classType == Integer.class || classType == Boolean.class || classType == Date.class) {
                object.put(key, values.get(key));
            } else if (classType == ParseProxyObject.class) {
                ParseObject user = ((ParseProxyObject) values.get(key)).getParseObject("User");
                object.put(key, user);
            } else {
                // do nothing, should never happen
                Log.e(TAG, "Error in getParseObject() with className: '" + className + "', on key: '"
                        + key + "'");
            }
        }
        return object;
    }
}