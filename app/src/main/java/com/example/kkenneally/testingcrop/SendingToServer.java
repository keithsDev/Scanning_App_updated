package com.example.kkenneally.testingcrop;

import android.util.Log;

/**
 * Created by k.kenneally on 08/02/2018.
 */

public class SendingToServer {


    private static String errorCode;
    private static final String TAG = SendingToServer.class.getSimpleName();


    public static String getErrorCode() {
        return errorCode;
    }

    public static void setErrorCode(String errorCode) {
        SendingToServer.errorCode = errorCode;
    }


    public void send() {

        Log.e(TAG, "Message to be sent is ");
        Log.e(TAG, this.errorCode);

    }


    // SEND REQUEST TO SERVER TO GET THE TYPE OF DEVICE SCANNED
    public String identifyDevice(String barcode){
        return "PNOZ m B1";
    }
}
