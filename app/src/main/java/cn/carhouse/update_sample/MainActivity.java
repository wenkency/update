package cn.carhouse.update_sample;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.boardour.permission.OnPermissionCallbackAdapter;
import com.boardour.permission.Permission;
import com.boardour.permission.XPermission;
import com.lven.retrofit.RetrofitPresenter;
import com.lven.retrofit.callback.BeanCallback;
import com.lven.retrofit.config.RestConfig;

import java.util.List;

import cn.carhouse.update.bean.AppUpdateBean;
import cn.carhouse.update.bean.UpdateBean;
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
        // 网络测试
        RestConfig.INSTANCE
                .baseUrl("https://en.boardour.com")
                .register(getApplication());
        mDownloadUtils = new UpdateUtils(this);

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
                        if (all) {
                            update();
                        }
                    }
                });

    }

    // 请求更新接口
    private void update() {
        final String url = "download/s80/apkupdater1.json";
        RetrofitPresenter.INSTANCE.get(this, url, new BeanCallback<UpdateBean>() {
            @Override
            public void onSucceed(UpdateBean data) {
                down(data);
            }
        });
    }

    private void down(UpdateBean data) {
        AppUpdateBean bean = new AppUpdateBean(data.getUrl(), data.getAppName(), data.getVersionCode());
        mDownloadUtils.setOnUpdateListener(new OnSingleUpdateListener() {
            @Override
            public void onProgress(int total, int current, float progress) {
                mTextView.setText(total + ":" + String.format("%.2f", progress) + "%");
            }
        });
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
