package cn.itcast.musicapp.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;


import java.util.ArrayList;
import java.util.List;

import cn.itcast.musicapp.application.MainApplication;
import cn.itcast.musicapp.R;
import cn.itcast.musicapp.adapter.NetMusicAdapter;
import cn.itcast.musicapp.bean.BillboardBean;
import cn.itcast.musicapp.bean.Mp3Info;
import cn.itcast.musicapp.util.BaiduMusicUtils;

/**
 * Created by CreazyMa on 2017/4/4.
 */

public class NetMusicActivity extends AppCompatActivity {
    private Toolbar toolbar = null;
    private ImageView tbImage = null;//Toolbar显示图片
    private RecyclerView recyclerView = null;
    private List<Mp3Info> mp3Infos;
    private BillboardBean billboardBean = null;//存储当前榜单信息
    private NetMusicAdapter adapter = null;
    private String strTitle;//存储Toobar显示文本
    private int type = 0;
    private int size = 0;//每次读取的歌曲数量
    private int offset = 0;//读取歌曲的偏移量,用于分页加载
    private int billboarMusicCount = 0;//当前榜单歌曲总数量
    private boolean loading = true;//是否正在加载
    int firstVisibleItem, visibleItemCount, totalItemCount;
    private LinearLayoutManager linearLayoutManager = null;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_music);

        type = getIntent().getIntExtra("type", 1);
        size = getIntent().getIntExtra("size", 10);
        offset = getIntent().getIntExtra("offset", 0);
        strTitle = getIntent().getStringExtra("name");


        //启动异步任务，加载榜单和音乐数据
        new LoadNetMusic().execute(type, size, offset);
        //ToolBar设置
        setToolbar();
        //初始化设置控件和事件处理
        initView();
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(strTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//显示左侧的箭头
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();//点击箭头的事件处理
            }
        });
    }

    private void initView() {
        tbImage = (ImageView) findViewById(R.id.app_bar_image);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);//获取到音乐歌曲，显示控件

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);//这句话加上以后会报错，提示在适配器中mp3Info中无数据
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));

        adapter = new NetMusicAdapter(this, null);//源代码给的没有传值

        //添加点击事件处理
        adapter.setOnItemClickListener(new NetMusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                play((ArrayList<Mp3Info>) mp3Infos, position);
            }

            @Override
            public void onItemLongClick(int position) {

            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = linearLayoutManager.getItemCount();
                firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                if (totalItemCount >= billboarMusicCount)
                    return;
                if (!loading && (totalItemCount - visibleItemCount) <= firstVisibleItem) {
                    offset += size;
                    new LoadNetMusic().execute(type, size, offset);
                    loading = true;
                }
            }
        });
    }

    public boolean play(ArrayList<Mp3Info> mp3Infos, int position) {
        MainApplication.mp3List = mp3Infos;
        Intent intent = new Intent("play");
        intent.putExtra("updateList", true);
        intent.putExtra("position", position);
        sendBroadcast(intent);
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class LoadNetMusic extends AsyncTask<Integer, Integer, Boolean> {

        List<Mp3Info> musics = null;

        @Override
        protected Boolean doInBackground(Integer... params) {
            loading = true;

            musics = BaiduMusicUtils.getNetMusic(params[0], params[1], params[2]);
            /**
             * 已经从网络上获取到音乐
             */

            if (params[2] == 0) {
                mp3Infos = musics;

                billboardBean = BaiduMusicUtils.getBillboardBean();

            } else {
                mp3Infos.addAll(musics);
            }
            if (musics == null)
                return false;
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                if (offset == 0) {
                    Glide.with(getApplication()).load(billboardBean.getPic_s444()).into(tbImage);
                    billboarMusicCount = Integer.parseInt(billboardBean.getBillboard_songnum());
                    adapter.setMp3Infos(musics);
                }
                adapter.notifyDataSetChanged();
                loading = false;
            }
        }
    }

}
