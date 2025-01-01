package msku.ceng;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import yuku.ambilwarna.AmbilWarnaDialog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskFragment extends Fragment {
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private int selectedColor = 0xFFFFFFFF;
    private Button dateFilterButton,categoryFilterButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_taskpage, container, false);
        initializeViews(view);
        return view;
    }

    private void initializeViews(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.taskView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        taskAdapter = new TaskAdapter(taskList, getContext());
        recyclerView.setAdapter(taskAdapter);

        dateFilterButton = view.findViewById(R.id.date_filter_button);
        categoryFilterButton = view.findViewById(R.id.category_filter_button);

        dateFilterButton.setOnClickListener(v -> showDateFilterDialog());
        categoryFilterButton.setOnClickListener(v -> showCategoryFilterDialog());

        FloatingActionButton addButton = view.findViewById(R.id.button3);
        addButton.setOnClickListener(v -> showAddTaskDialog());
    }

    private void showDateFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_date_filter, null);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group_filter);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filter by Date")
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    String filterType = "all";
                    Date selectedDate = new Date();

                    if (selectedId == R.id.radio_today) {
                        filterType = "today";
                        dateFilterButton.setText("Today");
                    } else if (selectedId == R.id.radio_week) {
                        filterType = "week";
                        dateFilterButton.setText("This Week");
                    } else if (selectedId == R.id.radio_month) {
                        filterType = "month";
                        dateFilterButton.setText("This Month");
                    } else if (selectedId == R.id.radio_choose_date) {
                        showDatePicker();
                        return;
                    } else {
                        dateFilterButton.setText("All");
                    }

                    taskAdapter.filterByDate(selectedDate, filterType);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCategoryFilterDialog() {
        String[] categories = getResources().getStringArray(R.array.task_categories);
        String[] allCategories = new String[categories.length + 1];
        allCategories[0] = "All Categories";
        System.arraycopy(categories, 0, allCategories, 1, categories.length);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filter by Category")
                .setItems(allCategories, (dialog, which) -> {
                    String selectedCategory = allCategories[which];
                    categoryFilterButton.setText(selectedCategory);
                    taskAdapter.filterByCategory(selectedCategory);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    dateFilterButton.setText(sdf.format(selectedDate.getTime()));

                    taskAdapter.filterByDate(selectedDate.getTime(), "specific");
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateFilterButtonText(Date date, String filterType) {
        if (filterType.equals("all")) {
            dateFilterButton.setText("All");
        } else if (filterType.equals("today")) {
            dateFilterButton.setText("Today");
        } else if (filterType.equals("week")) {
            dateFilterButton.setText("This Week");
        } else if (filterType.equals("month")) {
            dateFilterButton.setText("This Month");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateFilterButton.setText(sdf.format(date));
        }
    }

    private void showAddTaskDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        setupAddTaskDialog(dialogView);

    }

    private void setupAddTaskDialog(View dialogView) {
        EditText taskInput = dialogView.findViewById(R.id.task_input);
        Spinner categorySpinner = dialogView.findViewById(R.id.category_spinner);
        Button colorPickerButton = dialogView.findViewById(R.id.color_picker_button);

        colorPickerButton.setOnClickListener(v -> showColorPicker(colorPickerButton));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle("Add New Task")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> showDatePickerForNewTask(taskInput, categorySpinner))
                .setNegativeButton("Cancel", null);

        builder.show();
    }

    private void showColorPicker(Button colorPickerButton) {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(requireContext(), selectedColor,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {}

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        selectedColor = color;
                        colorPickerButton.setBackgroundColor(color);
                    }
                });
        colorPicker.show();
    }

    private void showDatePickerForNewTask(EditText taskInput, Spinner categorySpinner) {
        String taskText = taskInput.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();

        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                    showTimePickerForNewTask(taskText, date, category);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePickerForNewTask(String taskText, String date, String category) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    addNewTask(taskText, date, time, category);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // Use 24-hour format
        );
        timePickerDialog.show();
    }

    private void addNewTask(String taskText, String date, String timeStr, String category) {
        Task newTask = new Task(taskText, date, timeStr, category, selectedColor);
        taskList.add(newTask);
        Collections.sort(taskList, (t1, t2) -> t1.getDate().compareTo(t2.getDate()));
        taskAdapter.notifyDataSetChanged();
        taskAdapter.applyFilters();
    }
}