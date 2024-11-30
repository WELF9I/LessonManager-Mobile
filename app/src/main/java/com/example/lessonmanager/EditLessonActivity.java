package com.example.lessonmanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.lessonmanager.models.Lesson;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.Date;

public class EditLessonActivity extends AppCompatActivity {
    private EditText titleEditText, descriptionEditText, subjectEditText;
    private Spinner statusSpinner;
    private MaterialButton saveButton, cancelButton, chooseDateButton;
    private FirebaseFirestore db;
    private Lesson lesson;
    private Date selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lesson);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        lesson = getIntent().getParcelableExtra(LessonDetailsActivity.EXTRA_LESSON);

        if (lesson != null) {
            populateLessonDetails();
        } else {
            Toast.makeText(this, "Error loading lesson", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Lesson");

        titleEditText = findViewById(R.id.lessonTitleEdit);
        descriptionEditText = findViewById(R.id.lessonDescriptionEdit);
        subjectEditText = findViewById(R.id.lessonSubjectEdit);
        statusSpinner = findViewById(R.id.statusSpinner);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        chooseDateButton = findViewById(R.id.chooseDateButton);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this, R.array.status_array, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        chooseDateButton.setOnClickListener(v -> showDatePickerDialog());
        saveButton.setOnClickListener(v -> saveLesson());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void populateLessonDetails() {
        titleEditText.setText(lesson.getTitle());
        descriptionEditText.setText(lesson.getDescription());
        subjectEditText.setText(lesson.getSubject());
        int statusPosition = ((ArrayAdapter<String>) statusSpinner.getAdapter())
                .getPosition(lesson.getStatus());
        statusSpinner.setSelection(statusPosition);
        selectedDate = lesson.getDate();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.setTime(selectedDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedDate = selectedCalendar.getTime();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveLesson() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String subject = subjectEditText.getText().toString().trim();
        String status = statusSpinner.getSelectedItem().toString();

        if (title.isEmpty() || description.isEmpty() || selectedDate == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setSubject(subject);
        lesson.setStatus(status);
        lesson.setDate(selectedDate);
        lesson.setUpdatedAt(new Date());
        db.collection("lessons")
                .document(lesson.getLessonId())
                .set(lesson)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditLessonActivity.this, "Lesson updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditLessonActivity.this, "Error updating lesson", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}