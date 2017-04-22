package cn.itcast.musicapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;


import cn.itcast.musicapp.application.MainApplication;
import cn.itcast.musicapp.R;
import cn.itcast.musicapp.adapter.MyFragmentAdapter;
import cn.itcast.musicapp.bean.Mp3Info;

import cn.itcast.musicapp.fragment.LocalFragment;

import cn.itcast.musicapp.fragment.NetMusicFragment;
import cn.itcast.musicapp.service.PlayService;
import layout.MyFragment;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MyFragment.OnFragmentInteractionListener{
    private Fragment fragOne, fragTwo, fragThree;
    private List<Fragment> list_fragment = null;
    private List<String> list_title = null;
    private ViewPager viewPager = null;
    protected PlayService playService;
    private boolean isBound = false;//是否已经绑定
    private boolean isPlaying = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TabLayout t1 = (TabLayout) findViewById(R.id.tabs);


        t1.addTab(t1.newTab().setText("本地音乐"));
        t1.addTab(t1.newTab().setText("网络音乐"));


        list_title = new ArrayList<>();
        list_title.add("本地音乐");
        list_title.add("网络音乐");

        list_fragment = new ArrayList<>();

        LocalFragment localFragment = LocalFragment.newInstance();
        list_fragment.add(localFragment);
        NetMusicFragment netMusicFragment = NetMusicFragment.newInstance();
        list_fragment.add(netMusicFragment);


        viewPager = (ViewPager) findViewById(R.id.viewPaper);
        MyFragmentAdapter mfa = new MyFragmentAdapter(getSupportFragmentManager(), list_fragment, list_title);
        viewPager.setAdapter(mfa);
        t1.setupWithViewPager(viewPager);
        startService(new Intent(this, PlayService.class));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public boolean play(ArrayList<Mp3Info> mp3Infos,int position){
        MainApplication.mp3List = mp3Infos;
        Intent intent = new Intent("play");
        intent.putExtra("updateList",true);
        intent.putExtra("position",position);
        sendBroadcast(intent);
        return true;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
