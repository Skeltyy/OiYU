package com.askel.oiyu;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendRequestBtn, mDeclineBtn;

    private DatabaseReference mUsersDatabase;

    private ProgressDialog mProgressBar;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    private FirebaseUser mCurrent_user;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_display_name);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_total_friends);
        mProfileSendRequestBtn = (Button) findViewById(R.id.profile_send_requestbtn2);
        mDeclineBtn = (Button) findViewById(R.id.profile_decline_request);

        mCurrent_state = "not_friends";

        mProgressBar = new ProgressDialog(this);
        mProgressBar.setTitle("Loading User Data");
        mProgressBar.setMessage("Please wait while we load the user data");
        mProgressBar.setCanceledOnTouchOutside(false);
        mProgressBar.show();


        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(
                        R.drawable.default_avatar).into(mProfileImage);

                //------------------Friends List/Request Feature-------------------------------//

                mFriendReqDatabase.child(mCurrent_user.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type")
                                    .getValue().toString();
                            if (req_type.equals("recieved")) {

                                mCurrent_state = "req_recieved";
                                mProfileSendRequestBtn.setText("Accept Friend Request");
                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);
                            } else if (req_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mProfileSendRequestBtn.setText("Cancel Friend Request");
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }
                            mProgressBar.dismiss();

                        } else {

                            mFriendDatabase.child(mCurrent_user.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        mCurrent_state = "friends";
                                        mProfileSendRequestBtn.setText("Unfriend");
                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }
                                    mProgressBar.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressBar.dismiss();

                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mProfileSendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendRequestBtn.setEnabled(false);


                //Not friends-request-------------------------------------------------//
                if (mCurrent_state.equals("not_friends")) {

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id)
                            .child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mFriendReqDatabase.child(user_id)
                                                .child(mCurrent_user.getUid()).child("request_type")
                                                .setValue("recieved").addOnSuccessListener(
                                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                HashMap<String, String> notificationData = new
                                                        HashMap<>();
                                                notificationData.put("from", mCurrent_user.getUid());
                                                notificationData.put("type", "request");

                                                mNotificationDatabase.child(user_id).push()
                                                        .setValue(notificationData)
                                                        .addOnSuccessListener(
                                                                new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        mCurrent_state = "req_sent";
                                                        mProfileSendRequestBtn
                                                                .setText("Cancel Friend Request");
                                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                                        mDeclineBtn.setEnabled(false);
                                                    }
                                                });


                                                // Toast.makeText(ProfileActivity.this, "Request Sent Successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Toast.makeText(ProfileActivity.this,
                                                "Failed Sending Request", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                    mProfileSendRequestBtn.setEnabled(true);
                                }
                            });

                }
                //------------------------------------Cancel Request State
                if (mCurrent_state.equals("req_sent")) {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id)
                            .removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid())
                                    .removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendRequestBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendRequestBtn.setText("Send Friend Request");

                                    mDeclineBtn.setEnabled(false);
                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    });
                }
                //-------------------------Request Recieved State-------
                if (mCurrent_state.equals("req_recieved")) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendDatabase.child(mCurrent_user.getUid())
                            .child(user_id).setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override

                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(user_id).child(mCurrent_user.getUid())
                                            .setValue(currentDate).addOnSuccessListener(
                                                    new OnSuccessListener<Void>() {
                                        @Override

                                        public void onSuccess(Void aVoid) {
                                            mFriendReqDatabase.child(mCurrent_user.getUid())
                                                    .child(user_id)
                                                    .removeValue()
                                                    .addOnSuccessListener(
                                                            new OnSuccessListener<Void>() {
                                                @Override

                                                public void onSuccess(Void aVoid) {


                                                    mFriendReqDatabase.child(user_id)
                                                            .child(mCurrent_user.getUid())
                                                            .removeValue()
                                                            .addOnSuccessListener(
                                                                    new OnSuccessListener<Void>() {
                                                        @Override

                                                        public void onSuccess(Void aVoid) {
                                                            mProfileSendRequestBtn.setEnabled(true);
                                                            mCurrent_state = "friends";
                                                            mProfileSendRequestBtn.setText("UnFriend");


                                                            mDeclineBtn.setEnabled(false);
                                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                }
            }
        });
    }
}
