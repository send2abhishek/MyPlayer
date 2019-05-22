package com.attra.myplayer.MusicAdapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.attra.myplayer.R;
import com.attra.myplayer.Utils.Songs;
import java.util.ArrayList;
import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListHolder> {

    private Context context;
    private LayoutInflater layoutInflater;
    private List<Songs> mysongList;
    private SongClicked msongClicked;

    public SongListAdapter(Context context,SongClicked msongClicked) {
        this.context = context;
        layoutInflater=LayoutInflater.from(context);
        mysongList=new ArrayList<>();
        this.msongClicked=msongClicked;
    }

    public void AddSongList(ArrayList<Songs> songs){

        mysongList.addAll(songs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View songsView=layoutInflater.inflate(R.layout.actiivty_song_list_view,viewGroup,false);
        return new SongListHolder(songsView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongListHolder songListHolder, int position) {

        final Songs songs=mysongList.get(position);
        songListHolder.populate(songs);
        songListHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msongClicked.onSongClicked(songs);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mysongList.size();
    }

    public interface SongClicked{

        void onSongClicked(Songs songs);
    }

}
