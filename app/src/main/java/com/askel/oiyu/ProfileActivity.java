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

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendRequestBtn;

    private DatabaseReference mUsersDatabase;

    private ProgressDialog mProgressBar;

    private DatabaseReference mFriendReqDatabase;

    private FirebaseUser mCurrent_user;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id=getIntent().getStringExtra("user_id");

        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase= FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mCurrent_user= FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage=(ImageView) findViewById(R.id.profile_image);
        mProfileName=(TextView) findViewById(R.id.profile_display_name);
        mProfileStatus=(TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount=(TextView) findViewById(R.id.profile_total_friends);
        mProfileSendRequestBtn=(Button) findViewById(R.id.profile_send_requestbtn2);

        mCurrent_state="not_friends";

        mProgressBar=new ProgressDialog(this);
        mProgressBar.setTitle("Loading User Data");
        mProgressBar.setMessage("Please wait while we load the user data");
        mProgressBar.setCanceledOnTouchOutside(false);
        mProgressBar.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                //------------------Friends List/Request Feature-------------------------------//

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)){
                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("recieved")){

                                mCurrent_state="req_recieved";
                                mProfileSendRequestBtn.setText("Accept Friend Request");

                            }else if(req_type.equals("sent")){
                                mCurrent_state="req_sent";
                                mProfileSendRequestBtn.setText("Cancel Friend Request");

                            }

                        }

                        mProgressBar.dismiss();
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


                //Not friends-request
                 if (mCurrent_state.equals("not_friends")){

                     mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id)
                             .child("request_type").setValue("sent")
                             .addOnCompleteListener(new OnCompleteListener<Void>() {
                         @Override
                         public void onComplete(@NonNull Task<Void> task) {
                             if (task.isSuccessful()){
                                 mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type")
                                         .setValue("recieved").addOnSuccessListener(new OnSuccessListener<Void>() {
                                     @Override
                                     public void onSuccess(Void aVoid) {

                                         mProfileSendRequestBtn.setEnabled(true);
                                         mCurrent_state="req_sent";
                                         mProfileSendRequestBtn.setText("Cancel Friend Request");
                                        // Toast.makeText(ProfileActivity.this, "Request Sent Successfully", Toast.LENGTH_SHORT).show();
                                     }
                                 });
                             }else{
                                 Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                             }
                         }
                     });

                 }
                 //Cancel Request State
                 if (mCurrent_state.equals("req_sent")){
                     mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void aVoid) {
                             mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                 @Override
                                 public void onSuccess(Void aVoid) {

                                     mProfileSendRequestBtn.setEnabled(true);
                                     mCurrent_state="not_friends";
                                     mProfileSendRequestBtn.setText("Send Friend Request");

                                 }
                             });
                         }
                     });

                 }
            }
        });
    }
}
