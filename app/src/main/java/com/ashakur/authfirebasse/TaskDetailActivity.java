package com.ashakur.authfirebasse;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ashakur.authfirebasse.models.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    private EditText taskNameEditText;
    private Spinner taskTypeSpinner;
    private Button dateButton;
    private Button timeButton;
    private EditText reminderEditText;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String taskId;
    private Task currentTask;
    private Calendar taskDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        taskNameEditText = findViewById(R.id.taskNameEditText);
        taskTypeSpinner = findViewById(R.id.taskTypeSpinner);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        reminderEditText = findViewById(R.id.reminderEditText);

        taskId = getIntent().getStringExtra("TASK_ID");
        if (taskId == null) {
            Toast.makeText(this, "Error: Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupTaskTypeSpinner();
        setupDateTimeButtons();
        loadTaskDetails();
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
    }

    private void loadTaskDetails() {
        db.collection("tasks").document(taskId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentTask = documentSnapshot.toObject(Task.class);
                    if (currentTask != null) {
                        currentTask.setId(documentSnapshot.getId());
                        displayTaskDetails();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(TaskDetailActivity.this, "Error loading task details", Toast.LENGTH_SHORT).show());
    }

    private void displayTaskDetails() {
        taskNameEditText.setText(currentTask.getName());
        taskTypeSpinner.setSelection(getIndex(taskTypeSpinner, currentTask.getType()));
        taskDateTime = Calendar.getInstance();
        taskDateTime.setTime(currentTask.getDueDate());
        updateDateTimeButtons();
        reminderEditText.setText(String.valueOf(currentTask.getReminderMinutes()));
    }

    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }
        return 0;
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            taskDateTime.set(Calendar.YEAR, year);
            taskDateTime.set(Calendar.MONTH, month);
            taskDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateTimeButtons();
        }, taskDateTime.get(Calendar.YEAR), taskDateTime.get(Calendar.MONTH),
                taskDateTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            taskDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            taskDateTime.set(Calendar.MINUTE, minute);
            updateDateTimeButtons();
        }, taskDateTime.get(Calendar.HOUR_OF_DAY), taskDateTime.get(Calendar.MINUTE), false).show();
    }

    private void updateDateTimeButtons() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateButton.setText(dateFormat.format(taskDateTime.getTime()));
        timeButton.setText(timeFormat.format(taskDateTime.getTime()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveTask();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDeleteTask();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        currentTask.setName(taskName);
        currentTask.setType(taskType);
        currentTask.setDueDate(taskDateTime.getTime());
        currentTask.setReminderMinutes(Integer.parseInt(reminderMinutes));

        db.collection("tasks").document(taskId)
                .set(currentTask)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TaskDetailActivity.this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(TaskDetailActivity.this, "Error updating task", Toast.LENGTH_SHORT).show());
    }

    private void confirmDeleteTask() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> deleteTask())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteTask() {
        db.collection("tasks").document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TaskDetailActivity.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(TaskDetailActivity.this, "Error deleting task", Toast.LENGTH_SHORT).show());
    }
}