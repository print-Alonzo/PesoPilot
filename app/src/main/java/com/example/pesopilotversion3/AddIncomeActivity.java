package com.example.pesopilotversion3;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class AddIncomeActivity extends AppCompatActivity {
    private static final String TAG = "AddIncomeActivity";

    // Views needed
    private EditText incomeTitleValue;
    private EditText incomeDescriptionValue;
    private EditText incomeAmountValue;
    private Spinner categorySpinner;
    private TextView textIncomeDateValue;
    private ImageView imageIncomeDateValue;
    private Spinner bankAccountSpinner;

    long date_milli;
    Context context;

    private Button submitIncomeButton;

    // DB reference
    private FirebaseFirestore dbRef;

    private ArrayList<String> categories;
    private ArrayList<String> bankAccounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_income);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.incomeTitleValue = findViewById(R.id.incomeTitleValue);
        this.incomeDescriptionValue = findViewById(R.id.incomeDescriptionValue);
        this.incomeAmountValue = findViewById(R.id.incomeAmountValue);
        this.categorySpinner = findViewById(R.id.categorySpinner);
        this.textIncomeDateValue = findViewById(R.id.textIncomeDateValue);
        this.imageIncomeDateValue = findViewById(R.id.imageIncomeDateValue);
        this.bankAccountSpinner = findViewById(R.id.bankAccountSpinner);
        this.submitIncomeButton = findViewById(R.id.submitIncomeButton);
        this.context = this;

        getdate();

        this.categories = new ArrayList<String>();
        this.bankAccounts = new ArrayList<String>();

        this.dbRef = FirebaseFirestore.getInstance();

        ArrayAdapter<String> category_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        category_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(category_adapter);

        ArrayAdapter<String> accounts_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bankAccounts);
        accounts_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bankAccountSpinner.setAdapter(accounts_adapter);

        // get a list of category from the db collection category
        Query query = dbRef
                .collection(FirestoreReferences.CATEGORIES_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, "admin")
                .whereEqualTo(FirestoreReferences.CATEGORY_TYPE_FIELD, "income");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        Log.d(TAG, "No categories found");
                    } else {
                        String category = "";
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            category = document.get(FirestoreReferences.CATEGORY_NAME_FIELD, String.class);
                            categories.add(category);
                            category_adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        this.submitIncomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> income = new HashMap<>();
                income.put(FirestoreReferences.TITLE_FIELD, incomeTitleValue.getText().toString());
                income.put(FirestoreReferences.DESCRIPTION_FIELD, incomeDescriptionValue.getText().toString());
                income.put(FirestoreReferences.TIMESTAMP_FIELD, textIncomeDateValue.getText().toString());
                income.put(FirestoreReferences.AMOUNT_FIELD, Double.parseDouble(incomeAmountValue.getText().toString()));
                income.put(FirestoreReferences.ACCOUNT_FIELD, bankAccountSpinner.getSelectedItem().toString());
                income.put(FirestoreReferences.CATEGORY_FIELD, categorySpinner.getSelectedItem().toString());
                income.put(FirestoreReferences.USERNAME_FIELD, "admin");

                dbRef.collection(FirestoreReferences.INCOMES_COLLECTION).add(income)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(
                                        context,
                                        "Income successfully added",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(
                                        context,
                                        "Error Adding Income",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });

                finish();
            }
        });


        // get a list of bank accounts from the db collection account
        query = dbRef
                .collection(FirestoreReferences.ACCOUNTS_COLLECTION)
                .whereEqualTo(FirestoreReferences.USERNAME_FIELD, "admin");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        Log.d(TAG, "No accounts found");
                    } else {
                        String account = "";
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            account = document.getString(FirestoreReferences.ACCOUNT_NAME_FIELD);
                            bankAccounts.add(account);
                            accounts_adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }

    private void getdate() {
        imageIncomeDateValue.setOnClickListener(new View.OnClickListener() {
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
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year,month,dayOfMonth);
                        date_milli = selectedDate.getTimeInMillis();
                        textIncomeDateValue.setText(year+"-"+String.format("%02d",month+1)+"-"+String.format("%02d",dayOfMonth));
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

        textIncomeDateValue.setText(year+"-"+String.format("%02d",month+1)+"-"+String.format("%02d",dayOfMonth));
    }

    @Override
    protected void onStart(){
        super.onStart();
        getCurrentDate();
    }
}