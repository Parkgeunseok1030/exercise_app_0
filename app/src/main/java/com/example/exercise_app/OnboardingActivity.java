package com.example.exercise_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OnboardingActivity extends AppCompatActivity {

    private String selectedProfile = "";
    private int selectedLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        if (prefs.getString("user_id", null) != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        LinearLayout layoutStep1 = findViewById(R.id.layout_step1);
        LinearLayout layoutStep2 = findViewById(R.id.layout_step2);

        TextView img1 = findViewById(R.id.img_profile_1);
        TextView img2 = findViewById(R.id.img_profile_2);
        TextView img3 = findViewById(R.id.img_profile_3);

        EditText etNickname = findViewById(R.id.et_nickname);
        EditText etSignupId = findViewById(R.id.et_signup_id);
        EditText etSignupPassword = findViewById(R.id.et_signup_password);
        Button btnNext = findViewById(R.id.btn_next);

        Button btnLevel1 = findViewById(R.id.btn_level_1);
        Button btnLevel2 = findViewById(R.id.btn_level_2);
        Button btnLevel3 = findViewById(R.id.btn_level_3);
        Button btnStartReal = findViewById(R.id.btn_start_real);

        View.OnClickListener profileClickListener = v -> {
            img1.setBackgroundColor(Color.parseColor("#F0F0F0"));
            img2.setBackgroundColor(Color.parseColor("#F0F0F0"));
            img3.setBackgroundColor(Color.parseColor("#F0F0F0"));
            v.setBackgroundColor(Color.parseColor("#FFEB3B"));
            selectedProfile = ((TextView) v).getText().toString();
        };
        img1.setOnClickListener(profileClickListener);
        img2.setOnClickListener(profileClickListener);
        img3.setOnClickListener(profileClickListener);

        btnNext.setOnClickListener(v -> {
            String nickname = etNickname.getText().toString().trim();
            String loginId = etSignupId.getText().toString().trim();
            String password = etSignupPassword.getText().toString().trim();

            if (selectedProfile.isEmpty()) {
                Toast.makeText(this, "프로필을 선택해 주세요!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nickname.isEmpty() || loginId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "닉네임, 아이디, 비밀번호를 모두 입력해 주세요!", Toast.LENGTH_SHORT).show();
                return;
            }

            layoutStep1.setVisibility(View.GONE);
            layoutStep2.setVisibility(View.VISIBLE);
        });

        View.OnClickListener levelClickListener = v -> {
            btnLevel1.setBackgroundColor(Color.parseColor("#F0F0F0"));
            btnLevel2.setBackgroundColor(Color.parseColor("#F0F0F0"));
            btnLevel3.setBackgroundColor(Color.parseColor("#F0F0F0"));
            v.setBackgroundColor(Color.parseColor("#81C784"));

            if (v.getId() == R.id.btn_level_1) selectedLevel = 1;
            else if (v.getId() == R.id.btn_level_2) selectedLevel = 2;
            else if (v.getId() == R.id.btn_level_3) selectedLevel = 3;
        };
        btnLevel1.setOnClickListener(levelClickListener);
        btnLevel2.setOnClickListener(levelClickListener);
        btnLevel3.setOnClickListener(levelClickListener);

        btnStartReal.setOnClickListener(v -> {
            if (selectedLevel == 0) {
                Toast.makeText(this, "난이도를 선택해 주세요!", Toast.LENGTH_SHORT).show();
                return;
            }

            String nickname = etNickname.getText().toString().trim();
            String loginId = etSignupId.getText().toString().trim();
            String password = etSignupPassword.getText().toString().trim();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String randomUserId = UUID.randomUUID().toString();

            Map<String, Object> userData = new HashMap<>();
            userData.put("login_id", loginId);
            userData.put("password", password);
            userData.put("nickname", nickname);
            userData.put("profile_icon", selectedProfile);
            userData.put("level", selectedLevel);
            userData.put("exp", 0);
            userData.put("total_pushups", 0);
            userData.put("total_squats", 0);

            db.collection("users").document(randomUserId)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("user_id", randomUserId);
                        editor.putString("nickname", nickname);
                        editor.putString("profile_icon", selectedProfile);
                        editor.apply();

                        Toast.makeText(this, "가입 완료! 환영합니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "오류: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}