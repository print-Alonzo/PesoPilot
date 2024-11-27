package com.example.pesopilotversion3;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TransferViewHolder extends RecyclerView.ViewHolder {
    private TextView doc_id, title, description, date, amount, sender_account, receiver_account, username;

    public TransferViewHolder(@NonNull View itemView) {
        super(itemView);
        doc_id = itemView.findViewById(R.id.entry_doc_id);
        title = itemView.findViewById(R.id.entry_title);
        description = itemView.findViewById(R.id.entry_description);
        date = itemView.findViewById(R.id.entry_date);
        amount = itemView.findViewById(R.id.entry_amount);
        sender_account = itemView.findViewById(R.id.entry_sender);
        receiver_account = itemView.findViewById(R.id.entry_receiver);
        username = itemView.findViewById(R.id.entry_username);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TransferEntryActivity.class);
                intent.putExtra("doc_id", doc_id.getText().toString());
                v.getContext().startActivity(intent);
            }
        });
    }

    public void bindModel(TransferEntry entry) {
        this.doc_id.setText(entry.getDoc_id());
        this.title.setText(entry.getTitle());
        this.description.setText(entry.getDescription());
        this.date.setText(entry.getDate());
        this.amount.setText(String.valueOf(entry.getAmount()));
        this.sender_account.setText(entry.getSender());
        this.receiver_account.setText(entry.getReceiver());
        this.username.setText(entry.getUsername());
    }
}
