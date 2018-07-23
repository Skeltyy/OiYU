package com.askel.oiyu;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView myChatsList;
    private DatabaseReference FriendsReference;
    private DatabaseReference UsersReference;
    private FirebaseAuth mAuth;

    String online_user_id;

    private View myMainView;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myMainView=inflater.inflate(R.layout.fragment_chats, container, false);

        myChatsList=(RecyclerView) myMainView.findViewById(R.id.chats_list);

        mAuth=FirebaseAuth.getInstance();
        online_user_id=mAuth.getCurrentUser().getUid();

        FriendsReference= FirebaseDatabase.getInstance().getReference().child("Friends")
                .child(online_user_id);
        UsersReference=FirebaseDatabase.getInstance().getReference().child("Users");
        myChatsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myChatsList.setLayoutManager(linearLayoutManager);
        // Inflate the layout for this fragment
        return myMainView;
    }
////////////////////////////////////Retrieves the Chats from Firebase////////////////////////////////////////////////////////////////
    @Override
    public void onStart() {
        super.onStart();
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Friends");


        FirebaseRecyclerOptions<Chats> options =
                new FirebaseRecyclerOptions.Builder<Chats>()
                        .setQuery(query, Chats.class)
                        .build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>
                (options) {

            @Override
            public ChatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new ChatsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final ChatsViewHolder holder, int position,
                                            Chats model) {
                // Bind the Chat object to the ChatHolder


                final String user_id=getRef(position).getKey();
                UsersReference.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String userName=dataSnapshot.child("name").getValue().toString();
                        String image=dataSnapshot.child("image").getValue().toString();
                        String userStatus=dataSnapshot.child("status").getValue().toString();

                        if (dataSnapshot.hasChild("online")){
                            Object online_Status= dataSnapshot.child("online").getValue();
                            holder.setUserOnline(online_Status);
                        }
                        holder.setName(userName);
                        holder.setImage(image,getContext());
                        holder.setStatus(userStatus);
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (dataSnapshot.child("online").exists()){
                                    Intent chatIntent=new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("user_id",user_id);
                                    chatIntent.putExtra("user_name",userName);
                                    chatIntent.putExtra("online",true);
                                    startActivity(chatIntent);
                                }else{
                                    UsersReference.child(user_id).child("online")
                                            .setValue(ServerValue.TIMESTAMP)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent chatIntent=new Intent(getContext()
                                                    , ChatActivity.class);
                                            chatIntent.putExtra("user_id",user_id);
                                            chatIntent.putExtra("user_name",userName);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



                // ...
            }

        };

        myChatsList.setAdapter(adapter);
        adapter.startListening();
    }

    ////////////////////////////////////////Creating the Holder/////////////////////////////////////////////////////
    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        public void setStatus(String status){
            TextView userStatusView=(TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }
        public void setImage(String image, Context ctx){
            CircleImageView userImage=(CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(image).placeholder(R.drawable.default_avatar).into(userImage);
        }

        public void setUserOnline(Object online_Status) {
            ImageView onlineStatusView=(ImageView) mView.findViewById(R.id.online_status);

            if (online_Status instanceof String){
                onlineStatusView.setVisibility(View.VISIBLE);
            }else {
                onlineStatusView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
