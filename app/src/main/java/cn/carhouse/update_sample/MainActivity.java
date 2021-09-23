package cn.carhouse.update_sample;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.boardour.permission.OnPermissionCallback;
import com.boardour.permission.OnPermissionCallbackAdapter;
import com.boardour.permission.Permission;
import com.boardour.permission.XPermission;

import java.io.File;
import java.util.List;

import cn.carhouse.update.bean.AppUpdateBean;
import cn.carhouse.update.listener.OnSingleUpdateListener;
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
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallbackAdapter() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all){
                            down();
                        }
                    }
                });

    }

    private void down() {

        mDownloadUtils = new UpdateUtils(MainActivity.this);
        mDownloadUtils.setOnUpdateListener(new OnSingleUpdateListener() {
            @Override
            public void onStart() {
                Toast.makeText(getApplicationContext(), "正在后台更新...", Toast.LENGTH_SHORT).show();
            }

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
        String apkUrl = "https://img.car-house.cn/download/customer/app/20180507155306985.apk";
        AppUpdateBean bean = new AppUpdateBean(apkUrl, "abc.apk", 45);
        mDownloadUtils.downloadAPK(bean);
    }

    @Override
    protected void onDestroy() {
        if (mDownloadUtils != null) {
            mDownloadUtils.stop();
        }
        super.onDestroy();

    }
}
