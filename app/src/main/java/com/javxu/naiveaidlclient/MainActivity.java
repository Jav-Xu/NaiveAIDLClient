package com.javxu.naiveaidlclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.javxu.naiveaidlserver.IRemoteServiceMethod;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mStartSelfButton;
    private Button mStopSelfButton;
    private Button mBindSelfButton;
    private Button mUnBindSelfButton;
    private Button mCallSelfButton;

    private Button mStartRemoteButton;
    private Button mStopRemoteButton;
    private Button mBindRemoteButton;
    private Button mUnBindRemoteButton;
    private Button mCallRemoteButton;

    private boolean bindSelf = false; // 内部服务连接状态
    private boolean bindRemote = false; // 远程服务连接状态

    // private SelfService.SelfMethodBinder selfServiceMethodSolver;
    // 不能明确直接获取Sevice里的代理，因为私有起来了，如下，只是得到一个可以办理具体某个事情的人

    private ISelfMethod selfServiceMethodSolver; // 一个ISelfMethod实现类，单纯可以解决这个事的人

    private IRemoteServiceMethod remoteServiceMethodSolver;

    private Intent selfIntent;
    private Intent remoteIntent;

    private ServiceConnection conn_self = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //selfServiceMethodSolver = (SelfService.SelfMethodBinder) iBinder;
            selfServiceMethodSolver = (ISelfMethod) iBinder;
            Log.d("BindLog", "Client - SelfService已Bind成功，并获取了代理");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // 震惊！！！ onServiceDisconnected is only called in extreme situations (crashed / killed)
            // 也就是说，这个方法并不会在 unbindService(conn_self) 时调用！！！
            //selfServiceMethodSolver = null; // 没用
            //Log.d("BindLog", "bind连接已断开"); // 没用
        }
    };

    private ServiceConnection conn_remote = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //selfServiceMethodSolver = (SelfService.SelfBinder) iBinder;
            remoteServiceMethodSolver = IRemoteServiceMethod.Stub.asInterface(iBinder);
            Log.d("BindLog", "bind远程服务连接已成功，并获取了代理");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initIntent();
    }

    private void initView() {
        mStartSelfButton = (Button) findViewById(R.id.button_startself);
        mStartSelfButton.setOnClickListener(this);
        mStopSelfButton = (Button) findViewById(R.id.button_stopself);
        mStopSelfButton.setOnClickListener(this);

        mBindSelfButton = (Button) findViewById(R.id.button_bindself);
        mBindSelfButton.setOnClickListener(this);
        mUnBindSelfButton = (Button) findViewById(R.id.button_unbindself);
        mUnBindSelfButton.setOnClickListener(this);

        mStartRemoteButton = (Button) findViewById(R.id.button_startremote);
        mStartRemoteButton.setOnClickListener(this);
        mStopRemoteButton = (Button) findViewById(R.id.button_stopremote);
        mStopRemoteButton.setOnClickListener(this);

        mBindRemoteButton = (Button) findViewById(R.id.button_unbindremote);
        mBindRemoteButton.setOnClickListener(this);
        mUnBindRemoteButton = (Button) findViewById(R.id.button_unbindremote);
        mUnBindRemoteButton.setOnClickListener(this);

        mCallSelfButton = (Button) findViewById(R.id.button_callself);
        mCallSelfButton.setOnClickListener(this);
        mCallRemoteButton = (Button) findViewById(R.id.button_callremote);
        mCallRemoteButton.setOnClickListener(this);
    }

    private void initIntent() {

        // 内部服务，显示调用
        selfIntent = new Intent(MainActivity.this, SelfService.class);

        // 远程服务，本该隐式调用，但5.0之后需要明文标示地调用
        remoteIntent = new Intent().setComponent(
                new ComponentName("com.javxu.naiveaidlserver", "com.javxu.naiveaidlserver.RemoteService"));
        //remoteIntent.setAction("com.javxu.servicealipaycompany.service.action");
        //remoteIntent.setPackage("com.javxu.servicealipaycompany");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_startself:
                startService(selfIntent);
                break;
            case R.id.button_bindself:
                if (bindSelf) { // 必要的连接状态检查，不要重复 bind
                    return;
                }
                bindSelf = bindService(selfIntent, conn_self, BIND_AUTO_CREATE);
                // selfServiceMethodSolver.callSelfMethod();
                // 注意！！！不要紧接bind就调用代理的方法，此时bind可能还未获得，为空值！！！
                break;
            case R.id.button_callself:
                if (selfServiceMethodSolver == null) { // 必要的空值判断！！！
                    Log.d("BindLog", "selfBinderCopy为空");
                    return;
                }
                selfServiceMethodSolver.callSelfMethod();
                break;
            case R.id.button_unbindself:
                if (bindSelf) { // 必要的连接状态检查，重复 unBind 会报错
                    unbindService(conn_self);
                    bindSelf = false;
                    selfServiceMethodSolver = null;
                }
                // 即使解绑了，selfServiceMethodSolver 仍然有值
                // 也就是Service方法仍然可以调用，所以在断开连接时要设置空值
                // 但是 onServiceDisconnected 没有调用，不能去那里给 selfServiceMethodSolver 设置空值
                break;
            case R.id.button_stopself:
                stopService(selfIntent);
                break;

            // ------------------------------------------------------------------

            case R.id.button_startremote:
                startService(remoteIntent);
                break;
            case R.id.button_bindremote:
                if (bindRemote) {
                    return;
                }
                bindRemote = bindService(remoteIntent, conn_remote, BIND_AUTO_CREATE);
                //TODO bind失败 >_<
                break;
            case R.id.button_callremote:
                if (remoteServiceMethodSolver == null) { // 必要的空值判断！！！
                    Log.d("BindLog", "remoteServiceMethodSolver为空");
                    return;
                }
                try {
                    remoteServiceMethodSolver.callRemoteServiceMethod();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button_unbindremote:
                if (bindRemote) {
                    unbindService(conn_remote);
                    bindRemote = false;
                    remoteServiceMethodSolver = null;
                    Log.d("BindLog", "bind远程服务连接已断开");
                }
                break;
            case R.id.button_stopremote:
                stopService(remoteIntent);
                break;
        }
    }
}
