package com.example.pesopilotversion3;


public class FirestoreReferences {

    public static final String
        USERS_COLLECTION = "users",
        INCOMES_COLLECTION = "incomes",
        EXPENSES_COLLECTION = "expenses",
        CATEGORIES_COLLECTION = "categories",
        ACCOUNTS_COLLECTION = "accounts",

        // Fields for User collection
        USERNAME_FIELD = "username",
        PASSWORD_FIELD = "password",
        BALANCE_FIELD = "balance",
        FIRST_NAME_FIELD = "firstName",
        LAST_NAME_FIELD = "lastName",

        // Fields for Income collection
        INCOME_AMOUNT_FIELD = "amount",
        INCOME_CATEGORY_FIELD = "category",
        INCOME_TIMESTAMP_FIELD = "timestamp",

        // Fields for Expense collection
        EXPENSE_AMOUNT_FIELD = "amount",
        EXPENSE_CATEGORY_FIELD = "category",
        EXPENSE_TIMESTAMP_FIELD = "timestamp",

        // Fields for Category collection
        CATEGORY_NAME_FIELD = "category",
        CATEGORY_BUDGET_FIELD = "budget",
        CATEGORY_TYPE_FIELD = "type",

        // Fields for Account collection
        ACCOUNT_NAME_FIELD = "accountName",
        ACCOUNT_BALANCE_FIELD = "accountAmount";
}
