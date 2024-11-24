package com.example.pesopilotversion3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class MyFirestoreRecyclerAdapter extends FirestoreRecyclerAdapter<ExpenseIncomeEntry, MyFirestoreViewHolder> {

    public MyFirestoreRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ExpenseIncomeEntry> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyFirestoreViewHolder holder, int position, @NonNull ExpenseIncomeEntry model) {
        holder.bindData(model);
    }

    @NonNull
    @Override
    public MyFirestoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.income_expense_entry_layout, parent, false);
        MyFirestoreViewHolder viewHolder = new MyFirestoreViewHolder(v);
        return viewHolder;
    }
}
