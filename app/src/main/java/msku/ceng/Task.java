package msku.ceng;

import com.google.firebase.firestore.Exclude;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task {
    private String id;
    private String taskText;
    private String category;
    private boolean isCompleted;
    private int backgroundColor;
    private String taskDateStr;
    private String timeStr;
    private String userId;
    private String taskDate;


    public Task() {}

    public Task(String id, String taskText, String taskDateStr, String timeStr, String category, int backgroundColor, String userId) {
        this.id = id;
        this.taskText = taskText;
        this.category = category;
        this.backgroundColor = backgroundColor;
        this.isCompleted = false;
        this.taskDateStr = taskDateStr;
        this.timeStr = timeStr;
        this.userId = userId;
        this.taskDate = taskDateStr + " " + timeStr;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTaskText() { return taskText; }
    public void setTaskText(String taskText) { this.taskText = taskText; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(int backgroundColor) { this.backgroundColor = backgroundColor; }

    public String getTaskDateStr() { return taskDateStr; }
    public void setTaskDateStr(String taskDateStr) {
        this.taskDateStr = taskDateStr;
        updateTaskDate();
    }

    public String getTimeStr() { return timeStr; }
    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
        updateTaskDate();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    // Firebase için gerekli getter/setter
    public String getTaskDate() { return taskDate; }
    public void setTaskDate(String taskDate) { this.taskDate = taskDate; }

    private void updateTaskDate() {
        if (taskDateStr != null && timeStr != null) {
            this.taskDate = taskDateStr + " " + timeStr;
        }
    }

    @Exclude
    public Date getDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.parse(taskDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }
}