package com.example.emanager.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.emanager.models.Transaction;
import com.example.emanager.utils.Constants;

import java.util.Calendar;
import java.util.Date;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainViewModel extends AndroidViewModel {

    public MutableLiveData<RealmResults<Transaction>> transactions = new MutableLiveData<>();
    public MutableLiveData<RealmResults<Transaction>> categoriesTransactions = new MutableLiveData<>();

    public MutableLiveData<Double> totalIncome = new MutableLiveData<>();
    public MutableLiveData<Double> totalExpense = new MutableLiveData<>();
    public MutableLiveData<Double> totalAmount = new MutableLiveData<>();

    Realm realm;
    Calendar calendar;

    public MainViewModel(@NonNull Application application) {
        super(application);
        Realm.init(application);
        setupDatabase();
    }

    public void getTransactions(Calendar calendar, String type) {
        this.calendar = (Calendar) calendar.clone();
        this.calendar.set(Calendar.HOUR_OF_DAY, 0);
        this.calendar.set(Calendar.MINUTE, 0);
        this.calendar.set(Calendar.SECOND, 0);
        this.calendar.set(Calendar.MILLISECOND, 0);

        RealmResults<Transaction> newTransactions = null;
        if (Constants.SELECTED_TAB_STATS == Constants.DAILY) {
            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", this.calendar.getTime())
                    .lessThan("date", new Date(this.calendar.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .equalTo("type", type)
                    .findAll();

        } else if (Constants.SELECTED_TAB_STATS == Constants.MONTHLY) {
            this.calendar.set(Calendar.DAY_OF_MONTH, 1);
            Date startTime = this.calendar.getTime();

            this.calendar.add(Calendar.MONTH, 1);
            Date endTime = this.calendar.getTime();

            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .equalTo("type", type)
                    .findAll();
        }

        categoriesTransactions.setValue(newTransactions);
    }

    public void getTransactions(Calendar calendar) {
        this.calendar = (Calendar) calendar.clone();
        this.calendar.set(Calendar.HOUR_OF_DAY, 0);
        this.calendar.set(Calendar.MINUTE, 0);
        this.calendar.set(Calendar.SECOND, 0);
        this.calendar.set(Calendar.MILLISECOND, 0);

        double income = 0;
        double expense = 0;
        double total = 0;
        RealmResults<Transaction> newTransactions = null;

        if (Constants.SELECTED_TAB == Constants.DAILY || Constants.SELECTED_TAB == Constants.CALENDAR) {
            Date date = this.calendar.getTime();
            Date nextDate = new Date(date.getTime() + (24 * 60 * 60 * 1000));

            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", date)
                    .lessThan("date", nextDate)
                    .findAll();

            income = calculateSum(date, nextDate, Constants.INCOME);
            expense = calculateSum(date, nextDate, Constants.EXPENSE);
            total = calculateSum(date, nextDate, null);

        } else if (Constants.SELECTED_TAB == Constants.MONTHLY) {
            this.calendar.set(Calendar.DAY_OF_MONTH, 1);
            Date startTime = this.calendar.getTime();
            this.calendar.add(Calendar.MONTH, 1);
            Date endTime = this.calendar.getTime();

            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .findAll();

            income = calculateSum(startTime, endTime, Constants.INCOME);
            expense = calculateSum(startTime, endTime, Constants.EXPENSE);
            total = calculateSum(startTime, endTime, null);

        } else if (Constants.SELECTED_TAB == Constants.SUMMARY) {
            this.calendar.set(Calendar.DAY_OF_YEAR, 1);
            Date startTime = this.calendar.getTime();
            this.calendar.add(Calendar.YEAR, 1);
            Date endTime = this.calendar.getTime();

            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .findAll();

            income = calculateSum(startTime, endTime, Constants.INCOME);
            expense = calculateSum(startTime, endTime, Constants.EXPENSE);
            total = calculateSum(startTime, endTime, null);
        } else if (Constants.SELECTED_TAB == Constants.NOTES) {
            newTransactions = realm.where(Transaction.class)
                    .isNotEmpty("note")
                    .findAll();

            income = realm.where(Transaction.class).isNotEmpty("note").equalTo("type", Constants.INCOME).sum("amount").doubleValue();
            expense = realm.where(Transaction.class).isNotEmpty("note").equalTo("type", Constants.EXPENSE).sum("amount").doubleValue();
            total = realm.where(Transaction.class).isNotEmpty("note").sum("amount").doubleValue();
        }

        totalIncome.setValue(income);
        totalExpense.setValue(expense);
        totalAmount.setValue(total);
        transactions.setValue(newTransactions);
    }

    private double calculateSum(Date start, Date end, String type) {
        Number sum;
        if (type == null) {
            sum = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", start)
                    .lessThan("date", end)
                    .sum("amount");
        } else {
            sum = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", start)
                    .lessThan("date", end)
                    .equalTo("type", type)
                    .sum("amount");
        }
        return sum != null ? sum.doubleValue() : 0.0;
    }

    public void searchTransactions(String query) {
        RealmResults<Transaction> results = realm.where(Transaction.class)
                .contains("note", query, Case.INSENSITIVE)
                .or()
                .contains("category", query, Case.INSENSITIVE)
                .findAll();
        transactions.setValue(results);
    }

    public void addTransaction(Transaction transaction) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(transaction);
        realm.commitTransaction();
    }

    public void deleteTransaction(Transaction transaction) {
        realm.beginTransaction();
        transaction.deleteFromRealm();
        realm.commitTransaction();
        getTransactions(calendar);
    }

    void setupDatabase() {
        realm = Realm.getDefaultInstance();
    }
}
