package msku.ceng.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import msku.ceng.model.Budget;
import msku.ceng.R;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {
    private List<Budget> budgetList;
    private Context context;
    private SimpleDateFormat dateFormat;
    private OnBudgetClickListener listener;

    public BudgetAdapter(Context context, List<Budget> budgetList,OnBudgetClickListener listener) {
        this.context = context;
        this.budgetList = budgetList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Budget budget = budgetList.get(position);

        holder.categoryIcon.setImageResource(budget.getIconResourceId());
        holder.descriptionText.setText(budget.getDescription());
        holder.categoryText.setText(budget.getCategory());
        holder.dateText.setText(budget.getDate());

        String amountText = String.format("%.2f TL", budget.getAmount());
        if (budget.getType().equals("expense")) {
            amountText = "-" + amountText;
            holder.amountText.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.amountText.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }
        holder.amountText.setText(amountText);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBudgetClick(budget, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public interface OnBudgetClickListener {
        void onBudgetClick(Budget budget, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView categoryIcon;
        public TextView descriptionText;
        public TextView categoryText;
        public TextView dateText;
        public TextView amountText;

        public ViewHolder(View view) {
            super(view);
            categoryIcon = view.findViewById(R.id.categoryIcon);
            descriptionText = view.findViewById(R.id.descriptionText);
            categoryText = view.findViewById(R.id.categoryText);
            dateText = view.findViewById(R.id.dateText);
            amountText = view.findViewById(R.id.amountText);
        }
    }
}