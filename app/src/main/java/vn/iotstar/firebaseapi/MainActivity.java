package vn.iotstar.firebaseapi;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private VideosFireBaseAdapter videosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        viewPager2 = findViewById(R.id.vpager); // Sửa lỗi thiếu dấu "="
        getVideos(); // Gọi hàm lấy dữ liệu
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
}
