package com.attra.myplayer.MusicAdapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.attra.myplayer.R;
import com.attra.myplayer.Utils.Songs;

import java.util.concurrent.TimeUnit;

public class SongListHolder extends RecyclerView.ViewHolder {

    private TextView songTitle;
    private TextView songArtist;
    private TextView songDuration;
    private CardView cardView;
    public SongListHolder(@NonNull View itemView) {
        super(itemView);

        songTitle=itemView.findViewById(R.id.activity_song_list_song_title);
        songArtist=itemView.findViewById(R.id.activity_song_list_song_artist);
        songDuration=itemView.findViewById(R.id.activity_song_list_song_duration);
        cardView=itemView.findViewById(R.id.activity_song_list_card_view);
    }

    public void populate(Songs songs){

        long itemDuration = songs.getDuration();


        if(songs.getTitle().length()>18){
            String songInfo=songs.getTitle().substring(0,18)+"...";
            songTitle.setText(songInfo);
        }
        else {
            songTitle.setText(songs.getTitle());
        }


        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);


        songArtist.setText(songs.getArtistName());

       songDuration.setText(String.format("%02d:%02d", minutes, seconds));
    }
}
