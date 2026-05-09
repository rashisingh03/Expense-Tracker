package com.example.emanager.views.fragments;

import static com.example.emanager.utils.Constants.SELECTED_STATS_TYPE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anychart.AnyChart;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.example.emanager.R;
import com.example.emanager.databinding.FragmentStatsBinding;
import com.example.emanager.models.Transaction;
import com.example.emanager.utils.Constants;
import com.example.emanager.utils.Helper;
import com.example.emanager.viewmodels.MainViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsFragment extends Fragment {

    public StatsFragment() {
        // Required empty public constructor
    }

    FragmentStatsBinding binding;
    Calendar calendar;
    public MainViewModel viewModel;
    Pie pie;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        calendar = Calendar.getInstance();
        
        // Initialize Pie chart object
        pie = AnyChart.pie();
        binding.anyChart.setChart(pie);

        updateDate();

        binding.incomeBtn.setOnClickListener(view -> {
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.income_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.textColor));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.greenColor));

            SELECTED_STATS_TYPE = Constants.INCOME;
            updateDate();
        });

        binding.expenseBtn.setOnClickListener(view -> {
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.expense_selector));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.textColor));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.redColor));

            SELECTED_STATS_TYPE = Constants.EXPENSE;
            updateDate();
        });

        binding.nextDateBtn.setOnClickListener(c -> {
            if (Constants.SELECTED_TAB_STATS == Constants.DAILY) {
                calendar.add(Calendar.DATE, 1);
            } else if (Constants.SELECTED_TAB_STATS == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, 1);
            }
            updateDate();
        });

        binding.previousDateBtn.setOnClickListener(c -> {
            if (Constants.SELECTED_TAB_STATS == Constants.DAILY) {
                calendar.add(Calendar.DATE, -1);
            } else if (Constants.SELECTED_TAB_STATS == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, -1);
            }
            updateDate();
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() != null) {
                    if (tab.getText().equals("Monthly")) {
                        Constants.SELECTED_TAB_STATS = 1;
                    } else if (tab.getText().equals("Daily")) {
                        Constants.SELECTED_TAB_STATS = 0;
                    }
                    updateDate();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewModel.categoriesTransactions.observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                binding.emptyState.setVisibility(View.GONE);
                binding.anyChart.setVisibility(View.VISIBLE);

                List<DataEntry> data = new ArrayList<>();
                Map<String, Double> categoryMap = new HashMap<>();

                for (Transaction transaction : transactions) {
                    String category = transaction.getCategory();
                    double amount = Math.abs(transaction.getAmount());

                    if (categoryMap.containsKey(category)) {
                        categoryMap.put(category, categoryMap.get(category) + amount);
                    } else {
                        categoryMap.put(category, amount);
                    }
                }

                for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
                    data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));
                }
                
                // Set data only if view is visible to avoid NullPointer in AnyChart
                if (binding.anyChart.getVisibility() == View.VISIBLE) {
                    pie.data(data);
                }
            } else {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.anyChart.setVisibility(View.GONE);
            }
        });

        return binding.getRoot();
    }

    void updateDate() {
        if (Constants.SELECTED_TAB_STATS == Constants.DAILY) {
            binding.currentDate.setText(Helper.formatDate(calendar.getTime()));
        } else if (Constants.SELECTED_TAB_STATS == Constants.MONTHLY) {
            binding.currentDate.setText(Helper.formatDateByMonth(calendar.getTime()));
        }
        viewModel.getTransactions(calendar, SELECTED_STATS_TYPE);
    }
}
