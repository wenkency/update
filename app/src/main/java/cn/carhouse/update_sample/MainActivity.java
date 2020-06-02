package cn.carhouse.update_sample;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import cn.carhouse.permission.Permission;
import cn.carhouse.permission.PermissionListenerAdapter;
import cn.carhouse.permission.XPermission;
import cn.carhouse.update.bean.AppUpdateBean;
import cn.carhouse.update.listener.OnUpdateListener;
import cn.carhouse.update.utils.UpdateUtils;

public class MainActivity extends AppCompatActivity {

    private UpdateUtils mDownloadUtils;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.tv);
    }

    /**
     * 下载代码
     */
    public void downloadApk(View view) {
        // 申请内存卡读取权限
        XPermission.with(this)
                .permissions(Permission.STORAGE)
                .request(new PermissionListenerAdapter() {
                    @Override
                    public void onSucceed() {
                        down();
                    }
                });

    }

    private void down() {
        String apkUrl = "https://static.car-house.cn/download/business/app/B_2.9.3_0602_online.apk";
        AppUpdateBean bean = new AppUpdateBean(apkUrl, "B_2.9.3_0602_online.apk", 152);
        mDownloadUtils = new UpdateUtils(MainActivity.this, bean);
        mDownloadUtils.setOnUpdateListener(new OnUpdateListener() {
            @Override
            public void onFailed(String msg) {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSucceed(File apkFile) {
                // 安装
                mDownloadUtils.installApk(apkFile);
                Toast.makeText(getApplicationContext(), "下载成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(int total, int current, float progress) {
                mTextView.setText(String.format("%.2f", progress) + "%");
            }
        });
        mDownloadUtils.downloadAPK();
    }

    @Override
    protected void onDestroy() {
        if (mDownloadUtils != null) {
            mDownloadUtils.stop();
        }
        super.onDestroy();

    }
}
