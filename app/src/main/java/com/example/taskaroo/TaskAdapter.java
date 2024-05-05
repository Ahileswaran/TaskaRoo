package com.example.taskaroo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<Task> tasks;
    private OnTaskClickListener listener;

    public TaskAdapter() {
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewTaskName;
        private TextView textViewDescription;
        private TextView textViewDate;
        private TextView textViewTime;
        private ImageView imageButtonCheck; // Assuming you have an ImageView for the check icon

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTaskName = itemView.findViewById(R.id.textViewTaskName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            imageButtonCheck = itemView.findViewById(R.id.imageButtonCheck); // Linking the ImageView

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onTaskClick(tasks.get(position));
                }
            });
        }

        public void bind(Task task) {
            textViewTaskName.setText(task.getName());
            textViewDescription.setText(task.getDescription());
            textViewDate.setText(task.getDate());
            textViewTime.setText(task.getTime());

            // Check if the task's date and time passed the current date and time
            if (isTaskDueNow(task)) {
                imageButtonCheck.setVisibility(View.VISIBLE);
            } else {
                imageButtonCheck.setVisibility(View.GONE);
            }
        }

        private boolean isTaskDueNow(Task task) {
            // Get the current date and time
            Calendar now = Calendar.getInstance();

            try {
                // Parse the task's due date and time into the format "yyyy-MM-dd HH:mm"
                SimpleDateFormat sdfInput = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date dueDate = sdfInput.parse(task.getDate() + " " + task.getTime());

                // Create a new SimpleDateFormat object to ensure consistent formatting
                SimpleDateFormat sdfComparison = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String formattedTaskDateTime = sdfComparison.format(dueDate);

                // Parse the formatted date and time string back into a Date object
                dueDate = sdfComparison.parse(formattedTaskDateTime);

                // Convert the parsed Date objects to Calendar objects
                Calendar taskDue = Calendar.getInstance();
                taskDue.setTime(dueDate);

                // Check if the task's due date and time have passed the current date and time
                return now.after(taskDue);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return false;
        }


    }
}
