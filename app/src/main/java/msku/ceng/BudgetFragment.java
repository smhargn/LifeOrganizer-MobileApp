package msku.ceng;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BudgetFragment extends Fragment implements AddTransactionDialogFragment.OnTransactionAddedListener {
    private RecyclerView recyclerView;
    private BudgetAdapter adapter;
    private List<Budget> budgetList;
    private List<Budget> filteredList;
    private TextView totalBalanceText;
    private TextView totalIncomeText;
    private TextView totalExpenseText;
    private Button addTransactionButton;
    private Button buttonFilterAll;
    private Button buttonFilterIncome;
    private Button buttonFilterExpense;
    private Button buttonSelectDate;
    private Calendar selectedDate = null;
    private String currentFilter = "all";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewBudget);
        totalBalanceText = view.findViewById(R.id.textTotalBalance);
        totalIncomeText = view.findViewById(R.id.textTotalIncome);
        totalExpenseText = view.findViewById(R.id.textTotalExpense);
        addTransactionButton = view.findViewById(R.id.buttonAddTransaction);
        buttonFilterAll = view.findViewById(R.id.buttonFilterAll);
        buttonFilterIncome = view.findViewById(R.id.buttonFilterIncome);
        buttonFilterExpense = view.findViewById(R.id.buttonFilterExpense);
        buttonSelectDate = view.findViewById(R.id.buttonSelectDate);

        budgetList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new BudgetAdapter(getContext(), filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        setupListeners();
        applyFilters();

        return view;
    }

    private void setupListeners() {
        addTransactionButton.setOnClickListener(v -> showAddTransactionDialog());

        buttonFilterAll.setOnClickListener(v -> {
            currentFilter = "all";
            applyFilters();
        });

        buttonFilterIncome.setOnClickListener(v -> {
            currentFilter = "income";
            applyFilters();
        });

        buttonFilterExpense.setOnClickListener(v -> {
            currentFilter = "expense";
            applyFilters();
        });

        buttonSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void showAddTransactionDialog() {
        AddTransactionDialogFragment dialog = new AddTransactionDialogFragment();
        dialog.setOnTransactionAddedListener(this);
        dialog.show(getChildFragmentManager(), "AddTransaction");
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    buttonSelectDate.setText(String.format(Locale.getDefault(),
                            "Tarih: %02d/%02d/%d", dayOfMonth, monthOfYear + 1, year1));
                    applyFilters();
                }, year, month, day);

        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, "Filtreyi Kaldır",
                (dialog, which) -> {
                    selectedDate = null;
                    buttonSelectDate.setText("Tarihe Göre Filtrele");
                    applyFilters();
                });

        datePickerDialog.show();
    }

    private void applyFilters() {
        filteredList.clear();
        List<Budget> tempList = new ArrayList<>(budgetList);

        // Apply date filter
        if (selectedDate != null) {
            tempList = tempList.stream()
                    .filter(budget -> {
                        try {
                            Date budgetDate = dateFormat.parse(budget.getDate());
                            Date filterDate = dateFormat.parse(dateFormat.format(selectedDate.getTime()));
                            return budgetDate.equals(filterDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        // Apply type filter
        if (!currentFilter.equals("all")) {
            tempList = tempList.stream()
                    .filter(budget -> budget.getType().equals(currentFilter))
                    .collect(Collectors.toList());
        }

        filteredList.addAll(tempList);
        adapter.notifyDataSetChanged();
        updateBalances();
    }

    private void updateBalances() {
        double totalIncome = 0;
        double totalExpense = 0;
        double totalBalance = 0;

        // Calculate totals from all transactions
        for (Budget budget : budgetList) {
            if (budget.getType().equals("income")) {
                totalIncome += budget.getAmount();
            } else {
                totalExpense += budget.getAmount();
            }
        }
        totalBalance = totalIncome - totalExpense;

        // Update the UI
        totalIncomeText.setText(String.format("Toplam Gelir: %.2f TL", totalIncome));
        totalExpenseText.setText(String.format("Toplam Gider: %.2f TL", totalExpense));
        totalBalanceText.setText(String.format("Genel Bakiye: %.2f TL", totalBalance));
    }

    @Override
    public void onTransactionAdded(Budget budget) {
        budgetList.add(budget);
        applyFilters();
    }


}