package com.example.exercise_app;

public class Friend implements Comparable<Friend> {
    public String uid;
    public String nickname;
    public String profileIcon;
    public String title;
    public int totalPushups;
    public int totalSquats;
    public int totalStars;
    public int totalScore;

    public Friend(String uid, String nickname, String profileIcon, String title, int totalPushups, int totalSquats, int totalStars) {
        this.uid = uid;
        this.nickname = nickname;
        this.profileIcon = profileIcon;
        this.title = title;
        this.totalPushups = totalPushups;
        this.totalSquats = totalSquats;
        this.totalStars = totalStars;
        this.totalScore = totalPushups + totalSquats;
    }

    @Override
    public int compareTo(Friend other) {
        if (this.totalScore != other.totalScore) {
            return Integer.compare(other.totalScore, this.totalScore);
        }
        return Integer.compare(other.totalStars, this.totalStars);
    }
}