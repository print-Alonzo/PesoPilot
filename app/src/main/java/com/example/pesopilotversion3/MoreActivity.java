package com.example.pesopilotversion3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MoreActivity extends AppCompatActivity {
    private Button transfers, accounts, categories, edit_account, reset_account, logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_more);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.transfers = findViewById(R.id.transfers);
        this.accounts = findViewById(R.id.accounts);
        this.categories = findViewById(R.id.categories);
        this.edit_account = findViewById(R.id.edit_account);
        this.reset_account = findViewById(R.id.reset_account);
        this.logout = findViewById(R.id.logout);

        this.transfers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MoreActivity.this, TransferActivity.class);
                startActivity(intent);
            }
        });
    }
}