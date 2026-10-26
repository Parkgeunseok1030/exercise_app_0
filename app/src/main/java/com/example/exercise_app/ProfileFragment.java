package com.example.exercise_app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class ProfileFragment extends Fragment {

    private TextView tvNickname, tvLevel, tvNextExp, tvTitle, tvDday, tvUid;
    private ProgressBar progressProfileExp;
    private ImageView ivProfileIcon;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        String localPath = copyImageToInternalStorage(imageUri);
                        if (localPath != null) {
                            ivProfileIcon.setImageURI(Uri.fromFile(new File(localPath)));
                            saveProfileImage(localPath);
                        }
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfileIcon = view.findViewById(R.id.iv_my_profile_icon);
        tvNickname = view.findViewById(R.id.tv_my_nickname);
        tvLevel = view.findViewById(R.id.tv_profile_level);
        tvNextExp = view.findViewById(R.id.tv_profile_next_exp);
        tvTitle = view.findViewById(R.id.tv_profile_title);
        tvDday = view.findViewById(R.id.tv_profile_dday);
        tvUid = view.findViewById(R.id.tv_profile_uid);
        progressProfileExp = view.findViewById(R.id.profile_progress_exp);

        LinearLayout menuAchievements = view.findViewById(R.id.menu_achievements);
        LinearLayout menuDifficulty = view.findViewById(R.id.menu_set_difficulty);
        LinearLayout menuNotifications = view.findViewById(R.id.menu_notifications);
        LinearLayout btnLogout = view.findViewById(R.id.btn_logout);

        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        if (ivProfileIcon != null) {
            ivProfileIcon.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                pickImageLauncher.launch(intent);
            });
        }

        if (tvUid != null) {
            tvUid.setOnClickListener(v -> {
                String myUid = prefs.getString("user_id", "알 수 없는 유저");
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("User UID", myUid);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(requireContext(), "고유번호가 복사되었습니다!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (menuAchievements != null) {
            menuAchievements.setOnClickListener(v -> startActivity(new Intent(getActivity(), AchievementsActivity.class)));
        }

        if (menuDifficulty != null) {
            menuDifficulty.setOnClickListener(v -> startActivity(new Intent(getActivity(), DifficultyActivity.class)));
        }

        if (menuNotifications != null) {
            menuNotifications.setOnClickListener(v -> startActivity(new Intent(getActivity(), NotificationSettingsActivity.class)));
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
        }

        updateProfileUI();
        return view;
    }

    private String copyImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            File file = new File(requireContext().getFilesDir(), "profile_img.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.close();
            inputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveProfileImage(String path) {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("profile_image_uri", path).apply();
        Toast.makeText(getContext(), "프로필 사진이 변경되었습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProfileUI();
    }

    private void updateProfileUI() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        String savedPath = prefs.getString("profile_image_uri", null);
        if (savedPath != null) {
            File file = new File(savedPath);
            if (file.exists()) {
                ivProfileIcon.setImageURI(Uri.fromFile(file));
            } else {
                ivProfileIcon.setImageResource(R.drawable.favorite_16380633);
            }
        } else {
            ivProfileIcon.setImageResource(R.drawable.favorite_16380633);
        }

        int level = prefs.getInt("character_level", 1);
        int exp = prefs.getInt("character_exp", 0);
        int maxExp = level * 100;
        tvLevel.setText("Lv." + level);
        progressProfileExp.setMax(maxExp);
        progressProfileExp.setProgress(exp);
        tvNextExp.setText("다음 레벨까지 " + (maxExp - exp) + " XP");

        tvNickname.setText(prefs.getString("nickname", "사용자님"));
        String uid = prefs.getString("user_id", "알 수 없음");
        if (tvUid != null) tvUid.setText("UID: " + uid + " 📋");

        tvTitle.setText(prefs.getString("user_title", "칭호 없음"));
        long joinDate = prefs.getLong("join_date", System.currentTimeMillis());
        long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - joinDate) + 1;
        tvDday.setText("운동 시작한 지 " + days + "일째");
    }
}