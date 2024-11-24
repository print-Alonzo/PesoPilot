package com.example.pesopilotversion3;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class MyFirestoreViewHolder extends RecyclerView.ViewHolder {
    private TextView title, date, amount, account, category;

    public MyFirestoreViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.entry_title);
        date = itemView.findViewById(R.id.entry_date);
        amount = itemView.findViewById(R.id.entry_amount);
        account = itemView.findViewById(R.id.entry_account);
        category = itemView.findViewById(R.id.entry_category);
    }

    public void bindData(ExpenseIncomeEntry entry) {
        this.title.setText(entry.getTitle());
        this.date.setText(entry.getDate());
        this.amount.setText(String.valueOf(entry.getAmount()));
        this.account.setText(entry.getAccount());
        this.category.setText(entry.getCategory());
    }
}
