package com.ashakur.authfirebasse.models;

import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Task {
    @Exclude
    private String id;
    private String name;
    private String type;
    private Date dueDate;
    private int reminderMinutes;
    private String userId;
    private boolean completed;

    public Task() {}

    public Task(String name, String type, Date dueDate, int reminderMinutes, String userId) {
        this.name = name;
        this.type = type;
        this.dueDate = dueDate;
        this.reminderMinutes = reminderMinutes;
        this.userId = userId;
        this.completed = false;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public int getReminderMinutes() {
        return reminderMinutes;
    }

    public void setReminderMinutes(int reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getDueDateFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(dueDate);
    }

    public String getTimeUntilDue() {
        long diff = dueDate.getTime() - new Date().getTime();
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

        if (days > 0) {
            return String.format(Locale.getDefault(), "%d days, %d hours", days, hours);
        } else if (hours > 0) {
            return String.format(Locale.getDefault(), "%d hours, %d minutes", hours, minutes);
        } else if (minutes > 0) {
            return String.format(Locale.getDefault(), "%d minutes", minutes);
        } else {
            return "Due now";
        }
    }

    public boolean isOverdue() {
        return new Date().after(dueDate);
    }

    public Date getReminderTime() {
        return new Date(dueDate.getTime() - TimeUnit.MINUTES.toMillis(reminderMinutes));
    }

    @Override
    public String toString() {
        return name + " - " + type + " (Due: " + getDueDateFormatted() + ")";
    }
}