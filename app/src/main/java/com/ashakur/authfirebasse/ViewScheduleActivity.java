package com.ashakur.authfirebasse;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ashakur.authfirebasse.models.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewScheduleActivity extends AppCompatActivity {
    private TextView dateTextView;
    private ListView scheduleListView;
    private Button prevDayButton;
    private Button nextDayButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Calendar currentDate;
    private List<Task> taskList;
    private ArrayAdapter<Task> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_schedule);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        dateTextView = findViewById(R.id.dateTextView);
        scheduleListView = findViewById(R.id.scheduleListView);
        prevDayButton = findViewById(R.id.prevDayButton);
        nextDayButton = findViewById(R.id.nextDayButton);

        currentDate = Calendar.getInstance();
        taskList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
        scheduleListView.setAdapter(adapter);

        updateDateDisplay();
        loadTasksForCurrentDate();

        prevDayButton.setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, -1);
            updateDateDisplay();
            loadTasksForCurrentDate();
        });

        nextDayButton.setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
            updateDateDisplay();
            loadTasksForCurrentDate();
        });

        scheduleListView.setOnItemClickListener((parent, view, position, id) -> {
            Task task = taskList.get(position);
            // TODO: Open TaskDetailsActivity with the selected task
        });
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        dateTextView.setText(dateFormat.format(currentDate.getTime()));
    }

    private void loadTasksForCurrentDate() {
        String userId = mAuth.getCurrentUser().getUid();
        Date startOfDay = getStartOfDay(currentDate);
        Date endOfDay = getEndOfDay(currentDate);

        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("dueDate", startOfDay)
                .whereLessThan("dueDate", endOfDay)
                .orderBy("dueDate")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    taskList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Task task = document.toObject(Task.class);
                        task.setId(document.getId());
                        taskList.add(task);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(ViewScheduleActivity.this, "Error loading tasks", Toast.LENGTH_SHORT).show());
    }

    private Date getStartOfDay(Calendar calendar) {
        Calendar start = (Calendar) calendar.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        return start.getTime();
    }

    private Date getEndOfDay(Calendar calendar) {
        Calendar end = (Calendar) calendar.clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        return end.getTime();
    }
}