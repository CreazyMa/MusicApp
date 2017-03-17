package cn.itcast.musicapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cn.itcast.musicapp.util.MediaUtils;
import cn.itcast.musicapp.bean.Mp3Info;
import cn.itcast.musicapp.R;

/**
 * Created by CreazyMa on 2017/3/1.
 */

public class MyMusicListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Mp3Info> mp3Infos;

    public MyMusicListAdapter(Context context, ArrayList<Mp3Info> mp3Infos) {
        this.context = context;
        this.mp3Infos = mp3Infos;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    @Override
    public int getCount() {
        return mp3Infos.size();
    }

    @Override
    public Object getItem(int position) {
        return mp3Infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView==null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_music_layout,null);
            viewHolder = new ViewHolder();
            Mp3Info mp3Info = mp3Infos.get(position);
//            viewHolder.textView1_title = (TextView)convertView.findViewById(R.id.textView1_title);
//            viewHolder.textView2_singer = (TextView)convertView.findViewById(R.id.textView2_singer);
//            viewHolder.textView3_time = (TextView)convertView.findViewById(R.id.textView3_time);

            /**
             * 错误修改部分可能是
             * */
            viewHolder.textView1_title.setText(mp3Info.getTitle());
            viewHolder.textView2_singer.setText(mp3Info.getArtist());
            viewHolder.textView3_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));

            convertView.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        Mp3Info mp3Info = mp3Infos.get(position);
        viewHolder.textView1_title.setText(mp3Info.getTitle());
        viewHolder.textView2_singer.setText(mp3Info.getArtist());
        viewHolder.textView3_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));

        return convertView;
    }
    static class ViewHolder{
        TextView textView1_title;
        TextView textView2_singer;
        TextView textView3_time;
    }
}
