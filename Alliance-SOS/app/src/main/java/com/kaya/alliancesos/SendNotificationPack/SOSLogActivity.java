package com.kaya.alliancesos.SendNotificationPack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.kaya.alliancesos.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaya.alliancesos.SOSObj;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class SOSLogActivity extends AppCompatActivity {

    private ArrayList<String> mSosList;
    private ArrayList<SOSObj> mSOSObjectList;
    private ArrayAdapter<String> mAdapter;

    private String mGroupId;

    private DatabaseReference mSosRef;
    private ValueEventListener mSosListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s_o_s_response);
        getExtras();
        InitUi();
        Initialize();
    }

    private void Initialize() {
        mSosRef = FirebaseDatabase.getInstance().getReference().child("sos").child(mGroupId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachedListenerToSosList();
    }

    private void attachedListenerToSosList() {
        if (mSosListener == null) {
            mSosListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        showSosTask showSosTask = new showSosTask(snapshot);
                        showSosTask.execute();
                    } else {
                        Toast.makeText(SOSLogActivity.this, "Not Exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SOSLogActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };
            mSosRef.addValueEventListener(mSosListener);
        }
    }

    private void InitUi() {
        ListView mListView = findViewById(R.id.sos_list_view);
        mSosList = new ArrayList<>();
        mSOSObjectList = new ArrayList<>();
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSosList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setBackgroundResource(R.drawable.select_back2);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(Color.WHITE);
                return view;
            }
        };
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                int itm = (int) parent.getAdapter().getItemId(position);
                DialogDetailSos(itm);
            }
        });
    }

    private void DialogDetailSos(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setTitle("Detail ");
        builder.setCancelable(true);
        final View detailView = CreateDetailView(position);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.setView(detailView);
            }
        });
        builder.setNegativeButton("Ok", null);
        builder.setPositiveButton("Delete it !!!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder builderCooking = new AlertDialog.Builder(builder.getContext());
                builderCooking.setTitle("Attention")
                        .setIcon(R.drawable.delete_icon)
                        .setMessage("Are You Sure You Want to Delete ?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                remove_sos_database(mSOSObjectList.get(position));
                            }
                        });
                builderCooking.show();
            }
        });
        builder.show();
    }

    private void remove_sos_database(SOSObj sosObj) {
        mSosRef.child(sosObj.getSosId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SOSLogActivity.this, "Deleted Successfully .", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SOSLogActivity.this, "Error : " + task.getException(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private View CreateDetailView(final int position) {
        View root = getLayoutInflater().inflate(R.layout.sos_detail, null, false);
        TextView group, name, message, time_created;
        final ImageView image;
        final ProgressBar progressBar = root.findViewById(R.id.progress_sos_log);
        group = root.findViewById(R.id.detail_group_name);
        time_created = root.findViewById(R.id.detail_time);
        name = root.findViewById(R.id.detail_make_name);
        message = root.findViewById(R.id.detail_message);
        image = root.findViewById(R.id.detail_image);

        //set
        group.setText("Group Name  : " + mSOSObjectList.get(position).getGroupName());
        name.setText("Make By  : " + mSOSObjectList.get(position).getMakeBy());

        setTimeCreated(time_created, mSOSObjectList.get(position));

        if (mSOSObjectList.get(position).getSosMessage() == null) {
            progressBar.setVisibility(View.VISIBLE);
            Picasso.get().load(mSOSObjectList.get(position).getPhotoUrl()).into(image, new Callback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(SOSLogActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            mAdapter.notifyDataSetChanged();

        } else {
            message.setText("Message  : " + mSOSObjectList.get(position).getSosMessage());
            progressBar.setVisibility(View.GONE);
        }
        return root;
    }

    private void setTimeCreated(TextView time_created, SOSObj sosObj) {
        String current_local_time = sosObj.getDateFromUTC();
        time_created.setText("Created time : " + current_local_time);
    }

    private void getExtras() {
        Intent fromSOSMessage = getIntent();
        if (fromSOSMessage != null) {
            mGroupId = fromSOSMessage.getStringExtra("groupId");
        }
    }

    public void exitFromSOSLog(View view) {
        finish();
        return;
    }

    public class showSosTask extends AsyncTask<Void, Void, Void> {

        DataSnapshot snapshot;

        public showSosTask(DataSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Iterator iterator = snapshot.getChildren().iterator();
            mSosList.clear();
            mSOSObjectList.clear();
            while (iterator.hasNext()) {
                SOSObj dataSnapshot = ((DataSnapshot) iterator.next()).getValue(SOSObj.class);
                mSOSObjectList.add(dataSnapshot);
            }
            sortByTimeCreated();
            return null;
        }
    }

    private void sortByTimeCreated() {
        Collections.sort(mSOSObjectList, new Comparator<SOSObj>() {
            @Override
            public int compare(SOSObj o1, SOSObj o2) {
                if (o1.getTimeStamp() == o2.getTimeStamp())
                    return 0;
                return o1.getTimeStamp() > o2.getTimeStamp() ? -1 : 1;
            }
        });
        for (SOSObj obj :
                mSOSObjectList) {
            mSosList.add("Make By  :   " + obj.getMakeBy());
        }
    }

}