package com.example.kkenneally.testingcrop;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;


import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class BarcodeActivity extends AppCompatActivity implements View.OnClickListener, ZXingScannerView.ResultHandler {

    Button              camButton;
    ZXingScannerView    zx;
    private static final String TAG = BarcodeActivity.class.getSimpleName();
    String deviceID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        camButton = findViewById(R.id.barcode_Camera);
        camButton.setOnClickListener(this);
    }

    // ---------------------------------------------------------------------------------------------
    // if the user presses back button ensure the camera is shut down otherwise it wont be available
    // for using the ocr feature
    @Override
    public void onBackPressed() {
        super.onBackPressed();

            try{
                  zx.stopCameraPreview();
                  zx.stopCamera();
            }catch ( NullPointerException w){
                    Log.e(TAG, "FAILED TO STOP CAMERA");
            }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.barcode_Camera:

                try {
                    scan(view);
                    for (int i=0; i < 2; i++)
                    {

                        Toast.makeText(BarcodeActivity.this,"Tip: If failing to detect bar code try tilting the phone slightly to the left or right.", Toast.LENGTH_LONG).show();

                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                break;

            default:
                break;
        }
    }

    public void scan(View view) throws InterruptedException {

        zx = new ZXingScannerView(getApplicationContext());
        setContentView(zx);
        //zx.wait(10);
        zx.setResultHandler(this);
        zx.startCamera();
        zx.setAutoFocus(true);
       // zx.setFlash(true);

        try {
           /* Bitmap bMap = BitmapFactory.decodeResource(BarcodeActivity.this.getResources(),
                    R.drawable.image1*//* ANY DUMMY IMAGE WITH BARCODE *//*);*/

            Bitmap bMap = BitmapFactory.decodeResource(BarcodeActivity.this.getResources(), 0);
            int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
            // copy pixel data from the Bitmap into the 'intArray' array
            bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(),
                    bMap.getHeight());

            LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(),
                    bMap.getHeight(), intArray);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Reader reader = new MultiFormatReader();// use this otherwise

            Result result = reader.decode(bitmap);
            String res = result.getText();
            Log.e(TAG, "THE RESULT IS ");
            Log.e(TAG, res);


        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void handleResult(final Result result) {

        final String scanResult = result.getText();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan result");
        builder.setPositiveButton("Identify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                zx.stopCameraPreview();
                zx.stopCamera();
                Log.e(TAG, "RESULT HAS BEEN FOUND");

               // switchActivity(scanResult);
                displayAlertBox(scanResult);

            }
        });

        builder.setMessage(scanResult);
        AlertDialog alert = builder.create();
        alert.show();
    }



   /* // switch over the the activity which allows the code to be edited or sent to a server
    private void switchActivity(String scanResult) {

        // let the activity know it is a barcode being sent not a error code from LCD screen
        ocrActivity ocr = new ocrActivity();
        ocr.setHandle_ocr_or_barcode("barcode");

        finish();

        Intent i = new Intent(this, ocrActivity.class);
        i.putExtra("bar code string", scanResult);
        startActivity(i);
    }*/

    void displayAlertBox(String barcode){

        SendingToServer server = new SendingToServer();

      //   final String deviceID = "PNOZ m B1";
        deviceID = server.identifyDevice(barcode);

        final  Intent i = new Intent(this, BarcodeActivity.class);
        final  Intent main = new Intent(this, MainActivity.class);

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(BarcodeActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(BarcodeActivity.this);
        }
        builder.setTitle("Device ID: " + deviceID)
                .setMessage("Proceed to scan error code?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            zx.stopCameraPreview();
                            zx.stopCamera();
                            //switchActivity(deviceID);
                            main.putExtra("device type", deviceID);
                            startActivity(main);


                        }catch ( NullPointerException w){
                            Log.e(TAG, "FAILED TO STOP CAMERA");
                        }

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        zx.stopCamera();
                        zx.stopCameraPreview();
                        startActivity(i);

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }
}
