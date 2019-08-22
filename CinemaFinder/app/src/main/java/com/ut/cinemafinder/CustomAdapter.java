package com.ut.cinemafinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class CustomAdapter extends ArrayAdapter<Theater> {
    private ArrayList<Theater> dataSet;
    Context mContext;

    public CustomAdapter(Context context, ArrayList<Theater> t) {
        super(context, R.layout.custom_list_view, t);
        this.dataSet = t;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater customInflater = LayoutInflater.from(getContext());
        View customView = customInflater.inflate(R.layout.custom_list_view, parent, false);

        Theater t = getItem(position);
        TextView txt = customView.findViewById(R.id.theater_id);
        txt.setText(t.name);

        TextView txt2 = customView.findViewById(R.id.subtitle_id);
        txt2.setText(t.address/*+"\nLat/Lng("+t.coords.latitude+", "+t.coords.longitude+")"*/);

        return customView;
    }
}
