package com.example.pesopilotversion3;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TransferActivity extends AppCompatActivity {
    private String TAG = "TransferActivity";

    private Spinner time_filter_spinner;
    private Spinner sender_account_filter_spinner;
    private Spinner receiver_account_filter_spinner;
    private Button addTransferButton;

    private RecyclerView recyclerView;
    private TransferRecyclerAdapter transferRecyclerAdapter;

    private FirebaseFirestore dbRef;

    private String[] transferTimeFilters = {"All", "Today", "This Week", "This Month", "This Year"};
    private ArrayList<String> transferAccountFilters;

    private TransferEntry recentlyDeletedItem;
    private int recentlyDeletedItemPosition;
    private String recentlyDeletedDocumentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transfer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setting up the database reference
        this.dbRef = FirebaseFirestore.getInstance();

        // Setting up the time filter spinner
        loadTimeFilters();

        // Setting up the account filter spinner
        this.transferAccountFilters = new ArrayList<>();
        loadAccounts();

        // Setting up the Recycler View for the list of Transfers
        this.recyclerView = findViewById(R.id.transfersRecyclerView);
        loadTransfers();

        this.addTransferButton = findViewById(R.id.addTransferButton);
        this.addTransferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TransferActivity.this, AddTransferActivity.class));
            }
        });

        ItemTouchHelper deleteItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final ColorDrawable background = new ColorDrawable(Color.RED);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't support move in this case
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                deleteItemWithUndo(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                // Draw a red background while swiping
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    background.setBounds(viewHolder.itemView.getRight() + (int) dX,
                            viewHolder.itemView.getTop(), viewHolder.itemView.getRight(),
                            viewHolder.itemView.getBottom());
                    background.draw(c);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        deleteItemTouchHelper.attachToRecyclerView(recyclerView);

        ItemTouchHelper editItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            private final ColorDrawable background = new ColorDrawable(Color.GREEN);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't support move in this case
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                editEntry(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                // Draw a green background while swiping
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    background.setBounds(viewHolder.itemView.getLeft() + (int) dX,
                            viewHolder.itemView.getTop(), viewHolder.itemView.getLeft(),
                            viewHolder.itemView.getBottom());
                    background.draw(c);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        editItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void editEntry(int position) {
        String documentId = transferRecyclerAdapter.getSnapshots().getSnapshot(position).getId();
        Intent intent = new Intent(this, EditEntryActivity.class);
        intent.putExtra("doc_id", documentId);
        intent.putExtra("title", transferRecyclerAdapter.getSnapshots().getSnapshot(position).getString(FirestoreReferences.TITLE_FIELD));
        intent.putExtra("description", transferRecyclerAdapter.getSnapshots().getSnapshot(position).getString(FirestoreReferences.DESCRIPTION_FIELD));
        intent.putExtra("date", transferRecyclerAdapter.getSnapshots().getSnapshot(position).getString(FirestoreReferences.TIMESTAMP_FIELD));
        intent.putExtra("amount", transferRecyclerAdapter.getSnapshots().getSnapshot(position).getDouble(FirestoreReferences.AMOUNT_FIELD).toString());
        intent.putExtra("sender", transferRecyclerAdapter.getSnapshots().getSnapshot(position).getString(FirestoreReferences.TRANSFER_SENDER_FIELD));
        intent.putExtra("receiver", transferRecyclerAdapter.getSnapshots().getSnapshot(position).getString(FirestoreReferences.TRANSFER_RECEIVER_FIELD));
        intent.putExtra("username", transferRecyclerAdapter.getSnapshots().getSnapshot(position).getString(FirestoreReferences.USERNAME_FIELD));
        startActivity(intent);
    }

    private void deleteItemWithUndo(int position) {
        // Save details of the deleted item
        recentlyDeletedItem = transferRecyclerAdapter.getSnapshots().get(position);
        recentlyDeletedItemPosition = position;
        recentlyDeletedDocumentId = transferRecyclerAdapter.getSnapshots().getSnapshot(position).getId();

        // Remove the item from the adapter
        transferRecyclerAdapter.getSnapshots().getSnapshot(position).getReference().delete()
                .addOnSuccessListener(aVoid -> {
                    transferRecyclerAdapter.notifyItemRemoved(position);
                    transferRecyclerAdapter.notifyItemRangeChanged(0, transferRecyclerAdapter.getItemCount()-1);
                    transferRecyclerAdapter.notifyDataSetChanged();
                    showUndoSnackbar();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    transferRecyclerAdapter.notifyItemChanged(position);
                });
    }

    private void showUndoSnackbar() {
        Snackbar snackbar = Snackbar.make(recyclerView, "Item deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> undoDelete());
        snackbar.show();
    }

    private void undoDelete() {
        if (recentlyDeletedItem != null) {
            // Restore the deleted item to Firestore
            dbRef.collection(FirestoreReferences.TRANSFERS_COLLECTION) // Replace with your collection name
                    .document(recentlyDeletedDocumentId)
                    .set(recentlyDeletedItem)
                    .addOnSuccessListener(aVoid -> {
                        // Re-add the item to the adapter
                        transferRecyclerAdapter.notifyItemInserted(recentlyDeletedItemPosition);
                        transferRecyclerAdapter.notifyItemRangeChanged(0, transferRecyclerAdapter.getItemCount());
                        transferRecyclerAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Item restored", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to restore item", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public void loadTransfers() {
        Query query = dbRef.collection(FirestoreReferences.TRANSFERS_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, "admin");

        FirestoreRecyclerOptions<TransferEntry> options = new FirestoreRecyclerOptions.Builder<TransferEntry>()
                .setQuery(query, new SnapshotParser<TransferEntry>() {
                    @Nullable
                    @Override
                    public TransferEntry parseSnapshot(@Nullable DocumentSnapshot snapshot) {
                        TransferEntry temp = new TransferEntry(
                                snapshot.getId(),
                                snapshot.getString(FirestoreReferences.TITLE_FIELD),
                                snapshot.getString(FirestoreReferences.DESCRIPTION_FIELD),
                                snapshot.getString(FirestoreReferences.TIMESTAMP_FIELD),
                                snapshot.getDouble(FirestoreReferences.AMOUNT_FIELD),
                                snapshot.getString(FirestoreReferences.TRANSFER_SENDER_FIELD),
                                snapshot.getString(FirestoreReferences.TRANSFER_RECEIVER_FIELD),
                                snapshot.getString(FirestoreReferences.USERNAME_FIELD)
                        );

                        return temp;
                    }
                })
                .build();

        this.transferRecyclerAdapter = new TransferRecyclerAdapter(options);
        this.transferRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.scrollToPosition(transferRecyclerAdapter.getItemCount() - 1);
            }
        });

        this.recyclerView.setAdapter(this.transferRecyclerAdapter);
        this.transferRecyclerAdapter.notifyItemRangeChanged(0, this.transferRecyclerAdapter.getItemCount());
        this.transferRecyclerAdapter.notifyDataSetChanged();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        this.recyclerView.setLayoutManager(linearLayoutManager);
    }

    public void loadAccounts() {
        transferAccountFilters.add("Any");
        this.sender_account_filter_spinner = findViewById(R.id.sender_account_filter_spinner);
        ArrayAdapter<String> sender_account_filter_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, transferAccountFilters);
        sender_account_filter_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sender_account_filter_spinner.setAdapter(sender_account_filter_adapter);

        this.sender_account_filter_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTransfers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.receiver_account_filter_spinner = findViewById(R.id.receiver_account_filter_spinner);
        ArrayAdapter<String> receiver_account_filter_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, transferAccountFilters);
        receiver_account_filter_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        receiver_account_filter_spinner.setAdapter(receiver_account_filter_adapter);

        this.receiver_account_filter_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTransfers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Query query = dbRef
                .collection(FirestoreReferences.ACCOUNTS_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, "admin");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().isEmpty()) {
                        Log.d(TAG, "No accounts found");
                    } else {
                        String account = "";
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            account = document.get(FirestoreReferences.ACCOUNT_NAME_FIELD, String.class);
                            transferAccountFilters.add(account);
                            sender_account_filter_adapter.notifyDataSetChanged();
                            receiver_account_filter_adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }

    public void loadTimeFilters() {
        this.time_filter_spinner = findViewById(R.id.time_filter_spinner);
        ArrayAdapter<String> time_filter_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, transferTimeFilters);
        time_filter_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        time_filter_spinner.setAdapter(time_filter_adapter);

        this.time_filter_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTransfers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void updateTransfers(){
        String selectedTimeFilter = time_filter_spinner.getSelectedItem().toString();
        String selectedSenderAccountFilter = sender_account_filter_spinner.getSelectedItem().toString();
        String selectedReceiverAccountFilter = receiver_account_filter_spinner.getSelectedItem().toString();

        Query transferQuery = dbRef.collection(FirestoreReferences.TRANSFERS_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, "admin"); // Replace with the current user's identifier

        // Apply time filter
        String today = getFormattedDate(Calendar.getInstance());
        switch (selectedTimeFilter) {
            case "Today":
                transferQuery = transferQuery.whereEqualTo(FirestoreReferences.TIMESTAMP_FIELD, today);
                break;
            case "This Week":
                transferQuery = transferQuery.whereGreaterThanOrEqualTo(FirestoreReferences.TIMESTAMP_FIELD, getFormattedDate(getStartOfWeek()));
                break;
            case "This Month":
                transferQuery = transferQuery.whereGreaterThanOrEqualTo(FirestoreReferences.TIMESTAMP_FIELD, getFormattedDate(getStartOfMonth()));
                break;
            case "This Year":
                transferQuery = transferQuery.whereGreaterThanOrEqualTo(FirestoreReferences.TIMESTAMP_FIELD, getFormattedDate(getStartOfYear()));
                break;
            default: // "All"
                break;
        }

        // Apply category filter if not "All Categories"
        if (!selectedSenderAccountFilter.equals("Any")) {
            transferQuery = transferQuery.whereEqualTo("sender", selectedSenderAccountFilter);
        }

        if (!selectedReceiverAccountFilter.equals("Any")) {
            transferQuery = transferQuery.whereEqualTo("receiver", selectedReceiverAccountFilter);
        }

        FirestoreRecyclerOptions<TransferEntry> updatedOptions = new FirestoreRecyclerOptions.Builder<TransferEntry>()
                .setQuery(transferQuery, new SnapshotParser<TransferEntry>() {
                    @Nullable
                    @Override
                    public TransferEntry parseSnapshot(@Nullable DocumentSnapshot snapshot) {
                        return new TransferEntry(
                                snapshot.getId(),
                                snapshot.getString(FirestoreReferences.TITLE_FIELD),
                                snapshot.getString(FirestoreReferences.DESCRIPTION_FIELD),
                                snapshot.getString(FirestoreReferences.TIMESTAMP_FIELD),
                                snapshot.getDouble(FirestoreReferences.AMOUNT_FIELD),
                                snapshot.getString(FirestoreReferences.TRANSFER_SENDER_FIELD),
                                snapshot.getString(FirestoreReferences.TRANSFER_RECEIVER_FIELD),
                                snapshot.getString(FirestoreReferences.USERNAME_FIELD)
                        );
                    }
                })
                .build();

        this.transferRecyclerAdapter.updateOptions(updatedOptions);
        this.transferRecyclerAdapter.notifyItemRangeChanged(0, this.transferRecyclerAdapter.getItemCount());
        this.transferRecyclerAdapter.notifyDataSetChanged();
    }

    private String getFormattedDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    // Helper method to get the start of the week
    private Calendar getStartOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    // Helper method to get the start of the month
    private Calendar getStartOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    // Helper method to get the start of the year
    private Calendar getStartOfYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.transferRecyclerAdapter.startListening();
        this.transferRecyclerAdapter.notifyItemRangeChanged(0, this.transferRecyclerAdapter.getItemCount());
        this.transferRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.transferRecyclerAdapter.stopListening();
    }

}