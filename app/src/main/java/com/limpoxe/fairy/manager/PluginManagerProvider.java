package com.limpoxe.fairy.manager;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;

import com.limpoxe.fairy.content.LoadedPlugin;
import com.limpoxe.fairy.content.PluginDescriptor;
import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.PluginLauncher;
import com.limpoxe.fairy.manager.mapping.PluginStubBinding;
import com.limpoxe.fairy.manager.mapping.StubExact;
import com.limpoxe.fairy.manager.mapping.StubMappingProcessor;
import com.limpoxe.fairy.util.LogUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by cailiming on 16/3/11.
 *
 * 利用ContentProvider实现同步跨进程调用
 *
 */
public class PluginManagerProvider extends ContentProvider {

    private static Uri CONTENT_URI;

    public static final String ACTION_INSTALL = "install";
    public static final String INSTALL_RESULT = "install_result";

    public static final String ACTION_REMOVE = "remove";
    public static final String REMOVE_RESULT = "remove_result";

    public static final String ACTION_REMOVE_ALL = "remove_all";
    public static final String REMOVE_ALL_RESULT = "remove_all_result";

    public static final String ACTION_QUERY_BY_ID = "query_by_id";
    public static final String QUERY_BY_ID_RESULT = "query_by_id_result";

    public static final String ACTION_QUERY_BY_CLASS_NAME = "query_by_class_name";
    public static final String QUERY_BY_CLASS_NAME_RESULT = "query_by_class_name_result";

    public static final String ACTION_QUERY_BY_FRAGMENT_ID = "query_by_fragment_id";
    public static final String QUERY_BY_FRAGMENT_ID_RESULT = "query_by_fragment_id_result";

    public static final String ACTION_QUERY_ALL = "query_all";
    public static final String QUERY_ALL_RESULT = "query_all_result";

    public static final String ACTION_BIND_ACTIVITY = "bind_activity";
    public static final String BIND_ACTIVITY_RESULT = "bind_activity_result";

    public static final String ACTION_UNBIND_ACTIVITY = "unbind_activity";
    public static final String UNBIND_ACTIVITY_RESULT = "unbind_activity_result";

    public static final String ACTION_BIND_SERVICE = "bind_service";
    public static final String BIND_SERVICE_RESULT = "bind_service_result";

    public static final String ACTION_GET_BINDED_SERVICE = "get_binded_service";
    public static final String GET_BINDED_SERVICE_RESULT = "get_binded_service_result";

    public static final String ACTION_UNBIND_SERVICE = "unbind_service";
    public static final String UNBIND_SERVICE_RESULT = "unbind_service_result";

    public static final String ACTION_BIND_RECEIVER = "bind_receiver";
    public static final String BIND_RECEIVER_RESULT = "bind_receiver_result";

    public static final String ACTION_IS_EXACT = "is_exact";
    public static final String IS_EXACT_RESULT = "is_exact_result";

    public static final String ACTION_IS_STUB = "is_stub";
    public static final String IS_STUB_RESULT = "is_stub_result";

    public static final String ACTION_IS_PLUGIN_RUNNING = "is_plugin_running";
    public static final String IS_PLUGIN_RUNNING_RESULT = "is_plugin_running_result";

    public static final String ACTION_WAKEUP_PLUGIN = "wakeup_plugin";
    public static final String WAKEUP_PLUGIN_RESULT = "wakeup_plugin_result";

    public static final String ACTION_DUMP_SERVICE_INFO = "dump_service_info";
    public static final String DUMP_SERVICE_INFO_RESULT = "dump_service_info_result";

    private PluginManagerService managerService;
    private PluginStatusChangeListener changeListener;

    public static Uri buildUri() {
        if (CONTENT_URI == null) {
            CONTENT_URI = Uri.parse("content://"+ FairyGlobal.getHostApplication().getPackageName() + ".managerService" + "/call");
        }
        return CONTENT_URI;
    }

    @Override
    public boolean onCreate() {
        managerService = new PluginManagerService();
        changeListener = new PluginCallbackImpl();
        managerService.loadInstalledPlugins();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
        return getContext().getContentResolver().query(targetUrl, projection, selection, selectionArgs, sortOrder);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public Cursor query(Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
        return getContext().getContentResolver().query(targetUrl, projection, queryArgs, cancellationSignal);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
        return getContext().getContentResolver().query(targetUrl, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

    @Override
    public String getType(Uri uri) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
        return getContext().getContentResolver().getType(targetUrl);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
        return getContext().getContentResolver().insert(targetUrl, values);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
        return getContext().getContentResolver().delete(targetUrl, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
        return getContext().getContentResolver().update(targetUrl, values, selection, selectionArgs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Bundle call(String method, String arg, Bundle extras) {

        if (extras != null && extras.getParcelable("target_call") != null) {
            Uri targetUrl = extras.getParcelable("target_call");
            return getContext().getContentResolver().call(targetUrl, method, arg, extras);
        }

        if (Build.VERSION.SDK_INT >= 19) {
            LogUtil.v("callingPackage = ", getCallingPackage());
        }

        LogUtil.d("跨进程调用统计"
                + "Thead id", Thread.currentThread().getId()
                + "name", Thread.currentThread().getName()
                + "method", method
                + "arg", arg);

        Bundle bundle = new Bundle();

        if (ACTION_INSTALL.equals(method)) {

            InstallResult result = managerService.installPlugin(arg);
            bundle.putInt(INSTALL_RESULT, result.getResult());

            changeListener.onInstall(result.getResult(), result.getPackageName(), result.getVersion(), arg);

            return bundle;

        } else if (ACTION_REMOVE.equals(method)) {

            int code = managerService.remove(arg);
            bundle.putInt(REMOVE_RESULT, code);

            changeListener.onRemove(arg, code);

            return bundle;

        } else if (ACTION_REMOVE_ALL.equals(method)) {

            boolean success = managerService.removeAll();
            bundle.putBoolean(REMOVE_ALL_RESULT, success);

            return bundle;

        } else if (ACTION_QUERY_BY_ID.equals(method)) {

            PluginDescriptor pluginDescriptor = managerService.getPluginDescriptorByPluginId(arg);
            bundle.putSerializable(QUERY_BY_ID_RESULT, pluginDescriptor);

            return bundle;

        } else if (ACTION_QUERY_BY_CLASS_NAME.equals(method)) {

            PluginDescriptor pluginDescriptor = managerService.getPluginDescriptorByClassName(arg);
            bundle.putSerializable(QUERY_BY_CLASS_NAME_RESULT, pluginDescriptor);

            return bundle;

        } else if (ACTION_QUERY_BY_FRAGMENT_ID.equals(method)) {

            PluginDescriptor pluginDescriptor = managerService.getPluginDescriptorByFragmenetId(arg);
            bundle.putSerializable(QUERY_BY_FRAGMENT_ID_RESULT, pluginDescriptor);

            return bundle;

        } else if (ACTION_QUERY_ALL.equals(method)) {

            Collection<PluginDescriptor> pluginDescriptorList = managerService.getPlugins();
            ArrayList<PluginDescriptor> result =  new ArrayList<PluginDescriptor>(pluginDescriptorList.size());
            result.addAll(pluginDescriptorList);
            bundle.putSerializable(QUERY_ALL_RESULT, result);

            return bundle;

        } else if (ACTION_BIND_ACTIVITY.equals(method)) {

            bundle.putString(BIND_ACTIVITY_RESULT, PluginStubBinding.bindStub(arg, extras.getString("packageName"), StubMappingProcessor.TYPE_ACTIVITY));

            return bundle;

        } else if (ACTION_UNBIND_ACTIVITY.equals(method)) {

            PluginStubBinding.unBind(arg, extras.getString("className"), StubMappingProcessor.TYPE_ACTIVITY);

        } else if (ACTION_BIND_SERVICE.equals(method)) {
            bundle.putString(BIND_SERVICE_RESULT, PluginStubBinding.bindStub(arg, null, StubMappingProcessor.TYPE_SERVICE));

            return bundle;

        } else if (ACTION_GET_BINDED_SERVICE.equals(method)) {
            bundle.putString(GET_BINDED_SERVICE_RESULT, PluginStubBinding.getBindedPluginClassName(arg, StubMappingProcessor.TYPE_SERVICE));

            return bundle;

        } else if (ACTION_UNBIND_SERVICE.equals(method)) {

            PluginStubBinding.unBind(null, arg, StubMappingProcessor.TYPE_SERVICE);

        } else if (ACTION_BIND_RECEIVER.equals(method)) {
            bundle.putString(BIND_RECEIVER_RESULT, PluginStubBinding.bindStub(arg, null, StubMappingProcessor.TYPE_RECEIVER));

            return bundle;

        } else if (ACTION_IS_EXACT.equals(method)) {
            bundle.putBoolean(IS_EXACT_RESULT, StubExact.isExact(arg, extras.getInt("type")));

            return bundle;

        } else if (ACTION_IS_STUB.equals(method)) {
            bundle.putBoolean(IS_STUB_RESULT, PluginStubBinding.isStub(arg));

            return bundle;

        } else if (ACTION_DUMP_SERVICE_INFO.equals(method)) {
            bundle.putString(DUMP_SERVICE_INFO_RESULT, "TODO: not implement yet");

            return bundle;
        } else if (ACTION_IS_PLUGIN_RUNNING.equals(method)) {
            bundle.putBoolean(IS_PLUGIN_RUNNING_RESULT, PluginLauncher.instance().isRunning(arg));

            return bundle;
        } else if (ACTION_WAKEUP_PLUGIN.equals(method)) {
            LoadedPlugin loadedPlugin = PluginLauncher.instance().startPlugin(arg);
            bundle.putBoolean(WAKEUP_PLUGIN_RESULT, loadedPlugin!=null);

            return bundle;
        }

        return null;
    }
}
