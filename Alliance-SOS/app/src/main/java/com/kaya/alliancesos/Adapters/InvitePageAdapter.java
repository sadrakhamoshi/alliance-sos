package com.kaya.alliancesos.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.kaya.alliancesos.InviteObject;
import com.kaya.alliancesos.R;
import com.kaya.alliancesos.SendNotificationPack.InvitationResponseActivity;

import java.util.List;

public class InvitePageAdapter extends ArrayAdapter<InviteObject> {

    private Context mContext;
    private List<InviteObject> mInviteObjects;
    private DatabaseReference mInviteReference;

    public InvitePageAdapter(@NonNull Context context, @NonNull List<InviteObject> inviteObjects, DatabaseReference databaseReference) {
        super(context, 0, inviteObjects);
        this.mContext = context;
        this.mInviteReference = databaseReference;
        this.mInviteObjects = inviteObjects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.invite_pattern, parent, false);
        TextView groupId_txt, invited_by_txt;
        Button accept_btn;
        ImageButton decline_btn;
        groupId_txt = view.findViewById(R.id.invite_pattern_groupId);
        invited_by_txt = view.findViewById(R.id.invite_pattern_invitedby);
        accept_btn = view.findViewById(R.id.invite_pattern_accept);
        decline_btn = view.findViewById(R.id.invite_pattern_decline);
        InviteObject inviteObject = mInviteObjects.get(position);

        groupId_txt.setText(inviteObject.getGroupId());
        invited_by_txt.setText(inviteObject.getInvitedBy());
        decline_btn.setOnClickListener(onClickListenerDecline(inviteObject));
        accept_btn.setOnClickListener(onClickListenerAccept(inviteObject));

        return view;
    }

    private View.OnClickListener onClickListenerAccept(final InviteObject inviteObject) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               goToInvitationResponse(inviteObject);
            }
        };
    }

    private void goToInvitationResponse(InviteObject inviteObject) {
        Intent intent = new Intent(mContext, InvitationResponseActivity.class);
        intent.putExtra("groupId", inviteObject.getGroupId());
        intent.putExtra("groupName", inviteObject.getGroupName());
        intent.putExtra("toId", inviteObject.getUserId());
        mContext.startActivity(intent);
    }

    private View.OnClickListener onClickListenerDecline(final InviteObject inviteObject) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeInvitation(inviteObject);
            }
        };
    }

    private void removeInvitation(final InviteObject inviteObject) {
        mInviteReference.child("invite").child(inviteObject.getUserId()).child(inviteObject.getInviteId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mInviteObjects.remove(inviteObject);
                    notifyDataSetChanged();
//                    if (accept)
//                        goToInvitationResponse(inviteObject);
                } else {
                    Toast.makeText(mContext, "Error in adapter " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
