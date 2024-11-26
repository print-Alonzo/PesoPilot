package com.example.pesopilotversion3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class EntryActivity extends AppCompatActivity {
    private String TAG = "EntryActivity";

    private TextView doc_id, title, description, date, amount, account, category, username, entry_type;
    private Button editExpenseButton, deleteExpenseButton;

    private FirebaseFirestore dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.doc_id = findViewById(R.id.entry_doc_id);
        this.title = findViewById(R.id.entry_title);
        this.description = findViewById(R.id.entry_description);
        this.date = findViewById(R.id.entry_date);
        this.amount = findViewById(R.id.entry_amount);
        this.account = findViewById(R.id.entry_account);
        this.category = findViewById(R.id.entry_category);
        this.username = findViewById(R.id.entry_username);
        this.entry_type = findViewById(R.id.entry_type);

        this.dbRef = FirebaseFirestore.getInstance();

        loadEntryDetails();

        this.editExpenseButton = findViewById(R.id.editExpenseButton);
        this.editExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EditEntryActivity.class);
                intent.putExtra("doc_id", doc_id.getText().toString());
                intent.putExtra("title", title.getText().toString());
                intent.putExtra("description", description.getText().toString());
                intent.putExtra("date", date.getText().toString());
                intent.putExtra("amount", amount.getText().toString());
                intent.putExtra("account", account.getText().toString());
                intent.putExtra("category", category.getText().toString());
                intent.putExtra("username", username.getText().toString());
                intent.putExtra("entry_type", entry_type.getText().toString());
                startActivity(intent);
            }
        });
    }

    private void loadEntryDetails() {
        String current_doc_id = getIntent().getExtras().getString("doc_id");
        String current_entry_type = getIntent().getExtras().getString("entry_type");
        this.doc_id.setText(current_doc_id);
        this.entry_type.setText(current_entry_type);

        DocumentReference doc;

        if (current_entry_type.equals("expense")) {
            doc = dbRef.collection(FirestoreReferences.EXPENSES_COLLECTION).document(current_doc_id);
        } else {
            doc = dbRef.collection(FirestoreReferences.INCOMES_COLLECTION).document(current_doc_id);
        }

        doc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    this.title.setText(task.getResult().getString(FirestoreReferences.TITLE_FIELD));
                    this.description.setText(task.getResult().getString(FirestoreReferences.DESCRIPTION_FIELD));
                    this.date.setText(task.getResult().getString(FirestoreReferences.TIMESTAMP_FIELD));
                    this.amount.setText(String.valueOf(task.getResult().getDouble(FirestoreReferences.AMOUNT_FIELD)));
                    this.account.setText(task.getResult().getString(FirestoreReferences.ACCOUNT_FIELD));
                    this.category.setText(task.getResult().getString(FirestoreReferences.CATEGORY_FIELD));
                    this.username.setText(task.getResult().getString(FirestoreReferences.USERNAME_FIELD));
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadEntryDetails();
    }


}