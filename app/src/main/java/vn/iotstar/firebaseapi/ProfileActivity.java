package vn.iotstar.firebaseapi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView imageViewAvatar;
    private TextView textViewUsername, textViewEmail, textViewVideoCount;
    private EditText editTextVideoTitle, editTextVideoDescription;
    private Button buttonSelectVideo, buttonUploadVideo, buttonLogout;
    private ImageButton buttonBack;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST = 2;
    private Uri imageUri, videoUri;
    private FirebaseUser currentUser;
    private DatabaseReference userRef, videosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewVideoCount = findViewById(R.id.textViewVideoCount);
        editTextVideoTitle = findViewById(R.id.editTextVideoTitle);
        editTextVideoDescription = findViewById(R.id.editTextVideoDescription);
        buttonSelectVideo = findViewById(R.id.buttonSelectVideo);
        buttonUploadVideo = findViewById(R.id.buttonUploadVideo);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonBack = findViewById(R.id.buttonBack);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            videosRef = FirebaseDatabase.getInstance().getReference("videos");
            loadUserProfile();
        }

        // Xử lý sự kiện nhấn vào ảnh đại diện để chọn ảnh mới
        imageViewAvatar.setOnClickListener(v -> openImageChooser());

        // Xử lý sự kiện chọn video
        buttonSelectVideo.setOnClickListener(v -> openVideoChooser());

        // Xử lý sự kiện upload video
        buttonUploadVideo.setOnClickListener(v -> uploadVideoToFirebase());

        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });

        // Xử lý sự kiện nhấn nút quay lại
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserProfile() {
        if (userRef != null) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String avatarUrl = snapshot.child("avatar").getValue(String.class);

                    // Set username và email
                    textViewUsername.setText("Tên người dùng: " + username);
                    textViewEmail.setText("Email: " + email);

                    // Set avatar nếu có
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        try {
                            Uri uri = Uri.parse(avatarUrl);
                            imageViewAvatar.setImageURI(uri);
                        } catch (Exception e) {
                            Toast.makeText(ProfileActivity.this, "Không thể tải ảnh đại diện", Toast.LENGTH_SHORT).show();
                            imageViewAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Đếm số lượng video của user
        if (videosRef != null && currentUser != null) {
            videosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long videoCount = 0;
                    for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
                        String userId = videoSnapshot.child("userId").getValue(String.class);
                        if (userId != null && userId.equals(currentUser.getUid())) {
                            videoCount++;
                        }
                    }
                    textViewVideoCount.setText("Số video: " + videoCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Lỗi tải danh sách video", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openVideoChooser() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                imageUri = data.getData();
                imageViewAvatar.setImageURI(imageUri);
                uploadAvatarToFirebase();
            } else if (requestCode == PICK_VIDEO_REQUEST) {
                videoUri = data.getData();
                Toast.makeText(this, "Đã chọn video", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadAvatarToFirebase() {
        if (imageUri != null) {
            String imageUrl = copyFileToInternalStorage(imageUri, "avatar");
            if (imageUrl == null) {
                Toast.makeText(this, "Lỗi sao chép ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            userRef.child("avatar").setValue(imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Cập nhật ảnh thất bại", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void uploadVideoToFirebase() {
        if (videoUri == null) {
            Toast.makeText(this, "Vui lòng chọn video", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = editTextVideoTitle.getText().toString().trim();
        String description = editTextVideoDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            editTextVideoTitle.setError("Vui lòng nhập tiêu đề");
            return;
        }

        if (TextUtils.isEmpty(description)) {
            editTextVideoDescription.setError("Vui lòng nhập mô tả");
            return;
        }

        // Sao chép video vào bộ nhớ nội bộ
        String videoUrl = copyFileToInternalStorage(videoUri, "video");
        if (videoUrl == null) {
            Toast.makeText(this, "Lỗi sao chép video", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo ID mới cho video
        String newVideoId = String.valueOf(System.currentTimeMillis());
        DatabaseReference newVideoRef = videosRef.child(newVideoId);

        // Tạo đối tượng Video1Model với like = 0
        Video1Model newVideo = new Video1Model(title, description, videoUrl, currentUser.getUid(), 0);

        // Lưu thông tin video
        newVideoRef.setValue(newVideo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Upload video thành công", Toast.LENGTH_SHORT).show();
                    // Reset giao diện
                    editTextVideoTitle.setText("");
                    editTextVideoDescription.setText("");
                    videoUri = null;
                    // Cập nhật số lượng video
                    loadUserProfile();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Upload video thất bại", Toast.LENGTH_SHORT).show();
                });
    }


    private String copyFileToInternalStorage(Uri sourceUri, String type) {
        try {
            // Tạo tệp trong bộ nhớ nội bộ
            String prefix = type.equals("avatar") ? "avatar_" : "video_";
            String extension = type.equals("avatar") ? ".jpg" : ".mp4";
            File file = new File(getFilesDir(), prefix + System.currentTimeMillis() + extension);
            try (InputStream inputStream = getContentResolver().openInputStream(sourceUri);
                 FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            // Trả về URI dạng file://
            return Uri.fromFile(file).toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}