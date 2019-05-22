package com.attra.myplayer.PlayBackInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.util.Log;
import com.attra.myplayer.Services.MusicService;
import com.attra.myplayer.Utils.NotificationMangerHelper;
import com.attra.myplayer.Utils.Songs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.attra.myplayer.MainActivity.TAG;


public class MediaPlayerHolder implements PlayBackAdapter,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    // The volume we set the media player to when we lose audio focus, but are
    // allowed to reduce the volume instead of stopping playback.
    private static final float VOLUME_DUCK = 0.2f;
    // The volume we set the media player when we have audio focus.
    private static final float VOLUME_NORMAL = 1.0f;
    // we don't have audio focus, and can't duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    // we don't have focus, but can duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    // we have full audio focus
    private static final int AUDIO_FOCUSED = 2;



    //constant to for focus identification
    private int mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
    private int mState=0;
    private boolean mPlayOnFocusGain;
    private NotificationMangerHelper notificationMangerHelper;

    //Constant for current song and all Listed songs
    private ArrayList<Songs> mAllSongs;
    private Songs currentSong;


    //MediaPlayer Constants
    private MediaPlayer mediaPlayer;
    private PlayBackInfoListener mPlaybackInfoListener;
    private ScheduledExecutorService executorService;
    private Runnable seekbarPostitionUpdate;
    //General Constants
    private MusicService musicService;
    private Context context;
    private AudioManager audioManager;

    private NotificationReceiver notificationReceiver;

    private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListerner=new
            AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

            switch (focusChange){

                case AudioManager.AUDIOFOCUS_GAIN:

                    mCurrentAudioFocusState=AUDIO_FOCUSED;
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // Audio focus was lost, but it's possible to duck (i.e.: play quietly)
                    mCurrentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK;
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                    // Lost audio focus, probably "permanently"
                    mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
                    break;


            }
            
            if(mediaPlayer!=null){

                Log.d(TAG, "onAudioFocusChange: media player is not null");
            }



        }
    };


    public MediaPlayerHolder(MusicService musicService) {
        this.musicService = musicService;
        context=musicService.getApplicationContext();
        audioManager=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        Log.d(TAG, "MediaPlayerHolder: ");
    }

    @Override
    public void initMediaPlayer() {
        Log.d(TAG, "initMediaPlayer: ");
        Log.d(TAG, "initMediaPlayer:Current song is - "+currentSong.getPath());
        try {

        if(mediaPlayer!=null){

            mediaPlayer.reset();
            Log.d(TAG, "initMediaPlayer reset:Current song is - "+currentSong.getPath());
        }
        else {
                mediaPlayer=new MediaPlayer();
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
                notificationMangerHelper=new NotificationMangerHelper(musicService);
                Log.d(TAG, "initMediaPlayer Initialize: ");
            }

            tryToGetAudioFocus();
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tryToGetAudioFocus() {

        final int result = audioManager.requestAudioFocus(
                mOnAudioFocusChangeListerner,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_FOCUSED;
        } else {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    private void giveUpAudioFocus() {
        if (audioManager.abandonAudioFocus(mOnAudioFocusChangeListerner)
                == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }


    @Override
    public void release() {
        mediaPlayer.release();
        mediaPlayer=null;
        giveUpAudioFocus();
        Log.d(TAG, "release: ");
    }

    @Override
    public boolean isMediaPlayer()
    {
        return mediaPlayer!=null;
    }

    @Override
    public boolean isPlaying() {
        return isMediaPlayer() && mediaPlayer.isPlaying();
    }

    @Override
    public void resumeOrPause() {

        if(isPlaying()){
            pauseMediaPlayer();

        }
        else {
            resumeMediaPlayer();
        }
    }

    private void resumeMediaPlayer() {

        if(!isPlaying()){

            mediaPlayer.start();
            setStatus(PlayBackInfoListener.State.RESUMED);
            musicService.startForeground(NotificationMangerHelper.REQUEST_CODE,notificationMangerHelper.createNotification()    );
        }


    }

    private void pauseMediaPlayer() {

        setStatus(PlayBackInfoListener.State.PAUSED);
        mediaPlayer.pause();
        musicService.stopForeground(false);
        notificationMangerHelper.getNotificationManager().notify(NotificationMangerHelper.REQUEST_CODE,notificationMangerHelper.createNotification());
    }

    @Override
    public void reset() {

    }

    @Override
    public void isReset() {

    }

    @Override
    public void InstantReset() {

    }

    @Override
    public void skip(boolean isNext) {



            getNextSong(isNext);

    }

    private void getNextSong(boolean isNext) {

        int currentIndex=mAllSongs.indexOf(currentSong);

        int index;

        index=isNext? currentIndex+1 : currentIndex -1;
        currentSong=mAllSongs.get(index);
        initMediaPlayer();

    }

    @Override
    public void seekTo(int position) {

        if(isMediaPlayer()){

            mediaPlayer.seekTo(position);
        }

    }

    @Override
    public void setPlaybackInfoListener(PlayBackInfoListener playbackInfoListener) {

        mPlaybackInfoListener=playbackInfoListener;
        Log.d(TAG, "setPlaybackInfoListener: called");
    }

    @Override
    public Songs getCurrentSongs() {
        return currentSong;
    }

    @Override
    public final @PlayBackInfoListener.State
    int getState() {
        return mState;
    }

    @Override
    public int getPlayBackPosition() {

       return mediaPlayer.getCurrentPosition();

    }

    @Override
    public void registerNotificationActionsReceiver(boolean isRegister) {


        if(isRegister){

            registerNotficationActions();
        }
        else {
            unregisterNotificationActions();
        }
    }

    private void unregisterNotificationActions() {

        if(musicService!=null && notificationReceiver !=null){

            try {
                musicService.unregisterReceiver(notificationReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void registerNotficationActions() {

        notificationReceiver=new NotificationReceiver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(NotificationMangerHelper.ACTION_PLAY_PAUSE);
        intentFilter.addAction(NotificationMangerHelper.ACTION_NEXT);
        intentFilter.addAction(NotificationMangerHelper.ACTION_PREV);
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        musicService.registerReceiver(notificationReceiver,intentFilter);
    }

    @Override
    public void setCurrentSong(@NonNull Songs song, @NonNull ArrayList<Songs> songs) {

        currentSong=song;
        mAllSongs=songs;
        Log.d(TAG, "setCurrentSong: ");
    }

    @Override
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void onPauseActivity() {
        stopUpdatingCallbackWithPosition();
    }

    private void stopUpdatingCallbackWithPosition() {

        if(executorService!=null){

            executorService.shutdown();
            executorService=null;
            seekbarPostitionUpdate=null;
        }
    }

    @Override
    public void onResumeActivity() {
        startUpdatingCallbackWithPosition();
    }

    private void startUpdatingCallbackWithPosition() {

        if(executorService==null){

            executorService= Executors.newSingleThreadScheduledExecutor();
        }
        if(seekbarPostitionUpdate==null){

            seekbarPostitionUpdate=new Runnable() {
                @Override
                public void run() {
                    updateProgressCallbackTask();
                }
            };
        }
        executorService.scheduleAtFixedRate(seekbarPostitionUpdate,0,1000, TimeUnit.MILLISECONDS);

    }

    private void updateProgressCallbackTask() {

        if(isPlaying()){

            int currentPostion=mediaPlayer.getCurrentPosition();
            if(mPlaybackInfoListener!=null){

                mPlaybackInfoListener.onPositionChanged(currentPostion);
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        startUpdatingCallbackWithPosition();
        setStatus(PlayBackInfoListener.State.PLAYING);
        Log.d(TAG, "onPrepared: called");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    private void setStatus(final @PlayBackInfoListener.State int state) {

        mState = state;
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener.onStateChanged(state);
        }
    }



       public class NotificationReceiver extends BroadcastReceiver{

           @Override
           public void onReceive(Context context, Intent intent) {

               final String action=intent.getAction();

               if(action!=null) {
                   switch (action){

                       case NotificationMangerHelper.ACTION_PLAY_PAUSE:
                            resumeOrPause();
                            break;

                       case NotificationMangerHelper.ACTION_NEXT:
                           getNextSong(true);
                           break;

                       case NotificationMangerHelper.ACTION_PREV:
                           getNextSong(false);
                           break;

                       case Intent.ACTION_HEADSET_PLUG:
                            if(currentSong!=null){


                                switch (intent.getIntExtra("state",-1)){
                                    //0 means disconnected
                                    case 0:
                                        pauseMediaPlayer();
                                        break;
                                    //1 means connected
                                    case 1:
                                        if (!isPlaying()) {
                                            resumeMediaPlayer();
                                        }
                                        break;
                                }
                            }
                            break;


                   }
               }


           }
       }
}
