package com.askel.oiyu;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Todo-Assign the accept and decline buttons on the request fragments-//
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView myRequestsList;
    private View myMainView;

    private DatabaseReference friendsRequestReference, useraReference;

    private FirebaseAuth mAuth;

    String online_User_Id;

    private String mCurrent_state;



    private DatabaseReference FriendsDatabaseRef;
    private DatabaseReference FriendsReqDatabaseRef;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        myRequestsList = (RecyclerView) myMainView.findViewById(R.id.requests_list);

        mAuth = FirebaseAuth.getInstance();
        online_User_Id = mAuth.getCurrentUser().getUid();

        friendsRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(online_User_Id);
        useraReference = FirebaseDatabase.getInstance().getReference().child("Users");

        FriendsDatabaseRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsRequestReference=FirebaseDatabase.getInstance().getReference().child("Friend_req");

        mCurrent_state = "not_friends";


        myRequestsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myRequestsList.setLayoutManager(linearLayoutManager);


        // Inflate the layout for this fragment
        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        startListening();
    }

    public void startListening() {


        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Friend_req").child(online_User_Id);


        FirebaseRecyclerOptions<Requests> options =
                new FirebaseRecyclerOptions.Builder<Requests>()
                        .setQuery(query, Requests.class)
                        .build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Requests, RequestViewHolder>(options) {

            @Override
            public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.friend_request_all_users_layout, parent, false);

                return new RequestViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final RequestViewHolder holder, int position, Requests model) {
                final String list_Users_Id = getRef(position).getKey();

                final DatabaseReference get_Type_Ref=getRef(position).child("request_type").getRef();
                get_Type_Ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            final String request_type=dataSnapshot.getValue().toString();

                            if (request_type.equals("recieved")){
                                useraReference.child(list_Users_Id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String userName = dataSnapshot.child("name").getValue().toString();
                                        final String image = dataSnapshot.child("image").getValue().toString();
                                        final String userStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.setUserName(userName);
                                        holder.setImage(image, getContext());
                                        holder.setStatus(userStatus);
                                        holder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {


                                                final CharSequence options[]=new CharSequence[]{
                                                        "Accept Friend Request","Cancel Friend Request"
                                                };

                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle("Friend Request Options");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int position) {
                                                        if (position==0){


                                                            if (mCurrent_state.equals("req_recieved")) {

                                                                final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                                                                FriendsDatabaseRef.child(online_User_Id)
                                                                        .child(list_Users_Id).setValue(currentDate)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override

                                                                            public void onSuccess(Void aVoid) {
                                                                                FriendsDatabaseRef.child(list_Users_Id).child(online_User_Id)
                                                                                        .setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override

                                                                                    public void onSuccess(Void aVoid) {
                                                                                        FriendsReqDatabaseRef.child(online_User_Id).child(list_Users_Id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override

                                                                                            public void onSuccess(Void aVoid) {


                                                                                                FriendsReqDatabaseRef.child(list_Users_Id).child(online_User_Id)
                                                                                                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override

                                                                                                    public void onSuccess(Void aVoid) {

                                                                                                        mCurrent_state = "friends";
                                                                                                        Toast.makeText(getContext(),"Friend Request Accepted",Toast.LENGTH_SHORT).show();
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
                                                        if (position==1){

                                                           FriendsReqDatabaseRef.child(online_User_Id).child(list_Users_Id).removeValue()
                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                           if (task.isSuccessful()){
                                                                               FriendsReqDatabaseRef.child(list_Users_Id).child(online_User_Id).removeValue()
                                                                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                           @Override
                                                                                           public void onComplete(@NonNull Task<Void> task) {
                                                                                               if (task.isSuccessful()){
                                                                                                   Toast.makeText(getContext(),"Friend Request Cancelled Successfully",Toast.LENGTH_SHORT).show();
                                                                                               }
                                                                                           }
                                                                                       });
                                                                           }
                                                                       }
                                                                   });

                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }else if(request_type.equals("sent")){

                                Button req_send_btn=holder.mView.findViewById(R.id.request_accept_btn);
                                req_send_btn.setText("Request Sent");

                                holder.mView.findViewById(R.id.request_decline_btn).setVisibility(View.INVISIBLE);

                                useraReference.child(list_Users_Id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String userName = dataSnapshot.child("name").getValue().toString();
                                        final String image = dataSnapshot.child("image").getValue().toString();
                                        final String userStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.setUserName(userName);
                                        holder.setImage(image, getContext());
                                        holder.setStatus(userStatus);

                                        holder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[]=new CharSequence[]{
                                                        "Cancel Friend Request",
                                                };

                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle("Friend Request Sent");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int position) {

                                                        if (position==0){

                                                            if (mCurrent_state.equals("req_sent")) {
                                                                FriendsReqDatabaseRef.child(online_User_Id).child(list_Users_Id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        FriendsReqDatabaseRef.child(list_Users_Id).child(online_User_Id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                Toast.makeText(getContext(),"Friend Request Cancelled",Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }

                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }
        };
        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }


    public class RequestViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public RequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setUserName(String userName) {
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.request_profile_name);
            userNameDisplay.setText(userName);
        }

        public void setImage(String image, final Context ctx) {
            CircleImageView userImage = (CircleImageView) mView.findViewById(R.id.request_profile_image);
            Picasso.with(ctx).load(image).placeholder(R.drawable.default_avatar).into(userImage);
        }

        public void setStatus(String status) {
            TextView userStatusView = (TextView) mView.findViewById(R.id.request_profile_status);
            userStatusView.setText(status);
        }
    }
}


