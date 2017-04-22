package cn.itcast.musicapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import cn.itcast.musicapp.activity.MainActivity;
import cn.itcast.musicapp.R;
import cn.itcast.musicapp.activity.NetMusicActivity;

/**
 * Created by CreazyMa on 2017/3/29.
 */

public class NetMusicFragment extends Fragment {
    private MainActivity mainActivity;
    private ListView list_my_music;
    private String[] musicType = {"新歌榜", "热歌榜", "经典老歌榜", "欧美金曲榜", "情歌对唱榜", "网络歌曲榜"};
    private int[] typeValue={1,2,22,21,23,25};//和musictype对应

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    public NetMusicFragment() {

    }

    public static NetMusicFragment newInstance() {
        NetMusicFragment fragment = new NetMusicFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_net_music,null);
        list_my_music = (ListView) view.findViewById(R.id.listView);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1, musicType);
        list_my_music.setAdapter(arrayAdapter);

        list_my_music.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                Intent intent = new Intent(mainActivity,NetMusicActivity.class);
                intent.putExtra("type",typeValue[i]);
                intent.putExtra("size",10);
                intent.putExtra("offset",0);
                intent.putExtra("name",musicType[i]);//用来在ToolBar显示的文本
                startActivity(intent);
            }

        });
        return view;
    }
}
