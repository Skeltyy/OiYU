package com.askel.oiyu;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.TimeUnit;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.askel.oiyu.LastSeenTime.doSendMessage;
import static java.lang.System.currentTimeMillis;

public class ChatActivity extends AppCompatActivity {

    int MY_PERMISSIONS_REQUEST_SEND_SMS=1;

    private String messageReceiverID;
    private String messageReceiverName;
    private static final int SECOND_MILLIS = 1000;

    private static final int MINUTE_MILLIS = 20 * SECOND_MILLIS;

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

    private static int Gallery_Pick=1;
    private StorageReference messageImageStorageRef;

    private ProgressDialog loadingBar;

    FirebaseUser currentUser;
    private DatabaseReference userReference;

    private boolean isInChat = true;
    String smsMessageText="You have an unread Message on OiYU.";
    String receiverPhoneNumber;

    final Handler handler = new Handler();
    Runnable refresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();

        currentUser=mAuth.getCurrentUser();
        if (currentUser!=null) {
            String online_user_id=mAuth.getCurrentUser().getUid();
            userReference= rootRef.child("Users")
                    .child(online_user_id);
        }

        messageSenderID=mAuth.getCurrentUser().getUid();
        receiverPhoneNumber=



        messageReceiverID=getIntent().getExtras().get("user_id").toString();
        messageReceiverName=getIntent().getExtras().get("user_name").toString();
        messageImageStorageRef=FirebaseStorage.getInstance().getReference().child("Messages_Pictures");

        loadingBar=new ProgressDialog(this);

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

        RenderMessages();

        refresh = new Runnable() {
            public void run() {
                FetchMessages();
                // Do something
                handler.postDelayed(refresh, 5000);
            }
        };
        handler.post(refresh);


        userNameTitle.setText(messageReceiverName);

        rootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Object online=dataSnapshot.child("online").getValue();
                final String userThumb=dataSnapshot.child("image").getValue().toString();


                Picasso.with(ChatActivity.this).load(userThumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(userChatProfileImage);
                CircleImageView userImage=(CircleImageView) findViewById(R.id.user_single_image);
                if (online instanceof String){
                    if (online.toString().equals(messageSenderID)){
                        userLastSeen.setText("Currently in Chat");
                    }
                    else{
                        userLastSeen.setText("Online");
                    }
                }
                else{
                   LastSeenTime getTime=new LastSeenTime();

                   long last_seen=Long.parseLong(online.toString());

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
        SelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_Pick);
            }
        });
    }

    public void onResume(){
        super.onResume();

        isInChat = true;

        currentUser=mAuth.getCurrentUser();

        if (currentUser==null){
            sentToStart();
        }else if (currentUser!=null){
            userReference.child("online").setValue(messageReceiverID);//If user is online, this will be true

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInChat = false;

        if (currentUser!=null){
            userReference.child("online").setValue(ServerValue.TIMESTAMP);//If user minimizes the app, the status is offline
        }
    }

    private void sentToStart() {
        Intent startIntent=new Intent(ChatActivity.this, StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_Pick&&resultCode==RESULT_OK&&data!=null){


            loadingBar.setTitle("Sending Image");
            loadingBar.setMessage("Please wait while we send your image....");
            loadingBar.show();


            Uri imageUri=data.getData();

            final String message_sender_ref="Messages/"+messageSenderID+"/"+messageReceiverID;

            final String message_receiver_ref="Messages/"+messageReceiverID+"/"+messageSenderID;

            DatabaseReference user_message_key=rootRef.child("Messages").child(messageReceiverID).push();
            final String message_Push_Id=user_message_key.getKey();


            ///////////////////////////////////Sending Images-Not yet Operational///////////////////////////////////////////////////
            StorageReference filePath=messageImageStorageRef.child("Messages_Pictures")
                    .child(message_Push_Id+".jpg");
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    //Toast.makeText(ChatActivity.this, "Picture sent successfully",Toast.LENGTH_SHORT).show();
                    if (task.isSuccessful()){

                        final String downloadUrl=task.getResult().getDownloadUrl().toString();

                        Map messageTextBody=new HashMap();
                        messageTextBody.put("message",downloadUrl);
                        messageTextBody.put("seen",false);
                        messageTextBody.put("type","image");
                        messageTextBody.put("time", ServerValue.TIMESTAMP);
                        //messageTextBody.put("from",messageSenderID);

                        Map messageBodyDetails=new HashMap();
                        messageBodyDetails.put(message_sender_ref+"/"+message_Push_Id,
                                messageTextBody);
                        messageBodyDetails.put(message_receiver_ref+"/"+message_Push_Id,
                                messageTextBody);

                        rootRef.updateChildren(messageBodyDetails,
                                new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError,
                                                   DatabaseReference databaseReference) {
                                if (databaseError!=null){
                                    Log.d("Chat_Log",databaseError.getMessage().toString());
                                }
                                InputMessageText.setText("");

                                loadingBar.dismiss();
                            }
                        });

                        Toast.makeText(ChatActivity.this, "Picture sent successfully",
                                Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }else{
                        Toast.makeText(ChatActivity.this,
                                "Upload failed. Please try again.",Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }
    }

    private void FetchMessages() {
        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        final DataSnapshot dataSnapshot1=dataSnapshot;
                        final Messages messages=dataSnapshot.getValue(Messages.class);
                        Long msgTime=(Long)dataSnapshot.child("time").getValue();

                        if(!messages.isSeen())
                        {
                            if (isInChat && messages.getFrom().equals(messageReceiverID))
                            {
                                messages.setSeen(true);
                                updateMessageInFirebase(dataSnapshot, messages);
                            }

                            if (!messages.isSMSSent()&&doSendMessage(msgTime,MINUTE_MILLIS))
                            {

//                        final CharSequence smsOptions[]=new CharSequence[]{
//                                "Looks like your message has not been noticed. Would you like to send a reminder?"
//                        };
//
//
//                        AlertDialog.Builder builder=new AlertDialog.Builder(getApplicationContext());
//                        builder.setTitle("Message Not Seen");
//                        builder.setItems(smsOptions, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                if (which==0){
                                SendSMS(receiverPhoneNumber, smsMessageText);
                                messages.setSMSSent(true);
                                updateMessageInFirebase(dataSnapshot1, messages);
//                                }
//                            }
//                        });
/////


                                
                                ///

//                                try {
//                                    builder.wait(MINUTE_MILLIS);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                                builder.show();
                            }

                        }
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

    private void RenderMessages() {

        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
               .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        final DataSnapshot dataSnapshot1=dataSnapshot;
                        final Messages messages=dataSnapshot.getValue(Messages.class);

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

    private void updateMessageInFirebase(DataSnapshot dataSnapshot, Messages messages)
    {
        String message_sender_ref = "Messages/" + messageSenderID + "/" + messageReceiverID;
        String message_receiver_ref = "Messages/" + messageReceiverID + "/" + messageSenderID;
        String message_push_id = dataSnapshot.getKey();

        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messages);
        messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messages);

        rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.d("Chat_Log", databaseError.getMessage().toString());
                }

                InputMessageText.setText("");
            }
        });
    }
    private void SendSMS(String phoneNo, String msg)
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        }

        try
        {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        }
        catch (Exception ex)
        {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }


    ////////////////////////////////////////////Sending Messages-Operational/////////////////////////////////////////////////////////
    private void SendMessage() {
        String messageText=InputMessageText.getText().toString();
        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(ChatActivity.this,"Please enter a message.",
                    Toast.LENGTH_SHORT).show();
        }else{
            String message_sender_ref="Messages/"+messageSenderID+"/"+messageReceiverID;

            String message_receiver_ref="Messages/"+messageReceiverID+"/"+messageSenderID;

            DatabaseReference  user_message_key=rootRef.child("Messages").child(messageSenderID)
                    .child(messageReceiverID).push();

            String message_push_id=user_message_key.getKey();

            Map messageTextBody=new HashMap();//Writes to Firebase
            messageTextBody.put("message",messageText);
            messageTextBody.put("seen",false);
            messageTextBody.put("type","text");
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("smsSent",false);

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
