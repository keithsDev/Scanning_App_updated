package com.example.kkenneally.testingcrop;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;




import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();



    static {
        if (!OpenCVLoader.initDebug()){
            Log.e(TAG, "Opencv not loaded");
        }
        else{
            Log.e(TAG, "Opencv loaded successfully");
        }
    }




    ImageView imageView;
    Button buttonCamera, buttonGallery, buttonScan, buttonBarcode ;
    File file;
    Uri uri;
    Intent CamIntent, GalIntent, CropIntent ;
    public  static final int RequestPermissionCode  = 1 ;
   // DisplayMetrics displayMetrics ;
   // int width, height;
    Bitmap bitmap;
    Bitmap binarizedImage;
   // Bitmap greyScaleBitmap;
    Bitmap bitmapPreprocced;
    String deviceType;
    FloatingActionButton floatButtonShowDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // Toast.makeText(MainActivity.this,"IN THE MAIN ACTIVITY NOW", Toast.LENGTH_LONG).show();

        imageView = findViewById(R.id.imageview);
        buttonCamera = findViewById(R.id.btn_image_from_camera);
        buttonGallery = findViewById(R.id.btn_crop_from_gallery);
        buttonBarcode = findViewById(R.id.btn_barcode);
        floatButtonShowDevice = findViewById(R.id.floatingActionButton2_id);

        EnableRuntimePermission();

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClickImageFromCamera() ;

            }
        });

        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GetImageFromGallery();

            }
        });
        buttonBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                handleBarcode();
            }
        });
        floatButtonShowDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = "Device Type: " + deviceType;
                Toast.makeText(MainActivity.this,id, Toast.LENGTH_LONG).show();
            }
        });


        // these lines are needed to stop the app from crahsing on newver versions
        // it will ignore URI exposure
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // ------------------------------------------------------------------------------
        // EXTRACT THE DEVICE TYPE FROM THE BARCODE ACTIVITY
        Bundle type = getIntent().getExtras();
        if(type == null){
            //return;
             // Toast.makeText(MainActivity.this,"Device type not found", Toast.LENGTH_LONG).show();
              floatButtonShowDevice.setVisibility(View.INVISIBLE);

        }
        else{
            floatButtonShowDevice.setVisibility(View.VISIBLE);
            deviceType= type.getString("device type");

        }

    }

    private void handleBarcode() {
        Intent intent = new Intent(this, BarcodeActivity.class);
        startActivity(intent);
    }

    public void ClickImageFromCamera() {

      //  Toast.makeText(MainActivity.this,"Trying not to crash!!", Toast.LENGTH_LONG).show();

        CamIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        file = new File(Environment.getExternalStorageDirectory(),
                "file" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        uri = Uri.fromFile(file);

        CamIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);

        CamIntent.putExtra("return-data", true);

        startActivityForResult(CamIntent, 0);

    }

    public void GetImageFromGallery(){

        try{
            GalIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(Intent.createChooser(GalIntent, "Select Image From Gallery"), 2);
        }catch (IllegalArgumentException e){
            Log.e(TAG , "Illegal argument excpetion data path does not exist");
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0 && resultCode == RESULT_OK) {
           Log.e(TAG, "CODE IS 0");

           // retrievedCodeInitiated();

            ImageCropFunction();
           // Toast.makeText(MainActivity.this,"Success", Toast.LENGTH_LONG).show();
        }
        // if chosen from gallery is succesful
        else if (requestCode == 2) {

            if (data != null) {
                Log.e(TAG, "CODE IS 2");

                uri = data.getData();
               // Log.e(TAG, "THE LOCATION OF THE IMAGE IS ");
               // Log.e(TAG, uri.getPath());

                ImageCropFunction();
                retrievedCodeInitiated();

            }
        }
        // if taking a new pic is susscessful
        else if (requestCode == 1) {
            Log.e(TAG, "CODE IS 1");

            if (data != null) {

                Bundle bundle = data.getExtras();

                bitmap = bundle.getParcelable("data");
               // greyScaleBitmap= GrayscaleToBin(bitmap);


                // --HERE


               /* binarizedImage = convertToMutable(bitmap);


                for(int i=0;i<binarizedImage.getWidth();i++) {
                    for(int c=0;c<binarizedImage.getHeight();c++) {
                        int pixel = binarizedImage.getPixel(i, c);
                        if(shouldBeBlack(pixel))
                            binarizedImage.setPixel(i, c, Color.BLACK);
                        else
                            binarizedImage.setPixel(i, c, Color.WHITE);
                    }
                }*/

                // ---TO HERE
                bitmapPreprocced = bitmap;
                bitmapPreprocced = opencvconvertoToGreyScale(bitmap);
                //bitmapPreprocced = opencvBinarizeImage(bitmap);
                bitmapPreprocced = Thresholding(bitmapPreprocced);
//               bitmapPreprocced = morphOps(bitmapPreprocced);
               bitmapPreprocced = erodeandDialate(bitmapPreprocced);


                //  imageView.setImageBitmap(bitmap);


                imageView.setImageBitmap(bitmapPreprocced);

                //imageView.setImageBitmap(binarizedImage);
             //   imageView.setImageBitmap(greyScaleBitmap);



                retrievedCodeInitiated();

            }
        }
    }


    //-------------------------------------------------------------------
     // OPENCV PREPROCESSING IMAGE
    //-------------------------------------------------------------------
    Bitmap opencvconvertoToGreyScale(Bitmap b){

        Bitmap bit;
        Mat tmp = new Mat (b.getWidth(), b.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(b, tmp);
        Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_RGB2GRAY);

        Utils.matToBitmap(tmp, b);
        bit = b;
        return bit;

    }

    /*Bitmap opencvBinarizeImage(Bitmap b){

        Bitmap bit;
        Mat imageMat = new Mat();

        Utils.bitmapToMat(b, imageMat);
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);
       // Imgproc.GaussianBlur(imageMat, imageMat, new Size(3, 3), 0);
      //  Imgproc.adaptiveThreshold(imageMat, imageMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 4);

      //  Imgproc.medianBlur(imageMat, imageMat, 3);
        //Imgproc.threshold(imageMat, imageMat, 0, 255, Imgproc.THRESH_OTSU);

        bit = b;
        return bit;

    }*/

    public Bitmap Thresholding(Bitmap bitmap)
    {
        Mat imgMat = new Mat();
        Utils.bitmapToMat(bitmap, imgMat);
        imgMat.convertTo(imgMat, CvType.CV_32FC1, 1.0 / 255.0);

        Mat res = CalcBlockMeanVariance(imgMat, 21);
        Core.subtract(new MatOfDouble(1.0), res, res);
        Imgproc.cvtColor( imgMat, imgMat, Imgproc.COLOR_BGRA2BGR);
        Core.add(imgMat, res, res);

//        Imgproc.threshold(res, res, 0.85, 1, Imgproc.THRESH_BINARY);
       // Imgproc.threshold(res, res, 0.94, 1, Imgproc.THRESH_BINARY);
        Imgproc.threshold(res, res, 0.93, 1, Imgproc.THRESH_BINARY);



        res.convertTo(res, CvType.CV_8UC1, 255.0);
        Utils.matToBitmap(res, bitmap);


        return bitmap;
    }

    public Mat CalcBlockMeanVariance (Mat Img, int blockSide)
    {
        Mat I = new Mat();
        Mat ResMat;
        Mat inpaintmask = new Mat();
        Mat patch;
        Mat smallImg = new Mat();
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();

        Img.convertTo(I, CvType.CV_32FC1);
        ResMat = Mat.zeros(Img.rows() / blockSide, Img.cols() / blockSide, CvType.CV_32FC1);

        for (int i = 0; i < Img.rows() - blockSide; i += blockSide)
        {
            for (int j = 0; j < Img.cols() - blockSide; j += blockSide)
            {
              //  patch = new Mat(I,new Rect(j,i, blockSide, blockSide));
                patch = new Mat(I, new org.opencv.core.Rect(j,i,blockSide,blockSide));
                Core.meanStdDev(patch, mean, stddev);

                if (stddev.get(0,0)[0] > 0.01)
                    ResMat.put(i / blockSide, j / blockSide, mean.get(0,0)[0]);
                else
                    ResMat.put(i / blockSide, j / blockSide, 0);
            }
        }

        Imgproc.resize(I, smallImg, ResMat.size());
        Imgproc.threshold(ResMat, inpaintmask, 0.02, 1.0, Imgproc.THRESH_BINARY);

        Mat inpainted = new Mat();
        Imgproc.cvtColor(smallImg, smallImg, Imgproc.COLOR_RGBA2BGR);
        smallImg.convertTo(smallImg, CvType.CV_8UC1, 255.0);

        inpaintmask.convertTo(inpaintmask, CvType.CV_8UC1);
        Photo.inpaint(smallImg, inpaintmask, inpainted, 5, Photo.INPAINT_TELEA);

        Imgproc.resize(inpainted, ResMat, Img.size());
        ResMat.convertTo(ResMat, CvType.CV_32FC1, 1.0 / 255.0);

        return ResMat;
    }


    public Bitmap erodeandDialate(Bitmap bitmap){


       /* try{
            System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
           // Mat source = Highgui.imread("digital_image_processing.jpg",  Highgui.CV_LOAD_IMAGE_COLOR);

            Mat source = new Mat();
            Utils.bitmapToMat(b, source);
            Mat destination = new Mat(source.rows(),source.cols(),source.type());

            destination = source;

            int erosion_size = 5;
            int dilation_size = 5;

            Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(2*erosion_size + 1, 2*erosion_size+1));
            Imgproc.erode(source, destination, element);
            Imgcodecs.imread(b);
            Highgui.imwrite("erosion.jpg", destination);

            source = Highgui.imread("digital_image_processing.jpg",  Highgui.CV_LOAD_IMAGE_COLOR);

            destination = source;

            Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(2*dilation_size + 1, 2*dilation_size+1));
            Imgproc.dilate(source, destination, element1);
            Highgui.imwrite("dilation.jpg", destination);

        }catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }*/



       /* Imgproc.erode(b, b, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
        Imgproc.dilate(mInput, mInput, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));*/

       // Mat kernel = Imgproc.getStructuringElement(Imgproc.RECT, new Size(3,3));
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(6,6));

        Bitmap bmap;
        Mat imgMat = new Mat();
        Utils.bitmapToMat(bitmap, imgMat);
       // imgMat.convertTo(imgMat, CvType.CV_32FC1, 1.0 / 255.0);

       // Mat res = CalcBlockMeanVariance(imgMat, 21);


        //Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_CLOSE, new Size(2,2));
        //Imgproc.dilate(imgMat, imgMat, dilateElement);

        Imgproc.erode(imgMat, imgMat, kernel);
        Imgproc.dilate(imgMat, imgMat, kernel);
      //  Imgproc.morphologyEx(imgMat, imgMat, Imgproc.MORPH_CLOSE, kernel);


        // Imgproc.MORPH_CLOSE(imgMat, imgMat, dilateElement);

       // res.convertTo(res, CvType.CV_8UC1, 255.0);
        //Utils.matToBitmap(res, bitmap);
        Utils.matToBitmap(imgMat, bitmap);

        bmap= bitmap;
        return bmap;
    }
    private Bitmap morphOps(Bitmap b){
       // Mat target;

        Bitmap bmap;
        //Mat target = new Mat (b.getWidth(), b.getHeight(), CvType.CV_8UC1);


        Mat target = new Mat();
       // Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
      //  Utils.bitmapToMat(b, target);
       // Utils.bitmapToMat(b, target);

        //Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3));
        //Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8,8));

       // Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        //Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2));


        Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_CLOSE, new Size(2,2));


        // Imgproc.erode(target, target, erodeElement);

        Imgproc.dilate(target, target, dilateElement);
       // Imgproc.erode(target, target, erodeElement);


     //   Mat kernel = new Mat(1, 50, CvType.CV_8UC1, Scalar.all(255));

       // Imgproc.morphologyEx(target, target, Imgproc.MORPH_DILATE, target);
        Utils.bitmapToMat(b, target);


        bmap = b;
        return bmap;

    }





















    //-------------------------------------------------------------------------------
    // CLOSE OPENCV PREPROCESSING IMAGE
    //-------------------------------------------------------------------------------



    private void retrievedCodeInitiated() {

        buttonScan = (Button) findViewById(R.id.btn_retrieve_code);
        buttonScan.setVisibility(View.VISIBLE);

    }

    public void ImageCropFunction() {

        try {
            CropIntent = new Intent("com.android.camera.action.CROP");

            CropIntent.setDataAndType(uri, "image/*");

            CropIntent.putExtra("crop", "true");
            CropIntent.putExtra("outputX", 180);
            CropIntent.putExtra("outputY", 180);
            CropIntent.putExtra("aspectX", 3);
            CropIntent.putExtra("aspectY", 4);
            CropIntent.putExtra("scaleUpIfNeeded", true);
            CropIntent.putExtra("return-data", true);

            startActivityForResult(CropIntent, 1);

        } catch (ActivityNotFoundException e) {

        }
    }

    public void EnableRuntimePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.CAMERA))
        {

            Toast.makeText(MainActivity.this,"CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.CAMERA}, RequestPermissionCode);

        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                 //   Toast.makeText(MainActivity.this,"Permission Granted, Now your application can access CAMERA.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot access CAMERA.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    // BUTTON TO RETRIEVE CODE FROM PICTURE
    public void onClick(View view) {

        Log.e(TAG, "THE LOCATION OF THE IMAGE IS ");
        Log.e(TAG, uri.getPath());
        Log.e(TAG, "SCAN BUTTON PRESSED");

      //  ocrActivity ocr = new ocrActivity();
      //  ocr.setHandle_ocr_or_barcode("errorcode");

        if(deviceType == null){
            deviceType = "Unknown";
        }
        Intent intent = new Intent(this, ocrActivity.class);
        intent.putExtra("BitmapImage", bitmap);
       // intent.putExtra("BitmapImage", binarizedImage);
        // intent.putExtra("BitmapImage", greyScaleBitmap);


        intent.putExtra("DeviceType", deviceType);
        startActivity(intent);

      /* Bundle extras = new Bundle();
        Intent intent = new Intent(this, ocrActivity.class);
       // intent.putExtra("BitmapImage", bitmap);
        extras.putString(deviceType, "device type");
        intent.putExtra()
        startActivity(intent);*/
    }

   /* public Bitmap Thresholding(Bitmap bitmap)
    {
        Mat imgMat = new Mat();
        Utils.bitmapToMat(bitmap, imgMat);
        imgMat.convertTo(imgMat, CvType.CV_32FC1, 1.0 / 255.0);

        Mat res = CalcBlockMeanVariance(imgMat, 21);
        Core.subtract(new MatOfDouble(1.0), res, res);
        Imgproc.cvtColor( imgMat, imgMat, Imgproc.COLOR_BGRA2BGR);
        Core.add(imgMat, res, res);

        Imgproc.threshold(res, res, 0.85, 1, Imgproc.THRESH_BINARY);

        res.convertTo(res, CvType.CV_8UC1, 255.0);
        Utils.matToBitmap(res, bitmap);

        return bitmap;
    }

    public Mat CalcBlockMeanVariance (Mat Img, int blockSide)
    {
        Mat I = new Mat();
        Mat ResMat;
        Mat inpaintmask = new Mat();
        Mat patch;
        Mat smallImg = new Mat();
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();

        Img.convertTo(I, CvType.CV_32FC1);
        ResMat = Mat.zeros(Img.rows() / blockSide, Img.cols() / blockSide, CvType.CV_32FC1);

        for (int i = 0; i < Img.rows() - blockSide; i += blockSide)
        {
            for (int j = 0; j < Img.cols() - blockSide; j += blockSide)
            {
                patch = new Mat(I,new Rect(j,i, blockSide, blockSide));
                Core.meanStdDev(patch, mean, stddev);

                if (stddev.get(0,0)[0] > 0.01)
                    ResMat.put(i / blockSide, j / blockSide, mean.get(0,0)[0]);
                else
                    ResMat.put(i / blockSide, j / blockSide, 0);
            }
        }

        Imgproc.resize(I, smallImg, ResMat.size());
        Imgproc.threshold(ResMat, inpaintmask, 0.02, 1.0, Imgproc.THRESH_BINARY);

        Mat inpainted = new Mat();
        Imgproc.cvtColor(smallImg, smallImg, Imgproc.COLOR_RGBA2BGR);
        smallImg.convertTo(smallImg, CvType.CV_8UC1, 255.0);

        inpaintmask.convertTo(inpaintmask, CvType.CV_8UC1);
        Photo.inpaint(smallImg, inpaintmask, inpainted, 5, Photo.INPAINT_TELEA);

        Imgproc.resize(inpainted, ResMat, Img.size());
        ResMat.convertTo(ResMat, CvType.CV_32FC1, 1.0 / 255.0);

        return ResMat;
    }*/




    private static final boolean TRASNPARENT_IS_BLACK = false;
    /**
     * This is a point that will break the space into Black or white
     * In real words, if the distance between WHITE and BLACK is D;
     * then we should be this percent far from WHITE to be in the black region.
     * Example: If this value is 0.5, the space is equally split.
     */
    private static final double SPACE_BREAKING_POINT = 13.0/30.0;



    private static boolean shouldBeBlack(int pixel) {
        int alpha = Color.alpha(pixel);
        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);
        if(alpha == 0x00) //if this pixel is transparent let me use TRASNPARENT_IS_BLACK
            return TRASNPARENT_IS_BLACK;
        // distance from the white extreme
        double distanceFromWhite = Math.sqrt(Math.pow(0xff - redValue, 2) + Math.pow(0xff - blueValue, 2) + Math.pow(0xff - greenValue, 2));
        // distance from the black extreme //this should not be computed and might be as well a function of distanceFromWhite and the whole distance
        double distanceFromBlack = Math.sqrt(Math.pow(0x00 - redValue, 2) + Math.pow(0x00 - blueValue, 2) + Math.pow(0x00 - greenValue, 2));
        // distance between the extremes //this is a constant that should not be computed :p
        double distance = distanceFromBlack + distanceFromWhite;
        // distance between the extremes
        return ((distanceFromWhite/distance)>SPACE_BREAKING_POINT);
    }

    public static Bitmap convertToMutable(Bitmap imgIn) {
        try {
            //this is the file going to use temporally to save the bytes.
            // This file will not be a image, it will store the raw image data.
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

            //Open an RandomAccessFile
            //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            //into AndroidManifest.xml file
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // get the width and height of the source bitmap.
            int width = imgIn.getWidth();
            int height = imgIn.getHeight();
            Bitmap.Config type = imgIn.getConfig();

            //Copy the byte to the file
            //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes()*height);
            imgIn.copyPixelsToBuffer(map);
            //recycle the source bitmap, this will be no longer used.
            imgIn.recycle();
            System.gc();// try to force the bytes from the imgIn to be released

            //Create a new bitmap to load the bitmap again. Probably the memory will be available.
            imgIn = Bitmap.createBitmap(width, height, type);
            map.position(0);
            //load it back from temporary
            imgIn.copyPixelsFromBuffer(map);
            //close the temporary file and channel , then delete that also
            channel.close();
            randomAccessFile.close();

            // delete the temp file
            file.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imgIn;
    }




    // SECOND SOLUTION TO BINARIZE
   /* public static Bitmap GrayscaleToBin(Bitmap bm2)
    {
        Bitmap bm;
        bm=bm2.copy(Bitmap.Config.RGB_565, true);
        final   int width = bm.getWidth();
        final  int height = bm.getHeight();

        int pixel1,pixel2,pixel3,pixel4,A,R;
        int[]  pixels;
        pixels = new int[width*height];
        bm.getPixels(pixels,0,width,0,0,width,height);
        int size=width*height;
        int s=width/8;
        int s2=s>>1;
        double t=0.15;
        double it=1.0-t;
        int []integral= new int[size];
        int []threshold=new int[size];
        int i,j,diff,x1,y1,x2,y2,ind1,ind2,ind3;
        int sum=0;
        int ind=0;
        while(ind<size)
        {
            sum+=pixels[ind] & 0xFF;
            integral[ind]=sum;
            ind+=width;
        }
        x1=0;
        for(i=1;i<width;++i)
        {
            sum=0;
            ind=i;
            ind3=ind-s2;
            if(i>s)
            {
                x1=i-s;
            }
            diff=i-x1;
            for(j=0;j<height;++j)
            {
                sum+=pixels[ind] & 0xFF;
                integral[ind]=integral[(int)(ind-1)]+sum;
                ind+=width;
                if(i<s2)continue;
                if(j<s2)continue;
                y1=(j<s ? 0 : j-s);
                ind1=y1*width;
                ind2=j*width;

                if (((pixels[ind3]&0xFF)*(diff * (j - y1))) < ((integral[(int)(ind2 + i)] - integral[(int)(ind1 + i)] - integral[(int)(ind2 + x1)] + integral[(int)(ind1 + x1)])*it)) {
                    threshold[ind3] = 0x00;
                } else {
                    threshold[ind3] = 0xFFFFFF;
                }
                ind3 += width;
            }
        }

        y1 = 0;
        for( j = 0; j < height; ++j )
        {
            i = 0;
            y2 =height- 1;
            if( j <height- s2 )
            {
                i = width - s2;
                y2 = j + s2;
            }

            ind = j * width + i;
            if( j > s2 ) y1 = j - s2;
            ind1 = y1 * width;
            ind2 = y2 * width;
            diff = y2 - y1;
            for( ; i < width; ++i, ++ind )
            {

                x1 = ( i < s2 ? 0 : i - s2);
                x2 = i + s2;

                // check the border
                if (x2 >= width) x2 = width - 1;

                if (((pixels[ind]&0xFF)*((x2 - x1) * diff)) < ((integral[(int)(ind2 + x2)] - integral[(int)(ind1 + x2)] - integral[(int)(ind2 + x1)] + integral[(int)(ind1 + x1)])*it)) {
                    threshold[ind] = 0x00;
                } else {
                    threshold[ind] = 0xFFFFFF;
                }
            }
        }
   *//*-------------------------------
    * --------------------------------------------*//*
        bm.setPixels(threshold,0,width,0,0,width,height);

        return bm;
    }*/

}