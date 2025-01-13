package msku.ceng.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import msku.ceng.CalendarBottomSheetDialog;
import msku.ceng.R;
import msku.ceng.adapter.IconAdapter;
import msku.ceng.model.Budget;
import msku.ceng.model.IconItem;

public class AddTransactionDialogFragment extends DialogFragment {
    private OnTransactionAddedListener listener;
    private int selectedIconResourceId = -1;
    private EditText dateEdit;
    private Calendar selectedDate;
    private FirebaseFirestore db;
    private Map<Date, List<String>> taskMap = new HashMap<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

    }

    public interface OnTransactionAddedListener {
        void onTransactionAdded(Budget budget);
    }

    public void setOnTransactionAddedListener(OnTransactionAddedListener listener) {
        this.listener = listener;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_transaction, null);

        EditText amountEdit = view.findViewById(R.id.editTextAmount);
        EditText descriptionEdit = view.findViewById(R.id.editTextDescription);
        EditText categoryEdit = view.findViewById(R.id.editTextCategory);
        dateEdit = view.findViewById(R.id.editTextDate);
        RadioGroup typeGroup = view.findViewById(R.id.radioGroupType);
        Button saveButton = view.findViewById(R.id.buttonSave);
        Button cancelButton = view.findViewById(R.id.buttonCancel);

        Button iconPickerButton = view.findViewById(R.id.buttonSelectIcon);

        iconPickerButton.setOnClickListener(v -> {
            AlertDialog.Builder iconBuilder = new AlertDialog.Builder(requireContext());
            View iconDialogView = getLayoutInflater().inflate(R.layout.dialog_select_icon, null);
            RecyclerView iconRecyclerView = iconDialogView.findViewById(R.id.icon_recycler_view);

            List<IconItem> iconList = new ArrayList<>();
            iconList.add(new IconItem(R.drawable.ic_budget_1));
            iconList.add(new IconItem(R.drawable.ic_budget_2));
            iconList.add(new IconItem(R.drawable.ic_budget_3));
            iconList.add(new IconItem(R.drawable.ic_budget_4));
            iconList.add(new IconItem(R.drawable.ic_budget_5));
            iconList.add(new IconItem(R.drawable.ic_budget_6));
            iconList.add(new IconItem(R.drawable.ic_budget_7));
            iconList.add(new IconItem(R.drawable.ic_budget_8));
            iconList.add(new IconItem(R.drawable.ic_budget_9));
            iconList.add(new IconItem(R.drawable.ic_budget_10));
            iconList.add(new IconItem(R.drawable.ic_budget_11));
            iconList.add(new IconItem(R.drawable.ic_budget_12));

            iconRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));
            IconAdapter iconAdapter = new IconAdapter(iconList, requireContext(), icon -> {
                selectedIconResourceId = icon.getIconResource();
                iconPickerButton.setText("");
                iconPickerButton.setCompoundDrawablesWithIntrinsicBounds(0, icon.getIconResource(), 0, 0);
                iconPickerButton.setPadding(0, 0, 0, 0);
            });

            iconRecyclerView.setAdapter(iconAdapter);


            AlertDialog iconDialog = iconBuilder.setView(iconDialogView).create();
            iconDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            iconDialog.show();
        });

        selectedDate = Calendar.getInstance();
        updateDateDisplay();
        dateEdit.setOnClickListener(v -> showDatePicker());


        AlertDialog dialog = builder.setView(view)
                .create();

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        saveButton.setOnClickListener(v -> {
            if (selectedIconResourceId == -1) {
                selectedIconResourceId = R.mipmap.othericon_foreground;
            }

            try {
                double amount = Double.parseDouble(amountEdit.getText().toString());
                String description = descriptionEdit.getText().toString();
                String category = categoryEdit.getText().toString();
                String type = typeGroup.getCheckedRadioButtonId() == R.id.radioIncome ? "income" : "expense";

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String date = sdf.format(selectedDate.getTime());
                String userId = user.getUid();
                String id = db.collection("users").document(userId)
                        .collection("budgets").document().getId();

                updateTaskMap(selectedDate.getTime(), description);

                Budget newBudget = new Budget(id, amount, description, category, date, type, selectedIconResourceId);
                Log.d("AddBudget : ",newBudget.getId());


                if (listener != null) {
                    listener.onTransactionAdded(newBudget);
                }
                dialog.dismiss();
            } catch (NumberFormatException e) {
                amountEdit.setError("Geçerli bir miktar giriniz");
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    private void showDatePicker() {
        CalendarBottomSheetDialog calendarDialog = new CalendarBottomSheetDialog(requireContext());


        calendarDialog.setOnDateSelectedListener(date -> {
            selectedDate.setTime(date);
            updateDateDisplay();
            calendarDialog.dismiss();
        });
        calendarDialog.setTaskMap(taskMap);
        calendarDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateEdit.setText(sdf.format(selectedDate.getTime()));
    }

    private void updateTaskMap(Date date, String task) {
        Log.d("Calendar","Girildi ");
        Log.d("Calendar", String.valueOf(date));
        Log.d("Calendar",task);
        List<String> tasks = taskMap.getOrDefault(date, new ArrayList<>());
        tasks.add(task);
        Log.d("Calendar", String.valueOf(tasks));
        taskMap.put(date, tasks);


    }


}