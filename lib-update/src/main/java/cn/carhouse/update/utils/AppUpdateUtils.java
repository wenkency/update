package cn.carhouse.update.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;

/**
 * APP更新工具类
 */

public class AppUpdateUtils {
    /**
     * 获取版本号
     */
    public static int getVersionCode(Context context) {
        if (getPackageInfo(context) != null) {
            try {
                return getPackageInfo(context).versionCode;
            } catch (Exception e) {
            }
        }
        return 0;
    }

    private static PackageInfo getPackageInfo(Context context) {
        if (context == null) {
            return null;
        }
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(), 0);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pi;
    }

    /**
     * 检查是否SDK准备好
     */
    private static boolean checkSDExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 创建apk下载文件
     * 这里如果有其它需求，可以改成你想要的下载路径
     */
    public static File getApkFile(Context context, String fileName) {
        // 创建目录
        File directory = null;
        if (checkSDExist()) {
            directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        }
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File apkFile = new File(directory, fileName);
        return apkFile;
    }

    /**
     * 获取app缓存路径    SDCard/Android/data/你的应用的包名/cache
     *
     * @param context
     * @return
     */
    public static File getCacheDir(Context context) {
        File cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            //外部存储可用
            cachePath = context.getExternalCacheDir();
        } else {
            //外部存储不可用
            cachePath = context.getCacheDir();
        }
        return cachePath;
    }
}
