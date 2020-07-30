package cn.carhouse.update.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.io.File;

import cn.carhouse.filepaths.AppFileProvider;
import cn.carhouse.update.bean.AppUpdateBean;
import cn.carhouse.update.listener.OnUpdateListener;


/**
 * 下载更新APP的工具类
 */

public class UpdateUtils {
    private static final String CONFIG = "APK_UPDATE";
    private static final String DOWNLOADED = "DOWNLOADED";
    // 默认APK本地名称
    public static final String DEF_APK_NAME = "update.apk";
    // 描述信息
    public String description = "版本更新";
    // 你的App名称
    private String title = "爱车小屋";
    private final SharedPreferences mPreferences;
    // 下载器
    private DownloadManager downloadManager;
    private Context mContext;
    // 下载的ID
    private long downloadId = -1;
    // 下载要用到的类
    private AppUpdateBean mUpdateBean;
    private OnUpdateListener mOnUpdateListener;
    private static Handler mHandler = new Handler(Looper.getMainLooper());
    /**
     * 自动安装
     */
    private boolean autoInstall = true;
    private static boolean isStart = false;


    public UpdateUtils(Context context, AppUpdateBean updateBean) {
        if (mContext == null) {
            new RuntimeException("context is null");
        }
        this.mContext = context.getApplicationContext();
        this.mUpdateBean = updateBean;
        mPreferences = mContext.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAutoInstall(boolean autoInstall) {
        this.autoInstall = autoInstall;
    }

    /**
     * 注意：要自己添加内存卡读取权限
     * 下载apk主要方法
     */
    public void downloadAPK() {
        // 1. 非空校验
        if (mContext == null || mUpdateBean == null || isStart) {
            return;
        }
        // 校验有没有在下载了
        // 2. URL校验
        String url = mUpdateBean.getApkUrl();
        if (TextUtils.isEmpty(url) || !URLUtil.isNetworkUrl(mUpdateBean.getApkUrl())) {
            Toast.makeText(mContext, "APK下载地址不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        // 1. 在这里要做一下校验
        File apkFile = getApkFile();
        // 判断有没有下载成功
        if (apkFile.exists() && apkFile.isFile() && apkFile.length() > 1024 && isDownload()) {
            try {
                // 判断下载好的APK版本和正在使用的APK版本
                int versionCode = AppUpdateUtils.getVersionCode(mContext);
                // 获取下载好的APK版本号
                PackageInfo packageInfo = mContext.getPackageManager()
                        .getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
                int apkVersionCode = packageInfo.versionCode;
                // 1. 下载好的APK是不是最新的
                // 2. 正在使用的APK是不是最新的
                if (apkVersionCode >= mUpdateBean.getVersionCode() && apkVersionCode > versionCode) {
                    if (mOnUpdateListener != null) {
                        mOnUpdateListener.onSucceed(apkFile);
                    } else {
                        installApk();
                    }
                    return;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        // 2. 删除APK
        deleteApkFile(apkFile);
        // 3. 创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 移动网络情况下是否允许漫游
        request.setAllowedOverRoaming(false);
        request.allowScanningByMediaScanner();
        // 在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setTitle(title)
                .setDescription(description)
                .setVisibleInDownloadsUi(true);
        // 4. 设置下载的路径 --Uri.fromFile(getApkFile())
        request.setDestinationUri(Uri.fromFile(getApkFile()));
        // 获取DownloadManager
        if (downloadManager == null)
            downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        // 将下载请求加入下载队列，加入下载队列后会给该任务返回一个long型的id，通过该id可以取消任务，重启任务、获取下载的文件等等
        if (downloadManager != null) {
            downloadId = downloadManager.enqueue(request);
        }
        // 注册广播接收者，监听下载完成状态
        mContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        if (mOnUpdateListener != null) {
            // 开启个任务去每秒查询下载进度
            mHandler.postDelayed(mTask, 1000);
        }
        if (mOnUpdateListener != null) {
            mOnUpdateListener.onStart();
        }
        isStart = true;
    }

    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            if (mOnUpdateListener != null) {
                checkStatus();
                mHandler.postDelayed(mTask, 1000);
            }
        }
    };

    /**
     * 手动删除原来的APK，下载器不会覆盖。
     */
    private void deleteApkFile(File apkFile) {
        try {
            putDownload(false);
            if (apkFile.exists()) {
                apkFile.delete();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    /**
     * 广播监听下载完成
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 下载完成会调一次这里
            checkStatus();
        }
    };

    /**
     * 检查下载状态
     */
    private void checkStatus() {
        if (downloadId == -1) {
            return;
        }
        DownloadManager.Query query = new DownloadManager.Query();
        // 通过下载的id查找
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor == null) {
            return;
        }
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            try {
                // 如果有下载监听就去查询文件下载进度
                if (mOnUpdateListener != null) {
                    //已经下载文件大小
                    int current = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    //下载文件的总大小
                    int total = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    if (total >= 0 && current >= 0 && mOnUpdateListener != null) {
                        // 更新进度
                        float progress = current * 100f / total;
                        mOnUpdateListener.onProgress(total, current, progress);
                    }
                }
            } catch (Throwable e) {
            }
            switch (status) {
                // 下载暂停
                case DownloadManager.STATUS_PAUSED:
                    break;
                // 下载延迟
                case DownloadManager.STATUS_PENDING:
                    break;
                // 正在下载
                case DownloadManager.STATUS_RUNNING:
                    break;
                // 下载完成
                case DownloadManager.STATUS_SUCCESSFUL:
                    mHandler.removeCallbacks(mTask);
                    isStart = false;
                    // 下载完成安装APK
                    putDownload(true);
                    // 有监听让用户去做
                    if (mOnUpdateListener != null) {
                        mOnUpdateListener.onSucceed(getApkFile());
                        // 自动安装
                        if (autoInstall) {
                            installApk();
                        }
                    } else {
                        installApk();
                    }
                    cursor.close();
                    if (mContext != null) {
                        mContext.unregisterReceiver(receiver);
                    }
                    break;
                // 下载失败
                case DownloadManager.STATUS_FAILED:
                    isStart = false;
                    mHandler.removeCallbacks(mTask);
                    if (mOnUpdateListener != null) {
                        mOnUpdateListener.onFailed("下载失败");
                    } else {
                        Toast.makeText(mContext, "下载失败", Toast.LENGTH_SHORT).show();
                    }
                    cursor.close();
                    break;
            }
        }
    }

    /**
     * 安装APK
     */
    private void installApk() {
        // 安装APK
        AppFileProvider.installApk(mContext, getApkFile());
    }

    /**
     * 安装APK
     */
    public void installApk(File file) {
        // 安装APK
        AppFileProvider.installApk(mContext, file);
    }

    /**
     * 文件下载的路径
     */
    private File getApkFile() {
        String fileName = mUpdateBean.getApkName();
        if (TextUtils.isEmpty(fileName)) {
            fileName = DEF_APK_NAME;
        }
        return AppUpdateUtils.getApkFile(mContext, fileName);
    }


    /**
     * 缓存下载信息
     */
    private void putDownload(boolean isDownload) {
        if (mPreferences == null) {
            return;
        }
        mPreferences.edit().putBoolean(DOWNLOADED, isDownload).commit();
    }

    /**
     * 有没有下载完成
     */
    private boolean isDownload() {
        if (mPreferences == null) {
            return false;
        }
        return mPreferences.getBoolean(DOWNLOADED, false);
    }

    public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        mOnUpdateListener = onUpdateListener;
    }

    /**
     * 如果设置了下载进度回调，在Activity 的OnDestroy方法调用 一下。
     */
    public void stop() {
        try {
            mOnUpdateListener = null;
            mHandler.removeCallbacks(mTask);
            isStart = false;
            if (mContext != null) {
                mContext.unregisterReceiver(receiver);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}