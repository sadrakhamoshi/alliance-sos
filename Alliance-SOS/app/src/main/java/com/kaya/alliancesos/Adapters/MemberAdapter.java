package com.kaya.alliancesos.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kaya.alliancesos.R;

import java.util.ArrayList;


public class MemberAdapter extends ArrayAdapter {

    private ArrayList<String> members;
    private Context mContext;

    public MemberAdapter(@NonNull Context context, @NonNull ArrayList<String> objects) {
        super(context, 0, objects);

        mContext = context;
        members = objects;
    }

    @Override
    public int getCount() {
        return members.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.member_pattern, parent, false);


        return view;
    }
}
