/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.adapters;

import java.util.ArrayList;

import nl.victronenergy.R;
import nl.victronenergy.models.Attribute;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Shows a list of outputs
 */
public class OutputAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<Attribute> mListOutputs;

	/**
	 * Constructor
	 *
	 * @param pContext
	 */
	public OutputAdapter(Context pContext, ArrayList<Attribute> pListOutputs) {
		mContext = pContext;
		mListOutputs = pListOutputs;
	}

	@Override
	public int getCount() {
		if (mListOutputs == null) {
			return 0;
		}
		return mListOutputs.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater li = LayoutInflater.from(parent.getContext());
			convertView = li.inflate(R.layout.spinner_item, parent, false);
		}
		((TextView) convertView.findViewById(R.id.textview_spinner)).setText(mListOutputs.get(position).getLabel());
		return convertView;
	}

	@Override
	public Attribute getItem(int position) {
		if (mListOutputs != null && position < mListOutputs.size()) {
			return mListOutputs.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
