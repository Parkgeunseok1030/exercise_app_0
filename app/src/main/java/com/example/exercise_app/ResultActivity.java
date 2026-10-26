package com.example.exercise_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView tvTotal = findViewById(R.id.tv_res_total);
        TextView tvPerfect = findViewById(R.id.tv_res_perfect);
        TextView tvGood = findViewById(R.id.tv_res_good);
        TextView tvXp = findViewById(R.id.tv_res_xp);
        Button btnGoHome = findViewById(R.id.btn_go_home);

        LinearLayout layoutStars = findViewById(R.id.layout_stars_result);
        ImageView ivStar1 = findViewById(R.id.iv_res_star1);
        ImageView ivStar2 = findViewById(R.id.iv_res_star2);
        ImageView ivStar3 = findViewById(R.id.iv_res_star3);

        Intent intent = getIntent();
        int total = intent.getIntExtra("TOTAL", 0);
        int perfect = intent.getIntExtra("PERFECT", 0);
        int good = intent.getIntExtra("GOOD", 0);
        int xp = intent.getIntExtra("XP", 0);
        int starCount = intent.getIntExtra("STAR_COUNT", 0);
        boolean isCompleted = intent.getBooleanExtra("IS_COMPLETED", false);
        boolean isLevelUp = intent.getBooleanExtra("IS_LEVEL_UP", false);

        tvTotal.setText("총 횟수: " + total + "회");
        tvPerfect.setText("PERFECT: " + perfect + "회");
        tvGood.setText("GOOD: " + good + "회");
        tvXp.setText("획득 경험치: +" + xp + " XP");

        btnGoHome.setOnClickListener(v -> finish());

        if (isCompleted && starCount > 0) {
            layoutStars.setVisibility(View.VISIBLE);

            animateStar(ivStar1, 300, () -> {
                if (starCount >= 2) {
                    animateStar(ivStar2, 100, () -> {
                        if (starCount == 3) {
                            animateStar(ivStar3, 100, () -> finishStarAnimation(layoutStars, isLevelUp));
                        } else {
                            finishStarAnimation(layoutStars, isLevelUp);
                        }
                    });
                } else {
                    finishStarAnimation(layoutStars, isLevelUp);
                }
            });
        } else {
            if (isLevelUp) Toast.makeText(this, "🎉 LEVEL UP!", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateStar(ImageView star, long delay, Runnable onNextStar) {
        star.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(400)
                .setStartDelay(delay)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));

                    if (onNextStar != null) onNextStar.run();
                }).start();
    }

    private void finishStarAnimation(View layoutStars, boolean isLevelUp) {
        if (isLevelUp) {
            Toast.makeText(this, "🎉 LEVEL UP! 새로운 칭호를 확인해 보세요!", Toast.LENGTH_LONG).show();
        }

        layoutStars.animate()
                .alpha(0f)
                .setStartDelay(1500)
                .setDuration(500)
                .withEndAction(() -> layoutStars.setVisibility(View.GONE))
                .start();
    }
}