package com.example.pesopilotversion3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // Views needed
    private TextView remainingBalanceTV;
    private TextView totalIncomeTV;
    private TextView totalExpenseTV;
    private Button addIncomeBtn;
    private Button addExpenseBtn;

    private LinearLayout expenses;

    private double totalIncome;
    private double totalExpense;

    // DB reference
    private FirebaseFirestore dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.remainingBalanceTV = findViewById(R.id.remainingBalanceValue);
        this.totalIncomeTV = findViewById(R.id.totalIncomeValue);
        this.totalExpenseTV = findViewById(R.id.totalExpenseValue);
        this.addIncomeBtn = findViewById(R.id.addIncomeButton);
        this.addExpenseBtn = findViewById(R.id.addExpenseButton);

        this.expenses = findViewById(R.id.expenses);

        this.totalIncome = 0;
        this.totalExpense = 0;

        this.dbRef = FirebaseFirestore.getInstance();

        updateBalances();

        this.addIncomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddIncomeActivity.class);
                startActivity(intent);
            }
        });

        this.addExpenseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                startActivity(intent);
            }
        });

        this.expenses.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(MainActivity.this, ExpenseActivity.class);
                 startActivity(intent);
             }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // listen for changes in user balance
        updateBalances();
    }

    protected void updateBalances() {
        setTotalIncome();
    }

    protected void setRemainingBalance() {
        // set remaining balance
        Query query = dbRef
                .collection(FirestoreReferences.USERS_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, "admin")
                .whereEqualTo(FirestoreReferences.PASSWORD_FIELD, "12345");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().isEmpty()) {
                        remainingBalanceTV.setText("0.00");
                        totalIncomeTV.setText("0.00");
                        totalExpenseTV.setText("0.00");
                    } else {
                        Double initial_balance = task.getResult().getDocuments().get(0).get(FirestoreReferences.BALANCE_FIELD, Double.class);
                        initial_balance -= totalExpense;
                        initial_balance += totalIncome;
                        remainingBalanceTV.setText(initial_balance.toString());
                    }
                }
            }
        });
    }

    protected void setTotalIncome() {
        // set total income
        Query query = dbRef
                .collection(FirestoreReferences.INCOMES_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, "admin");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().isEmpty()) {
                        totalIncomeTV.setText("0.00");
                    } else {
                        totalIncome = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            totalIncome += document.getDouble(FirestoreReferences.AMOUNT_FIELD);
                        }
                        totalIncomeTV.setText(String.valueOf(totalIncome));
                        setTotalExpense();
                    }
                }
            }
        });
    }

    protected void setTotalExpense() {
        // set total expense
        Query query = dbRef
                .collection(FirestoreReferences.EXPENSES_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, "admin");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().isEmpty()) {
                        totalExpenseTV.setText("0.00");
                    } else {
                        totalExpense = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            totalExpense += document.getDouble(FirestoreReferences.AMOUNT_FIELD);
                        }
                        totalExpenseTV.setText(String.valueOf(totalExpense));
                        setRemainingBalance();
                    }
                }
            }
        });
    }
}