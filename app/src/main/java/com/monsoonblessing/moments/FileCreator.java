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

    /*
   Creates a new (image) file in app's image folder within phone's external storage
    */

    public static File createNewImageFile(Context context) {
        //check for external storage (sd card or part of built in phone's memory that's treated as external memory)
        if (isExternalStoreAvailable()) {
            //get the Uri

            //1. get the external storage directory
            File mediaStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);


            //2. create a unique file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timeStamp;
            String fileType = ".jpg";


            //3. create the file
            File file = null;
            try {
                file = File.createTempFile(fileName, fileType, mediaStorageDir);
                /*
                OR
                imageFile = new File(mediaStoragedir, fileName + fileType);
                imageFile.createNewFile;
                 */
                Log.d(TAG, "imageFile: " + file);
                Log.d(TAG, "uri from file: " + Uri.fromFile(file));

                return file;

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        //something went wrong
        return null;
    }


    /*
    External phone storage check to store image
     */
    private static boolean isExternalStoreAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
