package com.example.pesopilotversion3;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class AddTransferActivity extends AppCompatActivity {
    private static final String TAG = "AddTransferActivity";

    // Views needed
    private EditText transferTitleValue;
    private EditText transferDescriptionValue;
    private EditText transferAmountValue;
    private Spinner senderAccountSpinner;
    private Spinner receiverAccountSpinner;
    private TextView textTransferDateValue;
    private ImageView imageTransferDateValue;

    Context context;

    private Button submitTransferButton;

    // DB reference
    private FirebaseFirestore dbRef;

    private ArrayList<String> bankAccounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_transfer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_transfer_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.transferTitleValue = findViewById(R.id.transferTitleValue);
        this.transferDescriptionValue = findViewById(R.id.transferDescriptionValue);
        this.transferAmountValue = findViewById(R.id.transferAmountValue);
        this.senderAccountSpinner = findViewById(R.id.senderAccountSpinner);
        this.receiverAccountSpinner = findViewById(R.id.receiverAccountSpinner);
        this.textTransferDateValue = findViewById(R.id.textTransferDateValue);
        this.imageTransferDateValue = findViewById(R.id.imageTransferDateValue);
        this.submitTransferButton = findViewById(R.id.submitTransferButton);
        this.context = this;

        this.bankAccounts = new ArrayList<String>();

        getdate();

        this.dbRef = FirebaseFirestore.getInstance();

        loadAccounts();

        this.submitTransferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitTransfer();
            }
        });
    }

    private void submitTransfer() {
        Map<String, Object> transfer = new HashMap<>();
        transfer.put(FirestoreReferences.TITLE_FIELD, transferTitleValue.getText().toString());
        transfer.put(FirestoreReferences.DESCRIPTION_FIELD, transferDescriptionValue.getText().toString());
        transfer.put(FirestoreReferences.TIMESTAMP_FIELD, textTransferDateValue.getText().toString());
        transfer.put(FirestoreReferences.AMOUNT_FIELD, Double.parseDouble(transferAmountValue.getText().toString()));
        transfer.put(FirestoreReferences.TRANSFER_SENDER_FIELD, senderAccountSpinner.getSelectedItem().toString());
        transfer.put(FirestoreReferences.TRANSFER_RECEIVER_FIELD, receiverAccountSpinner.getSelectedItem().toString());
        transfer.put(FirestoreReferences.USERNAME_FIELD, "admin");

        dbRef.collection(FirestoreReferences.TRANSFERS_COLLECTION).add(transfer)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(
                                context,
                                "Transfer successfully added",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(
                                context,
                                "Error Adding Transfer",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

        finish();
    }

    private void loadAccounts() {
        bankAccounts.add("Any");
        ArrayAdapter<String> sender_account_filter_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bankAccounts);
        sender_account_filter_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.senderAccountSpinner.setAdapter(sender_account_filter_adapter);

        ArrayAdapter<String> receiver_account_filter_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bankAccounts);
        receiver_account_filter_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.receiverAccountSpinner.setAdapter(receiver_account_filter_adapter);

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
                            bankAccounts.add(account);
                        }
                        sender_account_filter_adapter.notifyDataSetChanged();
                        receiver_account_filter_adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void getdate() {
        imageTransferDateValue.setOnClickListener(new View.OnClickListener() {
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
                        textTransferDateValue.setText(year+"-"+String.format("%02d",month+1)+"-"+String.format("%02d",dayOfMonth));
                    }
                },y,m,d);
                dialog.show();
            }
        });
    }

    private void getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        textTransferDateValue.setText(year+"-"+String.format("%02d",month+1)+"-"+String.format("%02d",dayOfMonth));
    }

    @Override
    protected void onStart(){
        super.onStart();
        getCurrentDate();
    }
}