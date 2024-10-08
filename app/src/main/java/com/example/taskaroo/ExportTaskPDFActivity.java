package com.example.taskaroo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ExportTaskPDFActivity extends AppCompatActivity {

    private static final String TAG = "ExportTaskPDFActivity";
    private DatabaseHelper dbHelper;

    private final ActivityResultLauncher<Intent> createDocumentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            savePdfToFile(uri);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_pdf);

        dbHelper = new DatabaseHelper(this);

        Button generateButton = findViewById(R.id.generatePDFButton);
        generateButton.setOnClickListener(v -> generatePDF());
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
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(842, 595, 1).create(); // A4 size: 842x595 points (landscape)

        List<Task> tasks = dbHelper.getAllTasks();
        int pageWidth = pageInfo.getPageWidth();
        int pageCount = 0;
        int xPos = 50; // Initial X position for task view

        for (Task task : tasks) {
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            new Paint();

            TaskView taskView = new TaskView(this); // Create a new instance of TaskView
            taskView.setTask(task); // Set the task details
            taskView.measure(View.MeasureSpec.makeMeasureSpec(pageInfo.getPageHeight(), View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.UNSPECIFIED); // Measure the view
            taskView.layout(0, 0, taskView.getMeasuredWidth(), taskView.getMeasuredHeight()); // Layout the view

            if (xPos + taskView.getMeasuredWidth() > pageWidth) {
                // Start a new page if the task doesn't fit on the current page
                document.finishPage(page);
                pageCount++;
                pageInfo = new PdfDocument.PageInfo.Builder(842, 595, pageCount + 1).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                xPos = 50; // Reset X position
            }

            taskView.draw(canvas); // Draw the view on the canvas
            xPos += taskView.getMeasuredWidth() + 20; // Increment X position

            document.finishPage(page);
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "TaskarooTasks.pdf");
        createDocumentLauncher.launch(intent);
    }

    private void savePdfToFile(Uri uri) {
        try {
            int fileNumber = 0;
            String filename = "TaskarooTasks.pdf";

            // Check if the file already exists
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename);
            while (file.exists()) {
                fileNumber++;
                filename = "TaskarooTasks" + fileNumber + ".pdf";
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename);
            }

            // Create the PDF document
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(842, 595, 1).create(); // A4 size: 842x595 points (landscape)

            List<Task> tasks = dbHelper.getAllTasks();

            for (Task task : tasks) {
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                TaskView taskView = new TaskView(this); // Create a new instance of TaskView
                taskView.setTask(task); // Set the task details
                taskView.measure(View.MeasureSpec.makeMeasureSpec(pageInfo.getPageHeight(), View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.UNSPECIFIED); // Measure the view
                taskView.layout(0, 0, taskView.getMeasuredWidth(), taskView.getMeasuredHeight()); // Layout the view
                taskView.draw(canvas); // Draw the view on the canvas

                document.finishPage(page);
            }

            // Write the PDF to the file
            try (FileOutputStream outputStream = new FileOutputStream(Objects.requireNonNull(getContentResolver().openFileDescriptor(uri, "w")).getFileDescriptor())) {
                document.writeTo(outputStream);
            } catch (NullPointerException e) {
                Log.e(TAG, "Failed to get file descriptor: " + e.getMessage());
                Toast.makeText(this, "Failed to Save PDF", Toast.LENGTH_SHORT).show();
            }

            document.close();
            Toast.makeText(this, "PDF Saved Successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to save PDF: " + e.getMessage());
            Toast.makeText(this, "Failed to Save PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
