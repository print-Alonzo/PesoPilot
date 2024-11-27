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

public class EditTransferActivity extends AppCompatActivity {
    private String TAG = "EditTransferActivity";

    private EditText edit_title, edit_description, edit_amount;
    private TextView text_edit_date;
    private ImageView image_edit_date;
    private Spinner edit_sender_spinner, edit_receiver_spinner;
    private Button editEntryButton, cancelEditEntryButton;

    Context context;

    private FirebaseFirestore dbRef;
    private ArrayList<String> bankAccounts;

    private String doc_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_transfer);
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
        this.edit_sender_spinner = findViewById(R.id.edit_sender_spinner);
        this.edit_receiver_spinner = findViewById(R.id.edit_receiver_spinner);

        this.editEntryButton = findViewById(R.id.editEntryButton);
        this.cancelEditEntryButton = findViewById(R.id.cancelEditEntryButton);

        this.context = this;

        this.dbRef = FirebaseFirestore.getInstance();

        this.bankAccounts = new ArrayList<String>();

        getdate();

        Bundle extras = getIntent().getExtras();
        this.doc_id = extras.getString("doc_id");
        this.edit_title.setText(extras.getString("title"));
        this.edit_description.setText(extras.getString("description"));
        this.text_edit_date.setText(extras.getString("date"));
        this.edit_amount.setText(extras.getString("amount"));

        String username = extras.getString("username");
        loadAccounts(username);

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
        String sender = edit_sender_spinner.getSelectedItem().toString();
        String receiver = edit_receiver_spinner.getSelectedItem().toString();

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
        dbRef.collection(FirestoreReferences.TRANSFERS_COLLECTION)
                .document(doc_id)
                .update(
                        FirestoreReferences.TITLE_FIELD, title,
                        FirestoreReferences.AMOUNT_FIELD, amount,
                        FirestoreReferences.TIMESTAMP_FIELD, date,
                        FirestoreReferences.TRANSFER_SENDER_FIELD, sender,
                        FirestoreReferences.TRANSFER_RECEIVER_FIELD, receiver,
                        FirestoreReferences.DESCRIPTION_FIELD, description
                ).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditTransferActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditTransferActivity.this, "Failed to update entry", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadAccounts(String username) {
        Query query = dbRef
                .collection(FirestoreReferences.ACCOUNTS_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, username);

        ArrayAdapter<String> sender_accounts_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bankAccounts);
        sender_accounts_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.edit_sender_spinner.setAdapter(sender_accounts_adapter);

        ArrayAdapter<String> receiver_accounts_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bankAccounts);
        receiver_accounts_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.edit_receiver_spinner.setAdapter(receiver_accounts_adapter);

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
                        sender_accounts_adapter.notifyDataSetChanged();
                        receiver_accounts_adapter.notifyDataSetChanged();

                        int senderAccountPosition = bankAccounts.indexOf(getIntent().getExtras().getString("sender"));
                        int receiverAccountPosition = bankAccounts.indexOf(getIntent().getExtras().getString("receiver"));
                        edit_sender_spinner.setSelection(senderAccountPosition);
                        edit_receiver_spinner.setSelection(receiverAccountPosition);
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