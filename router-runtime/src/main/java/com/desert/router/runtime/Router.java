package com.desert.router.runtime;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Router {
    private static final String TAG = "Router";


    //编译期间生成的总映射表
    private static final String GENERATED_MAPPING = "com.desert.router.mapping.generated.RouterMapping";

    private final HashMap<String, String> routerMap = new HashMap<>();

    private static class RouterHolder {
        final static Router INSTANCE = new Router();
    }

    public static Router getInstance(){
        return  RouterHolder.INSTANCE;
    }

    public void init() {
        Class<?> clazz;
        try {
            clazz = Class.forName(GENERATED_MAPPING);
            Method method = clazz.getMethod("get");
            Map<String, String> map = (Map<String, String>) method.invoke(null);
            Log.i(TAG, "init :get all mapping:");
            Log.i(TAG, "init :get all mapping:" + map.keySet());

            for (String key : map.keySet()) {
                Log.i(TAG, " key:" + key + " value:" + map.get(key));
                routerMap.put(key, map.get(key));
            }
            Log.i(TAG, "init :get all mapping:" + map.keySet());

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "init : error while init router:" + e);
        }
    }

    public void go(Context context, String url) {
        if (context == null || url == null) {
            Log.i(TAG, "go: params error");
        }
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        String scheme = uri.getScheme();
        String path = uri.getPath();

        String targetActivityClass = null;
        Log.e(TAG, "go: key" + routerMap.keySet());

        for (String key : routerMap.keySet()) {
            Log.e(TAG, "go: key");

            Uri kUri = Uri.parse(key);
            String kHost = kUri.getHost();
            String kScheme = kUri.getScheme();
            String kPath = kUri.getPath();

            Log.i(TAG, "kHost :" + kHost);
            Log.i(TAG, "kScheme :" + kScheme);
            Log.i(TAG, "kPath :" + kPath);


            if (scheme.equals(kScheme) && host.equals(kHost) && path.equals(kPath)) {
                targetActivityClass = routerMap.get(key);
                break;
            }
        }

        if (targetActivityClass == null) {
            Log.e(TAG, "go: no destination fount");
            return;
        }

        //解析ULR里面的参数 构建出一个Bundle

        String query = uri.getQuery();
        Bundle bundle = new Bundle();
        if (query != null && query.length() > 3) {
            String[] args = query.split("&");
            for (String arg : args) {
                String[] split = arg.split("=");
                bundle.putString(split[0], split[1]);
            }
        }

        //打开对应的activity

        try {
            Intent intent = new Intent();
            intent.setClassName(context, targetActivityClass);
            intent.putExtras(bundle);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "go: start activity" + e);
        }

    }
}
