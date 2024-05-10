package com.example.taskaroo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExportTaskPDFActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_view);  // Ensure this layout exists and has a RecyclerView

        RecyclerView recyclerView = findViewById(R.id.calendarRecyclerView); // Ensure this ID exists in your layout
        recyclerView.setLayoutManager(new GridLayoutManager(this, 7)); // Grid layout with 7 columns for days of the week

        List<String> daysOfMonth = generateDaysOfMonth();
        CalendarAdapter adapter = new CalendarAdapter(daysOfMonth);
        recyclerView.setAdapter(adapter);
    }

    private List<String> generateDaysOfMonth() {
        List<String> days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // Set to the first day of the month
        calendar.set(year, month, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= daysInMonth; i++) {
            days.add(String.valueOf(i));
        }
        return days;
    }
}
