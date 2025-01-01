package msku.ceng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task {
    private String taskText;
    private Date taskDate;
    private boolean isCompleted;
    private int backgroundColor;
    private String category;

    public Task(String taskText, String taskDateStr, String timeStr, String category, int backgroundColor) {
        this.taskText = taskText;
        this.category = category;
        this.backgroundColor = backgroundColor;
        this.isCompleted = false;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            this.taskDate = sdf.parse(taskDateStr + " " + timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
            this.taskDate = new Date();
        }
    }

    public String getTaskText() {
        return taskText;
    }

    public String getTaskDate() {
        SimpleDateFormat dateSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateSdf.format(taskDate) + " " + timeSdf.format(taskDate);
    }

    public Date getDate() {
        return taskDate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public String getCategory() {
        return category;
    }
}