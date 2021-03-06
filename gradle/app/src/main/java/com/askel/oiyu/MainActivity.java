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

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolBar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();

//        mToolBar= (Toolbar) findViewById(R.id.main_page_toolbar);
//        setSupportActionBar(mToolBar);
//        getSupportActionBar().setTitle("OiYU");
        mViewPager=(ViewPager) findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter=new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout=(TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }
    public void onStart(){
        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();

        if (currentUser==null){
            sentToStart();
        }
    }

    private void sentToStart() {
        Intent startIntent=new Intent(MainActivity.this, StartActivity.class);
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
