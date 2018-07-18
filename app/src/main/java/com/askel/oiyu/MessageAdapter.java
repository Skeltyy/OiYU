package com.askel.oiyu;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private List<Messages>userMessagesList;

    public MessageAdapter(List<Messages>userMessagesList){

        this.userMessagesList=userMessagesList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_of_user,parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position){
        Messages messages=userMessagesList.get(position);
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
            userProfileImage=(CircleImageView) view.findViewById(R.id.messages_profile_image);
        }
    }
}
