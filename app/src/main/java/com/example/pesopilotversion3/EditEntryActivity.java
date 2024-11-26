package com.example.pesopilotversion3;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class EditEntryActivity extends AppCompatActivity {
    private String TAG = "EditEntryActivity";

    private EditText edit_title, edit_description, edit_amount;
    private TextView text_edit_date;
    private ImageView image_edit_date;
    private Spinner edit_account_spinner, edit_category_spinner;
    private Button editEntryButton, cancelEditEntryButton;

    Context context;

    private FirebaseFirestore dbRef;
    private ArrayList<String> categories;
    private ArrayList<String> bankAccounts;

    private String doc_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_entry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.edit_title = findViewById(R.id.edit_title);
        this.edit_description = findViewById(R.id.edit_description);
        this.text_edit_date = findViewById(R.id.text_edit_date);
        this.image_edit_date = findViewById(R.id.image_edit_date);
        this.edit_amount = findViewById(R.id.edit_amount);
        this.edit_account_spinner = findViewById(R.id.edit_account_spinner);
        this.edit_category_spinner = findViewById(R.id.edit_category_spinner);

        this.editEntryButton = findViewById(R.id.editEntryButton);
        this.cancelEditEntryButton = findViewById(R.id.cancelEditEntryButton);

        this.context = this;

        this.dbRef = FirebaseFirestore.getInstance();

        this.categories = new ArrayList<String>();
        this.bankAccounts = new ArrayList<String>();

        getdate();

        Bundle extras = getIntent().getExtras();
        this.doc_id = extras.getString("doc_id");
        this.edit_title.setText(extras.getString("title"));
        this.edit_description.setText(extras.getString("description"));
        this.text_edit_date.setText(extras.getString("date"));
        this.edit_amount.setText(extras.getString("amount"));

        String username = extras.getString("username");
        String type = extras.getString("entry_type");
        loadAccounts(username);
        loadCategories(username, type);

        this.editEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEntry();
            }
        });

        this.cancelEditEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void saveEntry() {
        String title = edit_title.getText().toString().trim();
        String description = edit_description.getText().toString().trim();
        String amountStr = edit_amount.getText().toString().trim();
        String date = text_edit_date.getText().toString().trim();
        String category = edit_category_spinner.getSelectedItem().toString();
        String account = edit_account_spinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update expense in Firestore
        DocumentReference expenseDocRef = dbRef.collection(FirestoreReferences.EXPENSES_COLLECTION).document(doc_id);

        expenseDocRef.update(
                "title", title,
                "amount", amount,
                "timestamp", date,
                "category", category,
                "account", account,
                "description", description
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(EditEntryActivity.this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditEntryActivity.this, "Failed to update expense", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadCategories(String username, String type) {
        Query query = dbRef
                .collection(FirestoreReferences.CATEGORIES_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, username)
                .whereEqualTo(FirestoreReferences.CATEGORY_TYPE_FIELD, type);

        ArrayAdapter<String> category_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        category_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.edit_category_spinner.setAdapter(category_adapter);

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
                            categories.add(category);
                        }
                        category_adapter.notifyDataSetChanged();

                        int categoryPosition = categories.indexOf(getIntent().getExtras().getString("category"));
                        edit_category_spinner.setSelection(categoryPosition);
                    }
                }
            }
        });
    }

    private void loadAccounts(String username) {
        Query query = dbRef
                .collection(FirestoreReferences.ACCOUNTS_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, username);

        ArrayAdapter<String> accounts_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bankAccounts);
        accounts_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.edit_account_spinner.setAdapter(accounts_adapter);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().isEmpty()) {
                        Log.d(TAG, "No accounts found");
                    } else {
                        String account = "";
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            account = document.getString(FirestoreReferences.ACCOUNT_NAME_FIELD);
                            bankAccounts.add(account);
                        }
                        accounts_adapter.notifyDataSetChanged();

                        int accountPosition = bankAccounts.indexOf(getIntent().getExtras().getString("account"));
                        edit_account_spinner.setSelection(accountPosition);
                    }
                }
            }
        });
    }

    private void getdate() {
        image_edit_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimeZone timeZone = TimeZone.getTimeZone("Asia/Colombo");
                Calendar calendar = Calendar.getInstance(timeZone);
                int y = calendar.get(Calendar.YEAR);
                int m = calendar.get(Calendar.MONTH);
                int d = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog=new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        text_edit_date.setText(year+"-"+String.format("%02d",month+1)+"-"+String.format("%02d",dayOfMonth));
                    }
                },y,m,d);
                dialog.show();
            }
        });
    }
}