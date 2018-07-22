package com.askel.oiyu;

import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private List<Messages>userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;
    private FirebaseUser currentUser;
    private TextView userLastSeen;


    public MessageAdapter(List<Messages>userMessagesList){

        this.userMessagesList=userMessagesList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_of_user,parent,false);
        mAuth=FirebaseAuth.getInstance();
        if (currentUser!=null){
            String onlineUserId=mAuth.getCurrentUser().getUid();
            usersReference=FirebaseDatabase.getInstance().getReference().child("Users").child(onlineUserId);
            usersReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }





        return new MessageViewHolder(v);
    }


    ///////////////////////////////////Message View-Displaying Messages///////////////////////////////////////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position){
        String message_sender_id=mAuth.getCurrentUser().getUid();
        Messages messages=userMessagesList.get(position);

        String fromUserId=messages.getFrom();

        if(Objects.equals(fromUserId, message_sender_id))
        {
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);
            holder.messageText.setGravity(Gravity.START);

        }else{
            holder.messageText.setBackgroundResource(R.drawable.message_text_background_two);
            holder.messageText.setTextColor(Color.BLACK);
            holder.messageText.setGravity(Gravity.END);

        }
        holder.messageText.setText(messages.getMessage());
    }
    @Override
    public int getItemCount(){
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public CircleImageView userProfileImage;

        public MessageViewHolder(View view){
            super(view);

            messageText=(TextView) view.findViewById(R.id.message_text);

            //userProfileImage=(CircleImageView) view.findViewById(R.id.messages_profile_image);
        }
    }
}
