package com.example.demo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.crash.PgyerCrashObservable;
import com.pgyersdk.crash.PgyerObserver;
import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;

import java.io.File;


/********************************************************
 *
 *     crash 上报错误日志无法使用
 *
 *
 *
 * ********************************************************/

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public final String TAG = "fanxiaobo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestStoragePermission();
        Init();
    }

    @Override
    public void onClick(View view) {

    }
    private void Init() {

        /*************************************PGY初始化********************************************/
        //1.启动 Pgyer 检测 Crash 功能；
        PgyCrashManager.register(); //启动 Pgyer 检测 Crash 功能；

        PgyCrashManager.setIsIgnoreDefaultHander(true); //默认设置为false;
        //设置为 true ,则忽略系统默认Crash 操作，SDK 会重启启动 app 的当前页面

        new PgyUpdateManager.Builder()
                .setForced(true)                //设置是否强制提示更新,非自定义回调更新接口此方法有用
                .setUserCanRetry(false)         //失败后是否提示重新下载，非自定义下载 apk 回调此方法有用
                .setDeleteHistroyApk(false)     //检查更新前是否删除本地历史 Apk， 默认为true
                .setUpdateManagerListener(new UpdateManagerListener() {
                    @Override
                    public void onNoUpdateAvailable() {
                        //没有更新回调此方法
                        Log.d(TAG, "there is no new version");
                    }
                    @Override
                    public void onUpdateAvailable(final AppBean appBean) {
                        //有更新回调此方法
                        Log.d(TAG, "there is new version can update"
                                + "new versionCode is " + appBean.getVersionCode());

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("提示");
                        builder.setMessage("有新版本: "+ appBean.getVersionCode() + ",是否更新?");
                        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //调用以下方法，DownloadFileListener 才有效；
                                //如果完全使用自己的下载方法，不需要设置DownloadFileListener
                                PgyUpdateManager.downLoadApk(appBean.getDownloadURL());
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }

                    @Override
                    public void checkUpdateFailed(Exception e) {
                        //更新检测失败回调
                        //更新拒绝（应用被下架，过期，不在安装有效期，下载次数用尽）以及无网络情况会调用此接口
                        Log.e(TAG, "check update failed 更新拒绝（应用被下架，过期，不在安装有效期，下载次数用尽）以及无网络情况会调用此接口");
                    }
                })
                //注意 ：
                //下载方法调用 PgyUpdateManager.downLoadApk(appBean.getDownloadURL()); 此回调才有效
                //此方法是方便用户自己实现下载进度和状态的 UI 提供的回调
                //想要使用蒲公英的默认下载进度的UI则不设置此方法
                .setDownloadFileListener(new DownloadFileListener() {
                    @Override
                    public void downloadFailed() {
                        //下载失败
                        Log.e(TAG, "download apk failed");
                        ShowUserMsg("请求更新失败！");
                    }

                    @Override
                    public void downloadSuccessful(File file) {
                        Log.e(TAG, "download apk success" + file.getAbsolutePath() + file.getName());
                        // 使用蒲公英提供的安装方法提示用户 安装apk
                        PgyUpdateManager.installApk(file);
                        //android.os.Process.killProcess(android.os.Process.myPid());
                    }

                    @Override
                    public void onProgressUpdate(Integer... integers) {
                        Log.e(TAG, "update download apk progress" + integers);
                    }})
                .register();
        /*****************************************************************************************/


    }

    public void ShowUserMsg(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("提示");
        builder.setMessage(msg);
        builder.setPositiveButton("好的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    private void RequestStoragePermission(){
        //请求存储权限用于在API23+以上，不仅要在AndroidManifest.xml里面添加权限 还要在JAVA代码中请求权限：
        // Storage Permissions
        final int REQUEST_EXTERNAL_STORAGE = 1;
        final String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE };

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1997);
            }
        }
    }
}