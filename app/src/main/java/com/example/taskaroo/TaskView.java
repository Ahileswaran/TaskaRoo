package com.example.taskaroo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;



public class TaskView extends LinearLayout {

    private TextView textViewTaskName;
    private TextView textViewDescription;
    private TextView textViewDate;
    private TextView textViewTime;

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
    }

    public void setTask(Task task) {
       textViewTaskName.setText(task.getName());
       textViewDescription.setText(task.getDescription());
       textViewDate.setText(task.getDate());
       textViewTime.setText(task.getTime());
    }
}
