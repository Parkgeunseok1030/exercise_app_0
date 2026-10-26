package com.example.exercise_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class DifficultyActivity extends AppCompatActivity {

    private RadioGroup rgDifficulty;
    private int selectedLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        rgDifficulty = findViewById(R.id.rg_difficulty);
        Button btnSave = findViewById(R.id.btn_save_difficulty);
        TextView btnCancel = findViewById(R.id.btn_cancel);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        selectedLevel = prefs.getInt("level", 1);

        if (selectedLevel == 1) rgDifficulty.check(R.id.rb_level1);
        else if (selectedLevel == 2) rgDifficulty.check(R.id.rb_level2);
        else if (selectedLevel == 3) rgDifficulty.check(R.id.rb_level3);

        rgDifficulty.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_level1) selectedLevel = 1;
            else if (checkedId == R.id.rb_level2) selectedLevel = 2;
            else if (checkedId == R.id.rb_level3) selectedLevel = 3;
        });

        btnSave.setOnClickListener(v -> {
            prefs.edit().putInt("level", selectedLevel).apply();

            String myUid = prefs.getString("user_id", "");
            if (!myUid.isEmpty()) {
                FirebaseFirestore.getInstance()
                        .collection("users").document(myUid)
                        .update("level", selectedLevel)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, selectedLevel + "단계로 난이도가 변경되었습니다! 🔥", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "서버 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        });
            } else {
                Toast.makeText(this, "난이도가 로컬에 저장되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}