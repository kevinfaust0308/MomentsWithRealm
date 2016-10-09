package com.monsoonblessing.moments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Kevin on 2016-06-21.
 */
public class RecyclerItemListener implements RecyclerView.OnItemTouchListener {

    public interface OnItemClickListener {
        //void OnItemClick(View v, int position);
        void OnItemLongClick(View v, int position);
    }

    private String TAG = RecyclerItemListener.class.getSimpleName();

    private OnItemClickListener mListener;
    private GestureDetector mGestureDetector;


    public RecyclerItemListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {

        mListener = listener;

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public void onLongPress(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && mListener != null) {
                    mListener.OnItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                }
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return super.onDoubleTap(e);
            }
        });
    }


    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return mGestureDetector.onTouchEvent(e);
    }


    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }


    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}


