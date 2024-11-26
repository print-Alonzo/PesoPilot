package com.example.pesopilotversion3;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class MyFirestoreViewHolder extends RecyclerView.ViewHolder {
    private TextView doc_id, title, description, date, amount, account, category, username, entry_type;

    public MyFirestoreViewHolder(View itemView) {
        super(itemView);
        doc_id = itemView.findViewById(R.id.entry_doc_id);
        title = itemView.findViewById(R.id.entry_title);
        description = itemView.findViewById(R.id.entry_description);
        date = itemView.findViewById(R.id.entry_date);
        amount = itemView.findViewById(R.id.entry_amount);
        account = itemView.findViewById(R.id.entry_account);
        category = itemView.findViewById(R.id.entry_category);
        username = itemView.findViewById(R.id.entry_username);
        entry_type = itemView.findViewById(R.id.entry_type);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EntryActivity.class);
                intent.putExtra("doc_id", doc_id.getText().toString());
                intent.putExtra("entry_type", entry_type.getText().toString());
                v.getContext().startActivity(intent);
            }
        });
    }

    public void bindData(ExpenseIncomeEntry entry) {
        this.doc_id.setText(entry.getDoc_id());
        this.title.setText(entry.getTitle());
        this.description.setText(entry.getDescription());
        this.date.setText(entry.getDate());
        this.amount.setText(String.valueOf(entry.getAmount()));
        this.account.setText(entry.getAccount());
        this.category.setText(entry.getCategory());
        this.username.setText(entry.getUsername());
        this.entry_type.setText(entry.getEntry_type());
    }
}
