package cn.itcast.musicapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import cn.itcast.musicapp.activity.MainActivity;
import cn.itcast.musicapp.adapter.MyMusicListAdapter;
import cn.itcast.musicapp.bean.Mp3Info;
import cn.itcast.musicapp.R;
import cn.itcast.musicapp.adapter.LocalMusicAdapter;
import cn.itcast.musicapp.util.MediaUtils;

/**
 * Created by CreazyMa on 2017/3/1.
 */

public class LocalFragment extends Fragment {
    //    private ListView listView_my_music;
    private ArrayList<Mp3Info> mp3Infos;
    private RecyclerView recyclerView;
    //    private MyMusicListAdapter myMusicListAdapter;
    private MainActivity mainActivity;
    private LocalMusicAdapter localMusicAdapter;
    private MyMusicListAdapter myMusicListAdapter;


    private Toolbar toolbar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }


    public LocalFragment() {
    }

    public static LocalFragment newInstance() {
        LocalFragment fragment = new LocalFragment();
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.localmusic, null);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));
        recyclerView.addItemDecoration(new DividerItemDecoration(mainActivity, LinearLayout.VERTICAL));

        loadData();
        return view;
    }

    private void loadData() {
        mp3Infos = (ArrayList<Mp3Info>) MediaUtils.getMp3Infos(mainActivity);
        localMusicAdapter = new LocalMusicAdapter(mainActivity, mp3Infos);
//        localMusicAdapter.notifyDataSetChanged();

        localMusicAdapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                Log.i("点击测试时间，看是否能获取当前点击音乐的编号：", "+" + position + "+");


//播放音乐
                mainActivity.play(mp3Infos, position);
            }

            @Override
            public void onItemLongClick(int position) {

            }
        });

        recyclerView.setAdapter(localMusicAdapter);

    }


}
