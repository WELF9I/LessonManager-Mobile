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
import com.example.lessonmanager.services.GeminiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddLessonActivity extends AppCompatActivity {
    private TextInputEditText titleInput, subjectInput, descriptionInput, dateInput;
    private MaterialButton saveButton, refineButton;
    private View progressBar;
    private Calendar selectedDate = Calendar.getInstance();
    private FirebaseFirestore db;
    private String userId;
    private ExecutorService executorService;
    private GeminiService geminiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lesson);
        executorService = Executors.newSingleThreadExecutor();
        initializeViews();
        setupToolbar();
        setupDatePicker();
        setupSaveButton();

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        geminiService = new GeminiService();
        setupRefineButton();
    }

    private void initializeViews() {
        titleInput = findViewById(R.id.titleInput);
        subjectInput = findViewById(R.id.subjectInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        dateInput = findViewById(R.id.dateInput);
        saveButton = findViewById(R.id.saveButton);
        refineButton = findViewById(R.id.refineButton);
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

    private void setupRefineButton() {
        refineButton.setOnClickListener(v -> refineInputs());
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

    private void refineInputs() {
        String title = titleInput.getText().toString().trim();
        String subject = subjectInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        geminiService.refineText(title, subject, description, new GeminiService.RefineCallback() {
            @Override
            public void onSuccess(GeminiService.RefinedText refinedText) {
                runOnUiThread(() -> {
                    if (!refinedText.getTitle().isEmpty()) {
                        titleInput.setText(refinedText.getTitle());
                    }
                    if (!refinedText.getSubject().isEmpty()) {
                        subjectInput.setText(refinedText.getSubject());
                    }
                    if (!refinedText.getDescription().isEmpty()) {
                        descriptionInput.setText(refinedText.getDescription());
                    }
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddLessonActivity.this, "Text refined successfully", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddLessonActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveLesson() {
        progressBar.setVisibility(View.VISIBLE);
        String lessonId = db.collection("lessons").document().getId();
        Calendar currentDate = Calendar.getInstance();
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.MILLISECOND, 0);
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
        lesson.setLessonId(lessonId);
        lesson.setStatus(status);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}