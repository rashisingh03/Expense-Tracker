package com.example.emanager.views.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.emanager.adapters.TransactionsAdapter;
import com.example.emanager.databinding.FragmentTransactionsBinding;
import com.example.emanager.utils.Constants;
import com.example.emanager.utils.Helper;
import com.example.emanager.viewmodels.MainViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;

public class TransactionsFragment extends Fragment {

    public TransactionsFragment() {
    }

    FragmentTransactionsBinding binding;
    Calendar calendar;
    public MainViewModel viewModel;
    TransactionsAdapter transactionsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        calendar = Calendar.getInstance();
        updateDate();

        binding.nextDateBtn.setOnClickListener(c -> {
            if (Constants.SELECTED_TAB == Constants.DAILY || Constants.SELECTED_TAB == Constants.CALENDAR) {
                calendar.add(Calendar.DATE, 1);
            } else if (Constants.SELECTED_TAB == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, 1);
            } else if (Constants.SELECTED_TAB == Constants.SUMMARY) {
                calendar.add(Calendar.YEAR, 1);
            }
            updateDate();
        });

        binding.previousDateBtn.setOnClickListener(c -> {
            if (Constants.SELECTED_TAB == Constants.DAILY || Constants.SELECTED_TAB == Constants.CALENDAR) {
                calendar.add(Calendar.DATE, -1);
            } else if (Constants.SELECTED_TAB == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, -1);
            } else if (Constants.SELECTED_TAB == Constants.SUMMARY) {
                calendar.add(Calendar.YEAR, -1);
            }
            updateDate();
        });

        binding.currentDate.setOnClickListener(view -> {
            if (Constants.SELECTED_TAB == Constants.CALENDAR) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
                datePickerDialog.setOnDateSetListener((datePicker, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.YEAR, year);
                    updateDate();
                });
                datePickerDialog.show();
            } else {
                Toast.makeText(getContext(), "Switch to Calendar tab to pick a specific date", Toast.LENGTH_SHORT).show();
            }
        });

        binding.floatingActionButton.setOnClickListener(c -> {
            new AddTransactionFragment().show(getParentFragmentManager(), null);
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() != null) {
                    String tabText = tab.getText().toString();
                    switch (tabText) {
                        case "Daily": Constants.SELECTED_TAB = Constants.DAILY; break;
                        case "Monthly": Constants.SELECTED_TAB = Constants.MONTHLY; break;
                        case "Calendar": Constants.SELECTED_TAB = Constants.CALENDAR; break;
                        case "Summary": Constants.SELECTED_TAB = Constants.SUMMARY; break;
                        case "Notes": Constants.SELECTED_TAB = Constants.NOTES; break;
                    }
                    updateDate();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        transactionsAdapter = new TransactionsAdapter(getActivity(), new ArrayList<>());
        binding.transactionsList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.transactionsList.setAdapter(transactionsAdapter);

        viewModel.transactions.observe(getViewLifecycleOwner(), transactions -> {
            transactionsAdapter.setTransactions(transactions);
            if (transactions != null && transactions.size() > 0) {
                binding.emptyState.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });

        viewModel.totalIncome.observe(getViewLifecycleOwner(), val -> binding.incomeLbl.setText(String.valueOf(val)));
        viewModel.totalExpense.observe(getViewLifecycleOwner(), val -> binding.expenseLbl.setText(String.valueOf(val)));
        viewModel.totalAmount.observe(getViewLifecycleOwner(), val -> binding.totalLbl.setText(String.valueOf(val)));

        return binding.getRoot();
    }

    void updateDate() {
        if (Constants.SELECTED_TAB == Constants.DAILY || Constants.SELECTED_TAB == Constants.CALENDAR) {
            binding.currentDate.setText(Helper.formatDate(calendar.getTime()));
        } else if (Constants.SELECTED_TAB == Constants.MONTHLY) {
            binding.currentDate.setText(Helper.formatDateByMonth(calendar.getTime()));
        } else if (Constants.SELECTED_TAB == Constants.SUMMARY) {
            binding.currentDate.setText("Yearly Summary - " + calendar.get(Calendar.YEAR));
        }
        viewModel.getTransactions(calendar);
    }
}
