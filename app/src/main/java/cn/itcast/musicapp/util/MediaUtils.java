package cn.itcast.musicapp.util;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cn.itcast.musicapp.bean.Mp3Info;

/**
 * Created by CreazyMa on 2017/3/1.
 */

public class MediaUtils {

    public static ArrayList<Mp3Info> getMp3Infos(Context context) {
        System.out.println("MediaUtils.java #2:" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.DURATION + ">=10000", null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        );
        ArrayList<Mp3Info> mp3Infos = new ArrayList<>();
        System.out.println("MediaUtils.java #3 :cursor.getCount()" + cursor.getCount());
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            Mp3Info mp3Info = new Mp3Info();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));//音乐id
            String title = cursor.getString((cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE))); // 音乐标题
            String artist = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST)); // 艺术家
            String album = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM)); // 专辑
            String displayName = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));//音乐名称
            long albumId = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));//专辑id
            long duration = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION)); // 时长
            long size = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.SIZE)); // 文件大小
            String url = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA)); // 文件路径
            int isMusic = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)); // 是否为音乐
            if (isMusic != 0) {
                mp3Info.setId(id);
                mp3Info.setTitle(title);
                mp3Info.setArtist(artist);
                mp3Info.setAlbum(album);
                mp3Info.setDisplayName(displayName);
                mp3Info.setAlbumId(albumId);
                mp3Info.setDuration(duration);
                mp3Info.setSize(size);
                mp3Info.setUrl(url);
                mp3Infos.add(mp3Info);
            }
        }
        cursor.close();
        System.out.println("MediaUtils.java #4 :mp3infos = " + mp3Infos.size());
        return mp3Infos;
    }

    public static List<HashMap<String, String>> getMusicMaps(
            List<Mp3Info> mp3Infos) {
        List<HashMap<String, String>> mp3list = new ArrayList<HashMap<String, String>>();
        for (Iterator iterator = mp3Infos.iterator(); iterator.hasNext();) {
            Mp3Info musicInfo = (Mp3Info) iterator.next();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("title", musicInfo.getTitle());
            map.put("Artist", musicInfo.getArtist());
            map.put("album", musicInfo.getAlbum());
            map.put("displayName", musicInfo.getDisplayName());
            map.put("albumId", String.valueOf(musicInfo.getAlbumId()));
            map.put("duration", formatTime(musicInfo.getDuration()));
            map.put("size", String.valueOf(musicInfo.getSize()));
            map.put("url", musicInfo.getUrl());
            mp3list.add(map);
        }
        return mp3list;
    }

    //格式化时间
    public static String formatTime(long time){
        String min = time / (1000*60)+"";
        String sec = time % (1000*60)+"";
        if (min.length()<2){
            min="0"+time/(1000*60)+"";
        }else
        {
            min = time/(1000*60)+"";
        }
        if (sec.length()==4){
            sec = "0"+time%(1000*60)+"";
        }
        else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }
}
