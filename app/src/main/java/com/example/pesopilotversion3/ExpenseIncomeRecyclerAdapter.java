package com.example.pesopilotversion3;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ExpenseIncomeRecyclerAdapter extends FirestoreRecyclerAdapter<ExpenseIncomeEntry, ExpenseIncomeViewHolder> {

    public ExpenseIncomeRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ExpenseIncomeEntry> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ExpenseIncomeViewHolder holder, int position, @NonNull ExpenseIncomeEntry model) {
        holder.bindData(model);

        holder.itemView.setOnLongClickListener(v -> {
            showPreviewDialog(v.getContext(), model);
            return true;
        });
    }

    @NonNull
    @Override
    public ExpenseIncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.income_expense_entry_layout, parent, false);
        ExpenseIncomeViewHolder viewHolder = new ExpenseIncomeViewHolder(v);
        return viewHolder;
    }

    private void showPreviewDialog(Context context, ExpenseIncomeEntry model) {
        // Create and configure the dialog
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_preview); // Use a custom layout for the preview

        // Populate dialog views with the item's data
        TextView titleTextView = dialog.findViewById(R.id.preview_title);
        TextView amountTextView = dialog.findViewById(R.id.preview_amount);
        TextView categoryTextView = dialog.findViewById(R.id.preview_category);
        TextView descriptionTextView = dialog.findViewById(R.id.preview_description);

        titleTextView.setText(model.getTitle());
        descriptionTextView.setText(model.getDescription());
        amountTextView.setText(String.format("%.2f", model.getAmount()));
        categoryTextView.setText(model.getCategory());

        // Set up the button to navigate to the next activity
        Button openButton = dialog.findViewById(R.id.preview_open_button);
        openButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EntryActivity.class);
            intent.putExtra("doc_id", model.getDoc_id()); // Pass data to the next activity
            intent.putExtra("entry_type", model.getEntry_type());
            context.startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
    }
}
