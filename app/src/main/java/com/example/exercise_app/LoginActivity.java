package com.example.exercise_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        if (prefs.getString("user_id", null) != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        EditText etLoginId = findViewById(R.id.et_login_id);
        EditText etPassword = findViewById(R.id.et_login_password);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvGoSignup = findViewById(R.id.tv_go_signup);

        FirebaseManager firebaseManager = new FirebaseManager();

        btnLogin.setOnClickListener(v -> {
            String loginId = etLoginId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (loginId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseManager.loginUser(loginId, password, new FirebaseManager.OnLoginListener() {
                @Override
                public void onSuccess(String userId, String nickname, String profileIcon,
                                      int level, int exp, int unlockedStage,
                                      int totalPushups, int totalSquats, int totalStars, String userTitle) {

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();

                    editor.putString("user_id", userId);
                    editor.putString("nickname", nickname);
                    editor.putString("profile_icon", profileIcon);
                    editor.putInt("character_level", level);
                    editor.putInt("character_exp", exp);
                    editor.putInt("unlocked_stage", unlockedStage);
                    editor.putInt("total_pushups", totalPushups);
                    editor.putInt("total_squats", totalSquats);
                    editor.putInt("total_stars", totalStars);
                    editor.putString("user_title", userTitle);
                    editor.putLong("join_date", System.currentTimeMillis());
                    editor.apply();

                    Toast.makeText(LoginActivity.this, nickname + "님 환영합니다!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvGoSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, OnboardingActivity.class));
        });
    }
}