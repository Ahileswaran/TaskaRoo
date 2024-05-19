package com.example.taskaroo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class TaskView extends LinearLayout {

    private TextView textViewTaskName;
    private TextView textViewDescription;
    private TextView textViewDate;
    private TextView textViewTime;
    private TextView textViewNotification;
    private ImageView imageViewCamera;
    private ImageView imageViewMap;

    public TaskView(Context context) {
        super(context);
        init(context);
    }

    public TaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.task_view, this);
        textViewTaskName = findViewById(R.id.textViewTaskName);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewDate = findViewById(R.id.textViewDate);
        textViewTime = findViewById(R.id.textViewTime);
        textViewNotification = findViewById(R.id.textViewNotification);
        imageViewCamera = findViewById(R.id.imageViewCamera);
        imageViewMap = findViewById(R.id.imageViewMap);
    }

    public void setTask(Task task) {
        textViewTaskName.setText(task.getName());
        textViewDescription.setText(task.getDescription());
        textViewDate.setText(task.getDate());
        textViewTime.setText(task.getTime());
        textViewNotification.setText(String.valueOf(task.getNumberOfNotifications()));

        // Load camera image
        if (task.getCameraImage() != null) {
            Glide.with(getContext())
                    .load(task.getCameraImage())
                    .into(imageViewCamera);
            Log.d("TaskView", "Camera image set");
        } else {
            Log.d("TaskView", "Camera image is null");
        }

        // Load map image
        if (task.getMapInfo() != null) {
            Glide.with(getContext())
                    .load(task.getMapInfo())
                    .into(imageViewMap);
            Log.d("TaskView", "Map image set");
        } else {
            Log.d("TaskView", "Map image is null");
        }
    }
}
