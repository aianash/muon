package com.shoplane.muon.adapters;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import com.shoplane.muon.fragments.ItemDetailImageFragment;

import java.util.List;

/**
 * Created by ravmon on 23/9/15.
 */
public class ItemDetailImagePagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = ItemDetailImagePagerAdapter.class.getSimpleName();

    private List<String> mItemImageUrl;

    public ItemDetailImagePagerAdapter(FragmentManager fm, List<String> imageUrl) {
        super(fm);
        this.mItemImageUrl = imageUrl;
    }

    @Override
    public Fragment getItem(int position) {
        ItemDetailImageFragment imagePagerFragment = ItemDetailImageFragment.getInstance(position,
                mItemImageUrl.get(position));
        return imagePagerFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        /*SpannableStringBuilder sb = new SpannableStringBuilder(" Page #" + position);

        Drawable myDrawable = new BitmapDrawable();
        myDrawable.setBounds(0, 0, myDrawable.getIntrinsicWidth(), myDrawable.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(myDrawable, ImageSpan.ALIGN_BASELINE);
        sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return sb;*/
        return ((Integer)(position + 1)).toString();
    }

    @Override
    public int getCount() {
        return mItemImageUrl.size();
    }
}

