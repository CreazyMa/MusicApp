package cn.itcast.musicapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import cn.itcast.musicapp.adapter.MyFragmentAdapter;
import cn.itcast.musicapp.bean.Mp3Info;

import cn.itcast.musicapp.fragment.LocalFragment;

import cn.itcast.musicapp.fragment.NetMusicFragment;
import cn.itcast.musicapp.service.PlayService;
import cn.itcast.musicapp.util.MediaUtils;
import layout.MyFragment;
import layout.MyFragment.OnFragmentInteractionListener;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener {
    private Fragment fragOne, fragTwo, fragThree;
    private List<Fragment> list_fragment = null;
    private List<String> list_title = null;
    private ViewPager viewPager = null;
    private TextView tvSongName;
    private TextView tvSinger;
    private ImageView ivSongPic;
    private ImageView ivPlay;
    private ImageView ivNext;
    protected PlayService playService;
    private boolean isBound = false;//是否已经绑定
    private boolean isPlaying = false;
    private BroadcastReceiver mReceiver;
    private boolean ifPlaying = false;

    //新控件，显示进度条
    private ProgressBar pb_time;
    private Timer mTimer;
    private TimerTask mTimerTask;

    private static final String TAG="mainactivity";



    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayService.PlayBinder playBinder = (PlayService.PlayBinder) iBinder;
            playService = playBinder.getPlayService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            playService = null;
            isBound = false;
        }
    };

    //绑定服务
    public void bindPlayService() {
        if (!isBound) {
            Intent intent = new Intent(this, PlayService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
            isBound = true;
        }
    }

    //解除绑定服务
    public void unbindPlayService() {
        if (isBound) {
            unbindService(conn);
            isBound = false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //网络歌曲的获取
//                sendRequest();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startService(new Intent(this, PlayService.class));
        bindPlayService();




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TabLayout tl = (TabLayout) findViewById(R.id.tabs);




//        t1.addTab(t1.newTab().setText("本地音乐"));
//        t1.addTab(t1.newTab().setText("网络音乐"));
//        t1.addTab(t1.newTab().setText("个人中心"));

        list_title = new ArrayList<>();
        list_title.add("本地音乐");
        list_title.add("网络音乐");
        list_title.add("动态");

        LocalFragment localFragment = LocalFragment.newInstance();
        NetMusicFragment netMusicFragment = NetMusicFragment.newInstance();
        //fragOne = MyFragment.newInstance("本地音乐", "one");
//        fragTwo = MyFragment.newInstance("网络音乐", "暂时没有");
        fragThree = MyFragment.newInstance("动态", "高级拓展");



        list_fragment = new ArrayList<>();
        //list_fragment.add(fragOne);
        list_fragment.add(localFragment);
        list_fragment.add(netMusicFragment);
        list_fragment.add(fragThree);

        viewPager = (ViewPager)findViewById(R.id.viewPaper);
        MyFragmentAdapter mfa = new MyFragmentAdapter(getSupportFragmentManager(),list_fragment,list_title);
        viewPager.setAdapter(mfa);
        tl.setupWithViewPager(viewPager);

        //进度条
        pb_time = (ProgressBar)findViewById(R.id.pb_time);
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
          pb_time.setProgress(playService.getPlayPosition());
            }
        };


        tvSongName = (TextView) findViewById(R.id.text_songName);
        tvSinger = (TextView) findViewById(R.id.text_singer);
        ivNext = (ImageView) findViewById(R.id.imageView3_next);
        ivPlay = (ImageView) findViewById(R.id.imageView2_play_pasue);
        ivSongPic = (ImageView) findViewById(R.id.songImage);


        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPlaying) {//正在播放则暂停
                    isPlaying = true;
                    playService.play(getApplicationContext(),0);
//                    playService.play(first);
                    if (playService.isPlaying()) {
                        ivPlay.setImageResource(R.mipmap.uamp_ic_play_arrow_white_48dp);
                    }
                } else if (playService.isPlaying()) {//正在播放，暂停
                    playService.pasue();
                    ivPlay.setImageResource(R.mipmap.uamp_ic_play_arrow_white_48dp);
                } else {  //暂停则播放
                    playService.start();
                    ivPlay.setImageResource(R.mipmap.uamp_ic_pause_white_48dp);
                }
            }
        });
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!ifPlaying){
                    playService.play(getApplicationContext(),0);
                    ifPlaying = true;
                }else {
                    playService.next();
                }
                }
        });


        mReceiver = new MusicChangeRecriver();
        IntentFilter intentFilter = new IntentFilter(Constant.RECRIVER_MUSIC_CHANGE);

         intentFilter.addAction("action.intent.action.PHONE_STATE");
        intentFilter.addAction("play");
        intentFilter.addAction("pause");
        intentFilter.addAction("pre");
        intentFilter.addAction("next");
        intentFilter.addAction("close");

        registerReceiver(mReceiver, intentFilter);


    }



    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        unbindPlayService();
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

//    public boolean play(int position){
//        Mp3Info mp3Info = playService.play(this,position);
//        tvSongName.setText(mp3Info.getTitle());
//        tvSinger.setText(mp3Info.getArtist());
//        ivSongPic.setImageBitmap(MediaUtils.getArtWork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, true));
//        ivPlay.setImageResource(R.mipmap.uamp_ic_pause_white_48dp);
//        isPlaying = true;
//        return true;
//    }



    public class Constant {
        public static final String DEFAULT_MUSIC_TITLE = "MusicApp";
        public static final String DEFAULT_ARTIST = "郑州轻工业学院";
        public static final String RECRIVER_MUSIC_CHANGE = "cn.itcast.musicapp";
    }

    private class MusicChangeRecriver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.RECRIVER_MUSIC_CHANGE.equals(intent.getAction())) {
                onPlayStateChanged();
            }
        }
    }

    private void onPlayStateChanged() {
        Mp3Info music = playService.getMusic();
        if (music == null) {
            tvSongName.setText(Constant.DEFAULT_MUSIC_TITLE);
            tvSinger.setText(Constant.DEFAULT_ARTIST);
            ivSongPic.setImageResource(R.mipmap.local_ic);
            ivPlay.setImageResource(R.mipmap.uamp_ic_play_arrow_white_48dp);
            return;
        }
        tvSongName.setText(music.getTitle());
        tvSinger.setText(music.getArtist());
        ivSongPic.setImageBitmap(MediaUtils.getArtWork(getApplicationContext(), music.getId(), music.getAlbumId(), true, true));

        //进度条
        pb_time.setMax((int)music.getDuration());
        pb_time.setProgress(0);


        if (playService.isPlaying()) {
            ivPlay.setImageResource(R.mipmap.uamp_ic_pause_white_48dp);
        } else {
            ivPlay.setImageResource(R.mipmap.uamp_ic_play_arrow_white_48dp);
        }

    }


    public boolean play(int position){//ArrayList<Mp3Info> mp3Infos,
//        requestPermission(102, Manifest.permission.READ_PHONE_STATE, new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this,"已授权，可以监听电话状态 ",Toast.LENGTH_SHORT).show();
//                    }
//                }, new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this,"无法监听电话状态，来电话时音乐将不会暂停，可能会影响通话！",Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//
//        );
//        playService.setMp3Infos(mp3Infos);
        playService.play(this,position);

        isPlaying = true;

        //进度条
        mTimer.schedule(mTimerTask,0,500);
        return true;
         }
        //原始播放方法
//    public boolean play(int position){
//        playService.play(this,position);
//        isPlaying = true;
//        return true;
//    }
}
