package com.example.exercise_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FriendFragment extends Fragment {

    private RecyclerView recyclerRanking;
    private RankingAdapter adapter;
    private List<Friend> friendList = new ArrayList<>();
    private TextView tvMyName, tvMyRank, tvMyScore, tvMyIcon;
    private FirebaseFirestore db;
    private String myUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        tvMyIcon = view.findViewById(R.id.tv_my_icon);
        tvMyName = view.findViewById(R.id.tv_my_name);
        tvMyRank = view.findViewById(R.id.tv_my_rank);
        tvMyScore = view.findViewById(R.id.tv_my_score);
        Button btnAddFriend = view.findViewById(R.id.btn_add_friend);
        recyclerRanking = view.findViewById(R.id.recycler_friend_ranking);

        recyclerRanking.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RankingAdapter(friendList);
        recyclerRanking.setAdapter(adapter);

        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        myUid = prefs.getString("user_id", "");
        db = FirebaseFirestore.getInstance();

        btnAddFriend.setOnClickListener(v -> showAddFriendDialog());
        loadRankingData();

        return view;
    }

    private void showAddFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("새로운 친구 추가");
        builder.setMessage("친구의 고유번호(UID)를 입력해 주세요.");

        final EditText input = new EditText(requireContext());
        builder.setView(input);

        builder.setPositiveButton("추가하기", (dialog, which) -> {
            String friendUid = input.getText().toString().trim();
            if (friendUid.isEmpty()) {
                Toast.makeText(getContext(), "고유번호를 입력해 주세요!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (friendUid.equals(myUid)) {
                Toast.makeText(getContext(), "자신은 이미 목록에 있습니다!", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").document(friendUid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    db.collection("users").document(myUid)
                            .update("friends", FieldValue.arrayUnion(friendUid))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "친구가 추가되었습니다! 🔥", Toast.LENGTH_SHORT).show();
                                loadRankingData();
                            });
                } else {
                    Toast.makeText(getContext(), "존재하지 않는 고유번호입니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadRankingData() {
        if (myUid.isEmpty()) return;

        db.collection("users").document(myUid).get().addOnSuccessListener(myDoc -> {
            if (!myDoc.exists() || !isAdded()) return;

            List<String> targetUids = new ArrayList<>();
            targetUids.add(myUid);

            List<String> friends = (List<String>) myDoc.get("friends");
            if (friends != null) {
                targetUids.addAll(friends);
            }

            List<Friend> tempRankingList = new ArrayList<>();
            AtomicInteger fetchCounter = new AtomicInteger(0);

            for (String uid : targetUids) {
                db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String name = userDoc.contains("nickname") ? userDoc.getString("nickname") : "알 수 없음";
                        String icon = userDoc.contains("profile_icon") ? userDoc.getString("profile_icon") : "👤";
                        String title = userDoc.contains("user_title") ? userDoc.getString("user_title") : "🌱 새싹 헬린이";
                        int pushups = userDoc.contains("total_pushups") ? userDoc.getLong("total_pushups").intValue() : 0;
                        int squats = userDoc.contains("total_squats") ? userDoc.getLong("total_squats").intValue() : 0;
                        int stars = userDoc.contains("total_stars") ? userDoc.getLong("total_stars").intValue() : 0;

                        tempRankingList.add(new Friend(uid, name, icon, title, pushups, squats, stars));
                    }

                    if (fetchCounter.incrementAndGet() == targetUids.size()) {
                        Collections.sort(tempRankingList);
                        friendList.clear();
                        friendList.addAll(tempRankingList);
                        adapter.notifyDataSetChanged();
                        updateMyTopCard();
                    }
                });
            }
        });
    }

    private void updateMyTopCard() {
        for (int i = 0; i < friendList.size(); i++) {
            Friend f = friendList.get(i);
            if (f.uid.equals(myUid)) {
                int myRank = i + 1;
                tvMyIcon.setText(f.profileIcon);
                tvMyName.setText(f.nickname);
                tvMyRank.setText("현재 " + myRank + "위");
                tvMyScore.setText("총 " + f.totalScore + "개 (푸쉬업 " + f.totalPushups + " + 스쿼트 " + f.totalSquats + ") | ⭐ " + f.totalStars);
                break;
            }
        }
    }
}