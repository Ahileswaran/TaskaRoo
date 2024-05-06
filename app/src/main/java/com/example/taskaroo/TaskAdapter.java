package com.example.taskaroo;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
        private ProgressBar progressBar; // Progress bar for task progress

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
            progressBar = itemView.findViewById(R.id.progressBar); // Initialize progress bar

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

                // Calculate the difference between current date and task due date
                long diffInMilliseconds = dueDate.getTime() - now.getTimeInMillis();

                // Calculate progress
                int progress = calculateProgress(diffInMilliseconds);

                // Set progress bar color based on progress
                setProgressBarColor(progress);

                // Set progress
                progressBar.setProgress(progress);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        private int calculateProgress(long diffInMilliseconds) {
            // Define thresholds for progress levels (in milliseconds)
            long highThreshold = 24 * 60 * 60 * 1000; // 1 day
            long mediumThreshold = 3 * 24 * 60 * 60 * 1000; // 3 days

            // Calculate progress based on the difference
            if (diffInMilliseconds < 0) {
                // Task is overdue
                return 100;
            } else if (diffInMilliseconds < highThreshold) {
                // Task due date is near
                return 75;
            } else if (diffInMilliseconds < mediumThreshold) {
                // Task due date is medium
                return 50;
            } else {
                // Task due date is far
                return 25;
            }
        }

        private void setProgressBarColor(int progress) {
            if (progress >= 75) {
                // Red color for high priority
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
            } else if (progress >= 50) {
                // Yellow color for medium priority
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
            } else {
                // Green color for low priority
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
            }
        }
    }
}
