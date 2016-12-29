package com.monsoonblessing.moments.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.monsoonblessing.moments.CalendarUtil;
import com.monsoonblessing.moments.CurrentDate;
import com.monsoonblessing.moments.FileCreator;
import com.monsoonblessing.moments.PermissionManager;
import com.monsoonblessing.moments.R;
import com.monsoonblessing.moments.R2;
import com.monsoonblessing.moments.RealmDatabaseHelper;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Kevin on 2016-06-21.
 */
public abstract class MomentConfig extends DialogFragment
        implements DatePickerFragment.OnDateChosenListener {

    private static final String TAG = MomentConfig.class.getSimpleName();

    @BindView(R.id.image_view_switcher)
    ViewSwitcher mImageViewSwitcher;
    @BindView(R.id.title_text)
    EditText mTitleText;
    @BindView(R.id.image_chosen)
    ImageView mImage;
    @BindView(R.id.date_text)
    TextView mDateText;

    //private static final int CAMERA_REQUEST_CODE = 0;
    //private static final int GALLERY_REQUEST_CODE = 1;
    private static final int MEDIA_REQUEST_CODE = 1;
    private static final int PREVIEW_PICTURE_LENGTH = 800;
    private static final int PREVIEW_PICTURE_WIDTH = 400;

    // keep track of whether we are creating or updating a moment
    protected boolean isNewMoment = false;
    protected int momentID;

    // file path of the chosen image
    protected String chosenPhotoFilePath;

    protected Long momentDateLong;
    protected String momentMonth;
    protected int momentYear;

    // boolean submitted new entry
    boolean submittedEntry = false; // delete image file if this is false


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Log.d(TAG, "Dismissing");
            dismiss();
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.new_moment, null);
        ButterKnife.bind(this, v);
        b.setView(v);

        Bundle bundle = getArguments();

        if (bundle != null) {
            // get preset settings
            momentID = bundle.getInt(RealmDatabaseHelper.COLUMN_ID);
            chosenPhotoFilePath = bundle.getString(RealmDatabaseHelper.COLUMN_PHOTO_URI);
            momentDateLong = bundle.getLong(RealmDatabaseHelper.COLUMN_DATE_LONG);
            String title = bundle.getString(RealmDatabaseHelper.COLUMN_TITLE);

            // update title text
            mTitleText.setText(title);

            // update date text
            Date dateObj = new Date(momentDateLong);
            SimpleDateFormat f = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            mDateText.setText(f.format(dateObj));

            // update preview picture
            addPreviewPicture(chosenPhotoFilePath);
        } else {
            isNewMoment = true;

            // set preset settings
            CurrentDate cd = new CurrentDate();
            // make date long contain current date
            momentDateLong = cd.getCurrentLongDate();

            // update date text
            setDateTextView(cd.getYear(), cd.getMonth(), cd.getDay());
        }

        // creates dialog
        return b.create();
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, MomentConfig.class.getSimpleName() + " is being dismissed");

        // if we are not submitting entry, we may or may not have chosen a image
        // we only care about scenario when they did (to delete it)
        if (chosenPhotoFilePath != null && !submittedEntry) {
            File f = new File(chosenPhotoFilePath);
            // delete it
            if (f.delete()) {
                Log.d(TAG, "Picture file associated with preview picture deleted");
            }
        }

    }


    @OnClick(R2.id.submit)
    void onSubmit() {
        if (hasPhoto()) {
            //date string to Long
            String dateText = mDateText.getText().toString();
            SimpleDateFormat f = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();

            try {
                Date date = f.parse(dateText);
                calendar.setTime(date);
                //store date
                momentDateLong = calendar.getTimeInMillis();
                momentMonth = CalendarUtil.monthNumberToText(calendar.get(Calendar.MONTH));
                momentYear = calendar.get(Calendar.YEAR);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (isNewMoment) {
                // insert row
                RealmDatabaseHelper.insertNewMomentIntoDB(mTitleText.getText().toString(), chosenPhotoFilePath, momentDateLong, momentMonth, momentYear);
            } else {
                // update row
                RealmDatabaseHelper.updateRow(momentID, mTitleText.getText().toString(), chosenPhotoFilePath, momentDateLong, momentMonth, momentYear);
            }

            submittedEntry = true; // now ondismiss wont delete this image file
            dismiss();
        }
    }


    @OnClick(R2.id.choose_date)
    void onChooseDate() {
        showDatePickerDialog();
    }


    @OnClick(R2.id.camera_button)
    void onCameraSelect() {
        //check if we have a camera
        boolean hasCameraPermission = PermissionManager.hasCameraPermission(getActivity());
        //check if we can access storage
        boolean hasExternalStoragePermission = PermissionManager.hasExternalStoragePermission(getActivity());

        if (!hasCameraPermission) {
            // request for the permission
            PermissionManager.requestCameraPermission(getActivity());
        } else if (!hasExternalStoragePermission) {
            // request for the permission
            PermissionManager.requestExternalStoragePermission(getActivity());
        }
        // if we have both permissions then launch camera
        else {
            storePhotoFromCamera();
        }

    }


    @OnClick(R2.id.gallery_button)
    void onGallerySelect() {
        //check if we can access storage
        boolean hasExternalStoragePermission = PermissionManager.hasExternalStoragePermission(getActivity());

        if (!hasExternalStoragePermission) {
            // request for the permission
            PermissionManager.requestExternalStoragePermission(getActivity());
        } else {
            choosePhotoFromGallery();
        }
    }


    @OnClick(R2.id.remove_photo)
    void onRemove() {
        removePreviewPictureAndDeleteImageFile();
    }


    public boolean hasPhoto() {
        boolean has = true;
        if (chosenPhotoFilePath == null) {
            Toast.makeText(getActivity(), "Please choose a photo!", Toast.LENGTH_SHORT).show();
            has = false;
        }
        return has;
    }


    public void storePhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // launch camera app
            startActivityForResult(takePictureIntent, MEDIA_REQUEST_CODE);
        } else {
            Toast.makeText(getActivity(), "Unable to reach camera app", Toast.LENGTH_LONG).show();
        }
    }


    public void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, MEDIA_REQUEST_CODE);
    }


    /*
    Remove image selector and replace with preview of photo chosen
    */
    public void addPreviewPicture(String imageFilePath) {
        mImageViewSwitcher.setDisplayedChild(1);
        Glide.with(this)
                .load(imageFilePath)
                .fitCenter()
                .override(PREVIEW_PICTURE_LENGTH, PREVIEW_PICTURE_WIDTH)
                .centerCrop()
                .into(mImage);
    }


    /*
    Remove preview of image and delete that stored image file and show image selector screen again
     */
    public void removePreviewPictureAndDeleteImageFile() {
        // in order to get here we must have chosen a picture so chosen image file path is not null
        File f = new File(chosenPhotoFilePath);

        // delete the file that we created for the image
        if (f.delete()) {
            Log.d(TAG, "Picture file associated with preview picture deleted");
            mImageViewSwitcher.setDisplayedChild(0);
            chosenPhotoFilePath = null;
        }
    }


    public void showDatePickerDialog() {
        DialogFragment frag = DatePickerFragment.newInstance(momentDateLong);
        frag.show(getFragmentManager(), "datePicker");
    }


    public void setDateTextView(int year, int month, int day) {
        mDateText.setText(String.format(Locale.getDefault(),
                "%s %d, %d",
                CalendarUtil.monthNumberToText(month), //month is 0 index based
                day,
                year)
        );
    }


    /*
    Callback interface implementation. Declaration in DatePickerFragment.java
    Updates UI and date object with selected date from calendar
    */
    @Override
    public void OnDateChose(int year, int month, int day) {
        setDateTextView(year, month, day);
    }


    /*
    Runs when we come back from launching camera/gallery
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "request code: " + requestCode + ", result code: " + resultCode);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    Uri croppedImgUri = CropImage.getActivityResult(data).getUri();
                    Log.d(TAG, "Cropped pic uri: " + croppedImgUri.getPath());

                    // cropped image file location (in app cache)
                    String cachedCropImageFilePath = croppedImgUri.getPath();

                    // move temporary image file to internal storage
                    chosenPhotoFilePath = moveAppCacheFileToInternalStorage(cachedCropImageFilePath);

                    // update profile pic and uri variable to the cropped image
                    addPreviewPicture(chosenPhotoFilePath);
                    Log.d(TAG, "Moment photo file (absPath/filePath): " + chosenPhotoFilePath);
                    break;

                case MEDIA_REQUEST_CODE:
                    // get the uri of the chosen picture/camera picture
                    Uri mediaPicUri = data.getData();
                    Log.d(TAG, "Selected pic uri: " + mediaPicUri.getPath());

                    // crop the selected picture
                    launchCropImageActivity(mediaPicUri);
                    break;
            }
        }
    }


    public String moveAppCacheFileToInternalStorage(String cacheImageFilePath) {

        File cachedCropImageFile = new File(cacheImageFilePath);

        // create empty image file in internal storage
        File internalStoragePhotoFile = FileCreator.createNewImageFileInInternalAppDirectory(getActivity());
        Log.d(TAG, "Size of this new empty file: " + internalStoragePhotoFile.length());
        Log.d(TAG, "Newly created file abs path: " + internalStoragePhotoFile.getAbsolutePath());

        // move the cached crop image file to the app's internal storage (no need to delete because we moved it)
        if (cachedCropImageFile.renameTo(internalStoragePhotoFile)) {
            Log.d(TAG, "Successfully moved file");
        } else {
            Log.d(TAG, "Failed to moved file");
        }

        Log.d(TAG, "Cropped file has been moved (size of it now): " + internalStoragePhotoFile.length());

        // return the file path of the new internal storage image file
        return internalStoragePhotoFile.getAbsolutePath();

    }


    private void launchCropImageActivity(Uri imageUri) {
        Intent intent = CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(2, 1)
                .getIntent(getActivity());
        startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
    }

}







