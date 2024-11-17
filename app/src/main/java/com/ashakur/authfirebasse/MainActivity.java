package com.ashakur.authfirebasse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.ashakur.authfirebasse.models.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button viewScheduleButton;
    private Button addTaskButton;
    private ListView upcomingListView;
    private ListView notificationsListView;
    private List<Task> upcomingTasks;
    private List<String> notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent(this, NotificationService.class);
        startService(serviceIntent);



        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
            return;
        }

        viewScheduleButton = findViewById(R.id.viewScheduleButton);
        addTaskButton = findViewById(R.id.addTaskButton);
        upcomingListView = findViewById(R.id.upcomingListView);
        notificationsListView = findViewById(R.id.notificationsListView);

        viewScheduleButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ViewScheduleActivity.class)));
        addTaskButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddTaskActivity.class)));

        upcomingTasks = new ArrayList<>();
        notifications = new ArrayList<>();

        setupUpcomingTasksList();
        setupNotificationsList();
        loadUpcomingTasks();
        loadNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUpcomingTasks();
        loadNotifications();
    }

    private void setupUpcomingTasksList() {
        ArrayAdapter<Task> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, upcomingTasks);
        upcomingListView.setAdapter(adapter);
        upcomingListView.setOnItemClickListener((parent, view, position, id) -> {
            Task task = upcomingTasks.get(position);
            Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
            intent.putExtra("TASK_ID", task.getId());
            startActivity(intent);
        });
    }

    private void setupNotificationsList() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notifications);
        notificationsListView.setAdapter(adapter);
    }

    private void loadUpcomingTasks() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .whereGreaterThan("dueDate", new Date())
                .orderBy("dueDate")
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    upcomingTasks.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Task task = document.toObject(Task.class);
                        task.setId(document.getId());
                        upcomingTasks.add(task);
                    }
                    ((ArrayAdapter<?>) upcomingListView.getAdapter()).notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error adding task", e);
                    Toast.makeText(MainActivity.this, "Error loading tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }



    private void loadNotifications() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notifications.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String notificationText = document.getString("text");
                        notifications.add(notificationText);
                    }
                    ((ArrayAdapter<?>) notificationsListView.getAdapter()).notifyDataSetChanged();
                }).addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error adding task", e);
                    Toast.makeText(MainActivity.this, "Error loading notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    public void onSignOutClick(View view) {
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, Login.class));
        finish();
    }
}