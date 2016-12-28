package com.monsoonblessing.moments.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.monsoonblessing.moments.MomentModel;
import com.monsoonblessing.moments.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Kevin on 2016-06-15.
 */
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    private Context mContext;
    private List<MomentModel> mMoments;


    public MyRecyclerViewAdapter(Context context, List<MomentModel> moments) {
        mContext = context;
        mMoments = moments;
    }


    public void updateData(List<MomentModel> moment) {
        mMoments = moment;
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

        Uri uri = Uri.parse(moment.getPhotoUri());

       /*String[] pics = {
                "http://wolfhoundpub.com/wp-content/uploads/2014/12/Haeundae_Beach.jpg",
                "http://s1.it.atcdn.net/wp-content/uploads/2015/08/6-Tokyo.jpg",
                "http://3.bp.blogspot.com/-pzG_woL1Ufw/Ue4roDBxcHI/AAAAAAAACrg/2QyFqmXpQ40/s1600/banpo_rainbow_fountain-1920x1080.jpg",
                "http://images.all-free-download.com/images/wallpapers_large/old_farm_wallpaper_landscape_nature_wallpaper_1439.jpg"
        };

        Random r = new Random();
        int min = 0;
        int max = pics.length - 1;


        String temp = pics[(r.nextInt(max - min + 1) + min)];*/


        Picasso.with(mContext)
                .load(uri)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.greyplaceholder)
                .into(holder.cardImage);

/*        holder.editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateMoment f = UpdateMoment.newInstance(moment.getId(), moment.getPhotoUri().toString(), moment.getTitle(), moment.getDate());
                f.show(((Activity) mContext).getFragmentManager(), "addMoment");
            }
        });

        holder.deleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
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


















