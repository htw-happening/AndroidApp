package com.happening.poc.poc_happening.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.happening.poc.poc_happening.bluetooth.Layer;
import com.happening.poc.poc_happening.handler.NotificationHandler;

public class Bluetooth4Service extends Service {

    private Layer bt4Layer = null;

    /**
     * interface for clients that bind
     */
    private final IBinder mBinder = new LocalBinder();

    /**
     * indicates how to behave if the service is killed
     */
    int mStartMode = START_STICKY;

    /**
     * indicates whether onRebind should be used
     */
    boolean mAllowRebind = false;

    /**
     * Called when the service is being created.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(this.getClass().getSimpleName(), "onCreate " + this.toString());

        bt4Layer = Layer.getInstance(this);
    }

    /**
     * The service is starting, due to a call to startService()
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(this.getClass().getSimpleName(), "onStartCommand");
        bt4Layer.startAdvertising();

        NotificationHandler.getInstance().doNotification("Happening", "This is an awseom Notification...");

        return mStartMode;
    }

    /**
     * Called when all clients have unbound with unbindService()
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(this.getClass().getSimpleName(), "onUnbind");
        return mAllowRebind;
    }

    /**
     * Called when a client is binding to the service with bindService()
     */
    @Override
    public void onRebind(Intent intent) {
        Log.d(this.getClass().getSimpleName(), "onRebind");
    }

    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {
        Log.d(this.getClass().getSimpleName(), "onDestroy");
        bt4Layer.stopAdvertising();
    }

    /**
     * A client is binding to the service with bindService()
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(this.getClass().getSimpleName(), "onBind");
        return mBinder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(this.getClass().getSimpleName(), "onTaskRemoved");
        super.onTaskRemoved(rootIntent);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public Bluetooth4Service getService() {
            // Return this instance of LocalService so clients can call public methods
            return Bluetooth4Service.this;
        }
    }

}
