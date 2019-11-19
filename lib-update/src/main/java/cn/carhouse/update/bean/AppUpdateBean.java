package cn.carhouse.update.bean;

import java.io.Serializable;

/**
 * 更新apk的Bean
 */

public class AppUpdateBean implements Serializable {
    // 更新APK的URL
    private String apkUrl;
    // 后台最新的版本号
    private int versionCode;

    // ==============附加信息可以不填写==============
    // apk文件名称（本地的名称）
    private String apkName;

    public AppUpdateBean(String apkUrl, String apkName, int versionCode) {
        this.apkUrl = apkUrl;
        this.apkName = apkName;
        this.versionCode = versionCode;
    }

    public String getApkUrl() {
        return apkUrl;
    }


    public int getVersionCode() {
        return versionCode;
    }

    public String getApkName() {
        return apkName;
    }
}
