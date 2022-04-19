package com.example.createpdf;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final int PERMISSION_REQUEST_CODE=10;
    public static final int CREATE_FILE=1;
    public static final String TAG ="computercell.com";
    Button btnCreate;
    int pdfHeight =1080;
    int pdfWidth = 720;
    EditText editText;
    private PdfDocument document;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCreate=findViewById(R.id.createFromText);
        editText=findViewById(R.id.edittext);


        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()){
                    generatePdf();
                } else {
                    requestPermission();
                }
            }
        });


    }

    private void generatePdf() {
        document=new PdfDocument();
        PdfDocument.PageInfo pageInfo= new PdfDocument.PageInfo.Builder(pdfWidth,pdfHeight,1).create();
        PdfDocument.Page page=document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint plainText = new Paint();
        plainText.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD,Typeface.NORMAL));
        plainText.setTextSize(25);
        plainText.setColor(ContextCompat.getColor(this,R.color.black));
        plainText.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("(PDF by computercell)",396,50,plainText);

        plainText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        plainText.setColor(ContextCompat.getColor(this,R.color.black));
        plainText.setTextSize(17);
        plainText.setTextAlign(Paint.Align.LEFT);

        canvas.drawText(((EditText)findViewById(R.id.edittext)).getText().toString(),50,100,plainText);
        document.finishPage(page);
        createFile();


    }



    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE,"invoice.pdf");
        startActivityForResult(intent,CREATE_FILE);

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
    }

    private boolean checkPermission() {
        int permission1= ContextCompat.checkSelfPermission(getApplicationContext(),WRITE_EXTERNAL_STORAGE);
        int permission2=ContextCompat.checkSelfPermission(getApplicationContext(),READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK){
            Uri uri = null;
            if (data != null){
                uri=data.getData();
                if (document != null){
                    ParcelFileDescriptor pfd = null;
                    try {
                        pfd= getContentResolver()
                                .openFileDescriptor(uri,"w");
                        FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                        document.writeTo(fileOutputStream);
                        document.close();

                    } catch (IOException e){
                        try {
                            DocumentsContract.deleteDocument(getContentResolver(),uri);
                        } catch (FileNotFoundException ex){
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}