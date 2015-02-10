package evoqe.com.evoqe.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.parse.ParseFile;

import java.lang.ref.WeakReference;

import evoqe.com.evoqe.utilities.ImageHelper;


/**
 * @author Aaron on 2/7/2015. - taken from developer.android.com
 */
public class BitmapWorkerTask extends AsyncTask<ParseFile, Void, Bitmap> {
    private Context mContext;
    private final WeakReference<ImageView> imageViewReference;
//    private int data = 0;
    private ParseFile data = null;
    private static final String TAG = "BitmapWorkerTask";

    public BitmapWorkerTask(ImageView imageView, Context context, int width, int height) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
        mContext = context;
    }

    public static boolean cancelPotentialWork(ImageView imageView, ParseFile picFile) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final ParseFile currData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (currData == null || currData.equals(picFile) == false) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(ParseFile... params) {
        data = params[0];
        int reqHeight = 100; // defaults
        int reqWidth = 100;
        if (imageViewReference != null && imageViewReference.get() != null) {
            reqHeight = imageViewReference.get().getHeight();
            reqWidth = imageViewReference.get().getWidth();
        }
        Bitmap bitmap = ImageHelper.decodeSampledBitmapFromParseFile(data, reqWidth, reqHeight);
        return ImageHelper.getRoundedCornerBitmap(bitmap);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask =
                    getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}