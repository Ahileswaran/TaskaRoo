package com.example.taskaroo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExportTaskPDFActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private DatabaseHelper databaseHelper;
    private BackupRestoreActivity backupRestoreActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_pdf);

        Button generatePDFButton = findViewById(R.id.generatePDFButton);
        databaseHelper = new DatabaseHelper(this);
        backupRestoreActivity = new BackupRestoreActivity(); // Create instance

        generatePDFButton.setOnClickListener(v -> {
            if (checkPermission()) {
                // Call performBackup from BackupRestoreActivity
                backupRestoreActivity.performBackup(getBackupUri());
                generatePDF();
            } else {
                requestPermission();
            }
        });
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    private Uri getBackupUri() {
        File backupDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File backupFile = new File(backupDir, "taskaroo_backup.db");
        return Uri.fromFile(backupFile);
    }

    private void generatePDF() {
        List<Task> taskList = databaseHelper.getAllTasks();

        // Sort tasks by month and date
        taskList.sort((t1, t2) -> {
            String date1 = t1.getDate();
            String date2 = t2.getDate();
            // Assuming date format is "YYYY-MM-DD"
            String[] parts1 = date1.split("-");
            String[] parts2 = date2.split("-");
            int month1 = Integer.parseInt(parts1[1]);
            int month2 = Integer.parseInt(parts2[1]);
            int day1 = Integer.parseInt(parts1[2]);
            int day2 = Integer.parseInt(parts2[2]);
            if (month1 != month2) {
                return month1 - month2;
            } else {
                return day1 - day2;
            }
        });

        // Create PDF
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TaskPDF";
            File dir = new File(path);
            if (!dir.exists()) {
                boolean mkdirs = dir.mkdirs();
            }
            File file = new File(dir, "task_list.pdf");
            FileOutputStream fOut = new FileOutputStream(file);

            PdfWriter writer = new PdfWriter(fOut);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            for (Task task : taskList) {
                document.add(new Paragraph(task.getName() + "\n" + task.getDescription() + "\n" +
                        task.getDate() + "\n" + task.getTime() + "\n\n"));
            }

            document.close();
            Toast.makeText(getApplicationContext(), "PDF generated successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
