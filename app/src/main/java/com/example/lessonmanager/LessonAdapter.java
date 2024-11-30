package com.example.lessonmanager;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lessonmanager.models.Lesson;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {
    private List<Lesson> lessons;
    private OnLessonActionListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnLessonActionListener {
        void onEditLesson(Lesson lesson);
        void onDeleteLesson(Lesson lesson);
    }

    public LessonAdapter(List<Lesson> lessons, OnLessonActionListener listener) {
        this.lessons = lessons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lesson_item, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.titleView.setText(lesson.getTitle());
        holder.subjectView.setText(lesson.getSubject());
        holder.dateView.setText(dateFormat.format(lesson.getDate()));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), LessonDetailsActivity.class);
            intent.putExtra(LessonDetailsActivity.EXTRA_LESSON, lesson);
            holder.itemView.getContext().startActivity(intent);
        });
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteLesson(lesson));
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public void updateLessons(List<Lesson> newLessons) {
        this.lessons = newLessons;
        notifyDataSetChanged();
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView subjectView;
        TextView dateView;
        ImageButton editButton;
        ImageButton deleteButton;

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.lessonTitle);
            subjectView = itemView.findViewById(R.id.lessonSubject);
            dateView = itemView.findViewById(R.id.lessonDate);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}