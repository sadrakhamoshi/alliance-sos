package com.example.alliancesos.SendNotificationPack;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.alliancesos.Member;
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

    private static final String BASE_URL = "https://fcm.googleapis.com/";

    private APIService mApiService;

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

    public void Send() {
        mGroupRef.child(this.mGroupId).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator iterator = snapshot.getChildren().iterator();

                while (iterator.hasNext()) {

                    Member member = ((DataSnapshot) iterator.next()).getValue(Member.class);
                    String id = member.getId();
                    if (!mFrom_id.equals(id)) {
                        goToUsersRef(id);
                    } else {
                        Toast.makeText(mContext, "not For Your self..:)", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "error in sendNotification " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToUsersRef(final String userId) {
        mUserRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("userName").getValue().toString();
                    String token = snapshot.child("token").getValue().toString();
                    sendNotif(token, name, userId);

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
                        Toast.makeText(mContext, "Failed ", Toast.LENGTH_LONG).show();
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
