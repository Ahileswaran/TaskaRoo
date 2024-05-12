package com.example.taskaroo;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExportTaskPDFActivity extends AppCompatActivity {

    private static final String TAG = "ExportTaskPDFActivity";
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_pdf);

        dbHelper = new DatabaseHelper(this);

        Button generateButton = findViewById(R.id.generatePDFButton);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePDF();
            }
        });
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void generatePDF() {
        if (!isExternalStorageWritable()) {
            Log.e(TAG, "External storage not writable");
            Toast.makeText(this, "External storage not writable", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        List<Task> tasks = dbHelper.getAllTasks();
        int yPos = 50; // Initial Y position for task view

        for (Task task : tasks) {
            TaskView taskView = new TaskView(this); // Create a new instance of TaskView
            taskView.setTask(task); // Set the task details
            taskView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED); // Measure the view
            taskView.layout(0, 0, 300, 0); // Layout the view within the specified width
            taskView.draw(canvas); // Draw the view on the canvas
            yPos += taskView.getMeasuredHeight() + 20; // Increment Y position
        }

        document.finishPage(page);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "TaskarooTasks.pdf");
        try {
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF Generated Successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to generate PDF: " + e.getMessage());
            Toast.makeText(this, "Failed to Generate PDF", Toast.LENGTH_SHORT).show();
        }

        document.close();
    }
}
