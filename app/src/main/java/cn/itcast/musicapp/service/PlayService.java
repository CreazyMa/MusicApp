package cn.itcast.musicapp.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.TelephonyManager;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import cn.itcast.musicapp.activity.MainActivity;
import cn.itcast.musicapp.application.MainApplication;
import cn.itcast.musicapp.R;
import cn.itcast.musicapp.Constant;
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
    private NotificationManager notificationManager;
    private Notification notification;
    public static final int TYPE_MEDIA = 7;

    private Timer timer;
    private TimerTask timerTask;
    private boolean isTimerStart = false;


    public PlayService() {

    }

    public int getPlay_mode() {
        return play_mode;
    }

    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
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


    @Override
    public void onCreate() {
        super.onCreate();

        mPlayer = new MediaPlayer();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        mp3Infos = MediaUtils.getMp3Infos(this);
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
        intentFilter.addAction("play");
        intentFilter.addAction("pause");
        intentFilter.addAction("next");
        intentFilter.addAction("pre");
        intentFilter.addAction("close");
        registerReceiver(systemReceiver, intentFilter);

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(systemReceiver);
        if (isTimerStart) {
            timer.cancel();
            isTimerStart = false;
        }
        super.onDestroy();
    }

    private void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(Constant.RECRIVER_PLAY_POSITION);
                intent.putExtra("position", mPlayer.getCurrentPosition() / 1000);
                mContext.sendBroadcast(intent);
            }
        };
        timer.schedule(timerTask, 0, 500);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public void play(Context context, int position) {
        if (mp3Infos == null) return;
        mContext = context;

        if (position >= 0 && position < mp3Infos.size()) {
            Mp3Info mp3Info = mp3Infos.get(position);
            try {
                mPlayer.reset();//复位
                mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
                mPlayer.prepare();//准备
                mPlayer.start();//开始播放
                setupNotification(mp3Info);
                currentPosition = position;//保存当前位置到currentPosition
                MainApplication.position = position;
                MainApplication.isPlaying = mPlayer.isPlaying();
                Intent intent = new Intent(Constant.RECRIVER_MUSIC_CHANGE);
                intent.putExtra("position", currentPosition);
                intent.putExtra("isPlaying", true);
                mContext.sendBroadcast(intent);
                if (!isTimerStart) {
                    startTimer();
                    isTimerStart = true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }


    //暂停
    public void pasue() {
        if (mPlayer.isPlaying()) {

            mPlayer.pause();
            MainApplication.isPlaying = mPlayer.isPlaying();
            setupNotification(mp3Infos.get(currentPosition));
            Intent intent = new Intent(Constant.RECRIVER_MUSIC_CHANGE);
            intent.putExtra("position", currentPosition);
            intent.putExtra("isPlaying", false);
            mContext.sendBroadcast(intent);
            stopTimer();
            isTimerStart = false;


        }
    }

    //下一首
    public void next() {
//        if (mp3Infos == null) return;
        if (currentPosition >= mp3Infos.size() - 1) {//如果当前位置超过总歌数，则返回第一首
            currentPosition = 0;//返回第一首歌
        } else {
            currentPosition++;
        }
        play(mContext, currentPosition);
//        setupNotification(mp3Infos.get(currentPosition));
    }


    //上一首
    public void prev() {
        if (currentPosition - 1 < 0) {//如果上一首小于1，说明已经是第一首
            currentPosition = mp3Infos.size() - 1;//返回最后一首
        } else {
            currentPosition--;
        }
        play(mContext, currentPosition);
//        setupNotification(mp3Infos.get(currentPosition));
    }


    public void start() {
        if (mPlayer != null && !mPlayer.isPlaying()) {//判断当前歌曲不等于空，并且没有在播放
            mPlayer.start();
            setupNotification(mp3Infos.get(currentPosition));
            Intent intent = new Intent(Constant.RECRIVER_MUSIC_CHANGE);
            intent.putExtra("position", currentPosition);
            intent.putExtra("isPlaying", true);
            mContext.sendBroadcast(intent);
            if (!isTimerStart) {
                startTimer();
                isTimerStart = true;
            }
        }
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public Mp3Info getMusic() {
        return mp3Infos.get(currentPosition);
    }

    public int getPlayPosition() {
        return mPlayer.getCurrentPosition() / 1000;
    }


    public class SystemReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("play".equals(intent.getAction())) {
                boolean isUpdateList = intent.getBooleanExtra("updateList", false);
                int position = intent.getIntExtra("position", 0);
                if (isUpdateList) {//如果是在列表中点击，则更新音乐列表
                    mp3Infos = (ArrayList<Mp3Info>) MainApplication.mp3List;
                    play(getApplicationContext(), position);
                } else {//如果暂停，则播放
                    start();
                }
            } else if ("pause".equals(intent.getAction())) {
                pasue();
            } else if ("pre".equals(intent.getAction())) {
                prev();
            } else if ("next".equals(intent.getAction())) {
                next();
            } else if ("close".equals(intent.getAction())) {
                stopForeground(true);
                pasue();
            }

            //如果是打电话
            else if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
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

    //通知栏显示歌曲播放控制栏
    private void setupNotification(final Mp3Info mp3Info) {

//        System.out.println("/************/");
//        System.out.println("图片地址"+mp3Info.getPicUrl());
//        System.out.println("/************/");
        if (mp3Info.getPicUrl() == null) {
            setupNotification(mp3Info, MediaUtils.getArtWork(mContext, mp3Info.getId(), mp3Info.getAlbumId(), true, true));
        } else {
            new AsyncTask<String, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(String... params) {
                    Bitmap bitmap = null;
                    try {
                        bitmap = Glide.with(PlayService.this).load(params[0]).asBitmap().into(100, 100).get();
//                        bitmap = Glide.with(PlayService.this).load(strings[0]).asBitmap().into(100, 100).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    return bitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
//                    super.onPostExecute(bitmap);
                    setupNotification(mp3Info, bitmap);
                }
            }.execute(mp3Info.getPicUrl());
        }
    }

    //两个参数的
    private void setupNotification(final Mp3Info mp3Info, Bitmap bitmap) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(mp3Info.getTitle());
        builder.setContentText(mp3Info.getArtist());
        builder.setSmallIcon(R.mipmap.local_ic);
        builder.setLargeIcon(bitmap);
        builder.setDefaults(NotificationCompat.FLAG_FOREGROUND_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 1, intent, 0);
        builder.setContentIntent(pIntent);

        //新建意图，并设置action标记“play”,用于接收广播时过滤意图信息

        Intent intentPlay = new Intent("play");
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(this, 0, intentPlay, 0);

        Intent intentPause = new Intent("pause");
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this, 0, intentPause, 0);

        Intent intentNext = new Intent("next");
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(this, 0, intentNext, 0);

        Intent intentPre = new Intent("pre");
        PendingIntent pendingIntentPre = PendingIntent.getBroadcast(this, 0, intentPre, 0);

        Intent intentClose = new Intent("close");
        PendingIntent pendingIntentClose = PendingIntent.getBroadcast(this, 0, intentClose, 0);

        //第一个参数是图片资源id，第二个是图标显示的名称，第三个点击要启动的PendingIntent
        builder.addAction(R.mipmap.ic_skip_previous_white_24dp, "", pendingIntentPre);
        if (isPlaying()) {
            builder.addAction(R.mipmap.uamp_ic_pause_white_24dp, "", pendingIntentPause);
        } else {
            builder.addAction(R.mipmap.uamp_ic_play_arrow_white_24dp, "", pendingIntentPlay);
        }
        builder.addAction(R.mipmap.ic_skip_next_white_24dp, "", pendingIntentNext);
        builder.addAction(R.mipmap.ic_close_black_24dp, "", pendingIntentClose);
        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();


        style.setMediaSession(new MediaSessionCompat(this, "MediaSession",
                new ComponentName(this, Intent.ACTION_MEDIA_BUTTON), null).getSessionToken());


        //CancelButton在5.0以下的机器有效
        style.setCancelButtonIntent(pIntent);
        style.setShowCancelButton(true);

        //设置要实现在通知栏有房的图标，最多三个

        style.setShowActionsInCompactView(2, 3);
        builder.setStyle(style);
        builder.setShowWhen(false);
//        mNotification = builder.build();
        Notification mNotification = builder.build();

        startForeground(1, mNotification);
    }

}
