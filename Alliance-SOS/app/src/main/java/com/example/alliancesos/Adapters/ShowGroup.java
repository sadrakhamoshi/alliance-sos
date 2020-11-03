package com.example.alliancesos.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alliancesos.GroupActivity;
import com.example.alliancesos.R;
import com.example.alliancesos.UpComingEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;


public class ShowGroup extends RecyclerView.Adapter<ShowGroup.ViewHolder> {

    private Context mContext;
    private ArrayList<String> mGroupNames, mGroupIds;
    private ArrayList<UpComingEvent> mUpComingEventArrayList;
    private String mUserId, mUsername;

    public ShowGroup(Context context, ArrayList<String> names, ArrayList<String> ids, ArrayList<UpComingEvent> upComeEvent, String userId) {
        mContext = context;
        mUpComingEventArrayList = upComeEvent;
        mUserId = userId;
        mGroupIds = ids;
        mGroupNames = names;
    }

    public HashMap<String, String> getData(int position) {
        HashMap<String, String> item = new HashMap<>();
        item.put("id", mGroupIds.get(position));
        item.put("name", mGroupNames.get(position));
        return item;
    }

    public UpComingEvent getData_upcoming(int position) {
        return mUpComingEventArrayList.get(position);
    }

    public void setUserName(String name) {
        this.mUsername = name;
    }

    @NonNull
    @Override
    public ShowGroup.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_pattern, parent, false);
        ViewHolder viewHolder = new ViewHolder(root);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ShowGroup.ViewHolder holder, final int position) {
        String name = mGroupNames.get(position);
        holder.groupName.setText(name);

        UpComingEvent currEvent = mUpComingEventArrayList.get(position);

        String eventName = currEvent.getUpcomingName();
        if (eventName.length() > 10) {
            eventName = eventName.substring(0, 8);
            eventName += "...";
        }
        holder.groupEvent.setText("event: " + eventName);


        String upComingEventDate = getUpcomingDate(currEvent);

        holder.groupTime.setText("Date: " + upComingEventDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toGroupActivity = new Intent(mContext, GroupActivity.class);
                toGroupActivity.putExtra("groupName", mGroupNames.get(position));
                toGroupActivity.putExtra("groupId", mGroupIds.get(position));
                toGroupActivity.putExtra("currUserName", mUsername);
                toGroupActivity.putExtra("currUserId", mUserId);
                mContext.startActivity(toGroupActivity);
            }
        });
    }

    private String getUpcomingDate(UpComingEvent currEvent) {
        if (currEvent.getUpcomingTime() == null) {
            return "_____";
        }
        String curr_zoneId = TimeZone.getDefault().getID();
        String source_zoneId = currEvent.getSourceTimeZoneId();
        String converted_date = currEvent.getUpcomingTime().ConvertTime(source_zoneId, curr_zoneId);
        return converted_date;
    }

    public void add(String name, UpComingEvent upComingEvent, String id) {
        mGroupNames.add(name);
        mGroupIds.add(id);
        mUpComingEventArrayList.add(upComingEvent);
        notifyDataSetChanged();
    }

    public void remove(String name, UpComingEvent upComingEvent, String id) {
        mGroupNames.remove(name);
        mUpComingEventArrayList.remove(upComingEvent);
        mGroupIds.remove(id);
        notifyDataSetChanged();
    }

    public void clearAll() {
        mGroupNames.clear();
        mGroupIds.clear();
        mUpComingEventArrayList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mGroupNames.size();
    }

    public void removeItem(int position) {
        mGroupNames.remove(position);
        mGroupIds.remove(position);
        notifyDataSetChanged();
    }

    public void restoreItem(String mUsername, String mUserId, int position) {
        mGroupNames.add(position, mUsername);
        mGroupIds.add(position, mUserId);
        notifyItemInserted(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageView;
        TextView groupName, groupEvent, groupTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.pattern_group_name);
            groupEvent = itemView.findViewById(R.id.pattern_group_event_upComing);
            groupTime = itemView.findViewById(R.id.pattern_group_time);
        }
    }
}
