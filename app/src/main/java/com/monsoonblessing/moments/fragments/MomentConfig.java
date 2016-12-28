package com.monsoonblessing.moments.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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

import com.monsoonblessing.moments.CalendarUtil;
import com.monsoonblessing.moments.CurrentDate;
import com.monsoonblessing.moments.FileCreator;
import com.monsoonblessing.moments.FileUtils;
import com.monsoonblessing.moments.MetricUtils;
import com.monsoonblessing.moments.PermissionManager;
import com.monsoonblessing.moments.R;
import com.monsoonblessing.moments.R2;
import com.monsoonblessing.moments.RealmDatabaseHelper;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.File;
import java.io.IOException;
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

    public interface OnSubmitListener {
        void OnSubmit();
    }

    private static final int CAMERA_REQUEST_CODE = 0;
    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int PREVIEW_PICTURE_LENGTH = 600;
    private static final int PREVIEW_PICTURE_WIDTH = 200;

    // keep track of whether we are creating or updating a moment
    protected boolean isNewMoment;
    protected int momentID;

    protected Uri momentPhotoUri;
    protected Long momentDateLong;
    protected String momentMonth;
    protected int momentYear;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.new_moment, null);
        ButterKnife.bind(this, v);
        b.setView(v);

        Bundle bundle = getArguments();
        if (bundle != null) {
            isNewMoment = false;

            // get preset settings
            momentID = bundle.getInt(RealmDatabaseHelper.COLUMN_ID);
            momentPhotoUri = Uri.parse(bundle.getString(RealmDatabaseHelper.COLUMN_PHOTO_URI));
            momentDateLong = bundle.getLong(RealmDatabaseHelper.COLUMN_DATE_LONG);
            String title = bundle.getString(RealmDatabaseHelper.COLUMN_TITLE);

            // update title text
            mTitleText.setText(title);

            // update date text
            Date dateObj = new Date(momentDateLong);
            SimpleDateFormat f = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            mDateText.setText(f.format(dateObj));

            // update preview picture
            addPreviewPicture(momentPhotoUri);
        } else {
            isNewMoment = true;

            // set preset settings
            momentPhotoUri = null;
            CurrentDate cd = new CurrentDate();
            // make date long contain current date
            momentDateLong = cd.getCurrentLongDate();

            // update date text
            setDateTextView(cd.getYear(), cd.getMonth(), cd.getDay());
        }

        return b.create();
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
                RealmDatabaseHelper.insertNewMomentIntoDB(mTitleText.getText().toString(), momentPhotoUri.toString(), momentDateLong, momentMonth, momentYear);
            } else {
                // update row
                RealmDatabaseHelper.updateRow(momentID, mTitleText.getText().toString(), momentPhotoUri.toString(), momentDateLong, momentMonth, momentYear);
            }

            // let mainactivity know about this
            ((OnSubmitListener) getActivity()).OnSubmit();
        }
    }


    ;


    @OnClick(R2.id.choose_date)
    void onChooseData() {
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
        removePreviewPicture();
    }


    public boolean hasPhoto() {
        boolean has = true;
        if (momentPhotoUri == null) {
            Toast.makeText(getActivity(), "Please choose a photo!", Toast.LENGTH_SHORT).show();
            has = false;
        }
        return has;
    }


    public void storePhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = FileCreator.createNewImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                momentPhotoUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, momentPhotoUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            } else {
                momentPhotoUri = null;
                Toast.makeText(getActivity(),
                        "There was a problem creating the image file",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Unable to reach camera app", Toast.LENGTH_LONG).show();
        }
    }


    public void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }


    /*
    Remove image selector and replace with preview of photo chosen
    */
    public void addPreviewPicture(Uri imagePath) {
        mImageViewSwitcher.setDisplayedChild(1);
        Picasso.with(getActivity())
                .load(imagePath)
                .resize(PREVIEW_PICTURE_LENGTH, PREVIEW_PICTURE_WIDTH)
                .centerCrop()
                .into(mImage);
    }


    /*
    Remove preview of image and show image selector screen again
     */
    public void removePreviewPicture() {
        mImageViewSwitcher.setDisplayedChild(0);
        momentPhotoUri = null;
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

/*        //if we are coming back to the fragment from taking/choosing a pic
        if (resultCode == RESULT_OK) {

            if (requestCode == GALLERY_REQUEST_CODE) {
                *//*
                If we took a picture, the passed in data will be null
                --> camera photo placed in the specified file when starting the intent
                --> we stored the uri to our camera variable

                If we chose a picture, the passed in data will ba uri to content provider location
                 *//*
                if (data != null) {

                    Uri selectedImage = data.getData();
                    String picturePath = FileUtils.getPath(getActivity(), selectedImage);

                    Log.d(TAG, "PICTURE PATH FROM GALLERY: " + picturePath);

                    mMoment.setPhotoUri(Uri.parse("file://" + picturePath));
                }
            }
            addPreviewPicture(mMoment.getPhotoUri());
        }*/


        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    Uri croppedImgUri = CropImage.getActivityResult(data).getUri();
                    Log.d(TAG, "Cropped pic uri: " + croppedImgUri.toString());

                    // cropped image file location (somewhere in cache)
                    File tempCroppedImage = new File(croppedImgUri.toString());

                    // create new image file in an album which will long-term store this cropped image
                    File newFile = FileCreator.createNewImageFilee(getActivity());
                    Log.d(TAG, "Size of this new empty file: " + newFile.length());
                    Log.d(TAG, "Newly created file in album which will contain image (abs path): " + newFile.getAbsolutePath());


                    // move the temp file to our long-term
                    FileUtils.moveFile(tempCroppedImage.getAbsolutePath(), tempCroppedImage.getName(), getActivity().getFilesDir().getAbsolutePath());

                    Log.d(TAG, "Cropped file has been moved: " + newFile.length());

                    // update profile pic and uri variable to the cropped image
                    addPreviewPicture(croppedImgUri);
                    //momentPhotoUri = Uri.fromFile(newFile);
                    break;

                case GALLERY_REQUEST_CODE:
                    // get the uri of the chosen picture
                    Uri selectPicUri = data.getData();
                    Log.d(TAG, "Selected pic uri: " + selectPicUri.toString());

                    // crop the selected picture
                    Intent intent = CropImage.activity(selectPicUri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(2, 1)
                            .getIntent(getActivity());
                    startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
                    break;
            }
        }


    }
}







