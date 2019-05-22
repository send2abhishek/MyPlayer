package com.attra.myplayer.PlayBackInfo;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import com.attra.myplayer.Utils.Songs;

import java.util.ArrayList;

public interface PlayBackAdapter {

    void initMediaPlayer();

    void release();


    // check whether mediaPlayer object is null or not
    boolean isMediaPlayer();

    // check whether mediaPlayer object is null or not also media player is playing the song
    boolean isPlaying();

    // MediaPlayer to play or pause
    void resumeOrPause();

    // To reset the mediaPlayer
    void reset();

    // To check the mediaPlayer is reset or not
    void isReset();

    //To skip to next song or restart the song by calling skip method
    void InstantReset();

    //To skip to next song or restart the song
    void skip(final boolean isNext);

    //To seek the song to desire postion
    void seekTo(final int position);

    void setPlaybackInfoListener(final PlayBackInfoListener playbackInfoListener);

    //To get the current song selected
    Songs getCurrentSongs();

    @PlayBackInfoListener.State
    int getState();

    //Gets the mediaPlayer current playback position.
    int getPlayBackPosition();

    void registerNotificationActionsReceiver(final boolean isRegister);


    void setCurrentSong(@NonNull final Songs song, @NonNull final ArrayList<Songs> songs);

    MediaPlayer getMediaPlayer();

    void onPauseActivity();

    void onResumeActivity();




}
