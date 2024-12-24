package msku.ceng;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddTransactionDialogFragment extends DialogFragment {
    private OnTransactionAddedListener listener;
    private int selectedIconResourceId = -1;

    public interface OnTransactionAddedListener {
        void onTransactionAdded(Budget budget);
    }

    public void setOnTransactionAddedListener(OnTransactionAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_transaction, null);

        EditText amountEdit = view.findViewById(R.id.editTextAmount);
        EditText descriptionEdit = view.findViewById(R.id.editTextDescription);
        EditText categoryEdit = view.findViewById(R.id.editTextCategory);
        RadioGroup typeGroup = view.findViewById(R.id.radioGroupType);
        Button saveButton = view.findViewById(R.id.buttonSave);
        Button cancelButton = view.findViewById(R.id.buttonCancel);

        setupIconSelection(view);

        AlertDialog dialog = builder.setView(view)
                .setTitle("Yeni İşlem Ekle")
                .create();

        saveButton.setOnClickListener(v -> {
            if (selectedIconResourceId == -1) {
                selectedIconResourceId = R.mipmap.othericon_foreground;
            }

            try {
                double amount = Double.parseDouble(amountEdit.getText().toString());
                String description = descriptionEdit.getText().toString();
                String category = categoryEdit.getText().toString();
                String type = typeGroup.getCheckedRadioButtonId() == R.id.radioIncome ? "income" : "expense";

                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String id = String.valueOf(System.currentTimeMillis());

                Budget newBudget = new Budget(id, amount, description, category, date, type, selectedIconResourceId);

                if (listener != null) {
                    listener.onTransactionAdded(newBudget);
                }
                dialog.dismiss();
            } catch (NumberFormatException e) {
                amountEdit.setError("Geçerli bir miktar giriniz");
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    private void setupIconSelection(View view) {
        ImageView icon1 = view.findViewById(R.id.icon1);
        ImageView icon2 = view.findViewById(R.id.icon2);
        ImageView icon3 = view.findViewById(R.id.icon3);
        ImageView icon4 = view.findViewById(R.id.icon4);

        View.OnClickListener iconClickListener = v -> {
            if (selectedIconResourceId != -1) {
                resetIconSelection(view);
            }

            v.setBackgroundResource(R.drawable.selected_icon_background);
            selectedIconResourceId = getIconResourceId(v.getId());
        };

        icon1.setOnClickListener(iconClickListener);
        icon2.setOnClickListener(iconClickListener);
        icon3.setOnClickListener(iconClickListener);
        icon4.setOnClickListener(iconClickListener);
    }

    private void resetIconSelection(View view) {
        ImageView previousIcon = view.findViewById(getIconViewId(selectedIconResourceId));
        if (previousIcon != null) {
            previousIcon.setBackground(null);
        }
    }

    private int getIconResourceId(int viewId) {
        if (viewId == R.id.icon1) return R.mipmap.buiscon_foreground;
        if (viewId == R.id.icon2) return R.mipmap.shoppingicon_foreground;
        if (viewId == R.id.icon3) return R.mipmap.eaticon_foreground;
        if (viewId == R.id.icon4) return R.mipmap.invoiceicon_foreground;
        return R.mipmap.othericon_foreground;
    }

    private int getIconViewId(int resourceId) {
        if (resourceId == R.mipmap.buiscon_foreground) return R.id.icon1;
        if (resourceId == R.mipmap.shoppingicon_foreground) return R.id.icon2;
        if (resourceId == R.mipmap.eaticon_foreground) return R.id.icon3;
        if (resourceId == R.mipmap.invoiceicon_foreground) return R.id.icon4;
        return R.id.icon1;
    }
}