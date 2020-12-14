package com.kaya.alliancesos.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kaya.alliancesos.DbForRingtone.ChoiceApplication;
import com.kaya.alliancesos.DoNotDisturb.notDisturbObject;
import com.kaya.alliancesos.R;

import java.util.ArrayList;
import java.util.Date;

public class notDisturbRules extends RecyclerView.Adapter<notDisturbRules.ViewHolder> {

    private ArrayList<notDisturbObject> rulesList;
    private Context mContext;
    private ChoiceApplication mChoice;

    public notDisturbRules(final Context context, ArrayList<notDisturbObject> objects, ChoiceApplication mDb) {
        mContext = context;
        rulesList = objects;
        mChoice = mDb;
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
        try {
            String dayOfWeek = notDisturbObject.DisplayDayOfWeek(current.day);
            holder.dayInWeek.setText(dayOfWeek);
        } catch (Exception e) {
            Toast.makeText(mContext, "Error in Parse : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (current.repeated || current.daily) {
            //do nothing
        } else {
            try {
                Date now = new Date();
                Date date = notDisturbObject.DisplayDate(current.day, current.until);
                if (now.after(date)) {
                    holder.isPassed.setText("Passed");
                }
            } catch (Exception e) {
                Toast.makeText(mContext, "Error in Parse " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        holder.until.setText(current.until);
        holder.from.setText(current.from);
        holder.repeat.setChecked(current.repeated);
        holder.daily.setChecked(current.daily);
        holder.repeat.setEnabled(false);
        holder.daily.setEnabled(false);
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mChoice.appDatabase.disturbDao().deleteRule(current);
                        rulesList.remove(current);
                    }
                }).start();
                Toast.makeText(mContext, "Refresh the Page after a sec ...", Toast.LENGTH_SHORT).show();
            }
        });

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

        TextView from, until, isPassed;
        TextView day, dayInWeek;
        CheckBox repeat, daily;
        ImageView delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            isPassed = itemView.findViewById(R.id.is_passed_do_not_disturb);
            from = itemView.findViewById(R.id.from_do_not_disturb);
            until = itemView.findViewById(R.id.until_do_not_disturb);
            daily = itemView.findViewById(R.id.daily_switch);
            repeat = itemView.findViewById(R.id.repeat_switch);
            day = itemView.findViewById(R.id.day_do_not_disturb);
            dayInWeek = itemView.findViewById(R.id.day_in_Week_do_not_disturb);
            delete = itemView.findViewById(R.id.delete_do_not_disturb);
        }
    }
}
