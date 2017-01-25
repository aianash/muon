package com.shoplane.muon.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shoplane.muon.R;

import java.util.List;

/**
 * Created by ravmon on 3/11/15.
 */
public class StylesToFilterListAdapter extends BaseAdapter{

    private final Context mContext;
    private final List<String> mStylesToFilterList;
    private ViewHolder viewHolder;
    private LayoutInflater mInflater;

    // class for caching the views in a row
    private class ViewHolder {
        TextView textView;
    }

    @Override
    public int getCount() {
        return mStylesToFilterList.size();
    }

    @Override
    public Object getItem(int position) {
        return mStylesToFilterList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public StylesToFilterListAdapter(Context context, List<String> filterSelectionItemList) {
        this.mContext = context;
        this.mStylesToFilterList = filterSelectionItemList;
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        if (mInflater == null) {
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.style_to_filter_list_row, null);
            final TextView textView = (TextView) convertView.findViewById(
                    R.id.style_to_filter_item);

            viewHolder = new ViewHolder();
            viewHolder.textView = textView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Fill textview
        viewHolder.textView.setText(changeToUpperCase(mStylesToFilterList.get(position)));
        return convertView;
    }

    private String changeToUpperCase(String inputString) {
        if (inputString != null && inputString.trim().length() > 0) {
            String[] splitString = inputString.split("\\s+");
            int length = splitString.length;
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < length; i++) {
                String convertedString = splitString[i];
                stringBuffer.append(Character.toUpperCase(convertedString
                        .charAt(0)));
                stringBuffer.append(convertedString.substring(1).toLowerCase());
                stringBuffer.append(" ");
            }
            return stringBuffer.toString();
        }
        // some issue with string
        return "";
    }
}
