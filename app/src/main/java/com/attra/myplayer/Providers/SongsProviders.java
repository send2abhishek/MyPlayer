package com.attra.myplayer.Providers;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import com.attra.myplayer.Utils.Songs;
import java.util.ArrayList;

public class SongsProviders {


    private ArrayList<Songs> mySongList;
    private Context context;

    public SongsProviders(Context context) {
        this.context = context;
        mySongList=new ArrayList<>();
    }

    public static String[] BASE_PROJECTION=new String[]{

            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.COMPOSER,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.YEAR,

    };


    public ArrayList<Songs> getAllSongs(Cursor cursor){



        while (cursor!=null && cursor.moveToNext()){

            Songs songs=getSongs(cursor);

            if(songs.getDuration()>=30000){

                mySongList.add(songs);
            }

        }

        if(cursor!=null){

            cursor.close();
        }


        return mySongList;
    }

    private Songs getSongs(Cursor cursor) {

                String title=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
                String albumName=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
                String path=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
                String ArtistName=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
                String composer=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.COMPOSER));
                int duration=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));
                int year=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.YEAR));

                return new Songs(title,albumName,path,ArtistName,composer,duration,year);

    }
}
