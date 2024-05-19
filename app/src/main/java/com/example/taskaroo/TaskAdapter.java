package com.example.taskaroo;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
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

import java.nio.ByteBuffer;
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

    @SuppressLint("NotifyDataSetChanged")
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
        private final Context context;
        private final TextView textViewTaskName;
        private final TextView textViewDescription;
        private final TextView textViewDate;
        private final TextView textViewTime;
        private final ImageView imageButtonOverdue;
        private final ImageView imageButtonPending;
        private final ImageView imageButtonCheck;
        private final TextView textViewNotification;
        private final TextView textViewLocation;
        private final View completeButton;
        private final ProgressBar progressBar;
        private final LottieAnimationView animationView;
        private final ImageView imageViewCamera;
        private final ImageView imageViewMap;

        private final GestureDetector gestureDetector;

        @SuppressLint("ClickableViewAccessibility")
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
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            completeButton = itemView.findViewById(R.id.completeButton);
            progressBar = itemView.findViewById(R.id.progressBar);
            animationView = itemView.findViewById(R.id.animation_view);
            imageViewCamera = itemView.findViewById(R.id.imageViewCamera);
            imageViewMap = itemView.findViewById(R.id.imageViewMap);

            context = itemView.getContext();

            //Map icon click
            imageViewMap.setOnClickListener(v -> imageViewMapClick());

            //Image click
            imageViewCamera.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                imageViewCameraClick(context, position);
            });


            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(@NonNull MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onDoubleTap(@NonNull MotionEvent e) {
                    editTask();
                    return true;
                }
            });

            itemView.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return true;
            });

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Task task = tasks.get(position);
                    Intent intent = new Intent(context, AddTaskActivity.class);
                    intent.putExtra("task_id", task.getId());
                    intent.putExtra("task_name", task.getName());
                    intent.putExtra("task_description", task.getDescription());
                    intent.putExtra("task_date", task.getDate());
                    intent.putExtra("task_time", task.getTime());
                    intent.putExtra("task_notification", task.getNumberOfNotifications());
                    intent.putExtra("task_camera_image", task.getCameraInfo());
                    intent.putExtra("task_map_info", task.getMapInfo());
                    context.startActivity(intent);
                    if (listener != null) {
                        listener.onTaskClick(task);
                    }
                }
            });

            completeButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Task task = tasks.get(position);

                    // Display the Lottie animation
                    playCompletionAnimation();

                    // Wait for the animation to finish before updating the completion status
                    animationView.addAnimatorListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(@NonNull Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(@NonNull Animator animation) {
                            // Update the completion status of the task
                            task.setCompleted(true);
                            task.setTimestamp(getDateTime());

                            try (DatabaseHelper dbHelper = new DatabaseHelper(itemView.getContext())) {
                                int isUpdated = dbHelper.updateTaskCompletionStatus(task.getId(), true);

                                if (isUpdated == 0) {
                                    Toast.makeText(itemView.getContext(), "Failed to update completion status", Toast.LENGTH_SHORT).show();
                                } else {
                                    completeButton.setVisibility(View.GONE);
                                    notifyItemChanged(position);
                                    String completeMessage = dbHelper.getCompleteMessage(task.getId());
                                    if (completeMessage != null) {
                                        Toast.makeText(itemView.getContext(), completeMessage, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onAnimationCancel(@NonNull Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(@NonNull Animator animation) {
                        }
                    });
                }
            });

        }

        public void bind(Task task) {
            textViewTaskName.setText(task.getName());
            textViewDescription.setText(task.getDescription());
            textViewDate.setText(task.getDate());
            textViewTime.setText(task.getTime());
            textViewNotification.setText(String.valueOf(task.getNumberOfNotifications()));

            // Display the saved camera image
            if (task.getCameraInfo() != null) {
                Bitmap cameraBitmap = BitmapFactory.decodeByteArray(task.getCameraInfo(), 0, task.getCameraInfo().length);
                imageViewCamera.setVisibility(View.VISIBLE);
                imageViewCamera.setImageBitmap(cameraBitmap);
            }

            // Display the saved map location
            if (task.getMapInfo() != null && task.getMapInfo().length > 0) {
                ByteBuffer buffer = ByteBuffer.wrap(task.getMapInfo());
                double lat = buffer.getDouble();
                double lng = buffer.getDouble();
                String locationInfo = "Location: Lat " + lat + ", Long " + lng;
                textViewLocation.setText(locationInfo);
                textViewLocation.setVisibility(View.VISIBLE);
                imageViewMap.setVisibility(View.VISIBLE);
            } else {
                textViewLocation.setVisibility(View.GONE);
                imageViewMap.setVisibility(View.GONE);
            }

            if (task.isCompleted()) {
                completeButton.setVisibility(View.GONE);
            } else {
                Calendar now = Calendar.getInstance();

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    Date dueDate = sdf.parse(task.getDate() + " " + task.getTime());
                    long diffInMilliseconds = 0;
                    if (dueDate != null) {
                        diffInMilliseconds = dueDate.getTime() - now.getTimeInMillis();
                    }

                    if (dueDate != null && (diffInMilliseconds < 0 || dueDate.equals(now.getTime()))) {
                        completeButton.setVisibility(View.VISIBLE);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            Calendar now = Calendar.getInstance();

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date dueDate = sdf.parse(task.getDate() + " " + task.getTime());
                long diffInMilliseconds = 0;
                if (dueDate != null) {
                    diffInMilliseconds = dueDate.getTime() - now.getTimeInMillis();
                }
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

        //Open the map Image view map clicked
        private void imageViewMapClick() {
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Task task = tasks.get(position);
                if (task.getMapInfo() != null && task.getMapInfo().length > 0) {
                    ByteBuffer buffer = ByteBuffer.wrap(task.getMapInfo());
                    double lat = buffer.getDouble();
                    double lng = buffer.getDouble();
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(Taskaroo)", lat, lng, lat, lng);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "No app available to open map", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "No location data available", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Display Lotti Animation when task complete button clicked
        private void playCompletionAnimation() {
            animationView.setVisibility(View.VISIBLE);
            animationView.playAnimation();
            animationView.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animation) {
                }

                @Override
                public void onAnimationEnd(@NonNull Animator animation) {
                    animationView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animation) {
                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animation) {
                }
            });
        }

        //Calculation for the progress bar
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

        //Progress bar color
        private void setProgressBarColor(int progress) {
            if (progress >= 75) {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
            } else if (progress >= 50) {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
            } else progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
        }


        private String getDateTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date());
        }

        private void editTask() {
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Task task = tasks.get(position);
                Intent intent = new Intent(context, AddTaskActivity.class);
                intent.putExtra("task_id", task.getId());
                intent.putExtra("task_name", task.getName());
                intent.putExtra("task_description", task.getDescription());
                intent.putExtra("task_date", task.getDate());
                intent.putExtra("task_time", task.getTime());
                intent.putExtra("task_notification", task.getNumberOfNotifications());
                intent.putExtra("task_camera_image", task.getCameraInfo());
                intent.putExtra("task_map_info", task.getMapInfo());
                context.startActivity(intent);
            }
        }
    }

    //Handle the camera image click
    public void imageViewCameraClick(Context context, int position) {
        if (position != RecyclerView.NO_POSITION && tasks != null && position < tasks.size()) {
            Task task = tasks.get(position);
            if (task.getCameraInfo() != null) {
                // Pass the image byte array to DisplayImageActivity
                Intent intent = new Intent(context, DisplayImageActivity.class);
                intent.putExtra("imageByteArray", task.getCameraInfo());
                context.startActivity(intent);
            } else {
                // If the camera image is null, show a message
                Toast.makeText(context, "No image available", Toast.LENGTH_SHORT).show();
            }
        }
    }



}
