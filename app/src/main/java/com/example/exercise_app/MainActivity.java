package com.example.exercise_app;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, new HomeFragment())
                    .commit();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (!prefs.contains("join_date")) {
            prefs.edit().putLong("join_date", System.currentTimeMillis()).apply();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_activity) {
                selectedFragment = new ActivityFragment();
            } else if (itemId == R.id.nav_friend) {
                selectedFragment = new FriendFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, selectedFragment)
                        .commit();
            }
            return true;
        });

        showMotivationPopup();
    }

    private void showMotivationPopup() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int totalPushups = prefs.getInt("total_pushups", 0);
        int totalSquats = prefs.getInt("total_squats", 0);

        if (totalPushups == 0 && totalSquats == 0) return;

        long joinDate = prefs.getLong("join_date", System.currentTimeMillis());
        Calendar joinCal = Calendar.getInstance();
        joinCal.setTimeInMillis(joinDate);
        joinCal.set(Calendar.HOUR_OF_DAY, 0);
        joinCal.set(Calendar.MINUTE, 0);
        joinCal.set(Calendar.SECOND, 0);
        joinCal.set(Calendar.MILLISECOND, 0);

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);

        long diff = todayCal.getTimeInMillis() - joinCal.getTimeInMillis();
        long days = TimeUnit.MILLISECONDS.toDays(diff) + 1;

        String[] randomMentions = {
                "땀은 배신하지 않습니다. 오늘도 득근! 💪",
                "어제보다 더 강해진 당신을 믿으세요. ✨",
                "가장 어려운 단계는 '운동복 입기'였습니다. 이미 성공하셨네요! 🏃‍♂️",
                "근육은 고통을 먹고 자랍니다. 오늘도 가시죠! 🔥",
                "운동을 시작한 것만으로도 상위 1%입니다. 🏆",
                "핑계는 칼로리를 태우지 못합니다. 움직이세요! 👊",
                "자신을 이기는 사람이 세상을 이깁니다. 🎖️",
                "성공의 비결은 시작하는 것입니다. 오늘도 화이팅! 🌟"
        };

        int randomIndex = (int) (Math.random() * randomMentions.length);
        String todayMention = randomMentions[randomIndex];

        String message = "운동 시작한 지 " + days + "일째!\n" +
                "누적 기록: 푸쉬업 " + totalPushups + "개, 스쿼트 " + totalSquats + "개\n\n" +
                "\"" + todayMention + "\"";

        new AlertDialog.Builder(this)
                .setTitle("오늘도 오셨군요! 🏆")
                .setMessage(message)
                .setPositiveButton("운동하러 가기", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}