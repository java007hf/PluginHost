package host.xsoft.com.pluginhost;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.limpoxe.fairy.manager.PluginManagerHelper;
import com.limpoxe.fairy.util.FileUtil;
import com.tencent.smtt.sdk.WebView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button mInstallBtn;
    private Button mStartBtn;
    private static final String PLUGIN_PKGNAME = "com.example.test_webview_demo";
    private static final String PLUGIN_APP = "app-debug.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
		WebView webView = (WebView) findViewById(R.id.webviews);
		webView.loadUrl("file:///android_asset/webpage/sport/html/heart_five.html");

        mInstallBtn = (Button)findViewById(R.id.install);
        mStartBtn = (Button)findViewById(R.id.start);

        mInstallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "开始安装插件", Toast.LENGTH_SHORT).show();
                boolean b = copyAndInstall(PLUGIN_APP);
                if (b) {
                    Toast.makeText(MainActivity.this, "安装成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "安装失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PluginManagerHelper.isInstalled(PLUGIN_PKGNAME)) {
                    Toast.makeText(MainActivity.this, "插件还未安装", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "启动插件", Toast.LENGTH_SHORT).show();

                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(PLUGIN_PKGNAME);
                    launchIntent.setAction("com.example.test_webview_demo.MainActivity");
                    startActivity(launchIntent);
                }
            }
        });
    }

    public boolean copyAndInstall(String name) {
        boolean isSuccess = false;
        InputStream assestInput = null;
        try {
            assestInput = getAssets().open(name);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null == assestInput) {
            Log.e(TAG, "读取Assets下面的：" + name + "失败，请确认该文件是否存在~");
            return isSuccess;
        }

        String dest = getApplication().getCacheDir().getAbsolutePath() + "/" + name;
        if (FileUtil.copyFile(assestInput, dest)) {
            PluginManagerHelper.installPlugin(dest);
            isSuccess = true;
        } else {
            Log.e(TAG, "抽取assets中的Apk失败");
        }

        return isSuccess;
    }
}
