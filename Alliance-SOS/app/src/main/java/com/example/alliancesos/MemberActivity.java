package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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

    private String mGroupId, mGroupName;


    private Button mAddToGroup;
    private ListView mMembersListView;
    private EditText mUsername;

    private ArrayList<String> mMembersList;
    private ArrayAdapter<String> adapter;

    //database
    private DatabaseReference mGroupRef, mUserRef;
    private ChildEventListener mMembersEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);
        Intent intent = getIntent();
        if (intent != null) {
            mGroupId = intent.getStringExtra("groupId");
            mGroupName = intent.getStringExtra("groupName");
        }

        initialize();
    }

    private void initialize() {

        //database
        mGroupRef = FirebaseDatabase.getInstance().getReference().child("groups");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users");

        mMembersListView = findViewById(R.id.members_list_view);
        mMembersList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, mMembersList);
        mMembersListView.setAdapter(adapter);

        mUsername = findViewById(R.id.target_user_edt);

        mAddToGroup = findViewById(R.id.add_to_group_btn);
        mAddToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addition_member = mUsername.getText().toString();
                if (TextUtils.isEmpty(addition_member)) {
                    Toast.makeText(MemberActivity.this, "You Have to write Members Username", Toast.LENGTH_LONG).show();
                } else {
                    addToGroup(addition_member);
                }
            }
        });
    }

    private void addToGroup(final String addition_member) {
        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    boolean isFoundUsername = false;
                    String foundedUserId = null;
                    String foundedUserToken = null;

                    Iterator iterator = snapshot.getChildren().iterator();

                    try {
                        while (iterator.hasNext()) {

                            DataSnapshot dataSnapshot = ((DataSnapshot) iterator.next());
                            String userName = dataSnapshot.child("userName").getValue().toString();

                            if (userName.equals(addition_member)) {
                                isFoundUsername = true;
                                foundedUserId = dataSnapshot.getKey();
                                foundedUserToken = dataSnapshot.child("token").getValue().toString();
                                break;
                            }
                        }
                        if (isFoundUsername) {

                            addingMemberFunc(foundedUserId, addition_member, foundedUserToken);

                        } else {
                            Toast.makeText(MemberActivity.this, addition_member + " not Valid Username", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MemberActivity.this, "on Catch get " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MemberActivity.this, " Don't have any User ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MemberActivity.this, "Error on addToGroup func " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addingMemberFunc(String newMemberId, String newMemberName, String newMemberToken) {
        addToUsersGroups(newMemberId);
        addMemberToGroups(newMemberName, newMemberId, newMemberToken);
    }

    private void addMemberToGroups(final String newMemberName, String newMemberId, String newMemberToken) {
        Member member = new Member(newMemberId, newMemberName);

        mGroupRef.child(mGroupId).child("members").child(member.getId()).setValue(member).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MemberActivity.this, newMemberName + " added to member " + mGroupId + " Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MemberActivity.this, newMemberName + " Can't added to member " + mGroupId, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addToUsersGroups(final String foundedUserId) {
        HashMap<String, String> groupInfo = new HashMap<>();
        groupInfo.put("groupName", mGroupName);
        groupInfo.put("groupId", mGroupId);
        mUserRef.child(foundedUserId).child("Groups").push().setValue(groupInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MemberActivity.this, "Added to Groups of " + foundedUserId, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MemberActivity.this, "Not Added to Groups of " + foundedUserId, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showAllMembers() {
        mGroupRef.child(mGroupId).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Iterator iterator = snapshot.getChildren().iterator();

//                  Set<String> membersName = new HashSet<>();
                    mMembersList.clear();
                    while (iterator.hasNext()) {
                        String memberId = ((DataSnapshot) iterator.next()).child("id").getValue().toString();
                        getUserByIdFromUsers(memberId);
                    }
//                    mMembersList.addAll(membersName);
//                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MemberActivity.this, "Error On showAllMembers Func " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserByIdFromUsers(final String memberId) {
        mUserRef.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("userName").getValue().toString();
                    mMembersList.add(name);
                    adapter.notifyDataSetChanged();
//                    adapter.add(name);
//                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MemberActivity.this, error.getMessage() + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        showAllMembers();
//        attachDatabaseMembersOFGroup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMembersEventListener != null) {
            mGroupRef.child(mGroupId).child("members").removeEventListener(mMembersEventListener);
        }
    }

    private void attachDatabaseMembersOFGroup() {
        if (mMembersEventListener == null) {
            mMembersEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String memberId = snapshot.child("id").getValue().toString();
                    getUserByIdFromUsers(memberId);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
        }
        mGroupRef.child(mGroupId).child("members").addChildEventListener(mMembersEventListener);
    }
}