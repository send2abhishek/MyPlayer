package com.attra.myplayer;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.attra.myplayer.MusicAdapters.SongListAdapter;
import com.attra.myplayer.PlayBackInfo.MediaPlayerHolder;
import com.attra.myplayer.PlayBackInfo.PlayBackAdapter;
import com.attra.myplayer.PlayBackInfo.PlayBackInfoListener;
import com.attra.myplayer.Providers.SongsProviders;
import com.attra.myplayer.Services.MusicService;
import com.attra.myplayer.Utils.NotificationMangerHelper;
import com.attra.myplayer.Utils.Songs;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SongListAdapter.SongClicked, View.OnClickListener {

    private RecyclerView recyclerView;
    private SongListAdapter adapter;
    private boolean readPermission=true;
    private ArrayList<Songs> songsList;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 100;
    private MusicService musicService;
    private PlayBackAdapter playBackAdapter;
    private boolean mServiceBound=false;
    public static String TAG="MyDemo";
    private PlaybackListener playbackListener;
    private TextView playerFileName;
    private SeekBar seekBar;
    private boolean isUserSeeking=false;
    private NotificationMangerHelper notificationMangerHelper;
    private TextView songDuration;
    private TextView songStartDuration;
    private ImageView playPauseBtn;
    private ImageView nextBtn;
    private ImageView prevBtn;




    private ServiceConnection mconnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder binder= (MusicService.MyBinder) service;
            musicService=binder.getInstance();
            Log.d(TAG, "onServiceConnected: ");
            playBackAdapter=musicService.getMediaPlayerHolder();
            notificationMangerHelper=musicService.getNotificationMangerHelper();

            if(playbackListener==null){
                Log.d(TAG, "onServiceConnected: Playback listener object is created");
                playbackListener=new PlaybackListener();
                playBackAdapter.setPlaybackInfoListener(playbackListener);
            }

            if(playBackAdapter!=null &&playBackAdapter.isPlaying()){

                restorePlayerStatus();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService=null;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissions();
        setviews();
        Log.d(TAG, "onCreate: ");
        recyclerView=findViewById(R.id.activity_main_recyler_view);
        adapter=new SongListAdapter(this,this);
        if(readPermission){
            getLoaderManager().initLoader(0,null,this);
        }
        else {

            Toast.makeText(this,"kindly Provide Storage Read Permission first",Toast.LENGTH_LONG).show();
        }

        BindService();
        seekarHandling();

    }

    @Override
    protected void onResume() {
        super.onResume();
        BindService();
        if(playBackAdapter!=null &&playBackAdapter.isPlaying()){

            restorePlayerStatus();
        }
    }

    private void restorePlayerStatus() {

        seekBar.setEnabled(playBackAdapter.isMediaPlayer());
        //if we are playing and the activity was restarted
        //update the controls panel

        if(playBackAdapter!=null && playBackAdapter.isMediaPlayer()){

            playBackAdapter.onResumeActivity();
            updatePlayingInfo(true,false);
        }
    }

    private void seekarHandling() {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int userSelectedPosition = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){

                    int itemDuration = progress;
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    songStartDuration.setText(String.format("%02d:%02d", minutes, seconds));

                    userSelectedPosition=progress;

                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking=true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                isUserSeeking=false;
                playBackAdapter.seekTo(userSelectedPosition);
            }
        });
    }

    private void setviews() {

        playerFileName=findViewById(R.id.sound_record_player_file_name);
        seekBar=findViewById(R.id.sound_record_player_seekbar);
        songDuration=findViewById(R.id.sound_record_player_file_duration_end);
        playPauseBtn=findViewById(R.id.sound_record_player_playBtn);
        songStartDuration=findViewById(R.id.sound_record_player_file_duration_start);
        nextBtn=findViewById(R.id.sound_record_player_file_forward);
        prevBtn=findViewById(R.id.sound_record_player_file_rewind);
        prevBtn.setOnClickListener(this);
        playPauseBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
    }

    private void BindService() {

        bindService(new Intent(this,MusicService.class),mconnection, Context.BIND_AUTO_CREATE);
        mServiceBound=true;
        
        Intent intent=new Intent(this,MusicService.class);
        startService(intent);
        Log.d(TAG, "BindService: ");
    }


    @Override
    protected void onPause() {
        super.onPause();

        if(playBackAdapter!=null && playBackAdapter.isPlaying()){

            playBackAdapter.onPauseActivity();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        doUndbindService();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity");
    }

    private void doUndbindService() {

        if(mServiceBound){

            unbindService(mconnection);
            mServiceBound=false;
        }
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                SongsProviders.BASE_PROJECTION,MediaStore.Audio.Media.DATA + " like ? ",new String[]{"%Download%"},null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        songsList=new SongsProviders(this).getAllSongs(data);
        adapter.AddSongList(songsList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getLoaderManager().restartLoader(0,null,this);
    }



    private void permissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts

                Toast.makeText(this, "Read Storage permission need to give man at any cost", Toast.LENGTH_SHORT).show();
            } else {


                ActivityCompat.requestPermissions(this,
                        new String[]{

                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);


            }
        }
    }




    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    readPermission=true;
                    Toast.makeText(this, "Read Storage permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    readPermission=false;
                    Toast.makeText(this, "Read Storage permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onSongClicked(Songs songs) {

        onSongSelected(songs,songsList);


    }

    private void onSongSelected(Songs songs, ArrayList<Songs> songsList) {

        Log.d(TAG, "onSongSelected: ");

        if(!seekBar.isEnabled()){
            seekBar.setEnabled(true);
        }

        try {
            playBackAdapter.setCurrentSong(songs, songsList);
            playBackAdapter.initMediaPlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.sound_record_player_playBtn){
            resumeOrPause();
        }
        else if(v.getId()==R.id.sound_record_player_file_forward){

            skipNext(true);
        }
        else if(v.getId()==R.id.sound_record_player_file_rewind){

            skipNext(false);
        }
    }

    private void skipNext(boolean state) {

        if(playBackAdapter.isPlaying()){

            playBackAdapter.skip(state);
        }
    }

    private void resumeOrPause() {


            playBackAdapter.resumeOrPause();

    }

    public class PlaybackListener extends PlayBackInfoListener{

        @Override
        public void onPositionChanged(int position) {
           if(!isUserSeeking){

                seekBar.setProgress(position);


           }
        }

        @Override
        public void onStateChanged(int state) {
            Log.d(TAG, "onStateChanged: from MainActivity");
            updatePlayingStatus();
            if (playBackAdapter.getState() != State.RESUMED && playBackAdapter.getState() != State.PAUSED) {
                updatePlayingInfo(false, true);
            }
        }

        @Override
        public void onPlaybackCompleted() {
            super.onPlaybackCompleted();
        }
    }

    private void updatePlayingStatus() {

        final int Drawable=playBackAdapter.getState() !=PlayBackInfoListener.State.PAUSED
                ? R.mipmap.ic_pause : R.mipmap.ic_play;

        playPauseBtn.post(new Runnable() {
            @Override
            public void run() {
                playPauseBtn.setImageResource(Drawable);
            }
        });

    }

    private void updatePlayingInfo(boolean restore, boolean startPlaying) {

        if(startPlaying){

            playBackAdapter.getMediaPlayer().start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    musicService.startForeground(NotificationMangerHelper.REQUEST_CODE,notificationMangerHelper.createNotification());
                }
            },250);

        }

        Songs songs=playBackAdapter.getCurrentSongs();
        playerFileName.setText(songs.getTitle());
        seekBar.setMax((int)songs.getDuration());
        long itemDuration = songs.getDuration();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);
        songDuration.setText(String.format("%02d:%02d", minutes, seconds));


        if(restore){

            seekBar.setProgress(playBackAdapter.getPlayBackPosition());
            updatePlayingStatus();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if(musicService.isRestoredFromPause()){

                        musicService.stopForeground(false);
                        musicService.getNotificationMangerHelper().getNotificationManager()
                                .notify(NotificationMangerHelper.REQUEST_CODE,musicService.getNotificationMangerHelper()
                                        .getNotificationBuilder().build());
                        musicService.setRestoredFromPause(false);
                    }


                }
            },250);

        }

    }
}
