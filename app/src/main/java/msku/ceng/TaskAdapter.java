package msku.ceng;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private final List<Task> mTaskList;
    private final List<Task> mFilteredList;
    private Context mContext;
    private String currentCategory = "All Categories";
    private String currentDateFilter = "all";
    private Date currentDate = new Date();

    public TaskAdapter(List<Task> tasks, Context context) {
        mTaskList = tasks;
        mFilteredList = new ArrayList<>(tasks);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Task taskEntry = mFilteredList.get(position);

        holder.taskText.setText(taskEntry.getTaskText());
        holder.taskDate.setText(taskEntry.getTaskDate());
        holder.categoryText.setText(taskEntry.getCategory());
        holder.checkBox.setChecked(taskEntry.isCompleted());
        holder.cardView.setCardBackgroundColor(taskEntry.getBackgroundColor());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            taskEntry.setCompleted(isChecked);
        });

        holder.deleteButton.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                mTaskList.remove(taskEntry);
                mFilteredList.remove(pos);
                notifyItemRemoved(pos);
            }
        });
    }

    public void applyFilters() {
        mFilteredList.clear();
        List<Task> tempList = new ArrayList<>(mTaskList);

        // Apply category filter
        if (!"All Categories".equals(currentCategory)) {
            tempList.removeIf(task -> !task.getCategory().equals(currentCategory));
        }

        // Apply date filter
        if (!"all".equals(currentDateFilter)) {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(currentDate);
            Calendar taskCal = Calendar.getInstance();

            tempList.removeIf(task -> {
                taskCal.setTime(task.getDate());
                switch (currentDateFilter) {
                    case "today":
                        return !isSameDay(taskCal, selectedCal);
                    case "week":
                        return !isSameWeek(taskCal, selectedCal);
                    case "month":
                        return !isSameMonth(taskCal, selectedCal);
                    case "specific":
                        return !isSameDay(taskCal, selectedCal);
                    default:
                        return false;
                }
            });
        }

        mFilteredList.addAll(tempList);
        notifyDataSetChanged();
    }

    public void filterByCategory(String category) {
        this.currentCategory = category;
        applyFilters();
    }

    public void filterByDate(Date selectedDate, String filterType) {
        this.currentDate = selectedDate;
        this.currentDateFilter = filterType;
        applyFilters();
    }



    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }



    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isSameWeek(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
    }

    private boolean isSameMonth(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }

    public String getCurrentDateFilter() {
        return currentDateFilter;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public String getCurrentCategory() {
        return currentCategory;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView taskText;
        public TextView taskDate;
        public TextView categoryText;
        public ImageButton deleteButton;
        public CheckBox checkBox;
        public CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            taskText = itemView.findViewById(R.id.task_text);
            taskDate = itemView.findViewById(R.id.task_date);
            categoryText = itemView.findViewById(R.id.category_text);
            deleteButton = itemView.findViewById(R.id.delete_button);
            checkBox = itemView.findViewById(R.id.task_checkbox);
            cardView = itemView.findViewById(R.id.task_card);
        }
    }
}