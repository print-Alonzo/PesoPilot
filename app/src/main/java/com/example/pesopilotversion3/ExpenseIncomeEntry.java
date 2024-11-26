package com.example.pesopilotversion3;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.type.Date;

public class ExpenseIncomeEntry {
    private String doc_id;
    private String title;
    private String description;
    private String date;
    private double amount;
    private String account;
    private String category;
    private String username;
    private String entry_type;


    // Empty Constructor for Firebase
    public ExpenseIncomeEntry() {

    }

    public ExpenseIncomeEntry(String doc_id, String title, String description, String date, double amount, String account, String category, String username, String entry_type) {
        this.doc_id = doc_id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.amount = amount;
        this.account = account;
        this.category = category;
        this.username = username;
        this.entry_type = entry_type;
    }

    public String getDoc_id() {
        return doc_id;
    }

    public void setDoc_id(String doc_id) {
        this.doc_id = doc_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEntry_type() {
        return entry_type;
    }

    public void setEntry_type(String entry_type) {
        this.entry_type = entry_type;
    }
}
