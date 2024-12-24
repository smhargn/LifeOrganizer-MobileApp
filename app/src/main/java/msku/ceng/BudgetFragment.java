package msku.ceng;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment implements AddTransactionDialogFragment.OnTransactionAddedListener {
    private RecyclerView recyclerView;
    private BudgetAdapter adapter;
    private List<Budget> budgetList;
    private TextView totalBalanceText;
    private Button addTransactionButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewBudget);
        totalBalanceText = view.findViewById(R.id.textTotalBalance);
        addTransactionButton = view.findViewById(R.id.buttonAddTransaction);

        budgetList = new ArrayList<>();
        adapter = new BudgetAdapter(getContext(), budgetList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        addTransactionButton.setOnClickListener(v -> showAddTransactionDialog());


        updateTotalBalance();

        return view;
    }

    private void showAddTransactionDialog() {
        AddTransactionDialogFragment dialog = new AddTransactionDialogFragment();
        dialog.setOnTransactionAddedListener(this);
        dialog.show(getChildFragmentManager(), "AddTransaction");
    }

    @Override
    public void onTransactionAdded(Budget budget) {
        budgetList.add(budget);
        adapter.notifyDataSetChanged();
        updateTotalBalance();
    }



    private void updateTotalBalance() {
        double total = 0;
        for (Budget budget : budgetList) {
            if (budget.getType().equals("income")) {
                total += budget.getAmount();
            } else {
                total -= budget.getAmount();
            }
        }
        totalBalanceText.setText(String.format("Toplam Bakiye: %.2f TL", total));
    }
}