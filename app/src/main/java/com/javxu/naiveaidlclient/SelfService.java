package com.javxu.naiveaidlclient;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class SelfService extends Service {

    public SelfService() {
    }

    private class SelfMethodBinder extends Binder implements ISelfMethod { // 把代理人 private 起来

        public SelfMethodBinder() {
            Log.d("BindLog", "SelfService - 内部代理已创建");
        }

        @Override
        public void callSelfMethod() {
            Log.d("BindLog", "SelfService - 内部代理去调用SelfService内部方法");
            selfMethod();
        }

    }

    @Override
    public void onCreate() {
        Log.d("BindLog", "SelfService - onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 以 startService() 开启服务，onStartCommand() 才会紧接 onCreate() 调用
        // 如果直接以 bindService() 开启服务并 bind，onStartCommand() 就不会调用
        Log.d("BindLog", "SelfService - onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        //bindService时调用，并返回给连接对象一个代理
        Log.d("BindLog", "SelfService - onBind");
        return new SelfMethodBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("BindLog", "SelfService - onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d("BindLog", "SelfService - onDestroy" + "\n" + "---------------------------------");
        super.onDestroy();
    }

    public void selfMethod() {
        Log.d("BindLog", "SelfService - 内部方法已被调用");
    }
}