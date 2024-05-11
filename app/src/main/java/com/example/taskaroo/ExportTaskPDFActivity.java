package com.example.taskaroo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExportTaskPDFActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_CODE_SAVE_PDF = 101;
    private String pdfPath; // Declare pdfPath variable at the class level

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_pdf);

        // Initialize dbHelper
        dbHelper = new DatabaseHelper(this);

        // Find the generatePDFButton by its id
        Button generatePDFButton = findViewById(R.id.generatePDFButton);

        // Set OnClickListener to the generatePDFButton
        generatePDFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for permission to write to external storage
                if (ContextCompat.checkSelfPermission(ExportTaskPDFActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // Permission is already granted, proceed with generating PDF
                    generatePDF();
                } else {
                    // Request permission to write to external storage
                    ActivityCompat.requestPermissions(ExportTaskPDFActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SAVE_PDF && resultCode == RESULT_OK) {
            // PDF preview activity
            Intent previewIntent = new Intent(this, PdfPreviewActivity.class);
            previewIntent.putExtra("pdfFilePath", pdfPath); // Pass the PDF file path to the preview activity
            startActivity(previewIntent);
        }
    }

    // Generate All PDF Into PDF File
    public void generatePDF() {
        // Get all tasks from the database
        List<Task> allTasks = dbHelper.getAllTasks();

        // Group tasks by month
        Map<String, List<Task>> tasksByMonth = groupTasksByMonth(allTasks);

        // Generate PDF with tasks grouped by month
        try {
            pdfPath = generatePDFWithTasksByMonth(tasksByMonth); // Assign value to pdfPath
            // Save the PDF to storage
            savePDFToStorage(pdfPath);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error while generating PDF
        }
    }

    // Group tasks by month
    private Map<String, List<Task>> groupTasksByMonth(List<Task> tasks) {
        Map<String, List<Task>> tasksByMonth = new TreeMap<>();
        for (Task task : tasks) {
            String month = task.getDate(); // Assuming date format is "MM.YYYY"
            List<Task> tasksOfMonth = tasksByMonth.getOrDefault(month, new ArrayList<>());
            tasksOfMonth.add(task);
            tasksByMonth.put(month, tasksOfMonth);
        }
        return tasksByMonth;
    }

    // Generate PDF with tasks grouped by month
    private String generatePDFWithTasksByMonth(Map<String, List<Task>> tasksByMonth) throws IOException {
        String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/taskaroo_tasks.pdf"; // Path where PDF will be saved

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(pdfPath));
        Document document = new Document(pdfDoc);

        for (Map.Entry<String, List<Task>> entry : tasksByMonth.entrySet()) {
            String month = entry.getKey();
            List<Task> tasks = entry.getValue();

            // Add month title
            document.add(new Paragraph(month));

            // Add tasks under the month
            for (Task task : tasks) {
                String taskDetails = task.getId() + "\t" +
                        task.getName() + "\t" +
                        task.getDescription() + "\t" +
                        task.getDate() + "\t" +
                        task.getTime() + "\t" +
                        (task.isCompleted() ? "Completed" : "Not Completed");
                document.add(new Paragraph(taskDetails));
            }
        }

        document.close();
        return pdfPath;
    }

    // Save PDF to storage
    private void savePDFToStorage(String pdfPath) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "taskaroo_tasks.pdf");
        startActivityForResult(intent, REQUEST_CODE_SAVE_PDF);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with generating PDF
            generatePDF();
        }
    }
}
