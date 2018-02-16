package com.example.kkenneally.testingcrop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;


public class ocrActivity extends AppCompatActivity implements  View.OnClickListener{

    private TessBaseAPI tessBaseAPI;
    Bitmap bitmapCropped;
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString()+"/Tess";

    //public static final String TESS_DATA = "/tessdata";

     EditText codeTextView;
     String deviceType;
     TextView deviceName;

    protected static String handle_ocr_or_barcode = "";

   /* public ocrActivity(Context context, String language) {
        tessBaseAPI = new TessBaseAPI();
        String datapath = context.getFilesDir() + "/tesseract/"; tessBaseAPI.init(datapath, "eng");
    }*/
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        Log.e(TAG, "SWITHCING TO OCR ACTIVITY");

        Button edit = findViewById(R.id.edit_error_code);
        edit.setOnClickListener(this); // calling onClick() method
        Button sendToServer = findViewById(R.id.btn_send_to_server);
        sendToServer.setOnClickListener(this);
        Button goBack = findViewById(R.id.btn_back_to_camera);
        goBack.setOnClickListener(this);

        deviceName = findViewById(R.id.device_type);   // LABEL WHICH SHOWS THE TYPE OF DEVICE

        Log.e(TAG, "THE LOCATION OF THE ENG FILE IS ");
        Log.e(TAG, DATA_PATH);

        codeTextView = findViewById(R.id.edit_text_id);
        handle_ocr_or_barcode = this.getHandle_ocr_or_barcode();


        Intent intent = getIntent();
        bitmapCropped = intent.getParcelableExtra("BitmapImage");
      //  deviceType = intent.getType().toString();
        deviceType = intent.getStringExtra("DeviceType");
       // Toast.makeText(ocrActivity.this,deviceType, Toast.LENGTH_LONG).show();
        deviceName.setText(deviceType);
        Log.e(TAG, "device type: ");
        Log.e(TAG, deviceType);





        doOCR(bitmapCropped);
        //------------------------------------------------------------------
        // if the local String setter equals errorcode then we have recieved a bitmap image, handle it
        //------------------------------------------------------------------------------
       /* if(handle_ocr_or_barcode == "errorcode") {

            // ACCEPT THE BITMAP IMAGE THAT WAS TAKEN IN THE MAIN_ACTIVITY
            Intent intent = getIntent();
            bitmapCropped = intent.getParcelableExtra("BitmapImage");
            doOCR(bitmapCropped);
        }


        Log.e(TAG, "THE STRING IS");
        Log.e(TAG, this.handle_ocr_or_barcode);


        //-------------------------------------------------------------
        // accept the barcode (NOT THE OCR ERROR CODE) from BarcodeActivity
        //--------------------------------------------------------------
        if(this.handle_ocr_or_barcode == "barcode") {

            Log.e(TAG, "WE HAVE JUST RECIEVED A BARCODE");
            Bundle barcodeData = getIntent().getExtras();
            if (barcodeData == null) {
                return;
            }

            String barCode = barcodeData.getString("bar code string");
            codeTextView.setText(barCode);
            codeTextView.setEnabled(false);

        }*/
    }


    //----------------------------------------------------------------------------------------
    // getter and setter for a static variable to determine in the onCreate method wether to try
    // recieve a barcode or try recieve an error code
    //--------------------------------------------------------------------------------
    public String getHandle_ocr_or_barcode() {
        return this.handle_ocr_or_barcode;
    }
   public void setHandle_ocr_or_barcode(String handle_ocr_or_barcode) {
        this.handle_ocr_or_barcode = handle_ocr_or_barcode;
    }


    //-----------------------------------------------------
    // Extract the code from the bitmap image
    //----------------------------------------------------
    public String getOCRResult(Bitmap bitmap) {

        try{
            tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.setDebug(false);
            // whitelist only the characters we need
            //tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_OSD_ONLY);
            tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST,"0123456789");
          //  tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST,"GHIJKLMNOPQRSTUVWXYZabcdefghijklmopqrstuvwxyz>?!/;:)(");

            tessBaseAPI.init(DATA_PATH+ File.separator,"eng");


           // tessBaseAPI.init(DATA_PATH, "eng");
           // tessBaseAPI.init(DATA_PATH, "digital");

            Log.e(TAG, "THE LOCATION OF THE ENG FILE IS ");
            Log.e(TAG, DATA_PATH);
            tessBaseAPI.setImage(bitmap);
        }catch (Exception e){
            Log.e(TAG, "FAILURE AT GET TEXT");
            return "Cannot scan this image";
        }
        return tessBaseAPI.getUTF8Text();
    }


    //----------------------------------------------------
    // HANDLE ALL BUTTON CLICKS
    //-----------------------------------------------------
    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.edit_error_code:    // this button edits the error code incase it wasn't scanned correctly

                codeTextView.setEnabled(true);

                break;

            case R.id.btn_send_to_server:     // Send the error code to a server

                String tempMessage = "Code being sent to server is : " + codeTextView.getText().toString();
                Toast.makeText(ocrActivity.this, tempMessage, Toast.LENGTH_LONG).show();
                SendingToServer server = new SendingToServer();
                server.setErrorCode(codeTextView.getText().toString());
                server.send();
                break;

            case R.id.btn_back_to_camera:  // go back to previous activity where the picture is chosen
                try{
                    finish();

                }catch(NullPointerException e){
                    Log.e(TAG, "caught arror when finishing ocr activity");
                }

                break;

            default:
                break;
        }
    }


    public void doOCR(final Bitmap bitmap){

        new Thread(new Runnable() {
            public void run() {

                final String srcText = getOCRResult(bitmap);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (srcText != null && !srcText.equals("")) {


                           // final EditText codeTextView = (EditText) findViewById(R.id.edit_text_id);
                            codeTextView.setText(srcText);
                            codeTextView.setEnabled(false);

                        }
                      //  onDestroy();
                      //  mProgressDialog.dismiss();
                    }
                });
            }
        }).start();
    }






}
