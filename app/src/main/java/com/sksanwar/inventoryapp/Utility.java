package com.sksanwar.inventoryapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by sksho on 27-Apr-17.
 */

public class Utility {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final String LOG_TAG = Utility.class.getSimpleName();

    private Utility() {}

    public static Bitmap getBitmapFromUri(Context context,ImageView imageView, Uri uri) {
        if (uri == null) {
            return null;
        }

        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        ParcelFileDescriptor parcelFileDescriptor = null;
        Bitmap image;
        try {
            parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "r");

            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, opts);

            int photoW = opts.outWidth;
            int photoH = opts.outHeight;

            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
            Log.e(LOG_TAG, "Scaling the image" + scaleFactor);

            opts.inJustDecodeBounds = false;
            opts.inSampleSize = scaleFactor;
            opts.inPurgeable = true;

            image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, opts);
            imageView.setImageBitmap(image);

            if (image.getWidth() > image.getHeight()) {
                Matrix mat = new Matrix();
                int degree = 90;
                mat.postRotate(degree);
                Bitmap imageRotate = Bitmap.createBitmap(image, 0, 0,
                        image.getWidth(), image.getHeight(), mat, true);
                return imageRotate;
            } else {
                return image;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion >= android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
