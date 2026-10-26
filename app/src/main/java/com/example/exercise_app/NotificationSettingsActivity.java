package com.example.exercise_app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import java.util.Calendar;

public class NotificationSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        SwitchCompat switchExercise = findViewById(R.id.switch_exercise_remind);
        TextView btnBack = findViewById(R.id.btn_back);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        boolean isAlarmOn = prefs.getBoolean("noti_exercise", true);
        switchExercise.setChecked(isAlarmOn);

        switchExercise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("noti_exercise", isChecked).apply();
            setDailyAlarm(isChecked);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void setDailyAlarm(boolean isOn) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        if (isOn) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 20);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }

            if (alarmManager != null) {
                alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                );
            }
        } else {
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }
}