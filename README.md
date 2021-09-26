# UpdateUtils

更新APP，有进度回调，适用于APP的更新和任何自定义更新UI的开发。 Android原生系统的下载器，不用依赖三方网络库，支持通知栏显示，兼容性较好。

### 引入

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

dependencies {

    // 更新(只依赖这个就行)
    implementation 'com.github.wenkency:update:2.0.0'

    // 网络请求(测试用，通过网络调用后台接口，获取APK更新信息)
    implementation 'com.github.wenkency:kotlin-retrofit:2.0.0'
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.google.code.gson:gson:2.8.8'
    
    // implementation files("${projectDir.getAbsolutePath()}/libs/update.aar")

   
}
```

### 网络初始化

```
    // 网络初始化
    RestConfig.INSTANCE
            .baseUrl("http://xx.xxx.xxx")
            .register(this);
```

### APK更新使用事例

```
/**
 * APK更新事例
 */
public class MainActivity extends AppCompatActivity {
    // 更新的工具类
    private UpdateUtils mDownloadUtils;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.tv);
        
        mDownloadUtils = new UpdateUtils(this);

    }

    /**
     * 调用更新
     */
    public void downloadApk(View view) {
        // 1. 申请内存卡读取权限
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

    // 2. 网络请求更新接口
    private void update() {
        // JSON地址
        final String url = "xx/xx/apkupdate.json";
        RetrofitPresenter.INSTANCE.get(this, url, new BeanCallback<UpdateBean>() {
            @Override
            public void onSucceed(UpdateBean data) {
                down(data);
            }
        });
    }

    // 3. 下载APK
    private void down(UpdateBean data) {
        // 创建更新类
        AppUpdateBean bean = new AppUpdateBean(data.getUrl(), data.getAppName(), data.getVersionCode());
        // 设置下载标题
        mDownloadUtils.setTitle(data.getApkName());
        // 更新进度回调
        mDownloadUtils.setOnUpdateListener(new OnSingleUpdateListener() {
            @Override
            public void onProgress(int total, int current, float progress) {
                mTextView.setText(total + ":" + String.format("%.2f", progress) + "%");
            }
        });
        // 更新
        mDownloadUtils.downloadAPK(bean);
    }

    @Override
    protected void onDestroy() {
        // 4. 销毁操作
        if (mDownloadUtils != null) {
            mDownloadUtils.stop();
            mDownloadUtils = null;
        }
        super.onDestroy();
    }
}
```