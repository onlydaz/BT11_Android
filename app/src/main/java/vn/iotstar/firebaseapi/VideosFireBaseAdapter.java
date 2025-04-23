package vn.iotstar.firebaseapi;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VideosFireBaseAdapter extends FirebaseRecyclerAdapter<Video1Model, VideosFireBaseAdapter.MyHolder> {

    public VideosFireBaseAdapter(@NonNull FirebaseRecyclerOptions<Video1Model> options) {
        super(options);
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        VideoView videoView;
        ProgressBar videoProgressBar;
        TextView textVideoTitle, textVideoDescription, textUserEmail, textLikeCount; // Thêm textLikeCount
        ImageView imPerson, favorites, imShare, imMore;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            videoProgressBar = itemView.findViewById(R.id.videoProgressBar);
            textVideoTitle = itemView.findViewById(R.id.textVideoTitle);
            textVideoDescription = itemView.findViewById(R.id.textVideoDescription);
            textUserEmail = itemView.findViewById(R.id.textUserEmail);
            textLikeCount = itemView.findViewById(R.id.textLikeCount);
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
        holder.textLikeCount.setText(String.valueOf(model.getLike())); // Hiển thị số lượt thích

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

        // Lấy khóa video từ Firebase (thay vì model.getId())
        String videoKey = getRef(position).getKey();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("likes").child(videoKey).child(currentUserId);

        // Kiểm tra xem người dùng đã thích video chưa
        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.exists(); // Nếu nút tồn tại, người dùng đã thích
                holder.favorites.setImageResource(isLiked ? R.drawable.ic_fill_favorite : R.drawable.ic_favorite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("VideosFireBaseAdapter", "Lỗi kiểm tra trạng thái thích: " + error.getMessage() + ", Code: " + error.getCode());
                Toast.makeText(holder.itemView.getContext(), "Lỗi kiểm tra trạng thái thích", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện nhấn thích/bỏ thích
        holder.favorites.setOnClickListener(v -> {
            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference("videos").child(videoKey);
                    if (snapshot.exists()) {
                        // Người dùng đã thích -> Bỏ thích
                        likeRef.removeValue(); // Xóa trạng thái thích
                        videoRef.child("like").setValue(model.getLike() - 1); // Giảm số lượt thích
                        holder.favorites.setImageResource(R.drawable.ic_favorite); // Trái tim rỗng
                        holder.textLikeCount.setText(String.valueOf(model.getLike() - 1)); // Cập nhật UI
                    } else {
                        // Người dùng chưa thích -> Thích
                        likeRef.setValue(true); // Đánh dấu người dùng đã thích
                        videoRef.child("like").setValue(model.getLike() + 1); // Tăng số lượt thích
                        holder.favorites.setImageResource(R.drawable.ic_fill_favorite); // Trái tim đỏ
                        holder.textLikeCount.setText(String.valueOf(model.getLike() + 1)); // Cập nhật UI
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(holder.itemView.getContext(), "Lỗi cập nhật lượt thích", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Tải avatar và email của user
        String userId = model.getUserId();
        if (userId != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String avatarUrl = snapshot.child("avatar").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    // Set avatar
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        try {
                            Uri uri = Uri.parse(avatarUrl);
                            holder.imPerson.setImageURI(uri);
                        } catch (Exception e) {
                            holder.imPerson.setImageResource(R.drawable.ic_avatar_placeholder);
                        }
                    } else {
                        holder.imPerson.setImageResource(R.drawable.ic_avatar_placeholder);
                    }

                    // Set email
                    if (email != null && !email.isEmpty()) {
                        holder.textUserEmail.setText(email);
                    } else {
                        holder.textUserEmail.setText("Unknown");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.imPerson.setImageResource(R.drawable.ic_avatar_placeholder);
                    holder.textUserEmail.setText("Unknown");
                }
            });
        } else {
            holder.imPerson.setImageResource(R.drawable.ic_avatar_placeholder);
            holder.textUserEmail.setText("Unknown");
        }
    }
}