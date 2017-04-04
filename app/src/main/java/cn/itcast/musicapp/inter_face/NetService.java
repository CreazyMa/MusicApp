package cn.itcast.musicapp.inter_face;

import cn.itcast.musicapp.bean.SongRankBean;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by CreazyMa on 2017/3/22.
 */

public interface NetService {

    String BASE_URL = "http://tingapi.ting.baidu.com/";

    @GET("v1/restserver/ting")
    Call<SongRankBean> getSongRank(
            @Query("format") String format,
            @Query("callback") String callback,
            @Query("from") String from,
            @Query("method") String method,
            @Query("type") int type,
            @Query("size") int size,
            @Query("offset") int offset);
}
