package com.example.alliancesos.GroupSetting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alliancesos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class GroupMemberActivity extends AppCompatActivity {

    ArrayList<String> members;
    ListView mListView;
    ArrayAdapter<String> mArrayAdapter;
    ProgressBar progressBar;

    String mGroupId;
    DatabaseReference mRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member);
        mGroupId = getIntent().getStringExtra("groupId");
        mRoot = FirebaseDatabase.getInstance().getReference();
        InitUi();
    }

    private void InitUi() {
        mListView = findViewById(R.id.list_view_members);
        members = new ArrayList<>();
        progressBar = findViewById(R.id.progress_members);
        mArrayAdapter = new ArrayAdapter<String>(this, R.layout.member_pattern, members) {
            @NonNull
            @Override
            public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_pattern, parent, false);
                view.findViewById(R.id.delete_member).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeAlertDialog(members.get(position));
                    }
                });
                TextView text = view.findViewById(R.id.member_name);
                text.setText(members.get(position));
                text.setTextColor(Color.WHITE);
                return view;
            }
        };
        mListView.setAdapter(mArrayAdapter);

    }

    private void makeAlertDialog(final String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setIcon(R.drawable.sos_icon);
        builder.setTitle("Attention");
        builder.setMessage("Are you sure you want to remove " + s + " ?");
        builder.setCancelable(true);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FindInDB(s);
            }
        });
        builder.create().show();
    }

    private void FindInDB(final String name) {
        progressBar.setVisibility(View.VISIBLE);
        mRoot.child("groups").child(mGroupId).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String key = "";
                    Iterator iterator = snapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot dataSnapshot = (DataSnapshot) iterator.next();
                        String name2 = dataSnapshot.child("userName").getValue().toString();
                        if (name.equals(name2)) {
                            key = dataSnapshot.getKey();
                            break;
                        }
                    }
                    if (!TextUtils.isEmpty(key)) {
                        deleteFromDatabase(key, name);
                    }
                    progressBar.setVisibility(View.GONE);

                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(GroupMemberActivity.this, "error not Exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupMemberActivity.this, "error " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteFromDatabase(final String key, final String name) {
        progressBar.setVisibility(View.VISIBLE);
        mRoot.child("groups").child(mGroupId).child("members").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mRoot.child("users").child(key).child("Groups").child(mGroupId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);
                            if (!task.isSuccessful())
                                Toast.makeText(GroupMemberActivity.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                            else{
                                members.remove(name);
                                mArrayAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    public void goBackGroupSetting(View view) {
        finish();
        return;
    }

    @Override
    protected void onStart() {
        super.onStart();
        showAllMember();
    }

    private void showAllMember() {
        progressBar.setVisibility(View.VISIBLE);
        mRoot.child("groups").child(mGroupId).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists())
                    Toast.makeText(GroupMemberActivity.this, "Not Exist", Toast.LENGTH_SHORT).show();
                else {

                    Iterator iterator = snapshot.getChildren().iterator();

                    ArrayList<String> names = new ArrayList<>();
                    while (iterator.hasNext()) {
                        DataSnapshot dataSnapShot = (DataSnapshot) iterator.next();
                        String name = dataSnapShot.child("userName").getValue().toString();
                        names.add(name);
                    }
                    members.clear();
                    members.addAll(names);
                    mArrayAdapter.notifyDataSetChanged();
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupMemberActivity.this, "error in show " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}