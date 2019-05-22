package com.attra.myplayer.Utils;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import com.attra.myplayer.MainActivity;
import com.attra.myplayer.PlayBackInfo.PlayBackInfoListener;
import com.attra.myplayer.R;
import com.attra.myplayer.Services.MusicService;

public class NotificationMangerHelper {

    public static final int REQUEST_CODE = 101;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private MusicService musicService;
    private Context context;
    private static final String CHANNEL_ID="channel_id";
    public static final String ACTION_PREV="action.PREV";
    public static final String ACTION_NEXT="action.NEXT";
    public static final String ACTION_PLAY_PAUSE="action.PLAY_PAUSE";
    private MediaSessionCompat mediaSession;
    private MediaSessionManager mediaSessionManager;
    private MediaControllerCompat.TransportControls transportControls;


    public NotificationMangerHelper(MusicService musicService) {
        this.musicService = musicService;
        context=musicService.getBaseContext();
        notificationManager=(NotificationManager)musicService.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public NotificationCompat.Builder getNotificationBuilder() {
        return notificationBuilder;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public Notification createNotification(){
        createNotificationChannel();
        final Songs songs=musicService.getMediaPlayerHolder().getCurrentSongs();
        final Intent openPlayerIntent = new Intent(musicService, MainActivity.class);
        openPlayerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent contentIntent = PendingIntent.getActivity(musicService, REQUEST_CODE,
                openPlayerIntent, 0);

        initMediaSession(songs);
        notificationBuilder=new NotificationCompat.Builder(musicService,CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.mipmap.ic_play)

                .setContentTitle(songs.getTitle())
                .setContentText(songs.getArtistName())
                .setColor(context.getResources().getColor(R.color.colorAccent))
                .setContentIntent(contentIntent)
                .addAction(setAction(ACTION_PREV))
                .addAction(setAction(ACTION_PLAY_PAUSE))
                .addAction(setAction(ACTION_NEXT))
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                );


        return notificationBuilder.build();
    }

    private void initMediaSession(Songs songs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSessionManager = ((MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE));
            mediaSession = new MediaSessionCompat(context, "AudioPlayer");
            transportControls = mediaSession.getController().getTransportControls();
            mediaSession.setActive(true);
            mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
            //updateMetaData(song);
        }

    }


    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                final NotificationChannel notificationChannel =
                        new NotificationChannel(CHANNEL_ID,
                                musicService.getString(R.string.app_name),
                                NotificationManager.IMPORTANCE_LOW);

                notificationChannel.setDescription(
                        musicService.getString(R.string.app_name));

                notificationChannel.enableLights(false);
                notificationChannel.enableVibration(false);
                notificationChannel.setShowBadge(false);

                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }


    private NotificationCompat.Action setAction(String action){

        int icon;

        switch (action){
            default:
            case ACTION_PREV:
                icon=R.mipmap.ic_rewind;
                break;
            case ACTION_NEXT:
                icon=R.mipmap.ic_forward;
                break;
            case ACTION_PLAY_PAUSE:
                icon=musicService.getMediaPlayerHolder().getState() !=
                        PlayBackInfoListener.State.PAUSED ? R.mipmap.ic_pause : R.mipmap.ic_play;
                break;
        }

        return new NotificationCompat.Action.Builder(icon,action,setIntentAction(action)).build();
    }


    private PendingIntent setIntentAction(String action){


        final Intent broadcastIntent=new Intent();
        broadcastIntent.setAction(action);



        return PendingIntent.getBroadcast(musicService,REQUEST_CODE,broadcastIntent,PendingIntent.FLAG_UPDATE_CURRENT);
    }



}

