package msku.ceng;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShoppingFragment extends Fragment {

    private ShoppingAdapter shoppingAdapter;
    private List<Shopping> shoppingList = new ArrayList<>(); // Görev listesi oluşturuldu

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Fragment'in layout dosyasını şişiriyoruz
        View view = inflater.inflate(R.layout.fragment_shopping, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.shoppingView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        shoppingAdapter = new ShoppingAdapter(shoppingList, getContext()); // Adapter'ı bağladık
        recyclerView.setAdapter(shoppingAdapter);

        ImageButton addShoppingButton = view.findViewById(R.id.addShopping);
        addShoppingButton.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Yeni Görev Ekle");

            final EditText input = new EditText(requireContext());
            input.setHint("Görev adı");

            builder.setView(input);

            builder.setPositiveButton("EKLE", (dialog, which) -> {
                String shoppingName = input.getText().toString().trim();
                if (!shoppingName.isEmpty()) {
                    Shopping newShopping = new Shopping(shoppingName, false);

                    // Yeni öğeyi listenin başına ekle
                    shoppingList.add(0, newShopping);

                    // RecyclerView'in en üstüne eklenen öğeyi bildir
                    shoppingAdapter.notifyItemInserted(0);

                    // RecyclerView'i en üste kaydır
                    recyclerView.scrollToPosition(0);
                }
            });


            builder.setNegativeButton("İPTAL", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        return view;
    }
}
