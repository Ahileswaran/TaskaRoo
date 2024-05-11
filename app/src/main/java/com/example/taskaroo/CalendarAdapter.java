package com.example.taskaroo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<String> daysOfMonth; // List of day numbers for the month
    private List<Task> tasksList; // List of tasks

    public CalendarAdapter(List<String> daysOfMonth, List<Task> tasksList) {
        this.daysOfMonth = daysOfMonth;
        this.tasksList = tasksList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.calendar_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String day = daysOfMonth.get(position);
        Task task = tasksList.get(position);
        holder.bind(day, task);
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private TextView taskTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.dayTextView);
            taskTextView = itemView.findViewById(R.id.taskTextView);
        }

        public void bind(String day, Task task) {
            textView.setText(day);
            if (task != null) {
                taskTextView.setVisibility(View.VISIBLE);
                taskTextView.setText(task.getName());
            } else {
                taskTextView.setVisibility(View.GONE);
            }
        }
    }

    public void pinTask(int position, Task task) {
        // Ensure the position is within bounds
        if (position >= 0 && position < tasksList.size()) {
            // Update the task list with the new task
            tasksList.set(position, task);
            // Notify the adapter about the change
            notifyItemChanged(position);
        }
    }
}
