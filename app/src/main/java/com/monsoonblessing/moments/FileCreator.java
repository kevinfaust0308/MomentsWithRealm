package com.monsoonblessing.moments;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Kevin on 2016-06-17.
 */
public class FileCreator {

    private static final String TAG = FileCreator.class.getSimpleName();

    public static File createNewImageFilee(Context context) {

        File file = null;

        //1. create a unique file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp;
        fileName += ".jpg";

        file = new File(context.getFilesDir(), fileName);
        return file;
    }

    /**
     * Creates a new (image) file in app's image folder within phone's external storage
     */
    public static File createNewImageFile() {
        File image = null;

        //check for external storage (sd card or part of built in phone's memory that's treated as external memory)
        if (isExternalStorageAvailable()) {
            //get the Uri

            //1. album storage directory
            File albumDirectory = getAlbumStorageDir("MomentsImages");

            Log.d(TAG, "Album storage directory: " + Uri.fromFile(albumDirectory));

            //1. create a unique file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timeStamp;
            String fileType = ".jpg";

            //2. create the file in the album storage
            try {
                image = File.createTempFile(
                        fileName,  /* prefix */
                        fileType,         /* suffix */
                        albumDirectory      /* directory */
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return image;
    }


    /**
     * Returns file of the specified album
     *
     * @param albumName name of the album we want
     */
    private static File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }


    /**
     * External phone storage check to store image
     */
    private static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


}
