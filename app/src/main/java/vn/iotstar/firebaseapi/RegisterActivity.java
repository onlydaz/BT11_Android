package vn.iotstar.firebaseapi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTextUsername, editTextEmail, editTextPassword;
    private Button buttonRegister;
    private TextView textViewLogin;
    private ImageView imageViewAvatar;
    private Uri imageUri;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);

        imageViewAvatar.setOnClickListener(v -> openImagePicker());

        buttonRegister.setOnClickListener(v -> registerUser());

        textViewLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewAvatar.setImageURI(imageUri);
        }
    }

    private void registerUser() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Vui lòng nhập tên người dùng");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Vui lòng nhập email");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            editTextPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh đại diện", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sao chép ảnh vào bộ nhớ nội bộ
        String imageUrl = copyImageToInternalStorage(imageUri);
        if (imageUrl == null) {
            Toast.makeText(this, "Lỗi sao chép ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        DatabaseReference userRef = mDatabase.child("users").child(userId);

                        // Lưu thông tin người dùng vào Realtime Database
                        userRef.child("username").setValue(username);
                        userRef.child("email").setValue(email);
                        userRef.child("avatar").setValue(imageUrl); // Lưu URI file nội bộ

                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String copyImageToInternalStorage(Uri sourceUri) {
        try {
            // Tạo tệp trong bộ nhớ nội bộ
            File file = new File(getFilesDir(), "avatar_" + System.currentTimeMillis() + ".jpg");
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