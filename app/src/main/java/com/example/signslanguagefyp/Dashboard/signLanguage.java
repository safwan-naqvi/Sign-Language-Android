package com.example.signslanguagefyp.Dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.signslanguagefyp.R;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class signLanguage {
    // should start from small letter

    // this is used to load model and predict
    private Interpreter interpreter;

    //create another interpreter for sign language
    private Interpreter interpreter2;

    // store all label in array
    private List<String> labelList;
    private int INPUT_SIZE;
    private int PIXEL_SIZE = 3; // for RGB
    private int IMAGE_MEAN = 0;
    private float IMAGE_STD = 255.0f;
    // use to initialize gpu in app
    private GpuDelegate gpuDelegate;
    private int height = 0;
    private int width = 0;
    private int CLASSIFICATION_INPUT_SIZE = 0;
    private String final_text = "";
    private String current_text = "";


    signLanguage(Context context,MaterialButtonToggleGroup materialButtonToggleGroup, Button send, EditText change_text, AssetManager assetManager, String modelPath, String labelPath, int inputSize, String classification_model, int classification_size) throws IOException {
        INPUT_SIZE = inputSize;
        CLASSIFICATION_INPUT_SIZE = classification_size;
        // use to define gpu or cpu // no. of threads
        Interpreter.Options options = new Interpreter.Options();
        gpuDelegate = new GpuDelegate();
        options.addDelegate(gpuDelegate);
        options.setNumThreads(2); // set it according to your phone
        // loading model
        interpreter = new Interpreter(loadModelFile(assetManager, modelPath), options);
        // load labelmap
        labelList = loadLabelList(assetManager, labelPath);

        //code for loading model

        Interpreter.Options options1 = new Interpreter.Options();
        // add 2 threads to this intercepter
        options1.setNumThreads(4);
        //load model
        interpreter2 = new Interpreter(loadModelFile(assetManager, classification_model), options1);

        materialButtonToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {

                    switch (checkedId) {
                        case R.id.addbtn:
                            final_text = final_text + current_text;
                            change_text.setText(final_text);
                            break;
                        case R.id.clearbtn:
                            final_text = "";
                            change_text.setText(final_text);
                            break;
                        case R.id.backbtn:
                            int input = final_text.length();
                            if (input > 0) {
                                final_text = final_text.substring(0, input - 1);
                                change_text.setText(final_text);
                            }
                            break;

                    }
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareBody = change_text.getText().toString().trim();
                if(!shareBody.isEmpty()){
                    /*Create an ACTION_SEND Intent*/
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    /*This will be the actual content you wish you share.*/
                    /*The type of the content is text, obviously.*/
                    intent.setType("text/plain");
                    /*Applying information Subject and Body.*/
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, change_text.getText().toString());
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    /*Fire!*/
                    context.startActivity(Intent.createChooser(intent, "Share Your Message On"));
                }else{
                    Toast.makeText(context, "Nothing to send!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        // to store label
        List<String> labelList = new ArrayList<>();
        // create a new reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        // loop through each line and store it to labelList
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        // use to get description of file
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // create new Mat function
    public Mat recognizeImage(Mat mat_image) {
        // Rotate original image by 90 degree get get portrait frame

        // This change was done in video: Does Your App Keep Crashing? | Watch This Video For Solution.
        // This will fix crashing problem of the app

        Mat rotated_mat_image = new Mat();

        Mat a = mat_image.t();
        Core.flip(a, rotated_mat_image, 1);
        // Release mat
        a.release();

        // if you do not do this process you will get improper prediction, less no. of object
        // now convert it to bitmap
        Bitmap bitmap = null;

        bitmap = Bitmap.createBitmap(rotated_mat_image.cols(), rotated_mat_image.rows(), Bitmap.Config.ARGB_8888);//ARGB_8888

        Utils.matToBitmap(rotated_mat_image, bitmap);
        // define height and width
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        // scale the bitmap to input size of model
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

        // convert bitmap to bytebuffer as model input should be in it
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);

        // defining output
        // 10: top 10 object detected
        // 4: there coordinate in image
        //  float[][][]result=new float[1][10][4];
        Object[] input = new Object[1];
        input[0] = byteBuffer;

        Map<Integer, Object> output_map = new TreeMap<>();
        // we are not going to use this method of output
        // instead we create treemap of three array (boxes,score,classes)

        float[][][] boxes = new float[1][10][4];
        // 10: top 10 object detected
        // 4: there coordinate in image
        float[][] scores = new float[1][10];
        // stores scores of 10 object
        float[][] classes = new float[1][10];
        // stores class of object

        // add it to object_map;
        output_map.put(0, boxes);
        output_map.put(1, classes);
        output_map.put(2, scores);

        // now predict
        interpreter.runForMultipleInputsOutputs(input, output_map);
        // Before watching this video please watch my previous 2 video of
        //      1. Loading tensorflow lite model
        //      2. Predicting object
        // In this video we will draw boxes and label it with it's name

        Object value = output_map.get(0);
        Object Object_class = output_map.get(1);
        Object score = output_map.get(2);

        // loop through each object
        // as output has only 10 boxes
        for (int i = 0; i < 10; i++) {
            float class_value = (float) Array.get(Array.get(Object_class, 0), i);
            float score_value = (float) Array.get(Array.get(score, 0), i);
            // define threshold for score

            // Here you can change threshold according to your model
            // Now we will do some change to improve app
            if (score_value >= 0.75) {
                Object box1 = Array.get(Array.get(value, 0), i);
                // we are multiplying it with Original height and width of frame
                // change this into x1,y1 and x2,y2
                float y1 = (float) Array.get(box1, 0) * height;
                float x1 = (float) Array.get(box1, 1) * width;
                float y2 = (float) Array.get(box1, 2) * height;
                float x2 = (float) Array.get(box1, 3) * width;

                //seet boundary list
                if (y1 < 0) {
                    y1 = 0;
                }
                if (x1 < 0) {
                    x1 = 0;
                }
                if (x2 > width) {
                    x2 = width;
                }
                if (y2 > height) {
                    y2 = height;
                }

                //now set height and width of box
                //so if you don't know
                // (x1,y1) is starting point of hand
                float w1 = x2 - x1;
                float h1 = y2 - y1;

                //crop hand image from original frame
                Rect cropped_roi = new Rect((int) x1, (int) y1, (int) w1, (int) h1);
                Mat cropped = new Mat(rotated_mat_image, cropped_roi).clone();
                //now convert this cropped mat to bitmap

                Bitmap bitmap1 = null;
                bitmap1 = Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888);
                //bitmap1 = toGrayscale(bitmap1); //coverting to grayscale image
                Utils.matToBitmap(cropped, bitmap1);

                //resize bitmap1 to classify input size 96
                Bitmap scaledBitmap1 = Bitmap.createScaledBitmap(bitmap1, CLASSIFICATION_INPUT_SIZE, CLASSIFICATION_INPUT_SIZE, false);
                //convert scaled Bitmap to byte buffer
                ByteBuffer byteBuffer1 = convertBitmapToByteBuffer1(scaledBitmap1);

                float[][] output_class_value = new float[1][1];

                //Predict output
                interpreter2.run(byteBuffer1, output_class_value);
                Log.i("objectDetectionClass", "output Class" + output_class_value[0][0]);
                //now create function to get alphabet
                String sign_val = get_alphabet(output_class_value[0][0]);
                current_text = sign_val;

                Imgproc.putText(rotated_mat_image, "" + sign_val, new Point(x1 + 10, y1 + 40), 3, 1.5, new Scalar(255, 255, 255, 255), 2);

                // draw rectangle in Original frame //  starting point    // ending point of box  // color of box       thickness
                Imgproc.rectangle(rotated_mat_image, new Point(x1, y1), new Point(x2, y2), new Scalar(0, 255, 0, 255), 2);
                // write text on frame
                // string of class name of object  // starting point                         // color of text           // size of text
                //
            }

        }
        // select device and run

        // before returning rotate back by -90 degree

        // Do same here
        Mat b = rotated_mat_image.t();
        Core.flip(b, mat_image, 0);
        b.release();
        // Now for second change go to CameraBridgeViewBase
        return mat_image;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private String get_alphabet(float sign_v) {
        String val = "";
        if (sign_v >= -0.5 & sign_v < 0.5) {
            val = "A";
        } else if (sign_v > 0.5 & sign_v < 1.5) {
            val = "B";
        } else if (sign_v > 1.5 & sign_v < 2.5) {
            val = "C";
        } else if (sign_v > 2.5 & sign_v < 3.5) {
            val = "D";
        } else if (sign_v > 3.5 & sign_v < 4.5) {
            val = "E";
        } else if (sign_v > 4.5 & sign_v < 5.5) {
            val = "F";
        } else if (sign_v > 5.5 & sign_v < 6.5) {
            val = "G";
        } else if (sign_v > 6.5 & sign_v < 7.5) {
            val = "H";
        } else if (sign_v > 7.5 & sign_v < 8.5) {
            val = "I";
        } else if (sign_v > 8.5 & sign_v < 9.5) {
            val = "J";
        } else if (sign_v > 9.5 & sign_v < 10.5) {
            val = "K";
        } else if (sign_v > 10.5 & sign_v < 11.5) {
            val = "L";
        } else if (sign_v > 11.5 & sign_v < 12.5) {
            val = "M";
        } else if (sign_v > 12.5 & sign_v < 13.5) {
            val = "N";
        } else if (sign_v > 13.5 & sign_v < 14.5) {
            val = "O";
        } else if (sign_v > 14.5 & sign_v < 15.5) {
            val = "P";
        } else if (sign_v > 15.5 & sign_v < 16.5) {
            val = "Q";
        } else if (sign_v > 16.5 & sign_v < 17.5) {
            val = "R";
        } else if (sign_v > 17.5 & sign_v < 18.5) {
            val = "S";
        } else if (sign_v > 18.5 & sign_v < 19.5) {
            val = "T";
        } else if (sign_v > 19.5 & sign_v < 20.5) {
            val = "U";
        } else if (sign_v > 20.5 & sign_v < 21.5) {
            val = "V";
        } else if (sign_v > 21.5 & sign_v < 22.5) {
            val = "W";
        } else if (sign_v > 22.5 & sign_v < 23.5) {
            val = "X";
        } else if (sign_v > 23.5 & sign_v < 24.5) {
            val = "Y";
        }else{
            val = "Z";
        }
        return val;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer;
        // some model input should be quant=0  for some quant=1
        // for this quant=0
        // Change quant=1
        // As we are scaling image from 0-255 to 0-1
        int quant = 1;
        int size_images = INPUT_SIZE;
        if (quant == 0) {
            byteBuffer = ByteBuffer.allocateDirect(1 * size_images * size_images * 3); //*3
        } else {
            byteBuffer = ByteBuffer.allocateDirect(4 * 1 * size_images * size_images * 3); //*3
        }
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[size_images * size_images];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;

        // some error
        //now run
        for (int i = 0; i < size_images; ++i) {
            for (int j = 0; j < size_images; ++j) {
                final int val = intValues[pixel++];
                if (quant == 0) {
                    byteBuffer.put((byte) ((val >> 16) & 0xFF));
                    byteBuffer.put((byte) ((val >> 8) & 0xFF));
                    byteBuffer.put((byte) (val & 0xFF));
                } else {
                    // paste this
                    byteBuffer.putFloat((((val >> 16) & 0xFF)) / 255.0f);
                    byteBuffer.putFloat((((val >> 8) & 0xFF)) / 255.0f);
                    byteBuffer.putFloat((((val) & 0xFF)) / 255.0f);
                }
            }
        }
        return byteBuffer;
    }

    //create a copy of
    private ByteBuffer convertBitmapToByteBuffer1(Bitmap bitmap) {
        ByteBuffer byteBuffer;
        // some model input should be quant=0  for some quant=1
        // for this quant=0
        // Change quant=1
        // As we are scaling image from 0-255 to 0-1
        int quant = 1;
        int size_images = CLASSIFICATION_INPUT_SIZE;
        if (quant == 0) {
            byteBuffer = ByteBuffer.allocateDirect(1 * size_images * size_images * 3); //*3
        } else {
            byteBuffer = ByteBuffer.allocateDirect(4 * 1 * size_images * size_images * 3); //*3
        }
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[size_images * size_images];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        //
        // some error
        //now run
        for (int i = 0; i < size_images; ++i) {
            for (int j = 0; j < size_images; ++j) {
                final int val = intValues[pixel++];
                if (quant == 0) {
                    byteBuffer.put((byte) ((val >> 16) & 0xFF));
                    byteBuffer.put((byte) ((val >> 8) & 0xFF));
                    byteBuffer.put((byte) (val & 0xFF));
                } else {
                    //                paste this
                    byteBuffer.putFloat((((val >> 16) & 0xFF)));
                    byteBuffer.putFloat((((val >> 8) & 0xFF)));
                    byteBuffer.putFloat((((val) & 0xFF)));
                }
            }
        }
        return byteBuffer;
    }
}
// Next video is about drawing box and labeling it
// If you have any problem please inform me