package msku.ceng;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import msku.ceng.placeholder.PlaceholderContent.PlaceholderItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private final List<Task> mTaskList;
    private Context mContext;


    public TaskAdapter(List<Task> tasks, Context context) {
        mTaskList = tasks;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // item_task.xml dosyasını şişiriyoruz
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Task taskEntry = mTaskList.get(position);

        // Görev metnini set ediyoruz
        holder.taskText.setText(taskEntry.getTaskText());

        // Tarih bilgisini set ediyoruz
        holder.taskDate.setText(taskEntry.getTaskDate());

        // Silme butonunun işlevini ekliyoruz
        holder.deleteButton.setOnClickListener(v -> {
            mTaskList.remove(position);  // Görevi listeden çıkarıyoruz
            notifyItemRemoved(position);  // RecyclerView'ı güncelliyoruz
        });
    }

    @Override
    public int getItemCount() {
        return mTaskList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView taskText;
        public TextView taskDate;
        public ImageButton deleteButton;


        public ViewHolder(View itemView) {
            super(itemView);
            taskText = itemView.findViewById(R.id.task_text);
            taskDate = itemView.findViewById(R.id.task_date);
            deleteButton = itemView.findViewById(R.id.delete_button);

        }

    }
}
