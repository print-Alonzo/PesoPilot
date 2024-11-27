package com.example.pesopilotversion3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class EntryActivity extends AppCompatActivity {
    private String TAG = "EntryActivity";

    private TextView doc_id, title, description, date, amount, account, category, username, entry_type;
    private Button editExpenseButton, deleteExpenseButton;

    private FirebaseFirestore dbRef;

    private String entryDocumentId;

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

        this.entryDocumentId = getIntent().getStringExtra("doc_id");

        loadEntryDetails();

        this.editExpenseButton = findViewById(R.id.editExpenseButton);
        this.editExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editEntry();
            }
        });

        this.deleteExpenseButton = findViewById(R.id.deleteExpenseButton);
        this.deleteExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteEntry();
            }
        });

    }

    private void confirmDeleteEntry() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEntry())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteEntry() {
        if (entryDocumentId == null || entryDocumentId.isEmpty()) {
            Toast.makeText(this, "Invalid entry ID", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference collection;
        if (entry_type.getText().toString().equals("expense"))
            collection = dbRef.collection(FirestoreReferences.EXPENSES_COLLECTION);
        else
            collection = dbRef.collection(FirestoreReferences.INCOMES_COLLECTION);

        collection.document(entryDocumentId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Entry successfully deleted.");
                            Toast.makeText(EntryActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity after deletion
                        } else {
                            Log.e(TAG, "Error deleting entry", task.getException());
                            Toast.makeText(EntryActivity.this, "Error deleting entry", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        finish();
    }

    private void editEntry() {
        Intent intent = new Intent(EntryActivity.this, EditEntryActivity.class);
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