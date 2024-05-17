package com.example.taskaroo;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

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
        private Context context;
        private TextView textViewTaskName;
        private TextView textViewDescription;
        private TextView textViewDate;
        private TextView textViewTime;
        private ImageView imageButtonOverdue;
        private ImageView imageButtonPending;
        private ImageView imageButtonCheck;
        private TextView textViewNotification;
        private View completeButton;
        private ProgressBar progressBar;
        private LottieAnimationView animationView;

        private GestureDetector gestureDetector;

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
            progressBar = itemView.findViewById(R.id.progressBar);
            animationView = itemView.findViewById(R.id.animation_view);

            context = itemView.getContext();

            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    editTask();
                    return true;
                }
            });

            itemView.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return true;
            });

            itemView.setOnClickListener(v -> {
                Task task = tasks.get(getAdapterPosition());
                Intent intent = new Intent(context, AddTaskActivity.class);
                intent.putExtra("task_id", task.getId());
                intent.putExtra("task_name", task.getName());
                intent.putExtra("task_description", task.getDescription());
                intent.putExtra("task_date", task.getDate());
                intent.putExtra("task_time", task.getTime());
                intent.putExtra("task_notification", task.getNumberOfNotifications());
                context.startActivity(intent);
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    task = tasks.get(position);
                    listener.onTaskClick(task);
                }
            });

            completeButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Task task = tasks.get(position);

                    task.setCompleted(true);
                    task.setTimestamp(getDateTime());

                    DatabaseHelper dbHelper = new DatabaseHelper(itemView.getContext());
                    int isUpdated = dbHelper.updateTaskCompletionStatus(task.getId(), true);

                    if (isUpdated == 0) {
                        Toast.makeText(itemView.getContext(), "Failed to update completion status", Toast.LENGTH_SHORT).show();
                    } else {
                        completeButton.setVisibility(View.GONE);
                        notifyItemChanged(position);
                        playCompletionAnimation();
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

            if (task.isCompleted()) {
                completeButton.setVisibility(View.GONE);
            } else {
                Calendar now = Calendar.getInstance();

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    Date dueDate = sdf.parse(task.getDate() + " " + task.getTime());
                    long diffInMilliseconds = dueDate.getTime() - now.getTimeInMillis();

                    if (diffInMilliseconds < 0 || dueDate.equals(now.getTime())) {
                        completeButton.setVisibility(View.VISIBLE);
                    } else {
                        completeButton.setVisibility(View.GONE);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            Calendar now = Calendar.getInstance();

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date dueDate = sdf.parse(task.getDate() + " " + task.getTime());
                long diffInMilliseconds = dueDate.getTime() - now.getTimeInMillis();
                int progress = calculateProgress(diffInMilliseconds);
                setProgressBarColor(progress);

                progressBar.setProgress(progress);

                if (task.isCompleted()) {
                    imageButtonOverdue.setVisibility(View.GONE);
                    imageButtonPending.setVisibility(View.GONE);
                    imageButtonCheck.setVisibility(View.VISIBLE);
                } else {
                    if (diffInMilliseconds < 0) {
                        imageButtonOverdue.setVisibility(View.VISIBLE);
                        imageButtonPending.setVisibility(View.GONE);
                        imageButtonCheck.setVisibility(View.GONE);
                    } else {
                        imageButtonOverdue.setVisibility(View.GONE);
                        imageButtonPending.setVisibility(View.VISIBLE);
                        imageButtonCheck.setVisibility(View.GONE);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        private int calculateProgress(long diffInMilliseconds) {
            long highThreshold = 24 * 60 * 60 * 1000;
            long mediumThreshold = 3 * 24 * 60 * 60 * 1000;
            if (diffInMilliseconds < 0) {
                return 100;
            } else if (diffInMilliseconds < highThreshold) {
                return 75;
            } else if (diffInMilliseconds < mediumThreshold) {
                return 50;
            } else {
                return 25;
            }
        }

        private void setProgressBarColor(int progress) {
            if (progress >= 75) {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
            } else if (progress >= 50) {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
            } else {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
            }
        }

        private void editTask() {
            Task task = tasks.get(getAdapterPosition());
            Intent intent = new Intent(context, AddTaskActivity.class);
            intent.putExtra("task_id", task.getId());
            intent.putExtra("task_name", task.getName());
            intent.putExtra("task_description", task.getDescription());
            intent.putExtra("task_date", task.getDate());
            intent.putExtra("task_time", task.getTime());
            intent.putExtra("task_notification", task.getNumberOfNotifications());
            context.startActivity(intent);
            int position = getAdapterPosition();
            if (listener != null && position != RecyclerView.NO_POSITION) {
                task = tasks.get(position);
                listener.onTaskClick(task);
            }
        }

        //Play the Lottie animation when complete (Done ?) button clicked

        private void playCompletionAnimation() {
            animationView.setVisibility(View.VISIBLE);
            animationView.setAnimation("animation.json");
            animationView.playAnimation();
            animationView.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // Animation started
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // Animation ended, hide the animation view
                    animationView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    // Animation cancelled
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    // Animation repeated
                }
            });
        }


        private String getDateTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date());
        }
    }
}
