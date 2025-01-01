package msku.ceng;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ShoppingFragment extends Fragment implements ShoppingListAdapter.OnListInteractionListener {
    private ShoppingListAdapter listAdapter;
    private List<ShoppingList> shoppingLists;
    private List<ShoppingList> filteredLists;
    private Spinner dateFilterSpinner;
    private Date selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping, container, false);

        shoppingLists = new ArrayList<>();
        filteredLists = new ArrayList<>();
        setupDateFilter(view);
        setupRecyclerView(view);
        setupAddButton(view);

        return view;
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.shoppingView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        listAdapter = new ShoppingListAdapter(filteredLists, requireContext(), this);
        recyclerView.setAdapter(listAdapter);
    }

    private void setupAddButton(View view) {
        view.findViewById(R.id.addShopping).setOnClickListener(v -> showAddListDialog());
    }

    private void setupDateFilter(View view) {
        dateFilterSpinner = view.findViewById(R.id.dateFilterSpinner);
        String[] filterOptions = {"Tüm Listeler", "Bu Hafta", "Bu Ay", "Bu Yıl"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateFilterSpinner.setAdapter(adapter);

        dateFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterLists(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterLists(int filterPosition) {
        filteredLists.clear();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        switch (filterPosition) {
            case 0: // Tüm Listeler
                filteredLists.addAll(shoppingLists);
                break;
            case 1: // Bu Hafta
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                Date weekStart = cal.getTime();
                for (ShoppingList list : shoppingLists) {
                    if (list.getCreatedDate().after(weekStart)) {
                        filteredLists.add(list);
                    }
                }
                break;
            case 2: // Bu Ay
                cal.set(Calendar.DAY_OF_MONTH, 1);
                Date monthStart = cal.getTime();
                for (ShoppingList list : shoppingLists) {
                    if (list.getCreatedDate().after(monthStart)) {
                        filteredLists.add(list);
                    }
                }
                break;
            case 3: // Bu Yıl
                cal.set(Calendar.DAY_OF_YEAR, 1);
                Date yearStart = cal.getTime();
                for (ShoppingList list : shoppingLists) {
                    if (list.getCreatedDate().after(yearStart)) {
                        filteredLists.add(list);
                    }
                }
                break;
        }
        listAdapter.notifyDataSetChanged();
    }

    private void showAddListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Yeni Alışveriş Listesi");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_list, null);
        EditText listNameInput = dialogView.findViewById(R.id.list_name_input);
        RadioGroup iconGroup = dialogView.findViewById(R.id.icon_group);

        // Date picker button
        dialogView.findViewById(R.id.date_picker_button).setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(year, month, dayOfMonth);
                        selectedDate = selectedCal.getTime();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        builder.setView(dialogView);

        builder.setPositiveButton("Oluştur", (dialog, which) -> {
            String listName = listNameInput.getText().toString().trim();
            if (!listName.isEmpty() && selectedDate != null) {
                int iconResId = getSelectedIcon(iconGroup.getCheckedRadioButtonId());
                ShoppingList newList = new ShoppingList(listName, iconResId, selectedDate);
                shoppingLists.add(0, newList);
                filterLists(dateFilterSpinner.getSelectedItemPosition());
                listAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private int getSelectedIcon(int checkedId) {
        if (checkedId == R.id.icon_grocery) {
            return R.drawable.ic_sport;
        } else if (checkedId == R.id.icon_market) {
            return R.drawable.ic_budget;
        }
        return R.drawable.ic_movie; // default icon
    }

    @Override
    public void onAddItemClick(ShoppingList list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Yeni Ürün Ekle");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        EditText itemNameInput = dialogView.findViewById(R.id.item_name_input);

        builder.setView(dialogView);

        builder.setPositiveButton("Ekle", (dialog, which) -> {
            String itemName = itemNameInput.getText().toString().trim();
            if (!itemName.isEmpty()) {
                ShoppingItem newItem = new ShoppingItem(itemName);
                list.addItem(newItem);
                listAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onDeleteList(ShoppingList list) {
        shoppingLists.remove(list);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListExpand(ShoppingList list) {
        // Expand/collapse işlemi adapter içinde yapılıyor
    }
}