package msku.ceng;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Shopping}.
 */
public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ShoppingViewHolder> {

    private List<Shopping> shoppingList;
    private Context context;

    public ShoppingAdapter(List<Shopping> shoppingList, Context context) {
        this.shoppingList = shoppingList;
        this.context = context;
    }

    @NonNull
    @Override
    public ShoppingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shopping, parent, false);
        return new ShoppingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingViewHolder holder, int position) {
        Shopping shoppingEntry = shoppingList.get(position);
        holder.shoppingText.setText(shoppingEntry.getShoppingText());
        holder.shoppingCheck.setChecked(shoppingEntry.getShoppingCheck());

        // Daha önce atanmış listener'ı temizle
        holder.shoppingCheck.setOnCheckedChangeListener(null);

        // Yeni listener'ı ekle
        holder.shoppingCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            shoppingEntry.setShoppingCheck(isChecked);

            // Listeyi sırala
            int oldPosition = shoppingList.indexOf(shoppingEntry);
            sortShoppingList();
            int newPosition = shoppingList.indexOf(shoppingEntry);

            // UI thread'inde işlemi yap
            holder.itemView.post(() -> {
                // Öğeyi güncelle ve taşınma işlemini bildir
                if (oldPosition != newPosition) {
                    notifyItemMoved(oldPosition, newPosition);
                }
                notifyItemChanged(newPosition); // Checkbox durumunu güncelle
            });
        });

        // Silme butonunun işlevi
        holder.deleteButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            shoppingList.remove(currentPosition);
            notifyItemRemoved(currentPosition);
        });
    }

    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    // Listeyi sıralamak için gerekli fonksiyon
    private void sortShoppingList() {
        Collections.sort(shoppingList, (item1, item2) -> {
            if (item1.getShoppingCheck() && !item2.getShoppingCheck()) {
                return 1; // işaretli öğeler en alta taşınacak
            } else if (!item1.getShoppingCheck() && item2.getShoppingCheck()) {
                return -1; // işaretlenmemiş öğeler en üste taşınacak
            }
            return 0;
        });
    }

    public static class ShoppingViewHolder extends RecyclerView.ViewHolder {
        TextView shoppingText;
        CheckBox shoppingCheck;
        ImageButton deleteButton;

        public ShoppingViewHolder(View itemView) {
            super(itemView);
            shoppingText = itemView.findViewById(R.id.shopping_text);
            shoppingCheck = itemView.findViewById(R.id.shopping_checkbox);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
