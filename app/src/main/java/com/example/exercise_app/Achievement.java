package com.example.exercise_app;

public class Achievement {
    public String title;
    public String condition;
    public int targetValue;
    public String type;

    public Achievement(String title, String condition, int targetValue, String type) {
        this.title = title;
        this.condition = condition;
        this.targetValue = targetValue;
        this.type = type;
    }
}