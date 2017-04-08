package cn.itcast.musicapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


import cn.itcast.musicapp.util.MediaUtils;
import cn.itcast.musicapp.bean.Mp3Info;
import cn.itcast.musicapp.R;

/**
 * Created by CreazyMa on 2017/3/7.
 */

public class LocalMusicAdapter extends RecyclerView.Adapter<LocalMusicAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private ArrayList<Mp3Info> mp3Infos;
    private Context context;

    private OnItemClickListener onItemClickListener;

    public LocalMusicAdapter(Context context, ArrayList<Mp3Info> mp3Infos) {
        this.context = context;
        this.mp3Infos = mp3Infos;
        this.mInflater = LayoutInflater.from(context);
    }

    //歌曲点击监听接口
    public interface OnItemClickListener {
        void onItemClick(int position);

        void onItemLongClick(int position);
    }



    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_music_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Mp3Info mp3Info = mp3Infos.get(position);
        holder.textView1_title.setText(mp3Info.getTitle());
        holder.textView2_singer.setText(mp3Info.getArtist());
        holder.textView3_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        holder.imageView1_icon.setImageBitmap(MediaUtils.getArtWork(context,mp3Info.getId(),mp3Info.getAlbumId(),true,true));
        if (onItemClickListener!=null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("Adapter里面：" ,"+"+position+"+");
                    onItemClickListener.onItemClick(position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onItemClickListener.onItemLongClick(position);
                    return false;
                }
            });
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView1_title;
        public TextView textView2_singer;
        public TextView textView3_time;
        public ImageView imageView1_icon;

        public ViewHolder(View view) {
            super(view);
            textView1_title = (TextView) view.findViewById(R.id.textView1_title);
            textView2_singer = (TextView) view.findViewById(R.id.textView2_singer);
            textView3_time = (TextView) view.findViewById(R.id.textView3_time);
            imageView1_icon = (ImageView) view.findViewById(R.id.imageView1_icon);
        }
    }




    @Override
    public int getItemCount() {
//        if (mp3Infos == null){
//            return mp3Infos.size();
//        }
//        return mp3Infos.size();
        return mp3Infos!=null?mp3Infos.size():0;

    }




}
