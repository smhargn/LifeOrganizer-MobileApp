package msku.ceng.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.ZoneId;
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
import msku.ceng.adapter.ShoppingListAdapter;
import msku.ceng.model.IconItem;
import msku.ceng.model.ShoppingItem;
import msku.ceng.model.ShoppingList;
import msku.ceng.repository.ShoppingRepository;

public class ShoppingFragment extends Fragment implements ShoppingListAdapter.OnListInteractionListener {
    private ShoppingListAdapter listAdapter;
    private List<ShoppingList> shoppingLists;
    private List<ShoppingList> filteredLists;
    private Spinner dateFilterSpinner;
    private FirebaseFirestore db;
    private Date selectedDate;
    RecyclerView recyclerView;
    private ShoppingRepository repository;
    private FirebaseAuth auth;
    private ImageView emptyshopping;
    private Date selectedDateForNewList = null;
    private int selectedIconResId = R.drawable.ic_shopping_cart_15017503;

    private Button dateFilterButton;
    private Map<Date, List<String>> taskMap;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        shoppingLists = new ArrayList<>();
        filteredLists = new ArrayList<>();
        repository = new ShoppingRepository();
        auth = FirebaseAuth.getInstance();
        taskMap = new HashMap<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping, container, false);

        setupDateFilter(view);
        setupRecyclerView(view);
        setupAddButton(view);

        fetchShoppingLists();
        checkEmptyState();

        Button openCalendarButton = view.findViewById(R.id.date_filter_button);
        openCalendarButton.setOnClickListener(v -> openCalendar());

        return view;
    }

    private void openCalendar() {
        CalendarBottomSheetDialog calendarDialog = new CalendarBottomSheetDialog(requireContext());
        taskMap = new HashMap<>();
        convertShoppingListsToTaskMap();
        calendarDialog.setTaskMap(taskMap);
        calendarDialog.setOnDateSelectedListener(selectedDate -> {
            filterListsByDate(selectedDate);
            Toast.makeText(requireContext(), "Seçilen Tarih: " + selectedDate, Toast.LENGTH_SHORT).show();
        });
        calendarDialog.show();
    }

    private void openCalendarForPickDate() {
        CalendarBottomSheetDialog calendarDialog = new CalendarBottomSheetDialog(requireContext());

        taskMap = new HashMap<>();
        convertShoppingListsToTaskMap();
        calendarDialog.setTaskMap(taskMap);
        calendarDialog.setOnDateSelectedListener(selectedDate -> {
            selectedDateForNewList = selectedDate;

            showAddListDialog(selectedDateForNewList);
        });

        calendarDialog.show();
    }


    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.shoppingView);
        emptyshopping = view.findViewById(R.id.emptyShopping);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        listAdapter = new ShoppingListAdapter(filteredLists, requireContext(), this);
        recyclerView.setAdapter(listAdapter);
        checkEmptyState();
    }

    private void setupAddButton(View view) {
        view.findViewById(R.id.addShopping).setOnClickListener(v -> showAddListDialog(new Date()));

    }

    private void checkEmptyState() {
        if (shoppingLists.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyshopping.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyshopping.setVisibility(View.GONE);
        }
    }

    private void showDatePicker() {
        openCalendarForPickDate();
    }

    private void filterListsByDate(Date selectedDate) {
        if (selectedDate == null) {
            Log.d("FilterListsByDate", "Selected date is null. Returning early.");
            return;
        }

        Log.d("FilterListsByDate", "Filtering lists for selected date: " + selectedDate.toString());

        filteredLists.clear();
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(selectedDate);

        for (ShoppingList list : shoppingLists) {
            Log.d("FilterListsByDate", "Processing shopping list with ID: " + list.getId());


            Date listDate = list.getCreatedDate();
            if (listDate == null) {
                Log.d("FilterListsByDate", "Shopping list with ID " + list.getId() + " has a null date. Skipping.");
                continue;
            }

            Calendar listCal = Calendar.getInstance();
            listCal.setTime(listDate);

            Log.d("FilterListsByDate", "Comparing list date: " + listDate.toString() + " with selected date: " + selectedDate.toString());


            if (listCal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                    listCal.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR)) {
                Log.d("FilterListsByDate", "Shopping list with ID " + list.getId() + " matches the selected date.");
                filteredLists.add(list);
            } else {
                Log.d("FilterListsByDate", "Shopping list with ID " + list.getId() + " does not match the selected date.");
            }
        }

        Log.d("FilterListsByDate", "Filtered lists count: " + filteredLists.size());

        listAdapter.notifyDataSetChanged();
    }

    private void setupDateFilter(View view) {
        dateFilterButton = view.findViewById(R.id.date_filter_button);
        dateFilterButton.setOnClickListener(v -> showDatePicker());
    }

    private void convertShoppingListsToTaskMap() {
        taskMap.clear();
        for (ShoppingList shoppingList : shoppingLists) {
            if (shoppingList.getCreatedDate() != null) {
                Date listDate = Date.from(shoppingList.getCreatedDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toInstant());

                List<String> taskNames = new ArrayList<>();
                for (ShoppingItem item : shoppingList.getItems()) {
                    taskNames.add(item.getName());
                }

                taskMap.putIfAbsent(listDate, new ArrayList<>());
                taskMap.get(listDate).addAll(taskNames);
            }
        }
        Log.d("TaskMap", "Task map updated: " + taskMap.toString());
    }

    private void filterLists(int filterPosition) {
        filteredLists.clear();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        switch (filterPosition) {
            case 0:
                filteredLists.addAll(shoppingLists);
                break;
            case 1:
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                Date weekStart = cal.getTime();
                for (ShoppingList list : shoppingLists) {
                    if (list.getCreatedDate().after(weekStart)) {
                        filteredLists.add(list);
                    }
                }
                break;
            case 2:
                cal.set(Calendar.DAY_OF_MONTH, 1);
                Date monthStart = cal.getTime();
                for (ShoppingList list : shoppingLists) {
                    if (list.getCreatedDate().after(monthStart)) {
                        filteredLists.add(list);
                    }
                }
                break;
            case 3:
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

    private void showAddListDialog(Date selectedDate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_list, null);
        EditText listNameInput = dialogView.findViewById(R.id.list_name_input);
        Button datePickerButton = dialogView.findViewById(R.id.date_picker_button);
        Button iconPickerButton = dialogView.findViewById(R.id.icon_picker_button);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);
        Button createButton = dialogView.findViewById(R.id.btn_create);


        final int[] selectedIconResId = {R.drawable.ic_shopping_cart_15017503};

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        datePickerButton.setText(sdf.format(selectedDate));

        AlertDialog dialog = builder.setView(dialogView)
                .create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        iconPickerButton.setOnClickListener(v -> {
            AlertDialog.Builder iconBuilder = new AlertDialog.Builder(requireContext());
            View iconDialogView = getLayoutInflater().inflate(R.layout.dialog_select_icon, null);
            RecyclerView iconRecyclerView = iconDialogView.findViewById(R.id.icon_recycler_view);

            List<IconItem> iconList = new ArrayList<>();
            iconList.add(new IconItem(R.drawable.ic_shopping_cart_15017503));
            iconList.add(new IconItem(R.drawable.ic_shopaholic_15018228));
            iconList.add(new IconItem(R.drawable.ic_food_15018697));
            iconList.add(new IconItem(R.drawable.ic_gift_card_15017793));
            iconList.add(new IconItem(R.drawable.ic_shopping_1));
            iconList.add(new IconItem(R.drawable.ic_shopping_2));
            iconList.add(new IconItem(R.drawable.ic_shopping_3));
            iconList.add(new IconItem(R.drawable.ic_shopping_4));
            iconList.add(new IconItem(R.drawable.ic_shopping_5));
            iconList.add(new IconItem(R.drawable.ic_shopping_6));
            iconList.add(new IconItem(R.drawable.ic_shopping_7));
            iconList.add(new IconItem(R.drawable.ic_shopping_8));

            iconRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));
            IconAdapter iconAdapter = new IconAdapter(iconList, requireContext(), icon -> {
                selectedIconResId[0] = icon.getIconResource();
                iconPickerButton.setCompoundDrawablesWithIntrinsicBounds(icon.getIconResource(), 0, 0, 0);
            });

            iconRecyclerView.setAdapter(iconAdapter);

            AlertDialog iconDialog = iconBuilder.setView(iconDialogView).create();
            iconDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            iconDialog.show();
        });

        datePickerButton.setOnClickListener(v -> {
            CalendarBottomSheetDialog calendarDialog = new CalendarBottomSheetDialog(requireContext());
            convertShoppingListsToTaskMap();
            calendarDialog.setTaskMap(taskMap);
            calendarDialog.setOnDateSelectedListener(newDate -> {
                selectedDateForNewList = newDate;
                datePickerButton.setText(sdf.format(newDate));
                calendarDialog.dismiss();
            });
            calendarDialog.show();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        createButton.setOnClickListener(v -> {
            String listName = listNameInput.getText().toString().trim();
            if (!listName.isEmpty() && selectedDateForNewList != null) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    ShoppingList newList = new ShoppingList(listName, selectedIconResId[0], selectedDateForNewList, user.getUid());

                    repository.addShoppingList(newList)
                            .addOnSuccessListener(aVoid -> {
                                shoppingLists.add(0, newList);
                                filterListsByDate(selectedDateForNewList);
                                checkEmptyState();
                                listAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Liste oluşturulurken hata oluştu.", Toast.LENGTH_SHORT).show());
                }
            }
        });

        dialog.show();
    }


    @Override
    public void onAddItemClick(ShoppingList list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);

        EditText itemNameInput = dialogView.findViewById(R.id.item_name_input);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnCreate = dialogView.findViewById(R.id.btn_create);

        AlertDialog dialog = builder.setView(dialogView)
                .create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btnCreate.setOnClickListener(v -> {
            String itemName = itemNameInput.getText().toString().trim();
            if (!itemName.isEmpty()) {
                ShoppingItem newItem = new ShoppingItem(itemName, list.getId());

                ShoppingRepository repository = new ShoppingRepository();
                repository.addItemToShoppingList(list.getId(), newItem)
                        .addOnSuccessListener(aVoid -> {
                            list.addItem(newItem);
                            listAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Öğe eklenirken hata oluştu.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(requireContext(), "Ürün adı boş olamaz!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    public void onListChanged() {
        checkEmptyState();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListExpand(ShoppingList list) {

    }

    public void fetchShoppingLists() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Log.d("FetchShoppingLists", "User found: " + user.getUid());

            repository.getShoppingLists(user.getUid())
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d("FetchShoppingLists", "Shopping lists fetched successfully.");

                        shoppingLists.clear();
                        if (queryDocumentSnapshots != null) {
                            Log.d("FetchShoppingLists", "Query document snapshots not null, processing...");

                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                ShoppingList shoppingList = document.toObject(ShoppingList.class);
                                if (shoppingList != null) {
                                    Log.d("FetchShoppingLists", "Shopping list fetched: " + shoppingList.getId());


                                    repository.getShoppingListItems(shoppingList.getId())
                                            .addOnSuccessListener(itemsSnapshot -> {
                                                List<ShoppingItem> items = new ArrayList<>();
                                                for (QueryDocumentSnapshot itemDocument : itemsSnapshot) {
                                                    ShoppingItem item = itemDocument.toObject(ShoppingItem.class);
                                                    items.add(item);
                                                }

                                                Log.d("FetchShoppingLists", "Items for shopping list " + shoppingList.getId() + ": " + items.size() + " items fetched.");


                                                for (ShoppingItem item : items) {
                                                    shoppingList.addItem(item);
                                                }

                                                shoppingLists.add(shoppingList);
                                                Log.d("FetchShoppingLists", "Shopping list added to general list: " + shoppingList.getId());

                                                Date selectedDate = new Date();

                                                filterListsByDate(selectedDate);
                                                convertShoppingListsToTaskMap();


                                                listAdapter.notifyDataSetChanged();
                                                checkEmptyState();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("FetchShoppingLists", "Failed to fetch shopping list items", e);
                                            });
                                } else {
                                    Log.w("FetchShoppingLists", "Null shopping list found.");
                                }
                            }
                        } else {
                            Log.w("FetchShoppingLists", "Query document snapshots are null.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FetchShoppingLists", "Failed to fetch shopping lists", e);
                    });
        } else {
            Log.w("FetchShoppingLists", "No user found.");
        }
    }
}
