package host.xsoft.com.pluginhost;

import android.content.Context;
import android.util.Log;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.PluginApplication;
import com.limpoxe.fairy.core.PluginLoader;
import com.tencent.smtt.sdk.QbSdk;

/**
 * Created by benylwang on 2017/11/29.
 */

public class TheApplication extends PluginApplication {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        //框架日志开关, 默认false
        FairyGlobal.setLogEnable(true);

        //首次加载插件会创建插件对象，比较耗时，通过弹出loading页来过渡。
        //这个方法是设置首次加载插件时, 定制loading页面的UI, 不传即默认没有loading页
        //在宿主中创建任意一个layout传进去即可
        //注意：首次唤起插件组件时，如果是通过startActivityForResult唤起的，如果配置了loading页，
        //则实际是先打开了loading页，再转到目标页面，此时会忽略ForResult的结果。这种情况下应该禁用loading页配置
//        FairyGlobal.setLoadingResId(R.layout.loading);

        //是否支持插件中使用本地html, 默认false
        FairyGlobal.setLocalHtmlenable(true);

        //初始化框架
        PluginLoader.initLoader(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(),  cb);
    }

    @Override
    public Context getBaseContext() {
        return PluginLoader.fixBaseContextForReceiver(super.getBaseContext());
    }
}
