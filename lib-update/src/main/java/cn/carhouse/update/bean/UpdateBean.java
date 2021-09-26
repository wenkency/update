package cn.carhouse.update.bean;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 后台请求，更新的JavaBean
 */
public class UpdateBean implements Serializable {
    private List<String> appname;
    private String versioncode;
    private String url;

    public List<String> getAppname() {
        return appname;
    }

    public void setAppname(List<String> appname) {
        this.appname = appname;
    }

    public String getAppName() {
        try {
            return appname.get(0) + ".apk";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public String getVersioncode() {
        return versioncode;
    }

    public void setVersioncode(String versioncode) {
        this.versioncode = versioncode;
    }

    public int getVersionCode() {
        try {
            return Integer.parseInt(versioncode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getUrl() {
        if (!url.startsWith("http")) {
            return "http://" + url;
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApkName() {
        try {
            String apkName = url.substring(url.lastIndexOf("/") + 1);
            if (!TextUtils.isEmpty(apkName)) {
                return apkName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "update.apk";
    }

    @Override
    public String toString() {
        return "UpdateBean{" +
                "appname=" + appname +
                ", versioncode='" + versioncode + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
