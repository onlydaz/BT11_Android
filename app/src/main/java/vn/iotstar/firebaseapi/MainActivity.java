package vn.iotstar.firebaseapi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private VideosFireBaseAdapter videosAdapter;
    private ImageView imgUserAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        viewPager2 = findViewById(R.id.vpager);
        getVideos();

        imgUserAvatar = findViewById(R.id.imgUserAvatar);
        loadUserAvatar();

        imgUserAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void getVideos() {
        // Lấy dữ liệu từ Firebase Realtime Database
        DatabaseReference mDataBase = FirebaseDatabase.getInstance().getReference("videos");

        FirebaseRecyclerOptions<Video1Model> options =
                new FirebaseRecyclerOptions.Builder<Video1Model>()
                        .setQuery(mDataBase, Video1Model.class)
                        .build();

        videosAdapter = new VideosFireBaseAdapter(options);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager2.setAdapter(videosAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (videosAdapter != null) {
            videosAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (videosAdapter != null) {
            videosAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videosAdapter != null) {
            videosAdapter.notifyDataSetChanged();
        }
    }

    private void loadUserAvatar() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        userRef.child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String avatarUrl = snapshot.getValue(String.class);
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    try {
                        Uri uri = Uri.parse(avatarUrl);
                        imgUserAvatar.setImageURI(uri);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Không thể tải ảnh đại diện", Toast.LENGTH_SHORT).show();
                        imgUserAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}