package com.askel.oiyu;

import android.content.Context;
import android.media.Image;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID;
    private String messageReceiverName;
    private Toolbar ChatToolBar;
    private TextView userNameTitle;
    private TextView userLastSeen;
    private CircleImageView userChatProfileImage;

    private ImageButton SendMessageButton,SelectImageButton;
    private EditText InputMessageText;




    private DatabaseReference rootRef;

    private FirebaseAuth mAuth;
    private String messageSenderID;

    private RecyclerView userMessagesList;

    private final List<Messages> messageList=new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;

    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        messageSenderID=mAuth.getCurrentUser().getUid();


        messageReceiverID=getIntent().getExtras().get("user_id").toString();
        messageReceiverName=getIntent().getExtras().get("user_name").toString();

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View action_bar_view=layoutInflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);

        userNameTitle=findViewById(R.id.custom_profile_name);
        userLastSeen=findViewById(R.id.custom_user_last_seen);
        userChatProfileImage=(CircleImageView) findViewById(R.id.custom_profile_image);


        SendMessageButton=(ImageButton)findViewById(R.id.send_message_btn);
        SelectImageButton=(ImageButton) findViewById(R.id.select_image);

        InputMessageText=(EditText) findViewById(R.id.input_message);

        messageAdapter=new MessageAdapter(messageList);

        userMessagesList=(RecyclerView) findViewById(R.id.messages_list_of_users);

        linearLayoutManager=new LinearLayoutManager(this);

        userMessagesList.setHasFixedSize(true);

        userMessagesList.setLayoutManager(linearLayoutManager);

        userMessagesList.setAdapter(messageAdapter);

        FetchMessages();

        userNameTitle.setText(messageReceiverName);

        rootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String online=dataSnapshot.child("online").getValue().toString();
                final String userThumb=dataSnapshot.child("image").getValue().toString();

                Picasso.with(ChatActivity.this).load(userThumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(userChatProfileImage);
                CircleImageView userImage=(CircleImageView) findViewById(R.id.user_single_image);
               if (online.equals("true")){
                   userLastSeen.setText("Currently Online");
               }else{
                   LastSeenTime getTime=new LastSeenTime();

                   long last_seen=Long.parseLong(online);

                   String lastSeenDisplayTime=getTime.getTimeAgo(last_seen,getApplicationContext()).toString();


                   userLastSeen.setText(lastSeenDisplayTime);
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();


            }
        });


    }

    private void FetchMessages() {
        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages=dataSnapshot.getValue(Messages.class);

                messageList.add(messages);

                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage() {
        String messageText=InputMessageText.getText().toString();
        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(ChatActivity.this,"Please enter a message.",Toast.LENGTH_SHORT).show();
        }else{
            String message_sender_ref="Messages/"+messageSenderID+"/"+messageReceiverID;

            String message_receiver_ref="Messages/"+messageReceiverID+"/"+messageSenderID;

            DatabaseReference  user_message_key=rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();

            String message_push_id=user_message_key.getKey();

            Map messageTextBody=new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("seen",false);
            messageTextBody.put("type","text");
            messageTextBody.put("time", ServerValue.TIMESTAMP);

            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(message_sender_ref+"/"+message_push_id,messageTextBody);
            messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,messageTextBody);

            rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError!=null){
                        Log.d("Chat_Log",databaseError.getMessage().toString());
                    }

                    InputMessageText.setText("");
                }
            });
        }
    }


}
