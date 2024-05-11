package com.example.taskaroo;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class PdfPreviewActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_preview);

        textView = findViewById(R.id.pdfTextView);

        // Get the PDF file path from the intent
        String pdfFilePath = getIntent().getStringExtra("pdfFilePath");

        // Load and display the text content of the PDF file
        String text = loadTextFromPdf(pdfFilePath);
        textView.setText(text);
    }

    private String loadTextFromPdf(String pdfFilePath) {
        StringBuilder text = new StringBuilder();
        try {
            File pdfFile = new File(pdfFilePath);
            FileInputStream fis = new FileInputStream(pdfFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line);
            }
            reader.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }
}
