package com.example.taskaroo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.nio.ByteBuffer;

public class TaskView extends LinearLayout {

    private TextView textViewTaskName;
    private TextView textViewDescription;
    private TextView textViewDate;
    private TextView textViewTime;
    private TextView textViewNotification;
    private ImageView imageViewCamera;
    private ImageView imageViewMap;

    private TextView textViewLocation;

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
        textViewLocation= findViewById(R.id.textViewLocation);
        imageViewMap = findViewById(R.id.imageViewMap);
    }

    public void setTask(Task task) {
        textViewTaskName.setText(task.getName());
        textViewDescription.setText(task.getDescription());
        textViewDate.setText(task.getDate());
        textViewTime.setText(task.getTime());
        textViewNotification.setText(String.valueOf(task.getNumberOfNotifications()));

        // Display the saved camera image
        if (task.getCameraInfo() != null && task.getCameraInfo().length > 0) {
            Bitmap cameraBitmap = BitmapFactory.decodeByteArray(task.getCameraInfo(), 0, task.getCameraInfo().length);
            imageViewCamera.setVisibility(View.VISIBLE);
            imageViewCamera.setImageBitmap(cameraBitmap);
        } else {
            imageViewCamera.setVisibility(View.GONE); // Hide imageView if no image is available
        }

        // Display the saved map location
        if (task.getMapInfo() != null && task.getMapInfo().length > 0) {
            ByteBuffer buffer = ByteBuffer.wrap(task.getMapInfo());
            double lat = buffer.getDouble();
            double lng = buffer.getDouble();
            Bitmap mapBitmap = getMapPreviewBitmap(lat, lng);
            imageViewMap.setVisibility(View.VISIBLE);
            imageViewMap.setImageBitmap(mapBitmap);
        } else {
            imageViewMap.setVisibility(View.GONE); // Hide imageView if no map info is available
        }
    }

    private Bitmap getMapPreviewBitmap(double lat, double lng) {

        return BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_map);
    }
}
