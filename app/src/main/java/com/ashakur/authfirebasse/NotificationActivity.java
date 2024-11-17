package com.ashakur.authfirebasse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ashakur.authfirebasse.models.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class NotificationActivity extends AppCompatActivity {

    private TextView taskNameTextView;
    private TextView taskTypeTextView;
    private TextView dueDateTextView;
    private Button completeButton;
    private Button postponeButton;

    private FirebaseFirestore db;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        db = FirebaseFirestore.getInstance();

        taskNameTextView = findViewById(R.id.taskNameTextView);
        taskTypeTextView = findViewById(R.id.taskTypeTextView);
        dueDateTextView = findViewById(R.id.dueDateTextView);
        completeButton = findViewById(R.id.completeButton);
        postponeButton = findViewById(R.id.postponeButton);

        Intent intent = getIntent();
        taskId = intent.getStringExtra("TASK_ID");

        if (taskId == null) {
            Toast.makeText(this, "Error: Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTaskDetails();

        completeButton.setOnClickListener(v -> completeTask());
        postponeButton.setOnClickListener(v -> postponeTask());
    }

    private void loadTaskDetails() {
        db.collection("tasks").document(taskId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Task task = documentSnapshot.toObject(Task.class);
                    if (task != null) {
                        taskNameTextView.setText(task.getName());
                        taskTypeTextView.setText(task.getType());
                        dueDateTextView.setText(task.getDueDateFormatted());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading task details", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void completeTask() {
        db.collection("tasks").document(taskId).update("completed", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Task marked as completed", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error completing task", Toast.LENGTH_SHORT).show());
    }

    private void postponeTask() {
        // Here you can implement logic to postpone the task
        // For example, you could add 1 day to the due date
        db.collection("tasks").document(taskId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Task task = documentSnapshot.toObject(Task.class);
                    if (task != null) {
                        long newDueDate = task.getDueDate().getTime() + (24 * 60 * 60 * 1000); // Add 1 day
                        db.collection("tasks").document(taskId).update("dueDate", new Date(newDueDate))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Task postponed by 1 day", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error postponing task", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading task details", Toast.LENGTH_SHORT).show());
    }
}