package com.example.callapp_user.customtooltip;

import android.app.Application;
import android.os.Handler;

import java.lang.ref.WeakReference;

public class ApplicationExtended extends Application {
    private static WeakReference<ApplicationExtended> application;
    private final Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        application = new WeakReference<>(this);
    }

    public void postRunnableDelayed(Runnable runnable, int delayMillis) {
        handler.postDelayed(runnable, delayMillis);
    }

    public static ApplicationExtended get() {
        return application.get();
    }

    @Override
    public void onTerminate() {
        application = null;
        super.onTerminate();
    }
}