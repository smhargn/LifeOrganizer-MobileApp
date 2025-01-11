package msku.ceng.view;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
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

import msku.ceng.CalendarBottomSheetDialog;
import msku.ceng.R;
import msku.ceng.adapter.TaskAdapter;
import msku.ceng.model.Task;
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
import java.util.HashMap;
import java.util.Map;

public class TaskFragment extends Fragment {
    private RecyclerView taskRecyclerView;
    private FirebaseFirestore db;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private int selectedColor = 0xFFFFFFFF;
    private Button dateFilterButton,categoryFilterButton;
    private TaskRepository taskRepository;
    private TaskNotificationManager notificationManager;
    private ImageView emptyStateImage;


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

        // ALARM PERMISSIN DEVICE
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
        emptyStateImage = view.findViewById(R.id.emptyTask);


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
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnApply = dialogView.findViewById(R.id.btn_apply);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnApply.setOnClickListener(v -> {
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
                dialog.dismiss();
                showCalendarForFilter();
                return;
            } else {
                dateFilterButton.setText("All");
            }

            taskAdapter.filterByDate(selectedDate, filterType);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showCalendarForFilter() {
        CalendarBottomSheetDialog calendarDialog = new CalendarBottomSheetDialog(requireContext());
        calendarDialog.setOnDateSelectedListener(selectedDate -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateFilterButton.setText(sdf.format(selectedDate));
            taskAdapter.filterByDate(selectedDate, "specific");
            calendarDialog.dismiss();
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            taskRepository.getUserTasks(currentUser.getUid())
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Map<Date, List<String>> taskMap = new HashMap<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Task task = document.toObject(Task.class);
                            if (task != null) {
                                try {
                                    SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    Date taskDate = parser.parse(task.getTaskDate());
                                    List<String> tasksForDate = taskMap.getOrDefault(taskDate, new ArrayList<>());
                                    tasksForDate.add(task.getTaskText());
                                    taskMap.put(taskDate, tasksForDate);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        calendarDialog.setTaskMap(taskMap);
                    });
        }

        calendarDialog.show();
    }

    private void showCategoryFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_category_filter, null);
        ListView categoryList = dialogView.findViewById(R.id.category_list);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnApply = dialogView.findViewById(R.id.btn_apply);

        String[] categories = getResources().getStringArray(R.array.task_categories);
        String[] allCategories = new String[categories.length + 1];
        allCategories[0] = "All Categories";
        System.arraycopy(categories, 0, allCategories, 1, categories.length);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_single_choice, allCategories) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.parseColor("#495057"));

                //RadioButton radioButton = (RadioButton) view.findViewById(R.id.radio_button);
                //radioButton.setButtonTintList(ContextCompat.getColorStateList(getContext(),R.color.radio_button_color));

                return view;
            }
        };

        categoryList.setAdapter(adapter);
        categoryList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnApply.setOnClickListener(v -> {
            int position = categoryList.getCheckedItemPosition();
            if (position != ListView.INVALID_POSITION) {
                String selectedCategory = allCategories[position];
                categoryFilterButton.setText(selectedCategory);
                taskAdapter.filterByCategory(selectedCategory);
            }
            dialog.dismiss();
        });

        dialog.show();
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

    private void checkEmptyState() {
        if (taskList.isEmpty()) {
            taskRecyclerView.setVisibility(View.GONE);
            emptyStateImage.setVisibility(View.VISIBLE);
        } else {
            taskRecyclerView.setVisibility(View.VISIBLE);
            emptyStateImage.setVisibility(View.GONE);
        }
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
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button addButton = dialogView.findViewById(R.id.add_button);

        colorPickerButton.setOnClickListener(v -> showColorPicker(colorPickerButton));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        addButton.setOnClickListener(v -> {
            showDatePickerForNewTask(taskInput, categorySpinner);
            dialog.dismiss();
        });

        dialog.show();
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

        CalendarBottomSheetDialog calendarDialog = new CalendarBottomSheetDialog(requireContext());
        calendarDialog.setOnDateSelectedListener(selectedDate -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String date = sdf.format(selectedDate);
            showTimePickerForNewTask(taskText, date, category);
            calendarDialog.dismiss();
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            taskRepository.getUserTasks(currentUser.getUid())
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Map<Date, List<String>> taskMap = new HashMap<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Task task = document.toObject(Task.class);
                            if (task != null) {
                                try {
                                    SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    Date taskDate = parser.parse(task.getTaskDate());
                                    List<String> tasksForDate = taskMap.getOrDefault(taskDate, new ArrayList<>());
                                    tasksForDate.add(task.getTaskText());
                                    taskMap.put(taskDate, tasksForDate);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        calendarDialog.setTaskMap(taskMap);
                    });
        }

        calendarDialog.show();
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
        String taskId = taskRepository.generateTaskId(userId);

        Task newTask = new Task(taskId, taskText, date, timeStr, category, selectedColor, userId);

        taskRepository.addTask(userId, newTask)
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
        taskRepository.getUserTasks(userId)
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
                    checkEmptyState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}