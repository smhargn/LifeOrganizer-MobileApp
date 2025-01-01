package msku.ceng;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BudgetFragment extends Fragment implements AddTransactionDialogFragment.OnTransactionAddedListener {
    // UI Components
    private RecyclerView recyclerView;
    private TextView totalBalanceText;
    private TextView totalIncomeText;
    private TextView totalExpenseText;
    private FloatingActionButton addTransactionButton;
    private Button buttonFilterAll, buttonFilterIncome, buttonFilterExpense;
    private Button buttonFilterDay, buttonFilterWeek, buttonFilterMonth, buttonFilterYear;

    private Button addPlanButton;
    private RecyclerView plansRecyclerView;
    private PlanAdapter planAdapter;
    private List<Plan> plansList = new ArrayList<>();

    // Data
    private List<Budget> budgetList;
    private List<Budget> filteredList;
    private BudgetAdapter adapter;
    private Calendar selectedDate = null;
    private String currentFilter = "all";
    private String currentPeriod = "day";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        initializeData();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewBudget);
        plansRecyclerView = view.findViewById(R.id.plansRecyclerView);

        totalBalanceText = view.findViewById(R.id.textTotalBalance);
        totalIncomeText = view.findViewById(R.id.textTotalIncome);
        totalExpenseText = view.findViewById(R.id.textTotalExpense);
        addTransactionButton = view.findViewById(R.id.buttonAddTransaction);

        // Filter buttons
        buttonFilterAll = view.findViewById(R.id.buttonFilterAll);
        buttonFilterIncome = view.findViewById(R.id.buttonFilterIncome);
        buttonFilterExpense = view.findViewById(R.id.buttonFilterExpense);

        // Period buttons
        buttonFilterDay = view.findViewById(R.id.buttonFilterDay);
        buttonFilterWeek = view.findViewById(R.id.buttonFilterWeek);
        buttonFilterMonth = view.findViewById(R.id.buttonFilterMonth);
        buttonFilterYear = view.findViewById(R.id.buttonFilterYear);

        addPlanButton = view.findViewById(R.id.addPlanButton);

    }

    private void setupRecyclerView() {
        // Existing budget setup
        budgetList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new BudgetAdapter(getContext(), filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Plans setup with fixed height
        planAdapter = new PlanAdapter(getContext(), plansList, this::showPlanDepositDialog);
        LinearLayoutManager plansLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        plansRecyclerView.setLayoutManager(plansLayoutManager);
        plansRecyclerView.setAdapter(planAdapter);

        // Set a fixed height for the plans RecyclerView to prevent collapsing
        ViewGroup.LayoutParams params = plansRecyclerView.getLayoutParams();
        params.height = getResources().getDimensionPixelSize(R.dimen.plan_card_height);
        plansRecyclerView.setLayoutParams(params);
    }

    private void setupListeners() {
        addTransactionButton.setOnClickListener(v -> showAddTransactionDialog());

        // Type filter listeners
        buttonFilterAll.setOnClickListener(v -> {
            updateTypeFilter("all");
            updateFilterButtonStyles();
        });

        buttonFilterIncome.setOnClickListener(v -> {
            updateTypeFilter("income");
            updateFilterButtonStyles();
        });

        buttonFilterExpense.setOnClickListener(v -> {
            updateTypeFilter("expense");
            updateFilterButtonStyles();
        });

        // Period filter listeners
        buttonFilterDay.setOnClickListener(v -> {
            updatePeriodFilter("day");
            updatePeriodButtonStyles();
        });

        buttonFilterWeek.setOnClickListener(v -> {
            updatePeriodFilter("week");
            updatePeriodButtonStyles();
        });

        buttonFilterMonth.setOnClickListener(v -> {
            updatePeriodFilter("month");
            updatePeriodButtonStyles();
        });

        buttonFilterYear.setOnClickListener(v -> {
            updatePeriodFilter("year");
            updatePeriodButtonStyles();
        });

        addPlanButton.setOnClickListener(v -> showAddPlanDialog());
    }

    private void initializeData() {
        // Initialize with default filters
        updateTypeFilter("all");
        updatePeriodFilter("day");
        applyFilters();
    }

    private void updateTypeFilter(String filter) {
        currentFilter = filter;
        applyFilters();
    }

    private void updatePeriodFilter(String period) {
        currentPeriod = period;
        applyFilters();
    }

    private void updateFilterButtonStyles() {
        buttonFilterAll.setBackgroundResource(currentFilter.equals("all") ?
                R.drawable.filter_button_selected : R.drawable.filter_button_background);
        buttonFilterIncome.setBackgroundResource(currentFilter.equals("income") ?
                R.drawable.filter_button_selected : R.drawable.filter_button_background);
        buttonFilterExpense.setBackgroundResource(currentFilter.equals("expense") ?
                R.drawable.filter_button_selected : R.drawable.filter_button_background);

        // Update text colors
        buttonFilterAll.setTextColor(getResources().getColor(currentFilter.equals("all") ?
                android.R.color.white : R.color.gradient_budget_start));
        buttonFilterIncome.setTextColor(getResources().getColor(currentFilter.equals("income") ?
                android.R.color.white : R.color.gradient_budget_start));
        buttonFilterExpense.setTextColor(getResources().getColor(currentFilter.equals("expense") ?
                android.R.color.white : R.color.gradient_budget_start));
    }

    private void updatePeriodButtonStyles() {
        buttonFilterDay.setBackgroundResource(currentPeriod.equals("day") ?
                R.drawable.filter_button_selected : R.drawable.filter_button_background);
        buttonFilterWeek.setBackgroundResource(currentPeriod.equals("week") ?
                R.drawable.filter_button_selected : R.drawable.filter_button_background);
        buttonFilterMonth.setBackgroundResource(currentPeriod.equals("month") ?
                R.drawable.filter_button_selected : R.drawable.filter_button_background);
        buttonFilterYear.setBackgroundResource(currentPeriod.equals("year") ?
                R.drawable.filter_button_selected : R.drawable.filter_button_background);

        // Update text colors
        buttonFilterDay.setTextColor(getResources().getColor(currentPeriod.equals("day") ?
                android.R.color.white : R.color.gradient_budget_start));
        buttonFilterWeek.setTextColor(getResources().getColor(currentPeriod.equals("week") ?
                android.R.color.white : R.color.gradient_budget_start));
        buttonFilterMonth.setTextColor(getResources().getColor(currentPeriod.equals("month") ?
                android.R.color.white : R.color.gradient_budget_start));
        buttonFilterYear.setTextColor(getResources().getColor(currentPeriod.equals("year") ?
                android.R.color.white : R.color.gradient_budget_start));
    }


    private void showAddPlanDialog() {
        AddPlanDialogFragment dialog = new AddPlanDialogFragment();
        dialog.setOnPlanAddedListener(plan -> {
            plansList.add(plan);
            planAdapter.notifyDataSetChanged();
        });
        dialog.show(getChildFragmentManager(), "AddPlan");
    }

    private void showPlanDepositDialog(Plan plan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_deposit_plan, null);

        TextView titleText = view.findViewById(R.id.planTitle);
        TextView remainingText = view.findViewById(R.id.remainingAmount);
        EditText amountEdit = view.findViewById(R.id.depositAmount);

        titleText.setText(plan.getTitle());
        remainingText.setText(String.format(Locale.getDefault(),
                "Kalan: ₺%.2f", plan.getRemainingAmount()));

        builder.setView(view)
                .setTitle("Para Biriktir")
                .setPositiveButton("Ekle", (dialog, which) -> {
                    try {
                        double amount = Double.parseDouble(amountEdit.getText().toString());
                        plan.deposit(amount);
                        planAdapter.notifyDataSetChanged();
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(),
                                "Lütfen geçerli bir miktar girin",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void showAddTransactionDialog() {
        AddTransactionDialogFragment dialog = new AddTransactionDialogFragment();
        dialog.setOnTransactionAddedListener(this);
        dialog.show(getChildFragmentManager(), "AddTransaction");
    }

    private void applyFilters() {
        filteredList.clear();
        List<Budget> tempList = new ArrayList<>(budgetList);

        // Apply period filter
        Calendar periodStart = Calendar.getInstance();
        switch (currentPeriod) {

            case "day":
                periodStart.set(Calendar.HOUR_OF_DAY, 0);
                periodStart.set(Calendar.MINUTE, 0);
                periodStart.set(Calendar.SECOND, 0);
                periodStart.set(Calendar.MILLISECOND, 0);
                break;
            case "week":
                periodStart.add(Calendar.DAY_OF_MONTH, -7);
                break;
            case "month":
                periodStart.add(Calendar.MONTH, -1);
                break;
            case "year":
                periodStart.add(Calendar.YEAR, -1);
                break;
        }

        // Filter by period
        tempList = tempList.stream()
                .filter(budget -> {
                    try {
                        Date transactionDate = dateFormat.parse(budget.getDate());
                        return !transactionDate.before(periodStart.getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .collect(Collectors.toList());

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

        for (Budget budget : budgetList) {
            if (budget.getType().equals("income")) {
                totalIncome += budget.getAmount();
            } else {
                totalExpense += budget.getAmount();
            }
        }

        double totalBalance = totalIncome - totalExpense;

        // Format currency values
        String formattedBalance = String.format(Locale.getDefault(), "%.2f ₺", totalBalance);
        String formattedIncome = String.format(Locale.getDefault(), "%.2f ₺", totalIncome);
        String formattedExpense = String.format(Locale.getDefault(), "%.2f ₺", totalExpense);

        // Update UI
        totalBalanceText.setText(formattedBalance);
        totalIncomeText.setText(formattedIncome);
        totalExpenseText.setText(formattedExpense);
    }

    @Override
    public void onTransactionAdded(Budget budget) {
        budgetList.add(budget);
        applyFilters();

        // Make sure plans are still visible
        if (planAdapter != null && !plansList.isEmpty()) {
            planAdapter.notifyDataSetChanged();
        }
    }
}