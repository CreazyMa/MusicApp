package cn.itcast.musicapp.application;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.List;

import cn.itcast.musicapp.bean.Mp3Info;

/**
 * Created by CreazyMa on 2017/4/22.
 */

public class MainApplication extends Application {
    public static Context context;
    private static String rootPath = "/zzulimusic";//下载的歌曲信息将放在创建的文件夹里边
    public static String lrcPath = "/lrc";
    public static List<Mp3Info> mp3List;//存储当前歌曲的序号
    public static int position = 0;//当前播放的音乐序号为0
    public static boolean isPlaying = false;//是否正在播放？控制播放或者暂停图标
    public static int playMode;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        initPath();
    }
    private void initPath(){
        String ROOT = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            ROOT = Environment.getExternalStorageDirectory().getPath();
        }
        rootPath = ROOT+rootPath;
        lrcPath = rootPath+lrcPath;
        File lrcFile = new File(lrcPath);
        if (lrcFile.exists()){
            lrcFile.mkdirs();
        }
    }
}
