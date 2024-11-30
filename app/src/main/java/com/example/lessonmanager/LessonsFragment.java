package com.example.lessonmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lessonmanager.models.Lesson;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class LessonsFragment extends Fragment implements LessonAdapter.OnLessonActionListener {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private ImageView emptyStateIcon;
    private LessonAdapter lessonAdapter;
    private String status;
    private FirebaseFirestore db;
    private String userId;

    public static LessonsFragment newInstance(String status) {
        LessonsFragment fragment = new LessonsFragment();
        Bundle args = new Bundle();
        args.putString("status", status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = getArguments().getString("status");
        }
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lessons, container, false);
        recyclerView = view.findViewById(R.id.lessonsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        emptyStateIcon = view.findViewById(R.id.emptyStateIcon);
        setupRecyclerView();
        loadLessons();
        return view;
    }

    private void setupRecyclerView() {
        lessonAdapter = new LessonAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(lessonAdapter);
    }

    private void loadLessons() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("lessons")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", status)
                .orderBy("date", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        //Toast.makeText(getContext(), "Error loading lessons", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        List<Lesson> lessons = value.toObjects(Lesson.class);
                        lessonAdapter.updateLessons(lessons);

                        if (lessons.isEmpty()) {
                            emptyStateIcon.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText("No lessons found");
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyStateIcon.setVisibility(View.GONE);
                            emptyView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onEditLesson(Lesson lesson) {
        Intent intent = new Intent(getContext(), EditLessonActivity.class);
        intent.putExtra(LessonDetailsActivity.EXTRA_LESSON, lesson);
        startActivity(intent);
    }

    @Override
    public void onDeleteLesson(Lesson lesson) {
        db.collection("lessons")
                .document(lesson.getLessonId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Lesson deleted", Toast.LENGTH_SHORT).show();
                    lessonAdapter.removeLesson(lesson);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error deleting lesson", Toast.LENGTH_SHORT).show());
    }
}