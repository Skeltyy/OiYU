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

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private List<Messages>userMessagesList;
    private FirebaseAuth mAuth;


    public MessageAdapter(List<Messages>userMessagesList){

        this.userMessagesList=userMessagesList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_of_user,parent,false);
        mAuth=FirebaseAuth.getInstance();
        return new MessageViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position){
        String message_sender_id=mAuth.getCurrentUser().getUid();
        Messages messages=userMessagesList.get(position);

        String fromUserId=messages.getFrom();

        if(Objects.equals(fromUserId, message_sender_id))
        {
            holder.messageText.setBackgroundResource(R.drawable.message_text_background_two);
            holder.messageText.setTextColor(Color.BLACK);
            holder.messageText.setGravity(Gravity.RIGHT);
        }else{
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);
            holder.messageText.setGravity(Gravity.LEFT);
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
