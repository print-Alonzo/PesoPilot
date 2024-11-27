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

public class TransferRecyclerAdapter extends FirestoreRecyclerAdapter<TransferEntry, TransferViewHolder> {

    public TransferRecyclerAdapter(@NonNull FirestoreRecyclerOptions<TransferEntry> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TransferViewHolder holder, int position, @NonNull TransferEntry model) {
        holder.bindModel(model);

        holder.itemView.setOnLongClickListener(v -> {
            showPreviewDialog(v.getContext(), model);
            return true;
        });
    }

    @NonNull
    @Override
    public TransferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.transfer_entry_layout, parent, false);
        TransferViewHolder viewHolder = new TransferViewHolder(v);
        return viewHolder;
    }

    private void showPreviewDialog(Context context, TransferEntry model) {
        // Create and configure the dialog
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.transfer_dialog_preview); // Use a custom layout for the preview

        // Populate dialog views with the item's data
        TextView titleTextView = dialog.findViewById(R.id.preview_title);
        TextView descriptionTextView = dialog.findViewById(R.id.preview_description);
        TextView amountTextView = dialog.findViewById(R.id.preview_amount);
        TextView senderTextView = dialog.findViewById(R.id.preview_sender);
        TextView receiverTextView = dialog.findViewById(R.id.preview_receiver);

        titleTextView.setText(model.getTitle());
        descriptionTextView.setText(model.getDescription());
        amountTextView.setText(String.format("%.2f", model.getAmount()));
        senderTextView.setText("Sender: " + model.getSender());
        receiverTextView.setText("Receiver: " + model.getReceiver());

        // Set up the button to navigate to the next activity
        Button openButton = dialog.findViewById(R.id.preview_open_button);
        openButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, TransferEntryActivity.class);
            intent.putExtra("doc_id", model.getDoc_id()); // Pass data to the next activity
            context.startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
    }
}
