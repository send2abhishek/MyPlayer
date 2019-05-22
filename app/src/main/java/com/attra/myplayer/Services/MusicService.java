package com.attra.myplayer.Services;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.attra.myplayer.PlayBackInfo.MediaPlayerHolder;
import com.attra.myplayer.Utils.NotificationMangerHelper;

import static com.attra.myplayer.MainActivity.TAG;

public class MusicService extends Service {

    private MediaPlayerHolder mediaPlayerHolder;
    private NotificationMangerHelper notificationMangerHelper;

    private boolean sRestoredFromPause = false;

    public final boolean isRestoredFromPause() {
        return sRestoredFromPause;
    }

    public void setRestoredFromPause(boolean restore) {
        sRestoredFromPause = restore;
    }


    public MediaPlayerHolder getMediaPlayerHolder() {
        return mediaPlayerHolder;
    }

    public NotificationMangerHelper getNotificationMangerHelper() {
        return notificationMangerHelper;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {

       if(mediaPlayerHolder==null){

        mediaPlayerHolder=new MediaPlayerHolder(this);
        notificationMangerHelper=new NotificationMangerHelper(this);
        mediaPlayerHolder.registerNotificationActionsReceiver(true);
       }
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }



    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Service");
        mediaPlayerHolder.release();
        mediaPlayerHolder.registerNotificationActionsReceiver(false);
        super.onDestroy();
    }

    public class MyBinder extends Binder{

        public MusicService getInstance(){

            return MusicService.this;
        }
    }
}
