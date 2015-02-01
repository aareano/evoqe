package evoqe.com.evoqe.objects;

import android.content.Context;
import android.widget.Toast;

/**
 * @author Aaron on 1/31/2015.
 * The purpose of this class is to keep Toasts from stacking up on each other and taking forevery to
 * clear out. Use this class anywhere there is expected to be buildup of Toasts.
 * This only supports plain old toasts with no customization like margins or gravity right now,
 * but these are easily added as needed.
 */
public class ToastWrapper {

    /** A static reference to the last toast made. This is cancelled before the next Toast is shown */
    private static Toast mLastToast;
    /** A member variable that is the Toast to be made next */
    private Toast mToast;



    public ToastWrapper(Toast toast) {
        mToast = toast;
    }

    public static ToastWrapper makeText(Context context, String text, int duration) {
        ToastWrapper tWrapper = new ToastWrapper(Toast.makeText(context, text, duration));
        return tWrapper;

    }

    public void show() {
        cancelLastToast();    // get that last toast otta there
        mToast.show();        // show the new Toast without any annoying delays waiting for others to clear
        mLastToast = mToast;  // this toast is now available to be cancelled if need be
                              // frees up the Toast that was just cancelled for garbage collection
    }

    private void cancelLastToast() {
        if (mLastToast != null) {
            mLastToast.cancel();  // Cancel the last toast
        }
    }

}
