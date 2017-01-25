package com.shoplane.muon.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.shoplane.muon.R;
import com.shoplane.muon.activities.ItemDetailActivity;
import com.shoplane.muon.common.handler.VolleyRequestHandler;

/**
 * Created by ravmon on 23/9/15.
 */
public class ItemDetailImageFragment extends Fragment {
    private static final String TAG = ItemDetailImageFragment.class.getSimpleName();

    private String mImageUrl;
    private ItemDetailActivity mActivity;

    public static ItemDetailImageFragment getInstance(int position, String imageUrl) {
        ItemDetailImageFragment filterPagerFragment = new ItemDetailImageFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("imageUrl", imageUrl);
        filterPagerFragment.setArguments(args);
        return filterPagerFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (ItemDetailActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int position = getArguments().getInt("position", 0);
        mImageUrl = getArguments().getString("imageUrl", "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_detail_image_view, container, false);
        NetworkImageView itemimage = (NetworkImageView) view.findViewById(R.id.item_image);

        itemimage.setDefaultImageResId(R.drawable.ic_no_image);
        itemimage.setErrorImageResId(R.drawable.ic_no_image);
        itemimage.setAdjustViewBounds(true);
        itemimage.setImageUrl(mImageUrl, VolleyRequestHandler.
                getVolleyRequestHandlerInstance(mActivity).getImageLoader());

        return view;
    }
}
