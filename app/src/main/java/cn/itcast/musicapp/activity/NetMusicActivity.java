package cn.itcast.musicapp.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.itcast.musicapp.MainActivity;
import cn.itcast.musicapp.R;
import cn.itcast.musicapp.adapter.NetMusicAdapter;
import cn.itcast.musicapp.bean.BillboardBean;
import cn.itcast.musicapp.bean.Mp3Info;
import cn.itcast.musicapp.service.PlayService;
import cn.itcast.musicapp.util.BaiduMusicUtils;
import cn.itcast.musicapp.util.MediaUtils;

/**
 * Created by CreazyMa on 2017/4/4.
 */

public class NetMusicActivity extends AppCompatActivity {
    private Toolbar toolbar = null;
    private ImageView tbImage = null;//Toolbar显示图片
    private RecyclerView recyclerView = null;
    private List<Mp3Info> mp3Infos = null;
    private BillboardBean billboardBean = null;//存储当前榜单信息
    private NetMusicAdapter adapter = null;
    private String strTitle;//存储Toobar显示文本
    private int type = 0;
    private int size = 0;//每次读取的歌曲数量
    private int offset = 0;//读取歌曲的偏移量,用于分页加载
    private int billboarMusicCount = 0;//当前榜单歌曲总数量
    private boolean loading = true;//是否正在加载
    int firstVisibleItem, visibleItemCount, totalItemCount;
    private LinearLayoutManager linearLayoutManager = null;
    protected PlayService playService;
    private boolean isBound = false;//是否已经绑定
    private TextView tvSongName;
    private TextView tvSonger;
    private ImageView ivSongPic;
    private ImageView ivPlay;
    private ImageView ivNext;
    private BroadcastReceiver mReceiver;
    private boolean isPlaying;
    List<Mp3Info> musics = null;
    private boolean ifPlaying = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_music);

        type = getIntent().getIntExtra("type", 1);
        size = getIntent().getIntExtra("size", 10);
        offset = getIntent().getIntExtra("offset", 0);
        strTitle = getIntent().getStringExtra("name");
        //启动异步任务，加载榜单和音乐数据
        new LoadNetMusic().execute(type, size, offset);
        //ToolBar设置
        setToolbar();
        //初始化设置控件和事件处理
        initView();

        //绑定服务，为了获取playservice
        bindPlayService();
        mReceiver = new MusicChangeRecriver();
        IntentFilter intentFilter = new IntentFilter(MainActivity.Constant.RECRIVER_MUSIC_CHANGE);
        registerReceiver(mReceiver, intentFilter);

    }

    private class MusicChangeRecriver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MainActivity.Constant.RECRIVER_MUSIC_CHANGE.equals(intent.getAction())) {
                onPlayStateChanged();
            }
        }
    }

    private void onPlayStateChanged() {
        Mp3Info music = playService.getMusic();
        if (music == null) {
            tvSongName.setText(MainActivity.Constant.DEFAULT_MUSIC_TITLE);
            tvSonger.setText(MainActivity.Constant.DEFAULT_ARTIST);
            ivSongPic.setImageResource(R.mipmap.local_ic);
            ivPlay.setImageResource(R.mipmap.uamp_ic_play_arrow_white_48dp);
            return;
        }
        tvSongName.setText(music.getTitle());
        tvSonger.setText(music.getArtist());
        ivSongPic.setImageBitmap(MediaUtils.getArtWork(getApplicationContext(), music.getId(), music.getAlbumId(), true, true));
        if (playService.isPlaying()) {
            ivPlay.setImageResource(R.mipmap.uamp_ic_pause_white_48dp);
        } else {
            ivPlay.setImageResource(R.mipmap.uamp_ic_play_arrow_white_48dp);
        }

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        unbindPlayService();
        super.onDestroy();
    }

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


    class LoadNetMusic extends AsyncTask<Integer, Integer, Boolean> {


        @Override
        protected Boolean doInBackground(Integer... params) {
            loading = true;

            musics = BaiduMusicUtils.getNetMusic(params[0], params[1], params[2]);
            if (params[2] == 0) {
                mp3Infos = musics;
                billboardBean = BaiduMusicUtils.getBillboardBean();
            } else {
                mp3Infos.addAll(musics);
            }
            if (musics == null) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                if (offset == 0) {
                    Glide.with(getApplication()).load(billboardBean.getPic_s444()).into(tbImage);
                    billboarMusicCount = Integer.parseInt(billboardBean.getBillboard_songnum());
                    adapter.setMp3Infos((ArrayList<Mp3Info>) musics);
                }
                adapter.notifyDataSetChanged();
                loading = false;
            }
        }
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(strTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//显示左侧的箭头
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();//点击箭头的事件处理
            }
        });
    }

    private void initView() {
        tbImage = (ImageView) findViewById(R.id.app_bar_image);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        adapter = new NetMusicAdapter(this, null);

        //添加点击事件处理
        adapter.setOnItemClickListener(new NetMusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                play((ArrayList<Mp3Info>) mp3Infos, position);
            }

            @Override
            public void onItemLongClick(int position) {

            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = linearLayoutManager.getItemCount();
                firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                if (totalItemCount >= billboarMusicCount)
                    return;
                if (!loading && (totalItemCount - visibleItemCount) <= firstVisibleItem) {
                    offset += size;
                    new LoadNetMusic().execute(type, size, offset);
                    loading = true;
                }
            }
        });
        tvSongName = (TextView)findViewById(R.id.textView_songName);
        tvSonger = (TextView)findViewById(R.id.textView2_singer);
        ivNext = (ImageView)findViewById(R.id.imageView3_next);
        ivPlay = (ImageView)findViewById(R.id.imageView2_play_pasue);
        ivSongPic = (ImageView)findViewById(R.id.imageView);
//        ivPlay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!isPlaying) {//正在播放则暂停
//                    isPlaying = true;
//                    playService.play(getApplicationContext(),0);
////                    playService.play(first);
//                    if (playService.isPlaying()) {
//                        ivPlay.setImageResource(R.mipmap.uamp_ic_play_arrow_white_48dp);
//                    }
//                } else if (playService.isPlaying()) {//正在播放，暂停
//                    playService.pasue();
//                    ivPlay.setImageResource(R.mipmap.uamp_ic_play_arrow_white_48dp);
//                } else {  //暂停则播放
//                    playService.start();
//                    ivPlay.setImageResource(R.mipmap.uamp_ic_pause_white_48dp);
//                }
//            }
//        });
//        ivNext.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (!ifPlaying){
//                    playService.play(getApplicationContext(),0);
//                    ifPlaying = true;
//                }else {
//                    playService.next();
//                }
//            }
//        });
       /* ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPlaying){
                    isPlaying = true;
                    playService.play(getApplicationContext(),0);
                    if (playService.isPlaying()){
                        ivPlay.setImageResource((R.mipmap.uamp_ic_play_arrow_white_48dp));
                    }
                }else if (playService.isPlaying()){
                    playService.pasue();
                    ivPlay.setImageResource(R.mipmap.uamp_ic_play_arrow_white_48dp);
                }else {
                    playService.start();
                    ivPlay.setImageResource(R.mipmap.uamp_ic_pause_white_48dp);
                }
            }
        });
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playService.next();
            }
        });*/
    }
    public boolean play(ArrayList<Mp3Info> mp3Infos,int position) {
        playService.setMp3Infos(mp3Infos);
        playService.play(this,position);
        isPlaying = true;
        return true;
    }
}
