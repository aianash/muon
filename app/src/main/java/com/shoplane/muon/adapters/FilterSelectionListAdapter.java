package com.shoplane.muon.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shoplane.muon.R;

import java.util.List;

/**
 * Created by ravmon on 2/10/15.
 */
public class FilterSelectionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final Context mContext;
    private final List<String> mFilterSelectionItemList;
    private static OnItemClickListener mActiveFilterItemClickListener;

    // class for caching the views in a row
    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mTextView;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.mTextView = (TextView) itemView.findViewById(
                    R.id.filter_selection_item);

            // Setup the click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {
                    if (mActiveFilterItemClickListener != null)
                        mActiveFilterItemClickListener.onItemClick(itemView, getLayoutPosition());
                }
            });

        }
    }

    public FilterSelectionListAdapter(Context context, List<String> filterSelectionItemList) {
        this.mContext = context;
        this.mFilterSelectionItemList = filterSelectionItemList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View activeFilterView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.filter_selection_list_row, parent, false);
        return new ViewHolder(activeFilterView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String filterSelectionText = mFilterSelectionItemList.get(position);
        filterSelectionText += "     X";
        ((ViewHolder)holder).mTextView.setText(filterSelectionText);
    }

    @Override
    public int getItemCount() {
        return mFilterSelectionItemList.size();
    }

    // Listener for click event
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mActiveFilterItemClickListener = listener;
    }

}
