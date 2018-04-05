package com.askel.oiyu;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class UsersActivity extends AppCompatActivity {
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onStart() {
        super.onStart();
        startListening();

    }

    public void startListening() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .limitToLast(50);

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, Users.class)
                        .build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(options) {
            @Override
            public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(UserViewHolder holder, int position, Users model) {
                // Bind the Chat object to the ChatHolder
                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                // ...
            }

        };

        mUsersList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class UserViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public UserViewHolder(View itemView) {
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
    }
}



//    @Override
//    protected void onStart() {
//
//        super.onStart();
//        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
//                Users.class,
//                R.layout.users_single_layout,
//                UsersViewHolder.class,
//                mUsersDatabaseReference
//        ) {
//            @Override
//            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {
//
//            }
//
//            @NonNull
//            @Override
//            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                return null;
//            }
//        };
//    }
//    @Override
//    protected void onStart(){
//        super.onStart();
//        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
//                Users.class,
//                R.layout.users_single_layout,
//                UsersViewHolder.class,
//                mUsersDatabaseReference
//        ) {
//            @Override
//            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {
//
//                UsersViewHolder.setDisplayName(model.getName());
//
//            }
//
//        };
//        mUsersList.setAdapter(firebaseRecyclerAdapter);
//
//    }
//    public static class UsersViewHolder extends RecyclerView.ViewHolder{
//        static View mView;
//        public UsersViewHolder(View itemView) {
//            super(itemView);
//            mView = itemView;
//        }
//        public static void setDisplayName(String name){
//            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
//            userNameView.setText(name);
//        }
//    }
//        }
//
////            protected void populateViewHolder(UsersViewHolder usersViewHolder, Users users, int i) {
////            usersViewHolder.setName(users.getName());
////            }
////        };
//        mUsersList.setAdapter(firebaseRecyclerAdapter);
//    }
//
//
//
//    public static class UsersViewHolder extends RecyclerView.ViewHolder{
//
//        View mView;
//
//        public UsersViewHolder(View itemView) {
//            super(itemView);
//            mView=itemView;
//        }
//        public void setName(String name){
//            TextView userNameView=(TextView) mView.findViewById(R.id.user_single_name);
//            userNameView.setText(name);
//        }
//    }
//}
