package com.example.exercise_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class StageAdapter extends RecyclerView.Adapter<StageAdapter.ViewHolder> {

    private final int totalStages = 100;
    private int currentUnlockedStage;
    private boolean isTodayCleared;

    private final int[] patternImages = {
            R.drawable.treasure_546931,
            R.drawable.chest_2047222,
            R.drawable.treasure_chest_2284674,
            R.drawable.chest_2597098,
            R.drawable.treasure_546931
    };

    public StageAdapter(int unlockedStage, boolean isTodayCleared) {
        this.currentUnlockedStage = unlockedStage;
        this.isTodayCleared = isTodayCleared;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int stageNumber = position + 1;
        Context context = holder.itemView.getContext();

        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int myStars = prefs.getInt("stage_" + stageNumber + "_stars", 0);

        int patternIndex = position % 5;
        int currentBoxImage = patternImages[patternIndex];

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        Paint grayscalePaint = new Paint();
        grayscalePaint.setColorFilter(filter);

        if (stageNumber < currentUnlockedStage || myStars > 0) {
            holder.btnStage.setLayerType(View.LAYER_TYPE_NONE, null);
            holder.btnStage.setBackgroundResource(R.drawable.chest_1530169);
            holder.btnStage.setText("");
            holder.layoutStars.setVisibility(View.VISIBLE);

            holder.ivStar1.setColorFilter(myStars >= 1 ? Color.parseColor("#FFD700") : Color.LTGRAY);
            holder.ivStar2.setColorFilter(myStars >= 2 ? Color.parseColor("#FFD700") : Color.LTGRAY);
            holder.ivStar3.setColorFilter(myStars >= 3 ? Color.parseColor("#FFD700") : Color.LTGRAY);

            holder.btnStage.setOnClickListener(v -> showExerciseDialog(context, stageNumber));

        } else if (stageNumber == currentUnlockedStage) {
            holder.btnStage.setLayerType(View.LAYER_TYPE_NONE, null);
            holder.btnStage.setBackgroundResource(currentBoxImage);
            holder.btnStage.setText(String.valueOf(stageNumber));
            holder.btnStage.setTextColor(Color.WHITE);
            holder.layoutStars.setVisibility(View.VISIBLE);

            holder.ivStar1.setColorFilter(Color.LTGRAY);
            holder.ivStar2.setColorFilter(Color.LTGRAY);
            holder.ivStar3.setColorFilter(Color.LTGRAY);

            holder.btnStage.setOnClickListener(v -> {
                if (isTodayCleared) {
                    Toast.makeText(context, "오늘의 운동은 이미 완료했습니다!\n내일 새로운 스테이지가 열립니다. 😴", Toast.LENGTH_SHORT).show();
                    return;
                }
                showExerciseDialog(context, stageNumber);
            });

        } else {
            holder.btnStage.setBackgroundResource(currentBoxImage);
            holder.btnStage.setLayerType(View.LAYER_TYPE_HARDWARE, grayscalePaint);
            holder.btnStage.setText(String.valueOf(stageNumber));
            holder.btnStage.setTextColor(Color.parseColor("#DDDDDD"));
            holder.layoutStars.setVisibility(View.INVISIBLE);

            holder.btnStage.setOnClickListener(v -> {
                Toast.makeText(context, "🔒 이전 일차를 먼저 완료해야 열립니다!", Toast.LENGTH_SHORT).show();
            });
        }

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.btnStage.getLayoutParams();
        int pattern = position % 4;
        if (pattern == 0) params.horizontalBias = 0.2f;
        else if (pattern == 1) params.horizontalBias = 0.5f;
        else if (pattern == 2) params.horizontalBias = 0.8f;
        else if (pattern == 3) params.horizontalBias = 0.5f;

        holder.btnStage.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return totalStages;
    }

    private void showExerciseDialog(Context context, int stageNumber) {
        String[] exercises = {"푸쉬업 (Push-up)", "스쿼트 (Squat)"};

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(stageNumber + "일차 운동 선택")
                .setItems(exercises, (d, which) -> {
                    Intent intent = new Intent(context, CameraActivity.class);
                    intent.putExtra("EXERCISE_TYPE", which == 0 ? "PUSHUP" : "SQUAT");
                    intent.putExtra("STAGE_NUMBER", stageNumber);
                    context.startActivity(intent);
                })
                .setNegativeButton("취소", null)
                .create();

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000000"));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        Button btnStage;
        LinearLayout layoutStars;
        ImageView ivStar1, ivStar2, ivStar3;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            btnStage = itemView.findViewById(R.id.btn_single_stage);
            layoutStars = itemView.findViewById(R.id.layout_stars);
            ivStar1 = itemView.findViewById(R.id.iv_star1);
            ivStar2 = itemView.findViewById(R.id.iv_star2);
            ivStar3 = itemView.findViewById(R.id.iv_star3);
        }
    }
}