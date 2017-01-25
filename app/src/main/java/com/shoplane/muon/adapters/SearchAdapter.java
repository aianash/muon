package com.shoplane.muon.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.shoplane.muon.R;
import com.shoplane.muon.common.handler.VolleyRequestHandler;
import com.shoplane.muon.common.utils.userinterface.CircleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by ravmon on 21/8/15.
 */
public class SearchAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = SearchAdapter.class.getSimpleName();

    private final Context mContext;
    private List<JSONObject> mSearchItemList;
    private static OnItemClickListener mSearchItemClickListener;
    private int prevItemPosition = 0;
    private ImageLoader mImageLoader;
    private LinearLayout.LayoutParams mLayoutParams;
    private int mScreenWidth;
    private int mScreenHeight;
    private float mScreenDensity;

    // Item type
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    // ViewHolder for Header
    public class ViewHolderHeader extends RecyclerView.ViewHolder {
        public ViewHolderHeader(final View headerView) {
            super(headerView);
        }
    }

    // ViewHolder for Footer
    public class ViewHolderFooter extends RecyclerView.ViewHolder {
        public ViewHolderFooter(final View footerView) {
            super(footerView);
        }
    }


    // Viewholder for each row element
    public class ViewHolderItem extends RecyclerView.ViewHolder {
        public NetworkImageView mSearchImgView;
        public TextView mSearchItemTitle;
        public CircleButton mSearchItemBrowser;
        public TextView mSearchItemSpecs;


        public ViewHolderItem(final View itemView) {
            super(itemView);
            this.mSearchImgView = (NetworkImageView) itemView.findViewById(
                    R.id.search_list_column_image);
            this.mSearchItemBrowser = (CircleButton) itemView.findViewById(
                    R.id.search_item_browser);
            this.mSearchItemTitle = (TextView) itemView.findViewById(R.id.search_item_title);
            this.mSearchItemSpecs = (TextView) itemView.findViewById(R.id.search_item_specs);

            mSearchImgView.setDefaultImageResId(R.drawable.ic_no_image);
            mSearchImgView.setErrorImageResId(R.drawable.ic_no_image);
            mSearchImgView.setAdjustViewBounds(true);


            //Typeface typeFace = Typeface.createFromAsset(mContext.getAssets(),
             //       "fonts/Roboto-Medium.ttf");
           // mSearchItemSpecs.setTypeface(typeFace);
           // mSearchItemTitle.setTypeface(typeFace);

            Iconify.with(new IoniconsModule());
            mSearchItemBrowser.setImageDrawable(new IconDrawable(mContext,
                    IoniconsIcons.ion_link).colorRes(R.color.materialblue_light).
                    actionBarSize());

            // Setup the click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {
                    if (mSearchItemClickListener != null)
                        mSearchItemClickListener.onItemClick(itemView, getLayoutPosition() - 1);
                }
            });

            // Open item in browser
            mSearchItemBrowser.setOnClickListener(new View.OnClickListener() {
                public void onClick(View searchItemButton) {
                    try {
                        openItemInBrowser(mSearchItemList.get((getAdapterPosition() - 1)).
                                getString("itemUrl"));
                    } catch (JSONException je) {
                        Log.e(TAG, "Failed to get item Url");
                    }
                }

            });
        }
    }

    public SearchAdapter(Context context, List<JSONObject> searchItemList,
                         LinearLayout.LayoutParams layoutParams, int screenWidth,
                         int screenHeight, float screenDensity) {
        this.mContext = context;
        this.mSearchItemList = searchItemList;
        this.mLayoutParams = layoutParams;
        this.mScreenWidth = screenWidth;
        this.mScreenHeight = screenHeight;
        this.mScreenDensity = screenDensity;
        mImageLoader = VolleyRequestHandler.getVolleyRequestHandlerInstance(mContext).
                getImageLoader();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (TYPE_HEADER == viewType) {
            View headerView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.search_list_header, parent, false);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)
                    (mScreenWidth * 0.05), (int) (mScreenHeight * 0.8));
            headerView.setLayoutParams(layoutParams);
            //TextView headerText = (TextView) headerView.findViewById(R.id.search_header_text);
            //headerText.setGravity(Gravity.CENTER);
            return new ViewHolderHeader(headerView);
        } else if (TYPE_FOOTER == viewType) {
            View footerView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.search_list_footer, parent, false);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)
                    (mScreenWidth * 0.05), (int) (mScreenHeight * 0.8));
            footerView.setLayoutParams(layoutParams);
            //TextView footerText = (TextView) footerView.findViewById(R.id.search_footer_text);
            //footerText.setGravity(Gravity.CENTER);
            return new ViewHolderFooter(footerView);
        } else {
            // search item
            View searchViewColumn = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_list_column, parent, false);
            //CardView searchListFrame = (CardView) searchViewColumn.findViewById(R.id.search_list_frame);
            //searchViewColumn.setPadding(10,0,10,0);
            //searchListFrame.setCardBackgroundColor(R.color.cardview_shadow_start_color);
            searchViewColumn.setLayoutParams(mLayoutParams);
            return new ViewHolderItem(searchViewColumn);
        }
    }

    // Replace contents of a view.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderItem) {
            // populate item here
            JSONObject item = mSearchItemList.get(position - 1);
            try {
                JSONArray jsonArr = item.getJSONArray("styles");
                StringBuilder text = new StringBuilder();
                // Style and brand
                int len = jsonArr.length();
                for (int i = 0; i < len - 1; i++) {
                    text.append(jsonArr.getString(i));
                    text.append("-");
                }
                text.append(jsonArr.get(len - 1));
                text.append("\n");
                text.append(item.getString("brand"));
                ((ViewHolderItem) holder).mSearchItemTitle.setText(text.toString());

                // title and size
                text.setLength(0);
                text.append(item.getString("title"));
                text.append("\n");

                jsonArr = item.getJSONArray("sizes");
                len = jsonArr.length();
                for (int i = 0; i < len - 1; i++) {
                    text.append(jsonArr.getString(i));
                    text.append("-");
                }
                text.append(jsonArr.getString(len - 1));
                ((ViewHolderItem) holder).mSearchItemSpecs.setText(text.toString());

                JSONObject imageUrl = item.getJSONObject("images");
                ((ViewHolderItem) holder).mSearchImgView.setImageUrl(imageUrl.getString("primary"),
                        mImageLoader);

            } catch (JSONException je) {
                Log.e(TAG, "Failed to load data for item position " + position);
            }
        }
    }

    // Return the size of your dataset
    @Override
    public int getItemCount() {
        // header and footer included
        return mSearchItemList.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (0 == position) {
            return TYPE_HEADER;
        }

        if ((getItemCount() - 1) == position) {
            return TYPE_FOOTER;
        }

        return TYPE_ITEM;
    }

    private void openItemInBrowser(String itemUrl) {
        String itemPage = itemUrl;
        if (!itemPage.startsWith("http://") && !itemPage.startsWith("https://"))
            itemPage = "https://" + itemPage;

        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(itemPage));
            mContext.startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "Please install a webbrowser",
                    Toast.LENGTH_LONG).show();
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        mSearchItemList.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<JSONObject> searchItemList) {
        mSearchItemList.addAll(searchItemList);
        notifyDataSetChanged();
    }

    // Listener for click event
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mSearchItemClickListener = listener;
    }
}
