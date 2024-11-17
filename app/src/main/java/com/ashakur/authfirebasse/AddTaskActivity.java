package com.ashakur.authfirebasse;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {
    private EditText taskNameEditText;
    private Spinner taskTypeSpinner;
    private Button dateButton;
    private Button timeButton;
    private EditText reminderEditText;
    private Button saveButton;
    private Button cancelButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Calendar selectedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        taskNameEditText = findViewById(R.id.taskNameEditText);
        taskTypeSpinner = findViewById(R.id.taskTypeSpinner);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        reminderEditText = findViewById(R.id.reminderEditText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        selectedDateTime = Calendar.getInstance();

        setupTaskTypeSpinner();
        setupDateTimeButtons();

        saveButton.setOnClickListener(v -> saveTask());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void setupTaskTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.task_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskTypeSpinner.setAdapter(adapter);
    }

    private void setupDateTimeButtons() {
        dateButton.setOnClickListener(v -> showDatePicker());
        timeButton.setOnClickListener(v -> showTimePicker());
        updateDateTimeButtons();
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateTimeButtons();
        }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute);
            updateDateTimeButtons();
        }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), false).show();
    }

    private void updateDateTimeButtons() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateButton.setText(dateFormat.format(selectedDateTime.getTime()));
        timeButton.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void saveTask() {
        String taskName = taskNameEditText.getText().toString().trim();
        String taskType = taskTypeSpinner.getSelectedItem().toString();
        String reminderMinutes = reminderEditText.getText().toString().trim();

        if (taskName.isEmpty()) {
            taskNameEditText.setError("Task name is required");
            return;
        }

        if (reminderMinutes.isEmpty()) {
            reminderEditText.setError("Reminder time is required");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Date dueDate = selectedDateTime.getTime();

        Map<String, Object> task = new HashMap<>();
        task.put("name", taskName);
        task.put("type", taskType);
        task.put("dueDate", dueDate);
        task.put("reminderMinutes", Integer.parseInt(reminderMinutes));
        task.put("userId", userId);

        db.collection("tasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddTaskActivity.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("AddTaskActivity", "Error adding task", e);
                    Toast.makeText(AddTaskActivity.this, "Error adding task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}