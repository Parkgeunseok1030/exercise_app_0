package com.example.exercise_app;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    private final FirebaseFirestore db;

    public FirebaseManager() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnSaveListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void saveExerciseData(String userId, int level, int exp, int pushups, int squats,
                                 int unlockedStage, int perfect, int good,
                                 int earnedStars, OnSaveListener listener) {
        WriteBatch batch = db.batch();

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("level", level);
        updateData.put("exp", exp);
        updateData.put("unlocked_stage", unlockedStage);
        updateData.put("total_pushups", FieldValue.increment(pushups));
        updateData.put("total_squats", FieldValue.increment(squats));
        updateData.put("total_stars", FieldValue.increment(earnedStars));
        batch.set(db.collection("users").document(userId), updateData, SetOptions.merge());

        Map<String, Object> log = new HashMap<>();
        log.put("type", "ROUTINE_SESSION");
        log.put("is_completed", true);
        log.put("perfect", perfect);
        log.put("good", good);
        log.put("pushups", pushups);
        log.put("squats", squats);
        log.put("timestamp", FieldValue.serverTimestamp());

        batch.set(db.collection("users").document(userId).collection("exercise_history").document(), log);

        batch.commit()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    public interface OnLoginListener {
        void onSuccess(String userId, String nickname, String profileIcon,
                       int level, int exp, int unlockedStage,
                       int totalPushups, int totalSquats, int totalStars, String userTitle);
        void onFailure(String errorMessage);
    }

    public void loginUser(String loginId, String password, OnLoginListener listener) {
        db.collection("users")
                .whereEqualTo("login_id", loginId)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);

                        String userId = doc.getId();
                        String nickname = doc.getString("nickname");
                        String profileIcon = doc.getString("profile_icon");

                        int level = doc.contains("level") ? doc.getLong("level").intValue() : 1;
                        int exp = doc.contains("exp") ? doc.getLong("exp").intValue() : 0;
                        int unlockedStage = doc.contains("unlocked_stage") ? doc.getLong("unlocked_stage").intValue() : 1;
                        int totalP = doc.contains("total_pushups") ? doc.getLong("total_pushups").intValue() : 0;
                        int totalS = doc.contains("total_squats") ? doc.getLong("total_squats").intValue() : 0;
                        int totalStars = doc.contains("total_stars") ? doc.getLong("total_stars").intValue() : 0;
                        String userTitle = doc.contains("user_title") ? doc.getString("user_title") : "칭호 없음";

                        listener.onSuccess(userId, nickname, profileIcon, level, exp, unlockedStage, totalP, totalS, totalStars, userTitle);
                    } else {
                        listener.onFailure("아이디 또는 비밀번호가 일치하지 않습니다.");
                    }
                });
    }
}