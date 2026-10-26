package com.example.exercise_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Calendar;
import java.util.Date;

public class ActivityFragment extends Fragment {

    private TextView tvWeekWorkouts, tvWeekStages, tvWeekPushups, tvWeekSquats;
    private TextView tvMonthWorkouts, tvMonthStages, tvMonthPushups, tvMonthSquats;
    private TextView tvTotalWorkouts, tvTotalStages, tvTotalPushups, tvTotalSquats;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        initViews(view);
        db = FirebaseFirestore.getInstance();
        loadActivityData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActivityData();
    }

    private void initViews(View view) {
        tvWeekWorkouts = view.findViewById(R.id.tv_week_workouts);
        tvWeekStages = view.findViewById(R.id.tv_week_stages);
        tvWeekPushups = view.findViewById(R.id.tv_week_pushups);
        tvWeekSquats = view.findViewById(R.id.tv_week_squats);

        tvMonthWorkouts = view.findViewById(R.id.tv_month_workouts);
        tvMonthStages = view.findViewById(R.id.tv_month_stages);
        tvMonthPushups = view.findViewById(R.id.tv_month_pushups);
        tvMonthSquats = view.findViewById(R.id.tv_month_squats);

        tvTotalWorkouts = view.findViewById(R.id.tv_total_workouts);
        tvTotalStages = view.findViewById(R.id.tv_total_stages);
        tvTotalPushups = view.findViewById(R.id.tv_total_pushups);
        tvTotalSquats = view.findViewById(R.id.tv_total_squats);
    }

    private void loadActivityData() {
        if (getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", "");

        if (userId.isEmpty()) return;

        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && isAdded()) {
                long totalPushups = documentSnapshot.contains("total_pushups") ? documentSnapshot.getLong("total_pushups") : 0;
                long totalSquats = documentSnapshot.contains("total_squats") ? documentSnapshot.getLong("total_squats") : 0;

                tvTotalPushups.setText(totalPushups + "개");
                tvTotalSquats.setText(totalSquats + "개");
            }
        });

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startOfMonth = cal.getTime();

        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -6);
        } else {
            cal.add(Calendar.DAY_OF_MONTH, Calendar.MONDAY - dayOfWeek);
        }
        Date startOfWeek = cal.getTime();

        db.collection("users").document(userId).collection("exercise_history").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    int wWorkouts = 0, wStages = 0, wPushups = 0, wSquats = 0;
                    int mWorkouts = 0, mStages = 0, mPushups = 0, mSquats = 0;
                    int tWorkouts = 0, tStages = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Date timestamp = doc.getDate("timestamp");
                        boolean isCompleted = doc.contains("is_completed") && Boolean.TRUE.equals(doc.getBoolean("is_completed"));
                        int pushups = doc.contains("pushups") ? doc.getLong("pushups").intValue() : 0;
                        int squats = doc.contains("squats") ? doc.getLong("squats").intValue() : 0;

                        tWorkouts++;
                        if (isCompleted) tStages++;

                        if (timestamp != null) {
                            if (!timestamp.before(startOfMonth)) {
                                mWorkouts++;
                                if (isCompleted) mStages++;
                                mPushups += pushups;
                                mSquats += squats;
                            }

                            if (!timestamp.before(startOfWeek)) {
                                wWorkouts++;
                                if (isCompleted) wStages++;
                                wPushups += pushups;
                                wSquats += squats;
                            }
                        }
                    }

                    tvWeekWorkouts.setText(wWorkouts + "회");
                    tvWeekStages.setText(wStages + "개");
                    tvWeekPushups.setText(wPushups + "개");
                    tvWeekSquats.setText(wSquats + "개");

                    tvMonthWorkouts.setText(mWorkouts + "회");
                    tvMonthStages.setText(mStages + "개");
                    tvMonthPushups.setText(mPushups + "개");
                    tvMonthSquats.setText(mSquats + "개");

                    tvTotalWorkouts.setText(tWorkouts + "회");
                    tvTotalStages.setText(tStages + "개");

                }).addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(getContext(), "데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}