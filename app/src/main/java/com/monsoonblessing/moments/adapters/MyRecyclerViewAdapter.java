package com.monsoonblessing.moments.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.monsoonblessing.moments.MomentModel;
import com.monsoonblessing.moments.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.RealmResults;

/**
 * Created by Kevin on 2016-06-15.
 */
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    private static final String TAG = "MyRecyclerViewAdapter";

    private Context mContext;
    private ArrayList<MomentModel> mMoments;


    public MyRecyclerViewAdapter(Context context, RealmResults<MomentModel> moments) {
        mContext = context;
        mMoments = new ArrayList<>(moments);
    }


    public MyRecyclerViewAdapter(Context context, ArrayList<MomentModel> moments) {
        mContext = context;
        mMoments = moments;
    }


    public void updateData(RealmResults<MomentModel> updatedRealmResults) {
        mMoments = new ArrayList<>(updatedRealmResults);
    }


    public void clearData() {
        mMoments.clear();
    }


    public MomentModel getData(int idx) {
        return mMoments.get(idx);
    }


    public void removeData(int idx) {
        mMoments.remove(idx);
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        //display preference
        if (mContext.getSharedPreferences("com.monsoonblessing.moments", Context.MODE_PRIVATE).getBoolean("DefaultDisplay", true)) {
            v = LayoutInflater.from(mContext).inflate(R.layout.item_row, parent, false);
        } else {
            v = LayoutInflater.from(mContext).inflate(R.layout.item_spotlight, parent, false);
        }
        return new MyViewHolder(v);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final MomentModel moment = mMoments.get(position);

        String title = moment.getTitle();
        Date dateObj = new Date(moment.getDateLong());
        SimpleDateFormat f = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String date = f.format(dateObj);

        holder.cardTitle.setText(title);
        holder.cardDate.setText(date);

        Typeface myTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Raleway-Regular.ttf");
        holder.cardTitle.setTypeface(myTypeface);
        holder.cardDate.setTypeface(myTypeface);

        Log.d(TAG, "Loading photo: " + moment.getPhotoUri());
        Uri uri = Uri.parse(moment.getPhotoUri());

        Glide.with(mContext)
                .load(uri)
                .fitCenter()
                .placeholder(R.drawable.greyplaceholder)
                .into(holder.cardImage);
    }


    @Override
    public int getItemCount() {
        return mMoments.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView cardImage;
        public TextView cardTitle;
        public TextView cardDate;
/*        public TextView editText;
        public TextView deleteText;*/


        public MyViewHolder(View itemView) {
            super(itemView);
            cardImage = (ImageView) itemView.findViewById(R.id.cardImage);
            cardTitle = (TextView) itemView.findViewById(R.id.cardTitle);
            cardDate = (TextView) itemView.findViewById(R.id.cardDate);
           /* editText = (TextView) itemView.findViewById(R.id.edit_text);
            deleteText = (TextView) itemView.findViewById(R.id.delete_text);*/
        }


    }
}


















