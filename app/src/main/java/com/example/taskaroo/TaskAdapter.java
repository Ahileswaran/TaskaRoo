package com.example.taskaroo;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTaskName;
        private TextView textViewDescription;
        private TextView textViewDate;
        private TextView textViewTime;
        private ImageView imageButtonOverdue; // ImageView for the Overdue icon
        private ImageView imageButtonPending; // ImageView for the pending icon
        private ImageView imageButtonCheck;
        private TextView textViewNotification;
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
            textViewNotification = itemView.findViewById(R.id.textViewNotification);
            completeButton = itemView.findViewById(R.id.completeButton);
            progressBar = itemView.findViewById(R.id.progressBar); // Initialize progress bar

            completeButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Task task = tasks.get(position);

                    // Update completion status in the Task object
                    task.setCompleted(true);
                    task.setTimestamp(getDateTime());

                    // Update completion status and timestamp in the database
                    DatabaseHelper dbHelper = new DatabaseHelper(itemView.getContext());
                    int isUpdated = dbHelper.updateTaskCompletionStatus(task.getId(), true);

                    // Handle completion status update
                    if (isUpdated == 0) {
                        Toast.makeText(itemView.getContext(), "Failed to update completion status", Toast.LENGTH_SHORT).show();
                    } else {
                        // Hide the completeButton after completion
                        completeButton.setVisibility(View.GONE);
                        // Notify adapter of data change
                        notifyItemChanged(position);
                        // Retrieve complete message with task ID from the database
                        String completeMessage = dbHelper.getCompleteMessage(task.getId());
                        if (completeMessage != null) {
                            Toast.makeText(itemView.getContext(), completeMessage, Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });
        }

        public void bind(Task task) {
            textViewTaskName.setText(task.getName());
            textViewDescription.setText(task.getDescription());
            textViewDate.setText(task.getDate());
            textViewTime.setText(task.getTime());
            textViewNotification.setText(String.valueOf(task.getNumberOfNotifications()));

            // Hide the completeButton if the task is completed
            if (task.isCompleted()) {
                completeButton.setVisibility(View.GONE);
            } else {
                Calendar now = Calendar.getInstance();

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    Date dueDate = sdf.parse(task.getDate() + " " + task.getTime());
                    long diffInMilliseconds = dueDate.getTime() - now.getTimeInMillis();

                    // Determine visibility of completeButton
                    if (diffInMilliseconds < 0 || dueDate.equals(now.getTime())) {
                        completeButton.setVisibility(View.VISIBLE);
                    } else {
                        completeButton.setVisibility(View.GONE);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
            // Get the current date and time
            Calendar now = Calendar.getInstance();

            //Overdue and pending task
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

                // Update icon visibility based on completion status
                if (task.isCompleted()) {
                    imageButtonOverdue.setVisibility(View.GONE);
                    imageButtonPending.setVisibility(View.GONE);
                    imageButtonCheck.setVisibility(View.VISIBLE);
                } else {

                    // if the task is overdue, show overdue icon, if it's pending, show pending icon
                    if (diffInMilliseconds < 0) {
                        // Task is overdue
                        imageButtonOverdue.setVisibility(View.VISIBLE);
                        imageButtonPending.setVisibility(View.GONE);
                        imageButtonCheck.setVisibility(View.GONE);
                    } else {
                        // Task is not overdue
                        imageButtonOverdue.setVisibility(View.GONE);
                        imageButtonPending.setVisibility(View.VISIBLE);
                        imageButtonCheck.setVisibility(View.GONE);

                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }



        //Calculate teh progress for the task
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

        //Set progress bar color for the priority of the task
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

    // Helper method to get current date and time
    private String getDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
