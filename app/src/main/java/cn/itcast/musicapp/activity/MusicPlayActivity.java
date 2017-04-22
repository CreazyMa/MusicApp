package cn.itcast.musicapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cn.itcast.musicapp.Constant;
import cn.itcast.musicapp.R;
import cn.itcast.musicapp.application.MainApplication;
import cn.itcast.musicapp.bean.Mp3Info;
import cn.itcast.musicapp.lrc.DefaultLrcParser;
import cn.itcast.musicapp.lrc.LrcRow;
import cn.itcast.musicapp.lrc.LrcView;
import cn.itcast.musicapp.service.PlayService;
import cn.itcast.musicapp.util.BaiduMusicUtils;
import cn.itcast.musicapp.util.CommonUtils;
import cn.itcast.musicapp.util.MediaUtils;
import cn.itcast.musicapp.widget.CircleImageView;
import cn.itcast.musicapp.widget.PlayerSeekBar;

public class MusicPlayActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RelativeLayout rlLrc;
    private FrameLayout flHeadView;
    private boolean isShowLrc = false;
    private ArrayList<Mp3Info> mp3Infos;
    private Mp3Info music;

    private ImageView iv_playMode;
    private ImageView iv_playPre;
    private ImageView iv_playOrPause;
    private ImageView iv_playNext;
    private ImageView iv_playLove;
    private CircleImageView iv_musicPic;

    private TextView tv_MusicDuration;
    private TextView tv_MusicPlayed;
    private PlayerSeekBar playerSeekBar;

    private BroadcastReceiver mReceiver;
    //是否正在拖动seekbar
    private boolean isUpdateProgess = false;
    private boolean isPlaying = false;

    private LrcView mLrcView;
    private TextView mTryGetLrc;
    private ImageView mBackAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        //初始化Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("music");
        toolbar.setSubtitle("wang");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //
        mp3Infos = (ArrayList<Mp3Info>) (MainApplication.mp3List);

        initView();
        //注册广播接收者
        mReceiver = new MusicChangeReceiver();
        IntentFilter intentFilter = new IntentFilter(Constant.RECRIVER_MUSIC_CHANGE);
        intentFilter.addAction(Constant.RECRIVER_PLAY_POSITION);
        registerReceiver(mReceiver, intentFilter);
        //设置歌曲信息
        onPlayStateChanged(MainApplication.position,MainApplication.isPlaying);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);//退出时，解除注册广播接收者，否则报错
        super.onDestroy();
    }

    private void initView() {
        rlLrc = (RelativeLayout)findViewById(R.id.lrcviewContainer);//歌词布局
        flHeadView = (FrameLayout)findViewById(R.id.headerView);//专辑图片布局
        //添加事件，实现两个布局的切换
        flHeadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlLrc.setVisibility(View.VISIBLE);
                flHeadView.setVisibility(View.INVISIBLE);
            }
        });
        rlLrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlLrc.setVisibility(View.INVISIBLE);
                flHeadView.setVisibility(View.VISIBLE);
            }
        });
        //播放控制播放
        iv_playLove = (ImageView)findViewById(R.id.playing_love);//喜欢的音乐
        iv_playPre = (ImageView)findViewById(R.id.playing_pre);//上一首
        iv_playOrPause = (ImageView)findViewById(R.id.playing_play);//播放或暂停
        iv_playNext = (ImageView)findViewById(R.id.playing_next);//下一首
        iv_playMode = (ImageView)findViewById(R.id.playing_mode);//播放模式
        iv_musicPic = (CircleImageView)findViewById(R.id.music_pic);//圆形的专辑图片
        //添加动画，让专辑图片转起来
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.music_pic_rotate);
        LinearInterpolator lin = new LinearInterpolator();//设置动画匀速运动
        animation.setInterpolator(lin);
        iv_musicPic.startAnimation(animation);
        //和前面使用的一样，使用广播进行音乐播放控制
        final Intent intentPlay = new Intent("play");
        final Intent intentPause = new Intent("pause");
        final Intent intentNext = new Intent("next");
        final Intent intentPrev = new Intent("pre");

        iv_playOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainApplication.isPlaying){
                    sendBroadcast(intentPause);
                    iv_playOrPause.setImageResource(R.mipmap.play_rdi_btn_play);
                }else {
                    sendBroadcast(intentPlay);
                    iv_playOrPause.setImageResource(R.mipmap.play_rdi_btn_pause);
                }
            }
        });
        iv_playPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(intentPrev);
            }
        });
        iv_playNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(intentNext);
            }
        });
        //我喜欢的音乐，目前没有进行保存，只是完成了图片的切换
        iv_playLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(music==null)return;
                if(!music.isLoveMusic()){
                    music.setLoveMusic(true);//前面没有添加此属性，请自己添加
                    iv_playLove.setImageResource(R.mipmap.play_icn_loved);
                }else {
                    music.setLoveMusic(false);
                    iv_playLove.setImageResource(R.mipmap.play_rdi_icn_love);
                }
            }
        });
        //点击，切换播放模式，使用广播的方式，请自己在PlayService中添加相应代码
        iv_playMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMode = new Intent("mode");
                switch (MainApplication.playMode){
                    case PlayService.ORDER_PLAY:
                        iv_playMode.setImageResource(R.mipmap.play_icn_shuffle);
                        intentMode.putExtra("playMode", PlayService.RANDOM_PLAY);
                        break;
                    case PlayService.RANDOM_PLAY:
                        iv_playMode.setImageResource(R.mipmap.play_icn_one_prs);
                        intentMode.putExtra("playMode",PlayService.SINGLE_PLAY);
                        break;
                    case PlayService.SINGLE_PLAY:
                        iv_playMode.setImageResource(R.mipmap.play_icn_loop_prs);
                        intentMode.putExtra("playMode",PlayService.ORDER_PLAY);
                        break;
                }
                sendBroadcast(intentMode);
            }
        });
        tv_MusicDuration = (TextView)findViewById(R.id.music_duration);//歌曲总时长
        tv_MusicPlayed = (TextView)findViewById(R.id.music_duration_played);//已播放时长
        playerSeekBar = (PlayerSeekBar)findViewById(R.id.play_seek);//进度控制
        //拖动进度条，修改歌曲播放进度
        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int pos;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser && MainApplication.isPlaying){
                    pos = progress;
                    isUpdateProgess = true;
                    tv_MusicPlayed.setText(MediaUtils.netFormatTime(progress));
                    mLrcView.seekTo(progress*1000,true,fromUser);//让歌词联动
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //为避免频繁发生广播，在停止拖动后，再发生广播通知PlayService
                //同样，这个广播以前没有添加，请自行添加
                Intent intentProgress = new Intent("progress");
                intentProgress.putExtra("progress",pos);
                sendBroadcast(intentProgress);
                isUpdateProgess = false;
            }
        });

        mLrcView = (LrcView) findViewById(R.id.lrcview);//歌词控件
        mTryGetLrc = (TextView) findViewById(R.id.tragetlrc);//获取歌词
        mLrcView.setOnSeekToListener(onSeekToListener);
        mLrcView.setOnLrcClickListener(onLrcClickListener);
        //获取歌词，思路：如果是网络歌曲，可以通过歌曲id，使用百度api直接获取
        //如果是本地音乐，先根据歌曲名称查询出歌曲ID，然后再获取歌词
        //由于需要访问网络，故需使用异步任务
        mTryGetLrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = music.getTitle();
                String id=null;
                if(music.getLrcLink()!=null){
                    id = music.getId()+"";
                }
                new AsyncTask<String,Void,Boolean>(){
                    @Override
                    protected Boolean doInBackground(String... params) {
                        BaiduMusicUtils.downloadLrc(params[0],params[1]);
                        return true;
                    }
                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        super.onPostExecute(aBoolean);
                        updateLrc();//获取到歌词后，通知更新
                    }
                }.execute(title,id);
            }
        });
        mBackAlbum = (ImageView)findViewById(R.id.albumArt);
    }
    //添加监听，实现两个视图的切换
    LrcView.OnLrcClickListener onLrcClickListener = new LrcView.OnLrcClickListener() {

        @Override
        public void onClick() {

            if (rlLrc.getVisibility() == View.VISIBLE) {
                rlLrc.setVisibility(View.INVISIBLE);
                flHeadView.setVisibility(View.VISIBLE);
            }
        }
    };
    //添加监听，实现拖动歌词，修改歌曲播放进度
    LrcView.OnSeekToListener onSeekToListener = new LrcView.OnSeekToListener() {
        @Override
        public void onSeekTo(int progress) {
            Intent intentProgress = new Intent("progress");
            intentProgress.putExtra("progress",progress/1000);
            sendBroadcast(intentProgress);
        }
    };
    //歌曲切换时，或者歌曲加载完成后，更新歌词
    public void updateLrc() {
        if(music == null) return;
        List<LrcRow> list = getLrcRows();
        if (list != null && list.size() > 0) {//已有歌词，则显示
            mTryGetLrc.setVisibility(View.INVISIBLE);
            mLrcView.setLrcRows(list);
        } else {//如手机上没有歌词，显示“获取歌词”，手动点击获取
            mTryGetLrc.setVisibility(View.VISIBLE);
            mLrcView.reset();
        }
    }
    //从手机的指定文件夹获取歌词
    private List<LrcRow> getLrcRows() {
        List<LrcRow> rows = null;
        InputStream is = null;
        try {
            is = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/zzulimusic/lrc/" + music.getTitle()+".lrc");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is == null) {
                return null;
            }
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            rows = DefaultLrcParser.getIstance().getLrcRows(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }
    private class MusicChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.RECRIVER_MUSIC_CHANGE.equals(intent.getAction())) {
                int position = intent.getIntExtra("position",0);
                boolean isplay = intent.getBooleanExtra("isPlaying",false);
                isPlaying = isplay;
                onPlayStateChanged(position,isplay);
            }else if(Constant.RECRIVER_PLAY_POSITION.equals(intent.getAction())){
                int position = intent.getIntExtra("position",0);
                onPlayPositionChanged(position);
            }
        }
    }

    private void onPlayPositionChanged(int position) {
        if(isUpdateProgess) return;
        playerSeekBar.setProgress(position);
        tv_MusicPlayed.setText(MediaUtils.netFormatTime(position));
        mLrcView.seekTo(position*1000,false,false);//让歌词动起来
    }

    private void onPlayStateChanged(int position,boolean isplay) {
        if(position>=0) {
            music = MainApplication.mp3List.get(position);
            if (music == null) {
                toolbar.setTitle(Constant.DEFAULT_MUSIC_TITLE);
                toolbar.setSubtitle(Constant.DEFAULT_ARTIST);
                //iv_musicPic.setImageResource(R.mipmap.ic_launcher);
                iv_playOrPause.setImageResource(R.mipmap.uamp_ic_play_arrow_white_48dp);
                return;
            }
            toolbar.setTitle(music.getTitle());
            toolbar.setSubtitle(music.getArtist());
            tv_MusicDuration.setText(MediaUtils.netFormatTime(music.getDuration()));
            if (music.isLoveMusic()) {
                iv_playLove.setImageResource(R.mipmap.play_icn_loved);
            } else {
                iv_playLove.setImageResource(R.mipmap.play_rdi_icn_love);
            }
            if (music.getPicUrl() == null) {
                Bitmap bitmap = MediaUtils.getArtWork(getApplicationContext(), music.getId(),
                        music.getAlbumId(), true, false);
                iv_musicPic.setImageBitmap(bitmap);
                Drawable drawable = CommonUtils.createBlurredImageFromBitmap(bitmap,MusicPlayActivity.this,3);
                setDrawable(drawable);
            } else {
                new AsyncTask<String, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        Bitmap bitmap = null;
                        try {
                            bitmap = Glide.with(MusicPlayActivity.this).load(params[0]).
                                    asBitmap().into(500, 500).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        return bitmap;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        iv_musicPic.setImageBitmap(bitmap);
                        Drawable drawable = CommonUtils.createBlurredImageFromBitmap(bitmap,MusicPlayActivity.this,3);
                        setDrawable(drawable);
                    }
                }.execute(music.getBigPicUri());
            }

            playerSeekBar.setMax((int) music.getDuration());
            playerSeekBar.setProgress(0);
            updateLrc();//更新歌词
        }
        if(isplay){
            iv_playOrPause.setImageResource(R.mipmap.play_rdi_btn_pause);
        }else {
            iv_playOrPause.setImageResource(R.mipmap.play_rdi_btn_play);
        }
    }
    private void setDrawable(Drawable result) {
        if (result != null) {
            if (mBackAlbum.getDrawable() != null) {
                final TransitionDrawable td =
                        new TransitionDrawable(new Drawable[]{mBackAlbum.getDrawable(), result});


                mBackAlbum.setImageDrawable(td);
                //去除过度绘制
                td.setCrossFadeEnabled(true);
                td.startTransition(200);

            } else {
                mBackAlbum.setImageDrawable(result);
            }
        }
    }
}
