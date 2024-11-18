package com.example.lessonmanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.lessonmanager.models.Lesson;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddLessonActivity extends AppCompatActivity {
    private TextInputEditText titleInput, subjectInput, descriptionInput, dateInput;
    private MaterialButton saveButton;
    private View progressBar;
    private Calendar selectedDate = Calendar.getInstance();
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lesson);
        initializeViews();
        setupToolbar();
        setupDatePicker();
        setupSaveButton();

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void initializeViews() {
        titleInput = findViewById(R.id.titleInput);
        subjectInput = findViewById(R.id.subjectInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        dateInput = findViewById(R.id.dateInput);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add New Lesson");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupDatePicker() {
        dateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        updateDateDisplay();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        dateInput.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveLesson();
            }
        });
    }

    private boolean validateInputs() {
        if (titleInput.getText().toString().trim().isEmpty()) {
            titleInput.setError("Title is required");
            return false;
        }
        if (subjectInput.getText().toString().trim().isEmpty()) {
            subjectInput.setError("Subject is required");
            return false;
        }
        if (dateInput.getText().toString().trim().isEmpty()) {
            dateInput.setError("Date is required");
            return false;
        }
        return true;
    }

    private void saveLesson() {
        progressBar.setVisibility(View.VISIBLE);

        // Create a new document reference first
        String lessonId = db.collection("lessons").document().getId();

        // Get current date (without time component)
        Calendar currentDate = Calendar.getInstance();
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.MILLISECOND, 0);

        // Determine status based on date comparison
        String status;
        if (selectedDate.compareTo(currentDate) >= 0) {
            status = "upcoming";
        } else {
            status = "completed";
        }

        Lesson lesson = new Lesson(
                userId,
                titleInput.getText().toString().trim(),
                descriptionInput.getText().toString().trim(),
                subjectInput.getText().toString().trim(),
                selectedDate.getTime()
        );

        // Set the lessonId and status
        lesson.setLessonId(lessonId);
        lesson.setStatus(status);  // Make sure you have this setter in your Lesson class

        saveLessonToFirestore(lesson);
    }

    private void saveLessonToFirestore(Lesson lesson) {
        if (lesson.getLessonId() == null) {
            Toast.makeText(this, "Error: Lesson ID is null", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        db.collection("lessons")
                .document(lesson.getLessonId())
                .set(lesson)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lesson saved successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to save lesson", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}