package com.example.alliancesos.Adapters;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.alliancesos.DbForRingtone.AppDatabase;
import com.example.alliancesos.DoNotDisturb.notDisturbObject;
import com.example.alliancesos.R;
import com.example.alliancesos.Utils.WeekDay;

import java.util.ArrayList;
import java.util.List;

public class notDisturbRules extends RecyclerView.Adapter<notDisturbRules.ViewHolder> {

    private ArrayList<notDisturbObject> rulesList;
    private Context mContext;


    public notDisturbRules(final Context context, ArrayList<notDisturbObject> objects) {
        mContext = context;
        rulesList = objects;
    }

    @NonNull
    @Override
    public notDisturbRules.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.not_distrub_rule_pattern, parent, false);
        ViewHolder holder = new ViewHolder(root);
        return holder;
    }

    public void RemoveAll() {
        rulesList.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull final notDisturbRules.ViewHolder holder, int position) {
        final notDisturbObject current = rulesList.get(position);

        holder.day.setText(current.day);
        holder.until.setText(current.until);
        holder.from.setText(current.from);
        holder.repeat.setChecked(current.daily);
        holder.repeat.setEnabled(false);
        holder.daily.setEnabled(false);
    }

    public void add(notDisturbObject newObj) {
        rulesList.add(newObj);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return rulesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView from, until;
        TextView day;
        CheckBox repeat, daily;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            from = itemView.findViewById(R.id.from_do_not_disturb);
            until = itemView.findViewById(R.id.until_do_not_disturb);
            daily = itemView.findViewById(R.id.daily_switch);
            repeat = itemView.findViewById(R.id.repeat_switch);
            day = itemView.findViewById(R.id.day_do_not_disturb);
        }
    }
}
