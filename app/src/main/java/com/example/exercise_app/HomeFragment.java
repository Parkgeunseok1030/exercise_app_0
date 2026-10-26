package com.example.exercise_app;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvLevel;
    private ProgressBar progressExp;
    private RecyclerView recyclerStage;
    private Button btnTestExp;
    private TextView tvLevelUpEffect;
    private TextView tvXpFloating;
    private ImageView ivBgBack, ivBgFront;
    private int currentBgPhase = 1;

    private final int[] bgImages = {
            R.drawable.bg_step_1,
            R.drawable.bg_step_2,
            R.drawable.bg_step_3,
            R.drawable.bg_step_4,
            R.drawable.bg_step_5
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvLevel = view.findViewById(R.id.tv_level);
        progressExp = view.findViewById(R.id.progress_exp);
        btnTestExp = view.findViewById(R.id.btn_test_exp);
        recyclerStage = view.findViewById(R.id.recycler_stage);
        tvLevelUpEffect = view.findViewById(R.id.tv_levelup_effect);
        tvXpFloating = view.findViewById(R.id.tv_xp_floating);
        ivBgBack = view.findViewById(R.id.iv_bg_back);
        ivBgFront = view.findViewById(R.id.iv_bg_front);

        updateLevelUI();

        if (btnTestExp != null) {
            btnTestExp.setOnClickListener(v -> {
                addExperience(50);
                updateLevelUI();
            });
        }

        setupRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLevelUI();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        if (recyclerStage != null && getContext() != null) {
            SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int myUnlockedStage = prefs.getInt("unlocked_stage", 1);
            String lastClearDate = prefs.getString("last_clear_date", "");
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            boolean isTodayCleared = lastClearDate.equals(today);

            if (recyclerStage.getLayoutManager() == null) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                layoutManager.setReverseLayout(true);
                recyclerStage.setLayoutManager(layoutManager);
            }

            recyclerStage.setAdapter(new StageAdapter(myUnlockedStage, isTodayCleared));

            recyclerStage.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int highestVisibleIndex = layoutManager.findLastVisibleItemPosition();
                        int visibleStage = highestVisibleIndex + 1;

                        int targetPhase = 1;
                        if (visibleStage > 80) targetPhase = 5;
                        else if (visibleStage > 60) targetPhase = 4;
                        else if (visibleStage > 40) targetPhase = 3;
                        else if (visibleStage > 20) targetPhase = 2;

                        if (targetPhase != currentBgPhase) {
                            changeBackgroundSmoothly(targetPhase);
                        }
                    }
                }
            });
        }
    }

    private void changeBackgroundSmoothly(int targetPhase) {
        currentBgPhase = targetPhase;
        int nextImageRes = bgImages[targetPhase - 1];

        ivBgFront.setImageResource(nextImageRes);
        ivBgFront.animate()
                .alpha(1f)
                .setDuration(800)
                .withEndAction(() -> {
                    ivBgBack.setImageResource(nextImageRes);
                    ivBgFront.setAlpha(0f);
                }).start();
    }

    private void addExperience(int baseExp) {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int currentLevel = prefs.getInt("character_level", 1);
        int currentExp = prefs.getInt("character_exp", 0);
        int myDifficulty = prefs.getInt("level", 1);

        double multiplier = (myDifficulty == 2) ? 1.5 : (myDifficulty == 3) ? 2.0 : 1.0;
        int finalGainedExp = (int) (baseExp * multiplier);
        currentExp += finalGainedExp;

        int maxExp = currentLevel * 100;
        boolean leveledUp = false;

        while (currentExp >= maxExp) {
            currentExp -= maxExp;
            currentLevel++;
            maxExp = currentLevel * 100;
            leveledUp = true;
            progressExp.setProgress(0);
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("character_level", currentLevel);
        editor.putInt("character_exp", currentExp);
        editor.apply();

        if (leveledUp) {
            playLevelUpAnimation();
        }
        playXpFloatingAnimation(finalGainedExp);
    }

    private void updateLevelUI() {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int currentLevel = prefs.getInt("character_level", 1);
        int currentExp = prefs.getInt("character_exp", 0);
        int maxExp = currentLevel * 100;

        if (tvLevel != null) tvLevel.setText("Lv." + currentLevel);

        if (progressExp != null) {
            progressExp.setMax(maxExp);
            ObjectAnimator animation = ObjectAnimator.ofInt(progressExp, "progress", progressExp.getProgress(), currentExp);
            animation.setDuration(500);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }
    }

    private void playXpFloatingAnimation(int gainedExp) {
        if (tvXpFloating == null) return;

        tvXpFloating.setText("+" + gainedExp + " XP");
        tvXpFloating.setVisibility(View.VISIBLE);
        tvXpFloating.setTranslationY(0f);
        tvXpFloating.setAlpha(1f);

        tvXpFloating.animate()
                .translationY(-150f)
                .alpha(0f)
                .setDuration(1000)
                .withEndAction(() -> tvXpFloating.setVisibility(View.GONE))
                .start();
    }

    private void playLevelUpAnimation() {
        if (tvLevelUpEffect == null) return;

        tvLevelUpEffect.setVisibility(View.VISIBLE);
        tvLevelUpEffect.animate()
                .scaleX(1.2f).scaleY(1.2f).alpha(1f)
                .setDuration(600)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .withEndAction(() -> {
                    tvLevelUpEffect.animate()
                            .scaleX(0f).scaleY(0f).alpha(0f)
                            .setStartDelay(1000)
                            .setDuration(400)
                            .withEndAction(() -> tvLevelUpEffect.setVisibility(View.GONE))
                            .start();
                }).start();
    }
}