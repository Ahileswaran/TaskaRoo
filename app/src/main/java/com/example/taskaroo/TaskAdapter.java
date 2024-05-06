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
        private ImageView imageButtonOverdue; // ImageView for the Overdue icon
        private ImageView imageButtonPending; // ImageView for the pending icon
        private ImageView imageButtonCheck;
        private View completeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTaskName = itemView.findViewById(R.id.textViewTaskName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            imageButtonOverdue = itemView.findViewById(R.id.imageButtonOverdue);
            imageButtonPending = itemView.findViewById(R.id.imageButtonPending);
            imageButtonCheck = itemView.findViewById(R.id.imageButtonCheck);
            completeButton = itemView.findViewById(R.id.completeButton);

            completeButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    // Call the onTaskClick method of the listener
                    listener.onTaskClick(tasks.get(position));

                    // Hide other images and show check image
                    imageButtonOverdue.setVisibility(View.GONE);
                    imageButtonPending.setVisibility(View.GONE);
                    imageButtonCheck.setVisibility(View.VISIBLE);
                }
            });
        }

        public void bind(Task task) {
            textViewTaskName.setText(task.getName());
            textViewDescription.setText(task.getDescription());
            textViewDate.setText(task.getDate());
            textViewTime.setText(task.getTime());

            // Get the current date and time
            Calendar now = Calendar.getInstance();

            try {
                // Parse the task's due date and time into a Date object
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date dueDate = sdf.parse(task.getDate() + " " + task.getTime());

                // Convert the due date to Calendar for comparison
                Calendar taskDue = Calendar.getInstance();
                taskDue.setTime(dueDate);

                // Check if the task is overdue
                if (now.after(taskDue)) {
                    imageButtonOverdue.setVisibility(View.VISIBLE);
                    imageButtonPending.setVisibility(View.GONE);
                } else if (now.before(taskDue)) {
                    // Display pending icon if current date and time are before the task's due date and time
                    imageButtonOverdue.setVisibility(View.GONE);
                    imageButtonPending.setVisibility(View.VISIBLE);
                } else {
                    // Hide both icons if task is neither overdue nor pending
                    imageButtonOverdue.setVisibility(View.GONE);
                    imageButtonPending.setVisibility(View.GONE);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
