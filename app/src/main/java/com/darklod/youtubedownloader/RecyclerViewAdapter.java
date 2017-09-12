package com.darklod.youtubedownloader;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewAdapter  extends RecyclerView.Adapter<RecyclerViewAdapter.VideoViewHolder> {
    private List<Item> videos;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    private OnItemClickListener onItemClickListenerMp3;
    private OnItemClickListener onItemClickListenerMp4;

    public void setOnItemClickListener(OnItemClickListener onItemClickListenerMp3, OnItemClickListener onItemClickListenerMp4) {
        this.onItemClickListenerMp3 = onItemClickListenerMp3;
        this.onItemClickListenerMp4 = onItemClickListenerMp4;
    }

    public RecyclerViewAdapter(Context context, List<Item> videos) {
        this.videos = videos;
        this.context = context;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder vh, int i) {
        final Item video = videos.get(i);

        if (video.snippet.thumbnails.medium != null && !TextUtils.isEmpty(video.snippet.thumbnails.medium.url)) {
            Picasso.with(context).load(video.snippet.thumbnails.medium.url)
                   .error(R.drawable.placeholder)
                   .placeholder(R.drawable.placeholder)
                   .into(vh.thumbnail);
        }

        vh.title.setText(video.snippet.title + " - " + video.snippet.channelTitle);
        vh.description.setText(video.snippet.description);

        vh.mp4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListenerMp4.onItemClick(video);
            }
        });

        vh.mp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListenerMp3.onItemClick(video);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != videos ? videos.size() : 0);
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        protected ImageView thumbnail;
        protected TextView description;
        protected TextView title;
        protected Button mp4, mp3;

        public VideoViewHolder(View view) {
            super(view);
            this.thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            this.title = (TextView) view.findViewById(R.id.title);
            this.description = (TextView) view.findViewById(R.id.description);
            this.mp4 = (Button) view.findViewById(R.id.btn_mp4);
            this.mp3 = (Button) view.findViewById(R.id.btn_mp3);
        }
    }
}