package com.example.alliancesos.Adapters;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alliancesos.Event;
import com.example.alliancesos.R;

import java.util.ArrayList;

public class showEvents extends RecyclerView.Adapter<showEvents.ViewHolder> {

    private ArrayList<Event> mEventList;
    private Context mContext;

    public showEvents(Context context, ArrayList<Event> object) {

        mEventList = object;
        mContext = context;
    }

    @NonNull
    @Override
    public showEvents.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_pattern, parent, false);
        ViewHolder holder = new ViewHolder(root);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull showEvents.ViewHolder holder, int position) {
        Event curr = mEventList.get(position);
        holder.mTitle.setText(curr.getScheduleObject().getTitle());
        holder.mCreated.setText(curr.getCreatedBy());
        String date = curr.getScheduleObject().GetDate();
        holder.mDate.setText(date);

        holder.mRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "go to event page", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEventList.size();
    }

    public void add(Event newEvent) {
        mEventList.add(newEvent);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mDate, mCreated, mTitle;
        ImageView mRight;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mDate = itemView.findViewById(R.id.event_pattern_date);
            mCreated = itemView.findViewById(R.id.event_pattern_created);
            mTitle = itemView.findViewById(R.id.event_pattern_title);
            mRight = itemView.findViewById(R.id.event_pattern_right_arrow);
        }
    }
}
