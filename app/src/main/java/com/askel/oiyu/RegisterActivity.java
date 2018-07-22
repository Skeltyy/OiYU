package com.askel.oiyu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword,mPhoneNumber;
    private Button mCreateBtn;

    private FirebaseAuth mAuth;
    private ProgressDialog mRegProcess;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProcess=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();

        mDisplayName=(TextInputLayout) findViewById(R.id.status_name);
        mEmail= (TextInputLayout)findViewById(R.id.login_login_email);
        mPassword=(TextInputLayout) findViewById(R.id.login_login_password);
        mCreateBtn=(Button) findViewById(R.id.reg_reg_btn);
        mPhoneNumber=findViewById(R.id.reg_phoneNumber);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name=mDisplayName.getEditText().getText().toString();
                String email=mEmail.getEditText().getText().toString();
                String password=mPassword.getEditText().getText().toString();
                String phoneNumber=mPhoneNumber.getEditText().getText().toString();

                if (!TextUtils.isEmpty(display_name)||!TextUtils.isEmpty(email)||!TextUtils
                        .isEmpty(password))//|!TextUtils.isEmpty(phoneNumber))
                        {
                    mRegProcess.setTitle("Registering User");
                    mRegProcess.setMessage("Please wait while we create your account!");
                    mRegProcess.setCanceledOnTouchOutside(false);
                    mRegProcess.show();
                    register_user(display_name,email,password,phoneNumber);
                }




            }
        });
    }
    public void register_user(final String display_name, String email, String password, final String phoneNumber){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    FirebaseUser currentUser=FirebaseAuth.getInstance().getCurrentUser();
                    String uid=currentUser.getUid();
                    mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    HashMap<String, String> userMap=new HashMap<>();
                    userMap.put("name",display_name);
                    userMap.put("status", "Hi, I am using OiYU!");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");
                    //userMap.put("phone_number",phoneNumber);

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                 mRegProcess.dismiss();

                                Intent mainIntent=new Intent(RegisterActivity.this,
                                        MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.
                                        FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });



                }else{
                    mRegProcess.hide();
                    Toast.makeText(RegisterActivity.this,
                            "Cannot Register your account. Please check your details and try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

        });

    }

}
