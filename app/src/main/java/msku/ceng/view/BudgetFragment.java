package msku.ceng.view;

import static android.util.Log.*;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import msku.ceng.CalendarBottomSheetDialog;
import msku.ceng.R;
import msku.ceng.adapter.BudgetAdapter;
import msku.ceng.adapter.PlanAdapter;
import msku.ceng.model.Budget;
import msku.ceng.model.Plan;
import msku.ceng.repository.BudgetRepository;

public class BudgetFragment extends Fragment implements AddTransactionDialogFragment.OnTransactionAddedListener, BudgetAdapter.OnBudgetClickListener {
    private RecyclerView recyclerView;
    private TextView totalBalanceText;
    private TextView totalIncomeText;
    private TextView totalExpenseText;
    private FloatingActionButton addTransactionButton;
    private Button buttonFilterAll, buttonFilterIncome, buttonFilterExpense;
    private Button buttonFilterDay, buttonFilterWeek, buttonFilterMonth, buttonFilterYear,buttonFilterChooseDate;

    private Button addPlanButton;
    private RecyclerView plansRecyclerView;
    private PlanAdapter planAdapter;
    private List<Plan> plansList = new ArrayList<>();

    private List<Budget> budgetList;
    private List<Budget> filteredList;
    private BudgetAdapter adapter;
    private Calendar selectedDate = null;
    private String currentFilter = "all";
    private String currentPeriod = "day";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private BudgetRepository budgetRepository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        initializeData();
        fetchBudgetsFromFirestore();
        fetchPlansFromFirestore();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        budgetRepository = new BudgetRepository();
    }

    @Override
    public void onBudgetClick(Budget budget, int position) {
        showEditDialog(budget, position);
    }

    private void showEditDialog(Budget budget, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_transaction, null);

        EditText amountEdit = view.findViewById(R.id.editTextAmount);
        EditText descriptionEdit = view.findViewById(R.id.editTextDescription);
        EditText categoryEdit = view.findViewById(R.id.editTextCategory);
        Button deleteButton = view.findViewById(R.id.buttonDelete);
        Button updateButton = view.findViewById(R.id.buttonUpdate);

        amountEdit.setText(String.valueOf(budget.getAmount()));
        descriptionEdit.setText(budget.getDescription());
        categoryEdit.setText(budget.getCategory());

        AlertDialog dialog = builder.setView(view)
                .create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        deleteButton.setOnClickListener(v -> {
            deleteBudget(budget, position);
            dialog.dismiss();
        });

        updateButton.setOnClickListener(v -> {
            updateBudget(budget, position,
                    Double.parseDouble(amountEdit.getText().toString()),
                    descriptionEdit.getText().toString(),
                    categoryEdit.getText().toString());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewBudget);
        plansRecyclerView = view.findViewById(R.id.plansRecyclerView);

        totalBalanceText = view.findViewById(R.id.textTotalBalance);
        totalIncomeText = view.findViewById(R.id.textTotalIncome);
        totalExpenseText = view.findViewById(R.id.textTotalExpense);
        addTransactionButton = view.findViewById(R.id.buttonAddTransaction);

        buttonFilterAll = view.findViewById(R.id.buttonFilterAll);
        buttonFilterIncome = view.findViewById(R.id.buttonFilterIncome);
        buttonFilterExpense = view.findViewById(R.id.buttonFilterExpense);

        buttonFilterDay = view.findViewById(R.id.buttonFilterDay);
        buttonFilterWeek = view.findViewById(R.id.buttonFilterWeek);
        buttonFilterMonth = view.findViewById(R.id.buttonFilterMonth);
        buttonFilterYear = view.findViewById(R.id.buttonFilterYear);
        buttonFilterChooseDate = view.findViewById(R.id.buttonFilterChooseDate);

        addPlanButton = view.findViewById(R.id.addPlanButton);

    }


    private void setupRecyclerView() {
        budgetList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new BudgetAdapter(getContext(), filteredList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        planAdapter = new PlanAdapter(getContext(), plansList, this::showPlanDepositDialog);
        LinearLayoutManager plansLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        plansRecyclerView.setLayoutManager(plansLayoutManager);
        plansRecyclerView.setAdapter(planAdapter);

        ViewGroup.LayoutParams params = plansRecyclerView.getLayoutParams();
        params.height = getResources().getDimensionPixelSize(R.dimen.plan_card_height);
        plansRecyclerView.setLayoutParams(params);
    }

    private void setupListeners() {
        addTransactionButton.setOnClickListener(v -> showAddTransactionDialog());

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

        buttonFilterChooseDate.setOnClickListener(v -> showDatePicker());
        addPlanButton.setOnClickListener(v -> showAddPlanDialog());
    }

    private void showDatePicker() {
        CalendarBottomSheetDialog calendarDialog = new CalendarBottomSheetDialog(requireContext());

        calendarDialog.setOnDateSelectedListener(date -> {
            selectedDate = Calendar.getInstance();
            selectedDate.setTime(date);

            updatePeriodFilter("custom");
            updatePeriodButtonStyles();
        });
        calendarDialog.show();
    }

    private void initializeData() {
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

        if (period.equals("custom") && selectedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            buttonFilterChooseDate.setText(sdf.format(selectedDate.getTime()));
        }

        applyFilters();
    }

    private void updateFilterButtonStyles() {
        buttonFilterAll.setBackgroundResource(currentFilter.equals("all") ?
                R.drawable.filter_button_selected : R.drawable.filter_button_background);
        buttonFilterIncome.setBackgroundResource(currentFilter.equals("income") ?
                R.drawable.filter_button_selected : R.drawable.filter_button_background);
        buttonFilterExpense.setBackgroundResource(currentFilter.equals("expense") ?
                R.drawable.filter_button_selected : R.drawable.filter_button_background);

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
        String userId = auth.getCurrentUser().getUid();
        AddPlanDialogFragment dialog = new AddPlanDialogFragment();
        dialog.setOnPlanAddedListener(plan -> {
            budgetRepository.addPlan(userId, plan)
                    .addOnSuccessListener(aVoid -> {
                        plansList.add(plan);
                        planAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Plan Added", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show());
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

        String userId = auth.getCurrentUser().getUid();
        builder.setView(view);

        AlertDialog dialog = builder.create();

        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnAdd = view.findViewById(R.id.btnAdd);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btnAdd.setOnClickListener(v -> {
            try {
                double amount = Double.parseDouble(amountEdit.getText().toString());
                d("BudgetFragmentS : ", String.valueOf(amount));

                double totalIncome = 0;
                double totalExpense = 0;

                for (Budget budget : budgetList) {
                    if (budget.getType().equals("income")) {
                        totalIncome += budget.getAmount();
                    } else {
                        totalExpense += budget.getAmount();
                    }
                }

                if (amount > totalIncome - totalExpense) {
                    Toast.makeText(getContext(),
                            "No Have Money: Higher than your balance",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                plan.deposit(amount);

                budgetRepository.depositToPlan(userId, plan, amount)
                        .addOnSuccessListener(aVoid -> {
                            planAdapter.notifyDataSetChanged();
                            Toast.makeText(getContext(),
                                    "Miktar başarıyla eklendi",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })

                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(),
                                    "Güncelleme başarısız: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(),
                        "Lütfen geçerli bir miktar girin",
                        Toast.LENGTH_SHORT).show();
            }
        });


        dialog.show();
    }

    private void showAddTransactionDialog() {
        AddTransactionDialogFragment dialog = new AddTransactionDialogFragment();
        dialog.setOnTransactionAddedListener(this);
        dialog.show(getChildFragmentManager(), "AddTransaction");
    }

    private void applyFilters() {
        filteredList.clear();
        List<Budget> tempList = new ArrayList<>(budgetList);

        final Calendar periodStart = Calendar.getInstance();

        periodStart.set(Calendar.HOUR_OF_DAY, 0);
        periodStart.set(Calendar.MINUTE, 0);
        periodStart.set(Calendar.SECOND, 0);
        periodStart.set(Calendar.MILLISECOND, 0);

        switch (currentPeriod) {
            case "day":
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
            case "custom":
                if (selectedDate != null) {
                    periodStart.setTime(selectedDate.getTime());
                }
                break;
        }

        System.out.println("Period Start: " + periodStart.getTime());

        tempList = tempList.stream()
                .filter(budget -> {
                    try {
                        Date transactionDate = dateFormat.parse(budget.getDate());
                        if (currentPeriod.equals("custom") && selectedDate != null) {
                            Calendar transactionCalendar = Calendar.getInstance();
                            transactionCalendar.setTime(transactionDate);
                            return transactionCalendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                                    transactionCalendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                                    transactionCalendar.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH);
                        }
                        if (currentPeriod.equals("day")) {
                            Calendar transactionCalendar = Calendar.getInstance();
                            transactionCalendar.setTime(transactionDate);
                            return transactionCalendar.get(Calendar.YEAR) == periodStart.get(Calendar.YEAR) &&
                                    transactionCalendar.get(Calendar.MONTH) == periodStart.get(Calendar.MONTH) &&
                                    transactionCalendar.get(Calendar.DAY_OF_MONTH) == periodStart.get(Calendar.DAY_OF_MONTH);
                        } else {
                            return !transactionDate.before(periodStart.getTime());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .collect(Collectors.toList());

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

        String formattedBalance = String.format(Locale.getDefault(), "%.2f ₺", totalBalance);
        String formattedIncome = String.format(Locale.getDefault(), "%.2f ₺", totalIncome);
        String formattedExpense = String.format(Locale.getDefault(), "%.2f ₺", totalExpense);

        totalBalanceText.setText(formattedBalance);
        totalIncomeText.setText(formattedIncome);
        totalExpenseText.setText(formattedExpense);
    }

    @Override
    public void onTransactionAdded(Budget budget) {
        String userId = auth.getCurrentUser().getUid();
        budgetRepository.addBudget(userId, budget)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Budget added successfully!", Toast.LENGTH_SHORT).show();
                    budgetList.add(budget);
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error adding budget.", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error: ", e);
                });

        if (planAdapter != null && !plansList.isEmpty()) {
            planAdapter.notifyDataSetChanged();
        }
    }

    private void deleteBudget(Budget budget, int position) {
        String userId = auth.getCurrentUser().getUid();
        budgetRepository.deleteBudget(userId, budget.getId())
                .addOnSuccessListener(aVoid -> {
                    budgetList.remove(budget);
                    applyFilters();
                    Toast.makeText(getContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error deleting transaction", Toast.LENGTH_SHORT).show());
    }

    private void updateBudget(Budget budget, int position, double amount, String description, String category) {
        String userId = auth.getCurrentUser().getUid();
        budget.setAmount(amount);
        budget.setDescription(description);
        budget.setCategory(category);

        budgetRepository.updateBudget(userId, budget)
                .addOnSuccessListener(aVoid -> {
                    applyFilters();
                    Toast.makeText(getContext(), "Transaction updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error updating transaction", Toast.LENGTH_SHORT).show());
    }

    private void fetchBudgetsFromFirestore() {
        String userId = auth.getCurrentUser().getUid();
        budgetRepository.getUserBudgets(userId)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    budgetList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Budget budget = doc.toObject(Budget.class);
                        budgetList.add(budget);
                    }
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Could not fetch data.", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error: ", e);
                });
    }

    private void fetchPlansFromFirestore() {
        String userId = auth.getCurrentUser().getUid();
        budgetRepository.getUserPlans(userId)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    plansList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Plan plan = document.toObject(Plan.class);
                        if (plan != null) {
                            plan.setId(document.getId());
                            plansList.add(plan);
                        }
                    }
                    planAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show());
    }

}