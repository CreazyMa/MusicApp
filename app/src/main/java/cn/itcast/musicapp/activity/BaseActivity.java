package cn.itcast.musicapp.activity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;

/**
 * Created by CreazyMa on 2017/3/19.
 */

public class BaseActivity extends AppCompatActivity {
    private Map<Integer,Runnable> allowablePermissionRunnables = new HashMap<>();
    private Map<Integer,Runnable> disallowablePermissionRunnables = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 请求权限
     * @param id 请求授权的id 唯一标识即可
     * @param permission 请求的权限
     * @param allowableRunnable 同意授权后的操作
     * @param disallowableRunnable 禁止权限后的操作
     */

    public void requestPermission(int id, String permission, Runnable allowableRunnable, Runnable disallowableRunnable) throws IllegalFormatException {
        if(allowableRunnable == null){
            throw new IllegalArgumentException("allowableRunnable == null");
        }

        allowablePermissionRunnables.put(id,allowableRunnable);

        if (disallowableRunnable != null){
            disallowablePermissionRunnables.put(id,disallowableRunnable);
        }
        if (Build.VERSION.SDK_INT >= 23){
            //检查是否拥有权限
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(getApplicationContext(),permission);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                //弹出对话框接收权限
                ActivityCompat.requestPermissions(BaseActivity.this,new String[]{permission},id);
                return;
            }else {
                allowableRunnable.run();
            }
        }else {
            allowableRunnable.run();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Runnable allowRun = allowablePermissionRunnables.get(requestCode);
            allowRun.run();
        }else {
            Runnable diaallowRun = allowablePermissionRunnables.get(requestCode);
            diaallowRun.run();
        }
    }
}
