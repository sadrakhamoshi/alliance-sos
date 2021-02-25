package com.kaya.alliancesos.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kaya.alliancesos.Event;
import com.kaya.alliancesos.R;

import java.util.List;
import java.util.TimeZone;

public class EventLogAdapter extends ArrayAdapter<Event> {

    private Context mContext;
    private List<Event> mEvents;

    public EventLogAdapter(@NonNull Context context, @NonNull List<Event> objects) {
        super(context, 0, objects);
        this.mContext = context;
        this.mEvents = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.event_log_pattern, parent, false);
        Event mEvent = mEvents.get(position);

        TextView title, madeby, date;
        title = view.findViewById(R.id.event_log_pattern_title);
        madeby = view.findViewById(R.id.event_log_pattern_created);
        date = view.findViewById(R.id.event_log_pattern_date);
        title.setText(mEvent.getScheduleObject().getTitle());
        madeby.setText(mEvent.getCreatedBy());
        String converted_time = mEvent.getScheduleObject().GetDate(mEvent.getCreatedTimezoneId(), TimeZone.getDefault().getID());
        date.setText(converted_time);
        return view;
    }
}
