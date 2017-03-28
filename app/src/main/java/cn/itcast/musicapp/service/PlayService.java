package cn.itcast.musicapp.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import cn.itcast.musicapp.MainActivity;
import cn.itcast.musicapp.bean.Mp3Info;
import cn.itcast.musicapp.util.MediaUtils;

/**
 * Created by CreazyMa on 2017/3/16.
 */

public class PlayService extends Service {
    private MediaPlayer mPlayer;
    private int currentPosition;//当前正在播放的歌曲位置
    ArrayList<Mp3Info> mp3Infos;
    private Context mContext;
    private SystemReceiver systemReceiver;//处理电话广播,注册广播接受者，用于来电话时，暂停音乐播放
    IntentFilter intentFilter;

    //播放模式
    public static final int ORDER_PLAY = 1;//顺序播放
    public static final int RANDOM_PLAY = 2;//随机播放
    public static final int SINGLE_PLAY = 3;//单曲循环
    private int play_mode = ORDER_PLAY;//播放模式，默认为顺序播放
    private Random random = new Random();//创建随机对象


    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }
    public class PlayBinder extends Binder {
        public PlayService getPlayService() {
            System.out.println("PlayService #1 " + PlayService.this);
            return PlayService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    public PlayService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPlayer = new MediaPlayer();
        mp3Infos = MediaUtils.getMp3Infos(this);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                switch (play_mode) {
                    case ORDER_PLAY:
                        next();
                        break;
                    case RANDOM_PLAY:
                        play(mContext, random.nextInt(mp3Infos.size()));
                        break;
                    case SINGLE_PLAY:
                        play(mContext, currentPosition);
                        break;
                    default:
                        break;
                }
            }
        });

        systemReceiver = new SystemReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(systemReceiver, intentFilter);
    }

    public Mp3Info play(Context context, int position) {
        mContext = context;

            if (position >= 0 && position < mp3Infos.size()) {
                Mp3Info mp3Info = mp3Infos.get(position);
                try {
                    mPlayer.reset();//复位
                    mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
                    mPlayer.prepare();//准备
                    mPlayer.start();//开始播放
                    currentPosition = position;//保存当前位置到currentPosition
                    mContext.sendBroadcast(new Intent(MainActivity.Constant.RECRIVER_MUSIC_CHANGE));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return mp3Info;
            }
            return null;
    }

public void setMp3Infos(ArrayList<Mp3Info> mp3Infos){
    this.mp3Infos = mp3Infos;
}

//    public void play(Context context, int position) {
//        mContext = context;
//        if (position >= 0 && position < mp3Infos.size()) {
//            Mp3Info mp3Info = mp3Infos.get(position);
//            try {
//                mPlayer.reset();//复位
//                mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
//                mPlayer.prepare();//准备
//                mPlayer.start();//开始播放
//                currentPosition = position;//保存当前位置到currentPosition
//                mContext.sendBroadcast(new Intent(MainActivity.Constant.RECRIVER_MUSIC_CHANGE));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }


    //暂停
    public void pasue() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    //下一首
    public void next() {

      if (mPlayer != null && !mPlayer.isPlaying()) {//判断是否有没有歌曲播放，如何没有歌曲，点击下一首的时候会从第一首歌开始播放

       play(getApplicationContext(),0);
      }else if (currentPosition >= mp3Infos.size() - 1) {//如果当前位置超过总歌数，则返回第一首
            currentPosition = 0;//返回第一首歌
        } else {
            currentPosition++;
        }
        play(mContext, currentPosition);

    }


    //上一首
    public void prev() {
        if (currentPosition - 1 < 0) {//如果上一首小于1，说明已经是第一首
            currentPosition = mp3Infos.size() - 1;//返回最后一首
        } else {
            currentPosition--;
        }
        play(mContext, currentPosition);
    }


    public void start() {
        if (mPlayer != null && !mPlayer.isPlaying()) {//判断当前歌曲不等于空，并且没有在播放
            mPlayer.start();
        }
    }

    public int getPlay_mode() {
        return play_mode;
    }

    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }


    @Override
    public void onDestroy() {
        unregisterReceiver(systemReceiver);
        super.onDestroy();
    }

    public Mp3Info getMusic() {
        return mp3Infos.get(currentPosition);
    }

    public class SystemReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //如果是打电话
            if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
                pasue();
            } else {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
                switch (tm.getCallState()) {
                    //响铃
                    case TelephonyManager.CALL_STATE_RINGING:
                        pasue();
                        break;
                    //摘机
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        pasue();
                        break;
                    //空闲
                    case TelephonyManager.CALL_STATE_IDLE:
                        start();
                        break;
                }
            }
        }
    }
}
