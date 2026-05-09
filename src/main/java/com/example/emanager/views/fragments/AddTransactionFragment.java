package com.example.emanager.views.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.emanager.R;
import com.example.emanager.adapters.AccountsAdapter;
import com.example.emanager.adapters.CategoryAdapter;
import com.example.emanager.databinding.FragmentAddTransactionBinding;
import com.example.emanager.databinding.ListDialogBinding;
import com.example.emanager.models.Account;
import com.example.emanager.models.Category;
import com.example.emanager.models.Transaction;
import com.example.emanager.utils.Constants;
import com.example.emanager.utils.Helper;
import com.example.emanager.views.activites.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddTransactionFragment extends BottomSheetDialogFragment {

    public AddTransactionFragment() {
    }

    FragmentAddTransactionBinding binding;
    Transaction transaction;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater);

        transaction = new Transaction();
        // ENSURE DEFAULTS ARE SET IMMEDIATELY
        transaction.setType(Constants.EXPENSE);
        transaction.setDate(new Date());
        transaction.setId(new Date().getTime());

        binding.date.setText(Helper.formatDate(transaction.getDate()));

        binding.incomeBtn.setOnClickListener(view -> {
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.income_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.textColor));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.greenColor));
            transaction.setType(Constants.INCOME);
        });

        binding.expenseBtn.setOnClickListener(view -> {
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.expense_selector));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.textColor));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.redColor));
            transaction.setType(Constants.EXPENSE);
        });

        binding.date.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
            datePickerDialog.setOnDateSetListener((datePicker, year, month, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.YEAR, year);
                String dateToShow = Helper.formatDate(calendar.getTime());
                binding.date.setText(dateToShow);
                transaction.setDate(calendar.getTime());
                transaction.setId(calendar.getTime().getTime());
            });
            datePickerDialog.show();
        });

        binding.category.setOnClickListener(c -> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog categoryDialog = new AlertDialog.Builder(getContext()).create();
            categoryDialog.setView(dialogBinding.getRoot());
            CategoryAdapter categoryAdapter = new CategoryAdapter(getContext(), Constants.categories, category -> {
                binding.category.setText(category.getCategoryName());
                transaction.setCategory(category.getCategoryName());
                categoryDialog.dismiss();
            });
            dialogBinding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            dialogBinding.recyclerView.setAdapter(categoryAdapter);
            categoryDialog.show();
        });

        binding.account.setOnClickListener(c -> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog accountsDialog = new AlertDialog.Builder(getContext()).create();
            accountsDialog.setView(dialogBinding.getRoot());
            ArrayList<Account> accounts = new ArrayList<>();
            accounts.add(new Account(0, "Cash"));
            accounts.add(new Account(0, "Bank"));
            accounts.add(new Account(0, "Card"));
            accounts.add(new Account(0, "Other"));
            AccountsAdapter adapter = new AccountsAdapter(getContext(), accounts, account -> {
                binding.account.setText(account.getAccountName());
                transaction.setAccount(account.getAccountName());
                accountsDialog.dismiss();
            });
            dialogBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            dialogBinding.recyclerView.setAdapter(adapter);
            accountsDialog.show();
        });

        binding.saveTransactionBtn.setOnClickListener(c -> {
            String amountStr = binding.amount.getText().toString();
            if (amountStr.isEmpty()) {
                binding.amount.setError("Enter amount");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (transaction.getCategory() == null) {
                Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }
            if (transaction.getAccount() == null) {
                Toast.makeText(getContext(), "Please select an account", Toast.LENGTH_SHORT).show();
                return;
            }

            if (Constants.EXPENSE.equals(transaction.getType())) {
                transaction.setAmount(amount * -1);
            } else {
                transaction.setAmount(amount);
            }
            transaction.setNote(binding.note.getText().toString());

            ((MainActivity) getActivity()).viewModel.addTransaction(transaction);
            ((MainActivity) getActivity()).getTransactions();
            dismiss();
        });

        return binding.getRoot();
    }
}
