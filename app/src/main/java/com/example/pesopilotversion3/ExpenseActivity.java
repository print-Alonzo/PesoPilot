package com.example.pesopilotversion3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ExpenseActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MyFirestoreRecyclerAdapter myFirestoreRecyclerAdapter;

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

        this.recyclerView = findViewById(R.id.expensesRecyclerView);

        Query query = FirebaseFirestore.getInstance()
                .collection("expenses")
                .whereEqualTo("username", "admin");

        FirestoreRecyclerOptions<ExpenseIncomeEntry> options = new FirestoreRecyclerOptions.Builder<ExpenseIncomeEntry>()
                .setQuery(query, new SnapshotParser<ExpenseIncomeEntry>() {
                    @Nullable
                    @Override
                    public ExpenseIncomeEntry parseSnapshot(@Nullable DocumentSnapshot snapshot) {
                        ExpenseIncomeEntry temp = new ExpenseIncomeEntry(
                                snapshot.getString("title"),
                                snapshot.getString("timestamp"),
                                snapshot.getDouble("amount"),
                                snapshot.getString("account"),
                                snapshot.getString("category")
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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        this.recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.myFirestoreRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.myFirestoreRecyclerAdapter.stopListening();
    }
}