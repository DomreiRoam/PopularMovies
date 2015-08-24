package com.example.gael.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by gael on 24/08/15.
 */
public class ImageAdapter extends BaseAdapter {
    private final String LOG_TAG = ImageAdapter.class.getSimpleName();
    private Context mContext;
    private int mResource;
    private int mImageViewResourceId;
    private List<Poster> mPosters;
    private LayoutInflater mLayoutInflater;
    private final Object mLock = new Object();
    private boolean mNotifyOnChange = true;

    public ImageAdapter(Context context, int resource, int imageViewResourceId, List<Poster> posters) {
        mContext = context;
        mResource = resource;
        mImageViewResourceId = imageViewResourceId;
        mPosters = posters;
        mLayoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public void add(Poster object) {
        synchronized (mLock) {
                mPosters.add(object);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void clear() {
        synchronized (mLock) {
             mPosters.clear();
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }


    public int getCount() {
        return mPosters.size();
    }

    public Poster getItem(int position) {
        return mPosters.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        // Log.e(LOG_TAG, "R.layout.grid_item_poster" +R.layout.grid_item_poster);

        ImageView imageView;
        if (convertView == null) {
            imageView  = (ImageView)mLayoutInflater.inflate(mResource, null);//mImageViewResourceId
        } else {
            imageView = (ImageView) convertView;
        }
        Poster poster = mPosters.get(position);

        Picasso.with(mContext).load(poster.getValue()).into(imageView);
        return imageView;
    }

}
