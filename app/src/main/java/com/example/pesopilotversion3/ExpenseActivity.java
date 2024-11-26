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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ExpenseActivity extends AppCompatActivity {
    private String TAG = "ExpenseActivity";

    private Spinner time_filter_spinner;
    private Spinner category_filter_spinner;
    private Button addExpenseButton;

    private RecyclerView recyclerView;
    private MyFirestoreRecyclerAdapter myFirestoreRecyclerAdapter;

    private FirebaseFirestore dbRef;

    private String[] expenseTimeFilters = {"All", "Today", "This Week", "This Month", "This Year"};
    private ArrayList<String> expenseCategoryFilters;

    private ExpenseIncomeEntry recentlyDeletedItem;
    private int recentlyDeletedItemPosition;
    private String recentlyDeletedDocumentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expense);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setting up the database reference
        this.dbRef = FirebaseFirestore.getInstance();

        // Setting up the time filter spinner
        this.time_filter_spinner = findViewById(R.id.time_filter_spinner);
        ArrayAdapter<String> time_filter_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, expenseTimeFilters);
        time_filter_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        time_filter_spinner.setAdapter(time_filter_adapter);

        this.time_filter_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateExpenses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setting up the category filter spinner
        this.expenseCategoryFilters = new ArrayList<>();
        expenseCategoryFilters.add("All Categories");
        this.category_filter_spinner = findViewById(R.id.category_filter_spinner);
        ArrayAdapter<String> cat_filter_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, expenseCategoryFilters);
        cat_filter_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category_filter_spinner.setAdapter(cat_filter_adapter);

        this.category_filter_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateExpenses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // get a list of category from the db collection category
        Query query = dbRef
                .collection(FirestoreReferences.CATEGORIES_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, "admin")
                .whereEqualTo(FirestoreReferences.CATEGORY_TYPE_FIELD, "expense");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().isEmpty()) {
                        Log.d(TAG, "No categories found");
                    } else {
                        String category = "";
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            category = document.get(FirestoreReferences.CATEGORY_NAME_FIELD, String.class);
                            expenseCategoryFilters.add(category);
                            cat_filter_adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        query = dbRef.collection(FirestoreReferences.EXPENSES_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, "admin");

        // Setting up the Recycler View for the list of Expenses
        this.recyclerView = findViewById(R.id.expensesRecyclerView);

        FirestoreRecyclerOptions<ExpenseIncomeEntry> options = new FirestoreRecyclerOptions.Builder<ExpenseIncomeEntry>()
                .setQuery(query, new SnapshotParser<ExpenseIncomeEntry>() {
                    @Nullable
                    @Override
                    public ExpenseIncomeEntry parseSnapshot(@Nullable DocumentSnapshot snapshot) {
                        ExpenseIncomeEntry temp = new ExpenseIncomeEntry(
                                snapshot.getId(),
                                snapshot.getString(FirestoreReferences.TITLE_FIELD),
                                snapshot.getString(FirestoreReferences.DESCRIPTION_FIELD),
                                snapshot.getString(FirestoreReferences.TIMESTAMP_FIELD),
                                snapshot.getDouble(FirestoreReferences.AMOUNT_FIELD),
                                snapshot.getString(FirestoreReferences.ACCOUNT_FIELD),
                                snapshot.getString(FirestoreReferences.CATEGORY_FIELD),
                                snapshot.getString(FirestoreReferences.USERNAME_FIELD),
                                "Expense"
                        );

                        return temp;
                    }
                })
                .build();

        this.myFirestoreRecyclerAdapter = new MyFirestoreRecyclerAdapter(options);
        this.myFirestoreRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.scrollToPosition(myFirestoreRecyclerAdapter.getItemCount() - 1);
            }
        });

        this.recyclerView.setAdapter(this.myFirestoreRecyclerAdapter);
        this.myFirestoreRecyclerAdapter.notifyItemRangeChanged(0, this.myFirestoreRecyclerAdapter.getItemCount());
        this.myFirestoreRecyclerAdapter.notifyDataSetChanged();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        this.recyclerView.setLayoutManager(linearLayoutManager);

        this.addExpenseButton = findViewById(R.id.addExpenseButton);
        this.addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExpenseActivity.this, AddExpenseActivity.class));
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

    private void deleteItemWithUndo(int position) {
        // Save details of the deleted item
        recentlyDeletedItem = myFirestoreRecyclerAdapter.getSnapshots().get(position);
        recentlyDeletedItemPosition = position;
        recentlyDeletedDocumentId = myFirestoreRecyclerAdapter.getSnapshots().getSnapshot(position).getId();

        // Remove the item from the adapter
        myFirestoreRecyclerAdapter.getSnapshots().getSnapshot(position).getReference().delete()
                .addOnSuccessListener(aVoid -> {
                    myFirestoreRecyclerAdapter.notifyItemRemoved(position);
                    myFirestoreRecyclerAdapter.notifyItemRangeChanged(0, myFirestoreRecyclerAdapter.getItemCount()-1);
                    myFirestoreRecyclerAdapter.notifyDataSetChanged();
                    showUndoSnackbar();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    myFirestoreRecyclerAdapter.notifyItemChanged(position);
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
            dbRef.collection("expenses") // Replace with your collection name
                    .document(recentlyDeletedDocumentId)
                    .set(recentlyDeletedItem)
                    .addOnSuccessListener(aVoid -> {
                        // Re-add the item to the adapter
                        myFirestoreRecyclerAdapter.notifyItemInserted(recentlyDeletedItemPosition);
                        myFirestoreRecyclerAdapter.notifyItemRangeChanged(0, myFirestoreRecyclerAdapter.getItemCount());
                        myFirestoreRecyclerAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Item restored", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to restore item", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateExpenses() {
        String selectedTimeFilter = time_filter_spinner.getSelectedItem().toString();
        String selectedCategoryFilter = category_filter_spinner.getSelectedItem().toString();

        Query expenseQuery = dbRef.collection(FirestoreReferences.EXPENSES_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, "admin"); // Replace with the current user's identifier

        // Apply time filter
        String today = getFormattedDate(Calendar.getInstance());
        switch (selectedTimeFilter) {
            case "Today":
                expenseQuery = expenseQuery.whereEqualTo(FirestoreReferences.TIMESTAMP_FIELD, today);
                break;
            case "This Week":
                expenseQuery = expenseQuery.whereGreaterThanOrEqualTo(FirestoreReferences.TIMESTAMP_FIELD, getFormattedDate(getStartOfWeek()));
                break;
            case "This Month":
                expenseQuery = expenseQuery.whereGreaterThanOrEqualTo(FirestoreReferences.TIMESTAMP_FIELD, getFormattedDate(getStartOfMonth()));
                break;
            case "This Year":
                expenseQuery = expenseQuery.whereGreaterThanOrEqualTo(FirestoreReferences.TIMESTAMP_FIELD, getFormattedDate(getStartOfYear()));
                break;
            default: // "All"
                break;
        }

        // Apply category filter if not "All Categories"
        if (!selectedCategoryFilter.equals("All Categories")) {
            expenseQuery = expenseQuery.whereEqualTo("category", selectedCategoryFilter);
        }

        FirestoreRecyclerOptions<ExpenseIncomeEntry> updatedOptions = new FirestoreRecyclerOptions.Builder<ExpenseIncomeEntry>()
                .setQuery(expenseQuery, new SnapshotParser<ExpenseIncomeEntry>() {
                    @Nullable
                    @Override
                    public ExpenseIncomeEntry parseSnapshot(@Nullable DocumentSnapshot snapshot) {
                        return new ExpenseIncomeEntry(
                                snapshot.getId(),
                                snapshot.getString(FirestoreReferences.TITLE_FIELD),
                                snapshot.getString(FirestoreReferences.DESCRIPTION_FIELD),
                                snapshot.getString(FirestoreReferences.TIMESTAMP_FIELD),
                                snapshot.getDouble(FirestoreReferences.AMOUNT_FIELD),
                                snapshot.getString(FirestoreReferences.ACCOUNT_FIELD),
                                snapshot.getString(FirestoreReferences.CATEGORY_FIELD),
                                snapshot.getString(FirestoreReferences.USERNAME_FIELD),
                                "expense"
                        );
                    }
                })
                .build();

        this.myFirestoreRecyclerAdapter.updateOptions(updatedOptions);
        this.myFirestoreRecyclerAdapter.notifyItemRangeChanged(0, this.myFirestoreRecyclerAdapter.getItemCount());
        this.myFirestoreRecyclerAdapter.notifyDataSetChanged();
    }

    private void editEntry(int position) {
        String documentId = myFirestoreRecyclerAdapter.getSnapshots().getSnapshot(position).getId();
        Intent intent = new Intent(this, EditEntryActivity.class);
        intent.putExtra("doc_id", documentId);
        intent.putExtra("title", myFirestoreRecyclerAdapter.getSnapshots().getSnapshot(position).getString(FirestoreReferences.TITLE_FIELD));
        intent.putExtra("description", myFirestoreRecyclerAdapter.getSnapshots().getSnapshot(position).getString(FirestoreReferences.DESCRIPTION_FIELD));
        intent.putExtra("date", myFirestoreRecyclerAdapter.getSnapshots().getSnapshot(position).getString(FirestoreReferences.TIMESTAMP_FIELD));
        intent.putExtra("amount", myFirestoreRecyclerAdapter.getSnapshots().getSnapshot(position).getDouble(FirestoreReferences.AMOUNT_FIELD).toString());
        intent.putExtra("account", myFirestoreRecyclerAdapter.getSnapshots().getSnapshot(position).getString(FirestoreReferences.ACCOUNT_FIELD));
        intent.putExtra("category", myFirestoreRecyclerAdapter.getSnapshots().getSnapshot(position).getString(FirestoreReferences.CATEGORY_FIELD));
        intent.putExtra("username", myFirestoreRecyclerAdapter.getSnapshots().getSnapshot(position).getString(FirestoreReferences.USERNAME_FIELD));
        intent.putExtra("entry_type", "expense");
        startActivity(intent);
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
        this.myFirestoreRecyclerAdapter.startListening();
        this.myFirestoreRecyclerAdapter.notifyItemRangeChanged(0, this.myFirestoreRecyclerAdapter.getItemCount());
        this.myFirestoreRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.myFirestoreRecyclerAdapter.stopListening();
    }
}