package cn.itcast.musicapp.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.itcast.musicapp.bean.BillboardBean;
import cn.itcast.musicapp.bean.Mp3Info;

/**
 * Created by CreazyMa on 2017/3/29.
 */

public class BaiduMusicUtils {

    private  static BillboardBean billboardBean;
    /**
     * 通过对url的分析发现，前两行内容基本固定，只要最后三个参数是变化的
     * http://tingapi.ting.baidu.com/v1/restserver/ting?
     * format=json&calback=&from=webapp_music&method=baidu.ting.billboard.billList&
     * type=1&size=10&offset=0
     * type：type = 1-新歌榜,2-热歌榜,11-摇滚榜,12-爵士,16-流行,21-欧美金曲榜,
     * 22-经典老歌榜, 23-情歌对唱榜,24-影视金曲榜,25-网络歌曲榜
     * size = 10 //返回条目数量
     * offset = 0 //获取偏移，在进行分页加载时，非常有用。
     */

    public static List<Mp3Info> getNetMusic(int type, int size, int offset) {
        //获取地址
        String path = getUrlByType(type, size, offset);

        // 获取服务器返回内容
        String ret = httpGet(path);

        //解析出歌曲信息,放到List中
        List<Mp3Info> mp3Infos = parseJson(ret);

        //如果是加载第一页，则加载榜单信息
        if (offset == 0) {
           billboardBean = parseJsonBillboard(ret);
        }
        return mp3Infos;
    }

    public static String getUrlByType(int type, int size, int offset) {
        return "http://tingapi.ting.baidu.com/v1/restserver/ting?format=json&calback=&" +
                "from=webapp_music&method=baidu.ting.billboard.billList&type=" + type +
                "&size=" + size + "&offset=" + offset;
    }

    public static String httpGet(String path) {
        URL url = null;
        int code;
        String result = null;
        try {
            url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                result = readInputStream(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将字节流转换为字符串的工具类
     */
    public static String readInputStream(InputStream is) {
        byte[] result;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            is.close();
            baos.close();
            result = baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            String errorStr = "获取数据失败。";
            return errorStr;
        }
        return new String(result);
    }

    /**
     * 解析获取音乐列表
     *
     * @param ret
     * @return {"artist_id":"5423310","language":"\u56fd\u8bed",
     * "pic_big":"http:\/\/musicdata.baidu.com\/data2\/pic\/11d8fcb9c6e0969d802fa37190dc6b93\/537792382\/537792382.JPG@s_0,w_150",
     * "pic_small":"http:\/\/musicdata.baidu.com\/data2\/pic\/11d8fcb9c6e0969d802fa37190dc6b93\/537792382\/537792382.JPG@s_0,w_90",
     * "country":"\u5185\u5730","area":"0","publishtime":"2017-03-07","album_no":"2",
     * "lrclink":"http:\/\/musicdata.baidu.com\/data2\/lrc\/84f12d4031daf74fcd0fca8a755e849e\/537792523\/537792523.lrc",
     * "copy_type":"1","hot":"314922","all_artist_ting_uid":"1422135,198167296","resource_type":"0",
     * "is_new":"1","rank_change":"0","rank":"1","all_artist_id":"5423310,69588714","style":"",
     * "del_status":"0","relate_status":"0","toneid":"0","all_rate":"64,128,256,320,flac",
     * "file_duration":254,"has_mv_mobile":0,"versions":"\u5f71\u89c6\u539f\u58f0",
     * "bitrate_fee":"{\"0\":\"129|-1\",\"1\":\"-1|-1\"}","song_id":"537792416",
     * "title":"\u56e0\u4e3a\u9047\u89c1\u4f60","ting_uid":"1422135",
     * "author":"\u963f\u6084,\u6731\u5143\u51b0","album_id":"537792411",
     * "album_title":"\u56e0\u4e3a\u9047\u89c1\u4f60 \u7535\u89c6\u5267\u539f\u58f0\u5e26",
     * "is_first_publish":0,"havehigh":2,"charge":0,"has_mv":0,"learn":0,"song_source":"web",
     * "piao_id":"0","korean_bb_song":"0","resource_type_ext":"0","mv_provider":"0000000000",
     * "artist_name":"\u963f\u6084,\u6731\u5143\u51b0"},
     */
    private static List<Mp3Info> parseJson(String ret) {
        List<Mp3Info> list = new ArrayList<>();
        Mp3Info info = null;
        try {
            JSONArray array = new JSONObject(ret).getJSONArray("song_list");
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                info = new Mp3Info();
                info.setAlbum(object.getString("album_title"));
                info.setAlbumId(object.getInt("album_id"));
                info.setArtist(object.getString("artist_name"));
                info.setDuration(object.getLong("file_duration"));
                info.setId(object.getLong("song_id"));
                info.setIsMusic(1);
                info.setTitle(object.getString("title"));
                //info.setSize(object.getLong(""));
//请注意：此方法为新增。在本地音乐文件播放时，歌曲的图片是从MediaStore中加载的，
//而播放网络音乐，无法使用上述方法，因此需要保存pic_small的值。
//需要在Mp3Info类中添加成员变量：picUri，并添加get和set方法
                info.setPicUrl(object.getString("pic_small"));
                info.setUrl(getMusicUrl(info.getId()));
/***
 * Json数据解析无错误，并已将数据存储到Mp3Info中
 */


                list.add(info);
/**
 * 列表已经存入数据
 */
//                System.out.println("+++++JSON数据获取，并添加到list中,显示列表数据+++++++++");
//                System.out.println( list.toString());
//                System.out.println("+++++JSON数据获取+++++++++");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String getMusicUrl(long song_id) {
        String path = getUrlPlayById(song_id);
        String ret = httpGet(path);
        String musicUrl = null;
        try {
            JSONObject obj = new JSONObject(ret).optJSONObject("bitrate");
            musicUrl = obj.getString("file_link");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return musicUrl;
    }


    public static String getUrlPlayById(long id) {
        return "http://tingapi.ting.baidu.com/v1/restserver/ting?format=json&calback=&" +
                "from=webapp_music&method=baidu.ting.song.play&songid=" + id;
    }


    /**
     * 获取榜单数据
     *
     * @param ret
     * @return "billboard":{
     * "billboard_type":"1",
     * "billboard_no":"2143",
     * "update_date":"2017-03-24",
     * "billboard_songnum":"199",
     * "havemore":1,"
     * name":"\u65b0\u6b4c\u699c",
     * "comment":"\u8be5\u699c\u5355\u662f\u6839\u636e\u767e\u5ea6\u97f3\u4e50\u5e73\u53f0\u6b4c\u66f2\u6bcf\u65e5\u64ad\u653e\u91cf\u81ea\u52a8\u751f\u6210\u7684\u6570\u636e\u699c\u5355\uff0c\u7edf\u8ba1\u8303\u56f4\u4e3a\u8fd1\u671f\u53d1\u884c\u7684\u6b4c\u66f2\uff0c\u6bcf\u65e5\u66f4\u65b0\u4e00\u6b21",
     * "pic_s640":"http:\/\/c.hiphotos.baidu.com\/ting\/pic\/item\/f7246b600c33874495c4d089530fd9f9d62aa0c6.jpg",
     * "pic_s444":"http:\/\/d.hiphotos.baidu.com\/ting\/pic\/item\/78310a55b319ebc4845c84eb8026cffc1e17169f.jpg",
     * "pic_s260":"http:\/\/b.hiphotos.baidu.com\/ting\/pic\/item\/e850352ac65c1038cb0f3cb0b0119313b07e894b.jpg",
     * "pic_s210":"http:\/\/business.cdn.qianqian.com\/qianqian\/pic\/bos_client_c49310115801d43d42a98fdc357f6057.jpg",
     * "web_url":"http:\/\/music.baidu.com\/top\/new"}
     */
    private static BillboardBean parseJsonBillboard(String ret) {
        BillboardBean bb = new BillboardBean();
        try {
            JSONObject obj = new JSONObject(ret);
            JSONObject billboard = obj.getJSONObject("billboard");
            bb.setBillboard_type(billboard.getString("billboard_type"));
            bb.setBillboard_no(billboard.getString("billboard_no"));
            bb.setUpdate_date(billboard.getString("update_date"));
            bb.setBillboard_songnum(billboard.getString("billboard_songnum"));
            bb.setHavemore(billboard.getInt("havemore"));
            bb.setName(billboard.getString("name"));
            bb.setComment(billboard.getString("comment"));
            bb.setPic_s640(billboard.getString("pic_s640"));
            bb.setPic_s444(billboard.getString("pic_s444"));
            bb.setPic_s260(billboard.getString("pic_s260"));
            bb.setPic_s210(billboard.getString("pic_s210"));
            bb.setWeb_url(billboard.getString("web_url"));
/***
 * 榜单数据获取无错误
 */
//            System.out.println("+++++++++存储在bb内的数据+++++++++++");
//            System.out.println(bb.toString());
//            System.out.println("+++++++++存储在bb内的数据+++++++++++");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bb;
    }


    public static BillboardBean getBillboardBean() {
        return billboardBean;
    }

    public static void setBillboardBean(BillboardBean billboardBean) {
        BaiduMusicUtils.billboardBean = billboardBean;
    }
}
