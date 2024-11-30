package com.example.lessonmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.lessonmanager.models.Lesson;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LessonDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_LESSON = "extra_lesson";
    private static final int EDIT_LESSON_REQUEST_CODE = 2;

    private TextView titleView, subjectView, dateView, statusView, descriptionView;
    private MaterialButton editButton;
    private SimpleDateFormat dateFormat;
    private Lesson lesson;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_details);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        lesson = getIntent().getParcelableExtra(EXTRA_LESSON);
        if (lesson != null) {
            displayLessonDetails(lesson);
        } else {
            Toast.makeText(this, "Error loading lesson details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Lesson Details");

        titleView = findViewById(R.id.lessonTitleDetails);
        subjectView = findViewById(R.id.lessonSubjectDetails);
        dateView = findViewById(R.id.lessonDateDetails);
        statusView = findViewById(R.id.lessonStatusDetails);
        descriptionView = findViewById(R.id.lessonDescriptionDetails);
        editButton = findViewById(R.id.editLessonButton);
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    private void displayLessonDetails(Lesson lesson) {
        titleView.setText(lesson.getTitle());
        subjectView.setText(lesson.getSubject());
        dateView.setText(dateFormat.format(lesson.getDate()));
        statusView.setText(lesson.getStatus());
        descriptionView.setText(lesson.getDescription());

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditLessonActivity.class);
            intent.putExtra(EXTRA_LESSON, lesson);
            startActivityForResult(intent, EDIT_LESSON_REQUEST_CODE);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_LESSON_REQUEST_CODE && resultCode == RESULT_OK) {
            // Refresh the lesson details
            String lessonId = lesson.getLessonId();
            db.collection("lessons").document(lessonId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        lesson = documentSnapshot.toObject(Lesson.class);
                        if (lesson != null) {
                            displayLessonDetails(lesson);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error loading updated lesson details", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}