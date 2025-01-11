package msku.ceng.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import msku.ceng.R;
import msku.ceng.model.Shopping;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ViewHolder> {
    private List<Shopping> shoppingList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
        void onCheckBoxClick(int position, boolean isChecked);
    }

    public ShoppingAdapter(List<Shopping> shoppingList, Context context, OnItemClickListener listener) {
        this.shoppingList = shoppingList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Shopping item = shoppingList.get(position);

        holder.listNameText.setText(item.getListName());
        holder.productText.setText(item.getProductName());
        holder.checkbox.setChecked(item.isChecked());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.dateText.setText(dateFormat.format(item.getCreatedDate()));
    }

    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView listNameText;
        TextView productText;
        TextView dateText;
        CheckBox checkbox;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listNameText = itemView.findViewById(R.id.list_name_text);
            productText = itemView.findViewById(R.id.product_text);
            dateText = itemView.findViewById(R.id.date_text);
            checkbox = itemView.findViewById(R.id.shopping_checkbox);
            deleteButton = itemView.findViewById(R.id.delete_button);

            deleteButton.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getAdapterPosition());
                }
            });

            checkbox.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onCheckBoxClick(getAdapterPosition(), checkbox.isChecked());
                }
            });
        }
    }
}