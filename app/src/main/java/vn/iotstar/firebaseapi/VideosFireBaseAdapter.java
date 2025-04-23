package vn.iotstar.firebaseapi;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class VideosFireBaseAdapter extends FirebaseRecyclerAdapter<Video1Model, VideosFireBaseAdapter.MyHolder> {

    public VideosFireBaseAdapter(@NonNull FirebaseRecyclerOptions<Video1Model> options) {
        super(options);
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        VideoView videoView;
        ProgressBar videoProgressBar;
        TextView textVideoTitle, textVideoDescription;
        ImageView imPerson, favorites, imShare, imMore;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            videoProgressBar = itemView.findViewById(R.id.videoProgressBar);
            textVideoTitle = itemView.findViewById(R.id.textVideoTitle);
            textVideoDescription = itemView.findViewById(R.id.textVideoDescription);
            imPerson = itemView.findViewById(R.id.imPerson);
            favorites = itemView.findViewById(R.id.favorites);
            imShare = itemView.findViewById(R.id.imShare);
            imMore = itemView.findViewById(R.id.imMore);
        }
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_video_row, parent, false);
        return new MyHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyHolder holder, int position, @NonNull Video1Model model) {
        holder.textVideoTitle.setText(model.getTitle());
        holder.textVideoDescription.setText(model.getDescription());

        holder.videoView.setVideoURI(Uri.parse(model.getUrl()));
        holder.videoProgressBar.setVisibility(View.VISIBLE);

        holder.videoView.setOnPreparedListener(mp -> {
            holder.videoProgressBar.setVisibility(View.GONE);
            mp.start();

            // Scale video
            float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
            float screenRatio = holder.videoView.getWidth() / (float) holder.videoView.getHeight();
            float scale = videoRatio / screenRatio;

            if (scale >= 1f) {
                holder.videoView.setScaleX(scale);
            } else {
                holder.videoView.setScaleY(1f / scale);
            }
        });

        holder.videoView.setOnCompletionListener(MediaPlayer::start);

        // Xử lý click yêu thích
        final boolean[] isFav = {false};
        holder.favorites.setOnClickListener(v -> {
            isFav[0] = !isFav[0];
            holder.favorites.setImageResource(isFav[0] ? R.drawable.ic_fill_favorite : R.drawable.ic_favorite);
        });
    }
}
