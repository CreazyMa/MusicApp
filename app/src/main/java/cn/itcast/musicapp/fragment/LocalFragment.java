package cn.itcast.musicapp.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import cn.itcast.musicapp.MainActivity;
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

//
//        LocalMusicAdapter localMusicAdapter = new LocalMusicAdapter(mainActivity, null);
//        localMusicAdapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(int position) {
//                mainActivity.play( position);//mp3Infos,
//            }
//
//            @Override
//            public void onItemLongClick(int position) {
//
//            }
//        });
//        recyclerView.setAdapter(localMusicAdapter);
//
//        mainActivity.requestPermission(101, Manifest.permission.READ_EXTERNAL_STORAGE,
//                new Runnable(){
//                    @Override
//                    public void run() {
//                        loadData();
//                    }
//                },new Runnable(){
//                    @Override
//                    public void run() {
//                        Toast.makeText(mainActivity, "不允许访问外部存储设备，无法获取音乐信息！", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                );


//        //版本判断
//        if (Build.VERSION.SDK_INT >= 23){
//            //检查是否拥有权限
//            int checkCallPhonePermission = ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
//            if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
//                //如果未授权，弹出对话框接收权限
//                ActivityCompat.requestPermissions(mainActivity,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
//                return null;
//            }else {
//                loadData();
//            }
//        }else {
//            loadData();
//        }
        loadData();
        return view;
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            loadData();
//        } else {
//            Toast.makeText(mainActivity, "不允许访问外部存储设备，无法获取音乐信息！", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void loadData() {
        mp3Infos = (ArrayList<Mp3Info>) MediaUtils.getMp3Infos(mainActivity);
        localMusicAdapter = new LocalMusicAdapter(mainActivity, mp3Infos);
//        localMusicAdapter.notifyDataSetChanged();

        localMusicAdapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                Log.i("点击测试时间，看是否能获取当前点击音乐的编号：", "+" + position + "+");


//播放音乐
                mainActivity.play(position);
            }

            @Override
            public void onItemLongClick(int position) {

            }
        });

        recyclerView.setAdapter(localMusicAdapter);

    }

    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }

    public MyMusicListAdapter getMyMusicListAdapter() {
        return myMusicListAdapter;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }


}
