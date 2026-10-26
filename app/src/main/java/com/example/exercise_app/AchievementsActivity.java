package com.example.exercise_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recycler_achievements);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Achievement> list = new ArrayList<>();
        list.add(new Achievement("운동의 시작", "첫 스테이지 완료", 1, "STAGE"));
        list.add(new Achievement("초보 운동가", "레벨 5 달성", 5, "LEVEL"));
        list.add(new Achievement("숙련된 트레이너", "레벨 15 달성", 15, "LEVEL"));
        list.add(new Achievement("체육관의 지배자", "레벨 30 달성", 30, "LEVEL"));
        list.add(new Achievement("정점에 도달한 자", "레벨 50 달성", 50, "LEVEL"));
        list.add(new Achievement("굽히지 않는 의지", "푸쉬업 누적 50개", 50, "PUSHUP"));
        list.add(new Achievement("강철 가슴", "푸쉬업 누적 300개", 300, "PUSHUP"));
        list.add(new Achievement("지면의 파괴자", "푸쉬업 누적 1,000개", 1000, "PUSHUP"));
        list.add(new Achievement("지구를 밀어내는 자", "푸쉬업 누적 5,000개", 5000, "PUSHUP"));
        list.add(new Achievement("튼튼한 뿌리", "스쿼트 누적 50개", 50, "SQUAT"));
        list.add(new Achievement("강철의 하체", "스쿼트 누적 300개", 300, "SQUAT"));
        list.add(new Achievement("멈추지 않는 엔진", "스쿼트 누적 1,000개", 1000, "SQUAT"));
        list.add(new Achievement("대지의 수호자", "스쿼트 누적 5,000개", 5000, "SQUAT"));
        list.add(new Achievement("성실한 거북이", "30스테이지 도달", 30, "STAGE"));
        list.add(new Achievement("살아있는 조각상", "100스테이지 도달", 100, "STAGE"));
        list.add(new Achievement("전설의 완성", "레벨 100 달성", 100, "LEVEL"));

        recyclerView.setAdapter(new AchievementAdapter(list));
    }

    class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {
        List<Achievement> items;
        SharedPreferences prefs;

        AchievementAdapter(List<Achievement> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Achievement item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvCondition.setText(item.condition);

            int userVal = 0;
            switch (item.type) {
                case "LEVEL": userVal = prefs.getInt("character_level", 1); break;
                case "PUSHUP": userVal = prefs.getInt("total_pushups", 0); break;
                case "SQUAT": userVal = prefs.getInt("total_squats", 0); break;
                case "STAGE": userVal = prefs.getInt("unlocked_stage", 1); break;
            }

            boolean isUnlocked = userVal >= item.targetValue;
            String currentEquipped = prefs.getString("user_title", "없음");

            if (isUnlocked) {
                holder.card.setAlpha(1.0f);
                if (currentEquipped.equals(item.title)) {
                    holder.tvStatus.setText("장착 중 ✅");
                    holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                    holder.card.setStrokeWidth(4);
                    holder.card.setStrokeColor(Color.parseColor("#4CAF50"));
                } else {
                    holder.tvStatus.setText("획득 완료");
                    holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
                    holder.card.setStrokeWidth(0);
                }

                holder.card.setOnClickListener(v -> {
                    prefs.edit().putString("user_title", item.title).apply();
                    notifyDataSetChanged();
                    Toast.makeText(AchievementsActivity.this, item.title + " 칭호를 장착했습니다!", Toast.LENGTH_SHORT).show();
                });
            } else {
                holder.card.setAlpha(0.5f);
                holder.tvStatus.setText("잠김 🔒");
                holder.tvStatus.setTextColor(Color.GRAY);
                holder.card.setStrokeWidth(0);
                holder.card.setOnClickListener(v -> Toast.makeText(AchievementsActivity.this, "아직 조건을 만족하지 못했습니다.", Toast.LENGTH_SHORT).show());
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvCondition, tvStatus;
            MaterialCardView card;

            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_achievement_title);
                tvCondition = v.findViewById(R.id.tv_achievement_condition);
                tvStatus = v.findViewById(R.id.tv_status);
                card = v.findViewById(R.id.card_achievement);
            }
        }
    }
}