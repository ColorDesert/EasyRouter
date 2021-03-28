package com.desert.router;

import android.app.Application;
import com.desert.router.runtime.Router;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Router.getInstance().init();

    }
}
