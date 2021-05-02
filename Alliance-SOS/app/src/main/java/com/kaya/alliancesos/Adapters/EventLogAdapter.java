package com.kaya.alliancesos.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.kaya.alliancesos.Event;
import com.kaya.alliancesos.R;

import java.util.List;
import java.util.TimeZone;

public class EventLogAdapter extends ArrayAdapter<Event> {

    private Context mContext;
    private List<Event> mEvents;
    private String mGroupId;
    private boolean isAdmin;

    public EventLogAdapter(@NonNull Context context, @NonNull List<Event> objects, String mGroupId) {
        super(context, 0, objects);
        this.mContext = context;
        this.mEvents = objects;
        this.mGroupId = mGroupId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.event_log_pattern, parent, false);
        final Event mEvent = mEvents.get(position);
        ImageView trash;
        TextView title, madeby, date;
        trash = view.findViewById(R.id.event_log_trash);
        trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                androidx.appcompat.app.AlertDialog.Builder builderCooking = new AlertDialog.Builder(mContext, R.style.AlertDialog);
                builderCooking.setTitle("Attention")
                        .setIcon(R.drawable.delete_icon)
                        .setMessage("Are You Sure You Want to Delete ?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                remove_from_database(mEvent);
                            }
                        });
                builderCooking.show();
            }
        });

        title = view.findViewById(R.id.event_log_pattern_title);
        madeby = view.findViewById(R.id.event_log_pattern_created);
        date = view.findViewById(R.id.event_log_pattern_date);
        title.setText(mEvent.getScheduleObject().getTitle());
        madeby.setText(mEvent.getCreatedBy());
        String converted_time = mEvent.getScheduleObject().GetDate(mEvent.getCreatedTimezoneId());
        date.setText(converted_time);
        return view;
    }


    private void remove_from_database(final Event mEvent) {
        if (this.isAdmin) {
            FirebaseDatabase.getInstance().getReference().child("groups").child(mGroupId).child("events").child(mEvent.getEventId())
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(mContext, "Error :  " + task.getException(), Toast.LENGTH_SHORT).show();
                    } else {
                        remove(mEvent);
                        notifyDataSetChanged();
                    }
                }
            });
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext,R.style.AlertDialog);
            builder.setMessage("Sorry Only Admin can Edit Events log ...");
            builder.setTitle("Note");
            builder.setIcon(R.drawable.close_icon);
            builder.setNegativeButton("Ok", null);
            builder.create().show();
        }
    }

    public void SetIsAdmin(boolean value) {
        this.isAdmin = value;
    }
}
