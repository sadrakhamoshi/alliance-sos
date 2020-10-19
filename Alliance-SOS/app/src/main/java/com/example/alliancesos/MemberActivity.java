package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alliancesos.Payment.TransferActivity;
import com.example.alliancesos.SendNotificationPack.DataToSend;
import com.example.alliancesos.SendNotificationPack.SendingNotification;
import com.example.alliancesos.Utils.MessageType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MemberActivity extends AppCompatActivity {

    private String mGroupId, mGroupName, mHostUsername, mHostUserId;

    private ProgressBar progressBar;
    private Button mAddToGroup;
    private ListView mMembersListView;
    private EditText mUsername;

    private ArrayList<String> mMembersList;
    private ArrayAdapter<String> adapter;

    //database
    private DatabaseReference mGroupRef, mUserRef;
    private ChildEventListener mMembersEventListener;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);
        Intent intent = getIntent();
        if (intent != null) {
            mGroupId = intent.getStringExtra("groupId");
            mGroupName = intent.getStringExtra("groupName");
            mHostUsername = intent.getStringExtra("currUsername");
            mHostUserId = intent.getStringExtra("currUserId");
        }

        initialize();
    }

    private void initialize() {
        progressBar = findViewById(R.id.progress_add_member);

        //database
        mGroupRef = FirebaseDatabase.getInstance().getReference().child("groups");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users");

        mMembersListView = findViewById(R.id.members_list_view);
        mMembersList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, mMembersList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(Color.WHITE);
                return view;
            }
        };
        mMembersListView.setAdapter(adapter);

        mUsername = findViewById(R.id.target_user_search_view);

        mAddToGroup = findViewById(R.id.add_to_group_btn);
        mAddToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addition_member = mUsername.getText().toString();
                if (TextUtils.isEmpty(addition_member)) {
                    Toast.makeText(MemberActivity.this, "You Have to write Members Username....", Toast.LENGTH_LONG).show();
                } else {
                    addToGroup(addition_member);
                }
            }
        });

        mToolbar = findViewById(R.id.member_toolbar);
        setSupportActionBar(mToolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, 1, 1, menuIconWithText(getResources().getDrawable(R.drawable.payment_icon, getApplicationContext().getTheme()), "Transfer Credit"));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 1) {
            goToTransferPage();
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToTransferPage() {
        Intent intent = new Intent(this, TransferActivity.class);
        intent.putExtra("userId", mHostUserId);
        startActivity(intent);
    }

    private CharSequence menuIconWithText(Drawable r, String title) {
        r.setBounds(0, 0, r.getIntrinsicWidth(), r.getIntrinsicHeight());
        SpannableString sb = new SpannableString("    " + title);
        ImageSpan imageSpan = new ImageSpan(r, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

    private void addToGroup(final String addition_member) {
        progressBar.setVisibility(View.VISIBLE);
        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    boolean isFoundUsername = false;
                    String foundedUserId = null;

                    Iterator iterator = snapshot.getChildren().iterator();

                    try {
                        while (iterator.hasNext()) {

                            DataSnapshot dataSnapshot = ((DataSnapshot) iterator.next());
                            String userName = dataSnapshot.child("userName").getValue().toString();

                            if (userName.equals(addition_member)) {
                                isFoundUsername = true;
                                foundedUserId = dataSnapshot.getKey();
                                break;
                            }
                        }
                        if (isFoundUsername) {
                            addMemberToGroups(addition_member, foundedUserId);

                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MemberActivity.this, addition_member + " not Valid Username", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MemberActivity.this, "on Catch get " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MemberActivity.this, " Don't have any User ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MemberActivity.this, "Error on addToGroup func " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMemberToGroups(final String foundedUsername, String foundedUserId) {
        DataToSend data = new DataToSend(mHostUsername, mGroupName, mGroupId, MessageType.INVITATION_TYPE);
        data.setToId(foundedUserId);
        data.setToName(foundedUsername);
        AddingMemberTask task = new AddingMemberTask(data, foundedUserId);
        task.execute();
//        SendingNotification sender = new SendingNotification(MemberActivity.this, foundedUserId, data);
//        sender.sendInvitation();
    }

    private void showAllMembers() {
        progressBar.setVisibility(View.VISIBLE);
        mGroupRef.child(mGroupId).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ShowMemberTask task = new ShowMemberTask(snapshot);
                    task.execute();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MemberActivity.this, "Error On showAllMembers Func " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        showAllMembers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMembersEventListener != null) {
            mGroupRef.child(mGroupId).child("members").removeEventListener(mMembersEventListener);
        }
    }

    private void getUserByIdFromUsers(final String memberId) {
        mUserRef.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("userName").getValue().toString();
                    mMembersList.add(name);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MemberActivity.this, error.getMessage() + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void SearchingMembers(View view) {
    }

    public void transferCredit(View view) {
        goToTransferPage();
    }

    public class AddingMemberTask extends AsyncTask<Void, Void, Void> {

        DataToSend dataToSend;
        String targetId;

        public AddingMemberTask(DataToSend dataSnapshot, String id) {
            this.dataToSend = dataSnapshot;
            targetId = id;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SendingNotification sender = new SendingNotification(MemberActivity.this, targetId, dataToSend);
            sender.sendInvitation();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }

    public class ShowMemberTask extends AsyncTask<Void, Void, Void> {

        DataSnapshot dataSnapshot;

        public ShowMemberTask(DataSnapshot dataSnapshot) {
            this.dataSnapshot = dataSnapshot;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Iterator iterator = dataSnapshot.getChildren().iterator();
            Set<String> membersName = new HashSet<>();
            while (iterator.hasNext()) {
                String name = ((DataSnapshot) iterator.next()).child("userName").getValue().toString();
                membersName.add(name);
            }
            mMembersList.clear();
            mMembersList.addAll(membersName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }
}