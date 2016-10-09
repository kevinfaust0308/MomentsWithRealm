package com.monsoonblessing.moments.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.monsoonblessing.moments.CalendarUtil;
import com.monsoonblessing.moments.CurrentDate;
import com.monsoonblessing.moments.FileCreator;
import com.monsoonblessing.moments.FileUtils;
import com.monsoonblessing.moments.Moment;
import com.monsoonblessing.moments.Permissions;
import com.monsoonblessing.moments.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Kevin on 2016-06-21.
 */
public abstract class MomentConfig extends DialogFragment
        implements DatePickerFragment.OnDateChosenListener, View.OnClickListener {

    public interface OnSubmitListener {
        void OnSubmit();
    }

    private static final String TAG = MomentConfig.class.getSimpleName();
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_CHOOSE_PHOTO = 1;
    private static final int PREVIEW_PICTURE_LENGTH = 600;
    private static final int PREVIEW_PICTURE_WIDTH = 200;
    protected Moment mMoment;
    private ViewSwitcher mImageViewSwitcher;
    private EditText mTitleText;
    private LinearLayout mDatePicker;
    private ImageView mImage;
    private TextView mDateText;
    private ImageButton mSubmit;
    private ImageButton mCameraButton;
    private ImageButton mGalleryButton;
    private ImageButton mRemovePhoto;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.new_moment, null);
        mImageViewSwitcher = (ViewSwitcher) v.findViewById(R.id.image_view_switcher);
        mTitleText = (EditText) v.findViewById(R.id.title_text);
        mDatePicker = (LinearLayout) v.findViewById(R.id.choose_date);
        mImage = (ImageView) v.findViewById(R.id.image_chosen);
        mDateText = (TextView) v.findViewById(R.id.date_text);
        mSubmit = (ImageButton) v.findViewById(R.id.submit);
        mCameraButton = (ImageButton) v.findViewById(R.id.camera_button);
        mGalleryButton = (ImageButton) v.findViewById(R.id.gallery_button);
        mRemovePhoto = (ImageButton) v.findViewById(R.id.remove_photo);


        //checks if we are updating an existing Moment object or not
        Bundle bundle = getArguments();
        mMoment = (bundle != null) ? loadStoredMoment(bundle) : loadNewMoment();

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if we have a camera
                if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
                    // this device has a camera
                    boolean permissionCheck = Permissions.checkCameraPermission(getActivity());
                    Log.d(TAG, "Camera permission status: " + permissionCheck);
                    if (permissionCheck) {
                        storePhotoFromCamera();
                    }
                } else {
                    // no camera on this device
                    Toast.makeText(getActivity(), "No camera available :(", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean permissionCheck = Permissions.checkReadPermission(getActivity());
                Log.d(TAG, "Gallery Permission status: " + permissionCheck);
                if (permissionCheck) {
                    choosePhotoFromGallery();
                }
            }
        });
        mRemovePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePreviewPicture();
            }
        });
        mSubmit.setOnClickListener(this); //implemented in subclasses
        mDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        b.setView(v);
        return b.create();
    }


    //returns a moment object with predefined settings if we are updating an entry
    public Moment loadStoredMoment(Bundle bundle) {
        //set preconfigured settings
        Moment m = new Moment(bundle.getInt("id"),
                bundle.getString("title"),
                Uri.parse(bundle.getString("uri")),
                bundle.getLong("date"));
        setUIFields(m);
        return m;
    }


    //sets the ui with predefined settings. called in loadStoredMoment
    public void setUIFields(Moment moment) {
        //update UI with those settings
        addPreviewPicture(moment.getPhotoUri());
        mTitleText.setText(moment.getTitle());
        Date dateObj = new Date(moment.getDate());
        SimpleDateFormat f = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        mDateText.setText(f.format(dateObj));
    }


    public Moment loadNewMoment() {
        Moment m = new Moment();
        CurrentDate cd = new CurrentDate();
        //set date text to display current date
        int num_month = cd.getMonth();
        String text_month = CalendarUtil.monthNumberToText(num_month);
        //store the date. this will determine what date the date popup selector will show
        m.setDate(cd.getCurrentLongDate());
        m.setMonth(text_month);
        m.setYear(cd.getYear());
        //update date text
        mDateText.setText(String.format(Locale.getDefault(),
                "%s %d, %d",
                text_month, //month is 0 index based
                cd.getDay(),
                cd.getYear())
        );
        return m;
    }


    /*
    Remove image selector and replace with preview of photo chosen
    */
    public void addPreviewPicture(Uri imagePath) {
        mImageViewSwitcher.showNext();
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
        mMoment.setPhotoUri(null);
    }


    //used for submission
    public void updateMomentFields() {
        //date string to Long
        String dateText = mDateText.getText().toString();
        SimpleDateFormat f = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            Date date = f.parse(dateText);
            calendar.setTime(date);
            //store date
            mMoment.setDate(calendar.getTimeInMillis());
            mMoment.setMonth(CalendarUtil.monthNumberToText(calendar.get(Calendar.MONTH)));
            mMoment.setYear(calendar.get(Calendar.YEAR));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //store title
        String title = mTitleText.getText().toString();
        mMoment.setTitle(title);
    }


    public void storePhotoFromCamera() {
        File f = FileCreator.createNewImageFile(getActivity());

        if (f != null) {
            Uri newPhotoUri = Uri.fromFile(f);
            mMoment.setPhotoUri(newPhotoUri);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, newPhotoUri);
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            //TODO: BUG: if user clicks camera but then exits, the uri image file is created although empty. thus submitting works
        } else {
            mMoment.setPhotoUri(null);
            Toast.makeText(getActivity(),
                    "There was a problem accessing your device's external storage",
                    Toast.LENGTH_SHORT).show();
        }
    }


    public void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CHOOSE_PHOTO);
    }


    public void showDatePickerDialog() {
        DialogFragment frag = DatePickerFragment.newInstance(mMoment.getDate());
        frag.show(getFragmentManager(), "datePicker");
    }


    /*
    Callback interface implementation. Declaration in DatePickerFragment.java
    Updates UI and date object with selected date from calendar
    */
    @Override
    public void OnDateChose(int year, int month, int day) {
        mDateText.setText(String.format(Locale.getDefault(),
                "%s %d, %d",
                CalendarUtil.monthNumberToText(month), //month is 0 index based
                day,
                year)
        );
    }


    /*
    Runs when we come back from launching camera/gallery
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "request code: " + requestCode + ", result code: " + resultCode);

        //if we are coming back to the fragment from taking/choosing a pic
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_CHOOSE_PHOTO) {
                /*
                If we took a picture, the passed in data will be null
                --> camera photo placed in the specified file when starting the intent
                --> we stored the uri to our camera variable

                If we chose a picture, the passed in data will ba uri to content provider location
                 */
                if (data != null) {

                    Uri selectedImage = data.getData();
                    String picturePath = FileUtils.getPath(getActivity(), selectedImage);

                    Log.d(TAG, "PICTURE PATH FROM GALLERY: " + picturePath);

                    mMoment.setPhotoUri(Uri.parse("file://" + picturePath));
                }
            }
            addPreviewPicture(mMoment.getPhotoUri());
        }
    }

}


