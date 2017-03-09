package cn.itcast.musicapp.listen;

import android.support.v7.widget.RecyclerView;

/**
 * Created by CreazyMa on 2017/3/7.
 */

public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {//实现滑动隐藏
    private static final int Hide_Threshold = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (scrolledDistance > Hide_Threshold && controlsVisible){
            onHide();
            controlsVisible = false;
            scrolledDistance = 0;
        }else if (scrolledDistance < -Hide_Threshold && !controlsVisible){
            onShow();
            controlsVisible = true;
            scrolledDistance = 0;
        }
        if ((controlsVisible && dy > 0)||(!controlsVisible && dy<0)){
            scrolledDistance += dy;
        }
    }
    public abstract void onHide();
    public abstract void onShow();
}
