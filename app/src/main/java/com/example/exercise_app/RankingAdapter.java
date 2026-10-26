package com.example.exercise_app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private List<Friend> friendList;

    public RankingAdapter(List<Friend> friendList) {
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Friend friend = friendList.get(position);
        int rank = position + 1;

        if (rank == 1) {
            holder.tvRankNumber.setText("🥇");
            holder.tvRankNumber.setTextColor(Color.parseColor("#FFD700"));
        } else if (rank == 2) {
            holder.tvRankNumber.setText("🥈");
            holder.tvRankNumber.setTextColor(Color.parseColor("#C0C0C0"));
        } else if (rank == 3) {
            holder.tvRankNumber.setText("🥉");
            holder.tvRankNumber.setTextColor(Color.parseColor("#CD7F32"));
        } else {
            holder.tvRankNumber.setText(String.valueOf(rank));
            holder.tvRankNumber.setTextColor(Color.parseColor("#333333"));
        }

        holder.tvIcon.setText(friend.profileIcon);
        holder.tvName.setText(friend.nickname);
        holder.tvTitle.setText(friend.title);
        holder.tvScore.setText(friend.totalScore + "개");
        holder.tvStars.setText("⭐ " + friend.totalStars);
    }

    @Override
    public int getItemCount() {
        return friendList != null ? friendList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRankNumber, tvIcon, tvName, tvTitle, tvScore, tvStars;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRankNumber = itemView.findViewById(R.id.tv_rank_number);
            tvIcon = itemView.findViewById(R.id.tv_friend_icon);
            tvName = itemView.findViewById(R.id.tv_friend_name);
            tvTitle = itemView.findViewById(R.id.tv_friend_title);
            tvScore = itemView.findViewById(R.id.tv_friend_score);
            tvStars = itemView.findViewById(R.id.tv_friend_stars);
        }
    }
}