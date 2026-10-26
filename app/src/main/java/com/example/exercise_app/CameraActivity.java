package com.example.exercise_app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private PreviewView viewFinder;
    private GraphicOverlay graphicOverlay;
    private PoseDetector poseDetector;
    private Executor analysisExecutor;
    private TTSManager ttsManager;
    private FirebaseManager firebaseManager;
    private TextView tvCount, tvCountdown, tvRestTimer, tvGrade, tvCoachingMsg, tvExerciseType;
    private Button btnStartExercise, btnSkipRest, btnNextReady;
    private RelativeLayout layoutRest;
    private ProgressBar progressRest;
    private String[] routine;
    private int currentExerciseIndex = 0;
    private int totalPerfect = 0, totalGood = 0;
    private int pushupCount = 0, squatCount = 0;
    private int currentSetPerfect = 0, currentSetGood = 0;
    private int currentSet = 1, totalSets = 3, targetReps = 10;
    private boolean isDown = false, isCountingActive = false, isCompleted = false, isResting = false, isAttempting = false;
    private int userLevel = 1, currentStage = 1;
    private double successThreshold, returnThreshold, perfectThreshold;
    private long downStartTime = 0;
    private double minAngleThisRep = 180.0;
    private CountDownTimer restTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ttsManager = new TTSManager(this);
        firebaseManager = new FirebaseManager();

        initViews();

        analysisExecutor = Executors.newSingleThreadExecutor();
        poseDetector = PoseDetection.getClient(new AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE).build());

        String selectedEx = getIntent().getStringExtra("EXERCISE_TYPE");
        routine = "SQUAT".equals(selectedEx) ? new String[]{"SQUAT", "PUSHUP"} : new String[]{"PUSHUP", "SQUAT"};

        currentStage = getIntent().getIntExtra("STAGE_NUMBER", 1);
        userLevel = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("level", 1);

        setupCurrentExercise();

        btnStartExercise.setOnClickListener(v -> startCountdownTimer());
        btnSkipRest.setOnClickListener(v -> { if (restTimer != null) restTimer.cancel(); endRestMode(); });
        btnNextReady.setOnClickListener(v -> {
            btnNextReady.setVisibility(View.GONE);
            setupCurrentExercise();
            startCountdownTimer();
        });

        if (checkCameraPermission()) startCamera();
        else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);
    }

    private void setupCurrentExercise() {
        String currentEx = routine[currentExerciseIndex];
        tvExerciseType.setText(currentEx.equals("PUSHUP") ? "푸쉬업 모드" : "스쿼트 모드");
        currentSet = 1; currentSetPerfect = 0; currentSetGood = 0;
        isDown = false; isAttempting = false;
        applyDifficultySettings(userLevel, currentEx);
        updateCountUI();
    }

    private void applyDifficultySettings(int level, String type) {
        totalSets = (level == 1) ? 3 : (level == 2) ? 4 : 5;
        int stageMultiplier = (currentStage - 1) / 5;

        if ("SQUAT".equals(type)) {
            int baseReps = (level == 1) ? 15 : (level == 2) ? 20 : 25;
            int bonusReps = (level == 1) ? (stageMultiplier * 5) : (level == 2) ? (stageMultiplier * 7) : (stageMultiplier * 10);
            targetReps = baseReps + bonusReps;
            if (level == 1) { successThreshold = 105; returnThreshold = 160; perfectThreshold = 85; }
            else if (level == 2) { successThreshold = 90; returnThreshold = 170; perfectThreshold = 75; }
            else { successThreshold = 80; returnThreshold = 175; perfectThreshold = 65; }
        } else {
            int baseReps = (level == 1) ? 8 : (level == 2) ? 12 : 15;
            int bonusReps = (level == 1) ? (stageMultiplier * 2) : (level == 2) ? (stageMultiplier * 3) : (stageMultiplier * 5);
            targetReps = baseReps + bonusReps;
            if (level == 1) { successThreshold = 110; returnThreshold = 150; perfectThreshold = 90; }
            else if (level == 2) { successThreshold = 90; returnThreshold = 160; perfectThreshold = 75; }
            else { successThreshold = 75; returnThreshold = 170; perfectThreshold = 60; }
        }
    }

    private void completeAllWorkouts() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int unlocked = prefs.getInt("unlocked_stage", 1);
        int nextUnlocked = unlocked;

        if (isCompleted) {
            if (currentStage == unlocked) {
                nextUnlocked = unlocked + 1;
                editor.putInt("unlocked_stage", nextUnlocked);
            }
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            editor.putString("last_clear_date", today);
        }

        int totalReps = totalPerfect + totalGood;
        int starCount = 0;
        if (totalReps > 0) {
            double perfectRatio = ((double) totalPerfect / totalReps) * 100.0;
            if (perfectRatio >= 80) starCount = 3;
            else if (perfectRatio >= 50) starCount = 2;
            else if (perfectRatio >= 20) starCount = 1;
        }

        if (starCount > prefs.getInt("stage_" + currentStage + "_stars", 0)) {
            editor.putInt("stage_" + currentStage + "_stars", starCount);
        }

        double mult = (userLevel == 2 ? 1.3 : userLevel == 3 ? 1.6 : 1.0);
        int earnedXP = (int) ((totalPerfect * 12 + totalGood * 6) * mult);
        int oldLevel = prefs.getInt("character_level", 1);
        int level = oldLevel;
        int exp = prefs.getInt("character_exp", 0);
        exp += earnedXP;
        while (exp >= level * 100) { exp -= level * 100; level++; }

        boolean isLevelUp = (level > oldLevel);
        editor.putInt("character_level", level);
        editor.putInt("character_exp", exp);
        editor.putInt("total_pushups", prefs.getInt("total_pushups", 0) + pushupCount);
        editor.putInt("total_squats", prefs.getInt("total_squats", 0) + squatCount);
        editor.apply();

        firebaseManager.saveExerciseData(prefs.getString("user_id", "example_user"), level, exp, pushupCount, squatCount, nextUnlocked, totalPerfect, totalGood, starCount, new FirebaseManager.OnSaveListener() {
            @Override public void onSuccess() {}
            @Override public void onFailure(Exception e) {}
        });

        Intent intent = new Intent(CameraActivity.this, ResultActivity.class);
        intent.putExtra("TOTAL", totalReps);
        intent.putExtra("PERFECT", totalPerfect);
        intent.putExtra("GOOD", totalGood);
        intent.putExtra("XP", earnedXP);
        intent.putExtra("IS_COMPLETED", isCompleted);
        intent.putExtra("STAR_COUNT", starCount);
        intent.putExtra("IS_LEVEL_UP", isLevelUp);
        startActivity(intent);

        ttsManager.speak(isCompleted ? "모든 루틴 완료! 대단합니다." : "운동을 종료합니다.");
        finish();
    }

    private void processCounting(com.google.mlkit.vision.pose.Pose pose) {
        PoseLandmark p1, p2, p3;
        String type = routine[currentExerciseIndex];
        if ("SQUAT".equals(type)) {
            p1 = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
            p2 = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
            p3 = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        } else {
            p1 = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
            p2 = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
            p3 = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        }
        if (p1 == null || p2 == null || p3 == null || p1.getInFrameLikelihood() < 0.5f) {
            showCoaching("⚠️ 몸 전체가 보이게 물러나세요!", Color.RED);
            return;
        }
        double angle = PoseAnalyzer.getAngle(p1, p2, p3);
        if (angle < minAngleThisRep) minAngleThisRep = angle;
        if (angle <= successThreshold) {
            if (!isDown) { isDown = true; isAttempting = false; downStartTime = System.currentTimeMillis(); }
            showCoaching("좋아요! 그대로 일어서세요.", Color.GREEN);
        } else if (isDown && angle >= returnThreshold) {
            long duration = System.currentTimeMillis() - downStartTime;
            isDown = false;
            if (userLevel == 3 && duration < 1100) {
                showGrade("TOO FAST!", Color.YELLOW); ttsManager.speak("너무 빨라요");
            } else {
                if (minAngleThisRep <= perfectThreshold) {
                    currentSetPerfect++; totalPerfect++;
                    if ("PUSHUP".equals(type)) pushupCount++; else squatCount++;
                    showGrade("PERFECT!", Color.parseColor("#4CAF50")); ttsManager.speak("완벽합니다");
                } else {
                    currentSetGood++; totalGood++;
                    if ("PUSHUP".equals(type)) pushupCount++; else squatCount++;
                    showGrade("GOOD!", Color.WHITE); ttsManager.speak("좋아요");
                }
                checkTargetReached();
            }
            minAngleThisRep = 180.0;
        } else if (!isDown && angle < successThreshold + 20) {
            isAttempting = true;
        } else if (isAttempting && !isDown && angle >= returnThreshold - 5) {
            isAttempting = false; showGrade("BAD ❌", Color.RED); ttsManager.speak("더 깊게!");
        }
    }

    private void checkTargetReached() {
        updateCountUI();
        if ((currentSetPerfect + currentSetGood) >= targetReps) {
            if (currentSet < totalSets) startRestMode();
            else {
                if (currentExerciseIndex < routine.length - 1) moveToNextExercise();
                else { isCompleted = true; completeAllWorkouts(); }
            }
        }
    }

    private void moveToNextExercise() {
        isCountingActive = false; currentExerciseIndex++;
        String nextExName = (routine[currentExerciseIndex].equals("SQUAT") ? "스쿼트" : "푸쉬업");
        showCoaching("다음 운동: " + nextExName, Color.CYAN);
        ttsManager.speak("다음 운동은 " + nextExName + " 입니다.");
        runOnUiThread(() -> {
            btnStartExercise.setVisibility(View.VISIBLE); btnStartExercise.setText("운동 종료");
            btnStartExercise.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_red_dark));
            btnStartExercise.setOnClickListener(v -> completeAllWorkouts());
            btnNextReady.setVisibility(View.VISIBLE); btnNextReady.setText(nextExName + " 시작하기");
        });
    }

    private void initViews() {
        viewFinder = findViewById(R.id.viewFinder);
        graphicOverlay = findViewById(R.id.graphicOverlay);
        tvCount = findViewById(R.id.tv_count);
        tvCountdown = findViewById(R.id.tv_countdown);
        tvRestTimer = findViewById(R.id.tv_rest_timer);
        tvGrade = findViewById(R.id.tv_grade);
        tvCoachingMsg = findViewById(R.id.tv_coaching_msg);
        tvExerciseType = findViewById(R.id.tv_exercise_type);
        btnStartExercise = findViewById(R.id.btn_start_exercise);
        btnSkipRest = findViewById(R.id.btn_skip_rest);
        btnNextReady = findViewById(R.id.btn_next_ready);
        layoutRest = findViewById(R.id.layout_rest);
        progressRest = findViewById(R.id.progress_rest);
    }

    private void updateCountUI() { runOnUiThread(() -> tvCount.setText((currentSetPerfect + currentSetGood) + " / " + targetReps + " (" + currentSet + "/" + totalSets + "세트)")); }
    private void showCoaching(String m, int c) { runOnUiThread(() -> { tvCoachingMsg.setText(m); tvCoachingMsg.setTextColor(c); }); }
    private void showGrade(String g, int c) { runOnUiThread(() -> { tvGrade.setVisibility(View.VISIBLE); tvGrade.setText(g); tvGrade.setTextColor(c); new Handler(Looper.getMainLooper()).postDelayed(() -> tvGrade.setVisibility(View.GONE), 1000); }); }

    private void startCountdownTimer() {
        runOnUiThread(() -> { btnStartExercise.setVisibility(View.GONE); tvCountdown.setVisibility(View.VISIBLE); });
        new CountDownTimer(3000, 1000) {
            @Override public void onTick(long l) { int s = (int) (l / 1000) + 1; tvCountdown.setText(String.valueOf(s)); ttsManager.speak(String.valueOf(s)); }
            @Override public void onFinish() {
                tvCountdown.setText("시작!"); ttsManager.speak("시작"); isCountingActive = true;
                runOnUiThread(() -> {
                    btnStartExercise.setVisibility(View.VISIBLE); btnStartExercise.setText("운동 종료");
                    btnStartExercise.setBackgroundTintList(ContextCompat.getColorStateList(CameraActivity.this, android.R.color.holo_red_dark));
                    btnStartExercise.setOnClickListener(v -> completeAllWorkouts());
                    new Handler(Looper.getMainLooper()).postDelayed(() -> tvCountdown.setVisibility(View.GONE), 1000);
                });
            }
        }.start();
    }

    private void startRestMode() {
        isResting = true; isCountingActive = false;
        runOnUiThread(() -> { layoutRest.setVisibility(View.VISIBLE); tvRestTimer.setText("30"); progressRest.setProgress(30); });
        ttsManager.speak("잠시 쉬세요");
        restTimer = new CountDownTimer(30000, 1000) {
            @Override public void onTick(long l) { int sec = (int)(l/1000); tvRestTimer.setText(String.valueOf(sec + 1)); progressRest.setProgress(sec + 1); if (sec < 3) ttsManager.speak(String.valueOf(sec + 1)); }
            @Override public void onFinish() { endRestMode(); }
        }.start();
    }

    private void endRestMode() {
        isResting = false; isCountingActive = true; currentSet++; currentSetPerfect = 0; currentSetGood = 0;
        runOnUiThread(() -> { layoutRest.setVisibility(View.GONE); updateCountUI(); ttsManager.speak(currentSet + "세트 시작"); });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
                ImageAnalysis analysis = new ImageAnalysis.Builder().setTargetResolution(new Size(480, 640)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                analysis.setAnalyzer(analysisExecutor, imageProxy -> {
                    InputImage img = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
                    poseDetector.process(img).addOnSuccessListener(pose -> {
                        graphicOverlay.clear();
                        int rot = imageProxy.getImageInfo().getRotationDegrees();
                        boolean flip = (rot == 90 || rot == 270);
                        graphicOverlay.setCameraInfo(flip ? imageProxy.getHeight() : imageProxy.getWidth(), flip ? imageProxy.getWidth() : imageProxy.getHeight(), CameraSelector.LENS_FACING_FRONT);
                        graphicOverlay.add(new PoseGraphic(graphicOverlay, pose));
                        if (!isCompleted && isCountingActive && !isResting) processCounting(pose);
                    }).addOnCompleteListener(task -> imageProxy.close());
                });
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis);
            } catch (Exception e) { e.printStackTrace(); }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean checkCameraPermission() { return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED; }

    @Override
    protected void onDestroy() {
        if (ttsManager != null) ttsManager.shutdown();
        super.onDestroy();
    }
}