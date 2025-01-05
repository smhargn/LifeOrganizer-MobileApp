package msku.ceng;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import msku.ceng.notification.TaskNotificationManager;
import msku.ceng.repository.TaskRepository;
import yuku.ambilwarna.AmbilWarnaDialog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.app.AlarmManager;
import android.os.Build;
import android.provider.Settings;

public class TaskFragment extends Fragment {
    private RecyclerView taskRecyclerView;
    private FirebaseFirestore db;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private int selectedColor = 0xFFFFFFFF;
    private Button dateFilterButton,categoryFilterButton;
    private TaskRepository taskRepository;
    private TaskNotificationManager notificationManager;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        taskList = new ArrayList<>();
        taskRepository = new TaskRepository();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_taskpage, container, false);
        notificationManager = new TaskNotificationManager(requireContext());

        initializeViews(view);

        // Add alarm permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        setupRecyclerView();
        fetchTasksFromFirebase();




        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
    }

    @Override
    public void onStart() {
        super.onStart();

        setupRecyclerView();

    }

    @Override
    public void onResume() {
        super.onResume();
        setupRecyclerView();
    }



    private void initializeViews(View view) {
        taskRecyclerView = view.findViewById(R.id.taskView);
        dateFilterButton = view.findViewById(R.id.date_filter_button);
        categoryFilterButton = view.findViewById(R.id.category_filter_button);


        dateFilterButton.setOnClickListener(v -> showDateFilterDialog());
        categoryFilterButton.setOnClickListener(v -> showCategoryFilterDialog());

        FloatingActionButton addButton = view.findViewById(R.id.button3);
        addButton.setOnClickListener(v -> showAddTaskDialog());
    }

    private void setupRecyclerView() {
        if (taskAdapter == null) {
            taskAdapter = new TaskAdapter(taskList, requireContext());
            taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            taskRecyclerView.setAdapter(taskAdapter);
        }

        fetchTasksFromFirebase();
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
                true
        );
        timePickerDialog.show();
    }

    private void addNewTask(String taskText, String date, String timeStr, String category) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        if (user == null) {
            Toast.makeText(getContext(), "Lütfen önce oturum açın.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String taskId = db.collection("users").document(userId)
                .collection("tasks").document().getId();


        Task newTask = new Task(taskId, taskText, date, timeStr, category, selectedColor, userId);


        db.collection("users").document(userId)
        .collection("tasks").document(taskId).set(newTask)
                .addOnSuccessListener(aVoid -> {
                    taskList.add(newTask);
                    Collections.sort(taskList, (t1, t2) -> t1.getTaskDate().compareTo(t2.getTaskDate()));
                    taskAdapter.notifyDataSetChanged();
                    taskAdapter.applyFilters();
                    fetchTasksFromFirebase();
                    notificationManager.scheduleTaskNotification(newTask);
                    Toast.makeText(getContext(), "Görev başarıyla eklendi!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Görev eklenemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchTasksFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users").document(userId)
        .collection("tasks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    taskList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Task task = document.toObject(Task.class);
                        if (task != null) {
                            task.setId(document.getId());
                            taskList.add(task);
                        }
                    }
                    Collections.sort(taskList, (t1, t2) -> t1.getTaskDate().compareTo(t2.getTaskDate()));
                    taskAdapter.notifyDataSetChanged();
                    taskAdapter.applyFilters();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}