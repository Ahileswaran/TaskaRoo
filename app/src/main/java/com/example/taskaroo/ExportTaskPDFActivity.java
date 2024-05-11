package com.example.taskaroo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

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
import java.util.List;

public class ExportTaskPDFActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_CODE_SAVE_PDF = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_pdf);

        // Initialize dbHelper
        dbHelper = new DatabaseHelper(this);

        // Find the generatePDFButton by its id
        Button generatePDFButton = findViewById(R.id.generatePDFButton);

        // Set OnClickListener to the generatePDFButton using lambda expression
        generatePDFButton.setOnClickListener(v -> {
            Log.d("ExportTaskPDFActivity", "Generate PDF button clicked");
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
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SAVE_PDF && resultCode == RESULT_OK) {
            // PDF preview activity
            Intent previewIntent = new Intent(this, PdfPreviewActivity.class);
            previewIntent.putExtra("pdfFilePath", data.getData().toString()); // Pass the PDF file path to the preview activity
            startActivity(previewIntent);
        }
    }

    // Generate All PDF Into PDF File
    public void generatePDF() {
        Log.d("ExportTaskPDFActivity", "generatePDF() called");
        // Get all tasks from the database
        List<Task> allTasks = dbHelper.getAllTasks();

        if (allTasks.isEmpty()) {
            Log.d("ExportTaskPDFActivity", "No tasks found in the database");
        } else {
            Log.d("ExportTaskPDFActivity", "Number of tasks: " + allTasks.size());
        }

        // Generate PDF with all tasks
        try {
            String pdfPath = generatePDFWithAllTasks(allTasks); // Assign value to pdfPath
            // No need to save the PDF to storage here, as it's done in the savePDFToStorage method
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error while generating PDF
        }
    }

    // Generate PDF with all tasks
    private String generatePDFWithAllTasks(List<Task> allTasks) throws IOException {
        String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/taskaroo_tasks.pdf"; // Path where PDF will be saved

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(pdfPath));
        Document document = new Document(pdfDoc);

        // Add tasks to the PDF
        for (Task task : allTasks) {
            String taskDetails = task.getId() + "\t" +
                    task.getName() + "\t" +
                    task.getDescription() + "\t" +
                    task.getDate() + "\t" +
                    task.getTime() + "\t" +
                    (task.isCompleted() ? "Completed" : "Not Completed");
            document.add(new Paragraph(taskDetails));
        }

        document.close();

        // Show a toast indicating that PDF generation is complete
        Toast.makeText(this, "PDF generated successfully", Toast.LENGTH_SHORT).show();

        // Save PDF to storage
        savePDFToStorage(pdfPath);

        // Return the PDF file path
        return pdfPath;
    }

    // Save PDF to storage
    private void savePDFToStorage(String pdfPath) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "taskaroo_tasks.pdf");
        startActivityForResult(Intent.createChooser(intent, "Save PDF"), REQUEST_CODE_SAVE_PDF);
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
