package com.example.pesopilotversion3;

public class TransferEntry {
    private String doc_id;
    private String title;
    private String description;
    private String date;
    private double amount;
    private String sender;
    private String receiver;
    private String username;

    // Empty Constructor for Firebase
    public TransferEntry() {

    }

    public TransferEntry(String doc_id, String title, String description, String date, double amount, String sender, String receiver, String username) {
        this.doc_id = doc_id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.amount = amount;
        this.sender = sender;
        this.receiver = receiver;
        this.username = username;
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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
