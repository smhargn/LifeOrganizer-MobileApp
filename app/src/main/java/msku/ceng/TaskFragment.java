package msku.ceng;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.widget.ImageView;

public class TaskFragment extends Fragment {

    private TaskAdapter toDoListAdapter;
    private ImageView imageView;
    private List<Task> taskList = new ArrayList<>(); // Görev listesi oluşturuldu


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Fragment'in layout dosyasını şişiriyoruz
        View view = inflater.inflate(R.layout.activity_taskpage, container, false);

        // ImageView'i bulun


        RecyclerView recyclerView = view.findViewById(R.id.taskView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        toDoListAdapter = new TaskAdapter(taskList, getContext()); // Adapter'ı bağladık
        recyclerView.setAdapter(toDoListAdapter);

        ImageButton taskAddButton = view.findViewById(R.id.button3);
        taskAddButton.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Yeni Görev Ekle");

            final EditText input = new EditText(requireContext());
            input.setHint("Görev adı");

            builder.setView(input);

            builder.setPositiveButton("EKLE", (dialog, which) -> {
                String taskName = input.getText().toString().trim();

                if (!taskName.isEmpty()) {
                    // Tarih seçimi için DatePickerDialog açalım
                    DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view2, year, month, dayOfMonth) -> {
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;

                        // Task nesnesini oluşturuyoruz
                        Task newTask = new Task(taskName, selectedDate); // Görev adı ve tarih ekleniyor
                        taskList.add(newTask); // Listeye yeni görev ekliyoruz

                        // Tarihe göre sıralama yapıyoruz
                        Collections.sort(taskList, (task1, task2) -> task1.getDate().compareTo(task2.getDate()));

                        toDoListAdapter.notifyDataSetChanged(); // RecyclerView'ı güncelliyoruz
                    }, 2024, 11, 9); // Başlangıç tarihi, örneğin bugünü alabiliriz
                    datePickerDialog.show();
                }
            });

            builder.setNegativeButton("İPTAL", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        return view;
    }
}
