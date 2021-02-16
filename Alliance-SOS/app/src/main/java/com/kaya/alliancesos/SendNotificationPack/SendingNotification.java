package com.kaya.alliancesos.SendNotificationPack;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.kaya.alliancesos.Member;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendingNotification {

    public boolean isInSendMode = false;
    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private APIService mApiService;

    private String targetUserId;

    private Context mContext;
    private String mGroupId, mGroupName;
    private String mFrom;
    private String mFrom_id;

    private DataToSend data;

    private DatabaseReference mGroupRef, mUserRef;

    public SendingNotification(String groupId, String groupName, String from_username, String mFrom_id, Context context, DataToSend dataToSendForSOS) {
        mGroupId = groupId;
        mGroupName = groupName;
        this.mFrom_id = mFrom_id;
        mFrom = from_username;
        data = dataToSendForSOS;
        mContext = context;
        mApiService = Client.getClient(BASE_URL).create(APIService.class);
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        mGroupRef = root.child("groups");
        mUserRef = root.child("users");
    }

    public SendingNotification(Context context, String targetUserId, DataToSend dataToSend) {
        mContext = context;
        this.data = dataToSend;
        this.targetUserId = targetUserId;
        mApiService = Client.getClient(BASE_URL).create(APIService.class);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users");
    }

    public void sendInvitation() {
        mUserRef.child(targetUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String token = snapshot.child("token").getValue().toString();
                    Invite(token);

                } else {
                    Toast.makeText(mContext, "Not exist this user for invite...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage() + " " + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void Invite(String token) {
        NotificationSender sender = new NotificationSender(data, token);
        mApiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        try {
                            Log.e("nothing", response.message() + ' ' + response.isSuccessful() + ' ' + response.body().success);
                            Log.e("nothing3", "msg" + response.toString());
                        } catch (Exception e) {
                            Log.e("nothing", response.message());
                        }
                    } else {
                        Log.e("nothing", response.message() + ' ' + response.isSuccessful() + ' ' + response.body().success);
                        Log.e("nothing3", "msg" + response.toString());
                        Toast.makeText(mContext, "Send Successfully ... ", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Send() {
        mGroupRef.child(this.mGroupId).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (isInSendMode) {

                    Iterator iterator = snapshot.getChildren().iterator();

                    while (iterator.hasNext()) {

                        Member member = ((DataSnapshot) iterator.next()).getValue(Member.class);
                        String id = member.getId();

                        if (!mFrom_id.equals(id)) {

                            String userNameForThisGroup = member.getUserName();
                            if (!member.isNotDisturb()) {
                                goToUsersRef(id, userNameForThisGroup);
                            } else {
                                Toast.makeText(mContext, userNameForThisGroup + " is in Do not Disturb mode....", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    isInSendMode = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "error in sendNotification " + error.getMessage(), Toast.LENGTH_SHORT).show();
                isInSendMode = false;
            }
        });

    }

    private void goToUsersRef(final String userId, final String userNameForThisGroup) {
        mUserRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String token = snapshot.child("token").getValue().toString();
                    if (!TextUtils.isEmpty(token))
                        sendNotif(token, userNameForThisGroup, userId);
                } else {
                    Toast.makeText(mContext, "not exist for sending notifi....", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage() + "\n" + error.getDetails() + " error in gotouserref sendingnotificaiton", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotif(String target_token, String name, String id) {
        data.setToId(id);
        data.setToName(name);
        NotificationSender sender = new NotificationSender(data, target_token);
        mApiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        try {
                            String respo = response.body().toString() + ' ' + response.errorBody().string();

                            Toast.makeText(mContext, respo + '\n' + response, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                        }
                    } else {
                        Toast.makeText(mContext, "Send Successfully ... ", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
