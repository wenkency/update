package cn.carhouse.update.listener;

import java.io.File;
import java.io.Serializable;

/**
 * 更新APK的监听
 */

public abstract class OnUpdateListener implements Serializable {
    /**
     * 下载失败
     */
    public abstract void onFailed(String msg);

    /**
     * 下载成功
     */
    public abstract void onSucceed(File apkFile);

    /**
     * 下载进度
     * @param total 总APK大小
     * @param current 当前进度
     * @param progress 进度百分比
     */
    public abstract void onProgress(int total, int current, float progress);
}
