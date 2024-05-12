package com.example.taskaroo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

public class ExportTaskPDFActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_pdf);

        Button generatePDFButton = findViewById(R.id.generatePDFButton);
        generatePDFButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                generatePDF();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            generatePDF();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void generatePDF() {
        DatabaseHelper db = new DatabaseHelper(this);
        List<Task> tasks = db.getAllTasks();
        Document document = new Document();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Tasks.pdf";

        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();
            for (Task task : tasks) {
                document.add(new Paragraph("ID: " + task.getId()));
                document.add(new Paragraph("Name: " + task.getName()));
                document.add(new Paragraph("Description: " + task.getDescription()));
                document.add(new Paragraph("Date: " + task.getDate()));
                document.add(new Paragraph("Time: " + task.getTime()));
                document.add(new Paragraph("Completed: " + (task.isCompleted() ? "Yes" : "No")));
                document.add(new Paragraph("\n\n"));
            }
            document.close();
            Toast.makeText(this, "PDF Generated at " + path, Toast.LENGTH_LONG).show();
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
