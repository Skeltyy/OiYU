package com.askel.oiyu;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import javax.net.ssl.SSLEngineResult;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolBar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;
    FirebaseUser currentUser;
    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();

        currentUser=mAuth.getCurrentUser();

        if (currentUser!=null) {
            String online_user_id=mAuth.getCurrentUser().getUid();
            userReference= FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(online_user_id);
        }



        mViewPager=(ViewPager) findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter=new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout=(TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }
    public void onResume(){
        super.onResume();

        currentUser=mAuth.getCurrentUser();

        if (currentUser==null){
            sentToStart();
        }else if (currentUser!=null){
            userReference.child("online").setValue("true");//If user is online, this will be true

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentUser!=null){
            userReference.child("online").setValue(ServerValue.TIMESTAMP);//If user minimizes the app, the status is offline
        }
    }

    private void sentToStart() {
        Intent startIntent=new Intent(MainActivity.this, StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
      super.onCreateOptionsMenu(menu);
      getMenuInflater().inflate(R.menu.main_menu,menu);

      return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id==R.id.profile_settings){
            Intent accountIntent=new Intent(MainActivity.this,SettingsActivity.class );
            startActivity(accountIntent);
        }
        if(id== R.id.logout) {
            if (currentUser!=null){
                userReference.child("online").setValue(ServerValue.TIMESTAMP);
            }
            FirebaseAuth.getInstance().signOut();
            sentToStart();

        }

        if (id == R.id.all_users) {
            Intent settingsIntent=new Intent(MainActivity.this,UsersActivity.class);
            startActivity(settingsIntent);
        }
            return true;

    }
}
