package com.askel.oiyu;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference usersReference;

    private FirebaseAuth mAuth;

    private String  mCurrentUser_id;


    private View mMainView;

    public FriendsFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflator,ViewGroup container,Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mMainView=inflator.inflate(R.layout.fragment_friends,container,false);

        mFriendsList=mMainView.findViewById(R.id.friends_list);
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth=FirebaseAuth.getInstance();

        mCurrentUser_id=mAuth.getCurrentUser().getUid();
       // final String current_uid = mCurrentUser_id.getUid();





//        userID.setUserId(mCurrentUser_id);
        mFriendsDatabase=FirebaseDatabase.getInstance().getReference("Friends").child(mCurrentUser_id);
        usersReference=FirebaseDatabase.getInstance().getReference().child("Users");

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        startListening();

    }

    public void startListening() {

       // final String friendsID=usersReference.getKey();



        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Friends");


        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, Friends.class)
                        .build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {

            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final FriendsViewHolder holder, int position, Friends model) {
                // Bind the Chat object to the ChatHolder
               // holder.setName(model.getName());
              //  holder.setImage(model.getImage(),getApplicationContext());

               final String user_id=getRef(position).getKey();
                usersReference.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String userName=dataSnapshot.child("name").getValue().toString();
                        String image=dataSnapshot.child("image").getValue().toString();
                        holder.setName(userName);
                        holder.setImage(image,getContext());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent=new Intent(getActivity(), ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });
                // ...
            }

        };

        mFriendsList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
         View mView;

        public FriendsViewHolder(View itemView) {
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
    }


}