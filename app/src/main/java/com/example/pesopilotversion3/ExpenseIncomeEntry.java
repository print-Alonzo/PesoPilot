package com.example.pesopilotversion3;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.type.Date;

public class ExpenseIncomeEntry {
    private String title;
    private String date;
    private double amount;
    private String account;
    private String category;


    // Empty Constructor for Firebase
    public ExpenseIncomeEntry() {

    }

    public ExpenseIncomeEntry(String title, String date, double amount, String account, String category) {
        this.title = title;
        this.date = date;
        this.amount = amount;
        this.account = account;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public String getAccount() {
        return account;
    }

    public String getCategory() {
        return category;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
