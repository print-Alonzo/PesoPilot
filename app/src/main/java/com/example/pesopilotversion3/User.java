package com.example.pesopilotversion3;

public class User {
    private long id;
    private String username;
    private String password;
    private double balance;

    private String firstName;
    private String lastName;

    // Empty Constructor for Firebase
    public User() {

    }

    public User(long id, String username, String password, double balance, String firstName, String lastName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
