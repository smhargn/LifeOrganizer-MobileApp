package msku.ceng;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import java.text.SimpleDateFormat;
import android.widget.ArrayAdapter;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import msku.ceng.repository.ShoppingRepository;
import msku.ceng.repository.TaskRepository;

public class ShoppingFragment extends Fragment implements ShoppingListAdapter.OnListInteractionListener {
    private ShoppingListAdapter listAdapter;
    private List<ShoppingList> shoppingLists;
    private List<ShoppingList> filteredLists;
    private Spinner dateFilterSpinner;
    private FirebaseFirestore db;
    private Date selectedDate;
    private ShoppingRepository repository;
    private FirebaseAuth auth;

    private Button dateFilterButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        shoppingLists = new ArrayList<>();
        filteredLists = new ArrayList<>();
        repository = new ShoppingRepository();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping, container, false);

        setupDateFilter(view);
        setupRecyclerView(view);
        setupAddButton(view);


        fetchShoppingLists();

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

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    filterListsByDate(selectedDate.getTime()); // Date nesnesi olarak gönder
                }, year, month, day);

        datePickerDialog.show();
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




                        Button datePickerButton = dialogView.findViewById(R.id.date_picker_button);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        datePickerButton.setText(sdf.format(selectedDate));
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
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    ShoppingList newList = new ShoppingList(listName, iconResId, selectedDate, user.getUid());


                    repository.addShoppingList(newList)
                            .addOnSuccessListener(aVoid -> {
                                shoppingLists.add(0, newList);
                                filterListsByDate(selectedDate); // Tarih filtresi butonuna uygun şekilde değiştirildi
                                listAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {

                                Toast.makeText(requireContext(), "Liste oluşturulurken hata oluştu.", Toast.LENGTH_SHORT).show();
                            });
                }
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
        return R.drawable.ic_movie;
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
                ShoppingItem newItem = new ShoppingItem(itemName, list.getId());
                list.addItem(newItem);

                // Firebase'e de kaydet
                db.collection("users")
                        .document(auth.getCurrentUser().getUid())
                        .collection("shopping_lists")
                        .document(list.getId())
                        .collection("items")
                        .document(newItem.getId())
                        .set(newItem)
                        .addOnSuccessListener(aVoid -> {

                            listAdapter.notifyDataSetChanged();
                        })
                        .addOnFailureListener(e -> {

                            Toast.makeText(requireContext(), "Öğe eklenirken hata oluştu.", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onDeleteList(ShoppingList list) {

        repository.deleteShoppingList(list.getId())
                .addOnSuccessListener(aVoid -> {
                    shoppingLists.remove(list);
                    listAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(requireContext(), "Liste silinirken hata oluştu.", Toast.LENGTH_SHORT).show();
                });
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

                        shoppingLists.clear(); // Eski verileri temizle
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


                                                listAdapter.notifyDataSetChanged();
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
