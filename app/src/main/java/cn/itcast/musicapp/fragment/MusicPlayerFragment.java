package cn.itcast.musicapp.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;


import cn.itcast.musicapp.application.MainApplication;
import cn.itcast.musicapp.R;
import cn.itcast.musicapp.activity.MusicPlayActivity;
import cn.itcast.musicapp.Constant;
import cn.itcast.musicapp.bean.Mp3Info;
import cn.itcast.musicapp.util.MediaUtils;

/**
 * Created by CreazyMa on 2017/4/19.
 */

public class MusicPlayerFragment extends Fragment {
    private TextView tvSongName;
    private TextView tvSinger;
    private ImageView ivSongPic;
    private ImageView ivPlay;
    private ImageView ivNext;
    private ProgressBar pb_time;
    private boolean isPlaying = false;
    private BroadcastReceiver mReceiver;

    public MusicPlayerFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.music_play_control, container, false);
        tvSongName = (TextView) view.findViewById(R.id.text_songName);
        tvSinger = (TextView) view.findViewById(R.id.text_singer);
        ivNext = (ImageView) view.findViewById(R.id.imageView3_next);
        ivPlay = (ImageView) view.findViewById(R.id.imageView2_play_pasue);
        ivSongPic = (ImageView) view.findViewById(R.id.songImage);
        pb_time = (ProgressBar) view.findViewById(R.id.pb_time);
        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//使用广播通知PlayService,进行播放控制
                if (!isPlaying) {
                    isPlaying = true;
                    Intent intent = new Intent("play");
                    getActivity().sendBroadcast(intent);
                } else {
                    Intent intent = new Intent("pause");
                    getActivity().sendBroadcast(intent);
                }
            }
        });
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("next");
                getActivity().sendBroadcast(intent);
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                System.out.print("被点击");
//                Intent intent = new Intent(getContext(), MusicPlayActivity.class);
//                startActivity(intent);
             //Toast.makeText(getContext(),"控制栏被点击",Toast.LENGTH_SHORT).show();
             Intent intent = new Intent(getContext(),MusicPlayActivity.class);
                startActivity(intent);

            }
        });

        mReceiver = new MusicChangeReceiver();
        IntentFilter intentFilter = new IntentFilter(Constant.RECRIVER_MUSIC_CHANGE);
        intentFilter.addAction(Constant.RECRIVER_PLAY_POSITION);
        getActivity().registerReceiver(mReceiver,intentFilter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (MainApplication.isPlaying){
            onPlayStateChanged(MainApplication.position,true);//如果音乐正在播放，则在打开时显示音乐信息
        }
    }
    private class MusicChangeReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.RECRIVER_MUSIC_CHANGE.equals(intent.getAction())){
                int position = intent.getIntExtra("position",0);//当前播放音乐序号
                boolean isplay = intent.getBooleanExtra("isPlaying",false);
                isPlaying = isplay;
                onPlayStateChanged(position,isplay);
            }else if (Constant.RECRIVER_PLAY_POSITION.equals(intent.getAction())){
                int position = intent.getIntExtra("position", 0);
                onPlayPositionChanged(position);
            }
        }
    }

    private void onPlayPositionChanged(int position){
        pb_time.setProgress(position);
    }
    private void onPlayStateChanged(int position,boolean isplay){
        Mp3Info music = MainApplication.mp3List.get(position);
        if (music == null){
            tvSongName.setText(Constant.DEFAULT_MUSIC_TITLE);
            tvSinger.setText(Constant.DEFAULT_ARTIST);
            ivSongPic.setImageResource(R.mipmap.local_ic);
            ivPlay.setImageResource(R.mipmap.uamp_ic_play_arrow_white_48dp);
            return;
        }
        tvSongName.setText(music.getTitle());
        tvSinger.setText(music.getArtist());
        if (music.getPicUrl() == null){
            ivSongPic.setImageBitmap(MediaUtils.getArtWork(getActivity(),music.getId(),music.getAlbumId(),true,true));
        }else {
            Glide.with(this).load(music.getPicUrl()).into(ivSongPic);
        }
        pb_time.setMax((int)music.getDuration());
        pb_time.setProgress(0);
        if (isplay){
            ivPlay.setImageResource(R.mipmap.uamp_ic_pause_white_48dp);
        }else {
            ivPlay.setImageResource(R.mipmap.uamp_ic_play_arrow_white_48dp);
        }
    }

}
