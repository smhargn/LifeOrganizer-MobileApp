package msku.ceng;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Task {

    private String taskText;
    private Date taskDate;

    public Task(String taskText, String taskDateStr) {
        this.taskText = taskText;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // Locale eklendi
        try {
            this.taskDate = sdf.parse(taskDateStr);  // Tarihi Date türüne dönüştürüyoruz
        } catch (ParseException e) {
            e.printStackTrace();
            this.taskDate = new Date();  // Hata durumunda bugünün tarihi
        }
    }

    public String getTaskText() {
        return taskText;
    }

    public String getTaskDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // Locale eklendi
        return sdf.format(taskDate);  // Tarihi String olarak döndürüyoruz
    }

    public Date getDate() {
        return taskDate;
    }
}