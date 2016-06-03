package edu.northwestern.li_chunlu2016u.trackingcamera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


// libraries
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

public class MainActivity extends Activity implements TextureView.SurfaceTextureListener {
    private Camera mCamera;
    private TextureView mTextureView;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Bitmap bmp = Bitmap.createBitmap(640,480,Bitmap.Config.ARGB_8888);
    private Canvas canvas = new Canvas(bmp);
    private Paint paint1 = new Paint();
    private TextView mTextView;
    private int threshold;

    static long prevtime = 0; // for FPS calculation

    private SeekBar myControl;
    private TextView myTextView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keeps the screen from turning off

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();

        mTextureView = (TextureView) findViewById(R.id.textureview);
        mTextureView.setSurfaceTextureListener(this);
        mTextView = (TextView) findViewById(R.id.cameraStatus);

        paint1.setColor(0xffff0000); // red
        paint1.setTextSize(24);


        myControl = (SeekBar) findViewById(R.id.seek1);
        myTextView = (TextView) findViewById(R.id.textView01);
        myControl.setMax(600);
        myControl.setKeyProgressIncrement(1);
        myTextView.setText("Enter whatever you Like!");
        setMyControlListener();
        threshold = 600;
    }

    private void setMyControlListener() {
        myControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold = progress;
                myTextView.setText("The value is: "+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(640, 480);
        parameters.setColorEffect(Camera.Parameters.EFFECT_NONE); // black and white
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY); // no autofocusing
        parameters.setAutoWhiteBalanceLock(true);
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90); // rotate to portrait mode

        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    // the important function
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Invoked every time there's a new Camera preview frame
        mTextureView.getBitmap(bmp);

        final Canvas c = mSurfaceHolder.lockCanvas();
        if (c != null) {

            int[] pixels = new int[bmp.getWidth()];
            int[] pixels1 = new int[bmp.getWidth()];
            int[] pixels2 = new int[bmp.getWidth()];
            int[] pixels3 = new int[bmp.getWidth()];
            int[] pixels4 = new int[bmp.getWidth()];
            int[] pixels5 = new int[bmp.getWidth()];

            int startY = 50; // which row in the bitmap to analyse to read
            int startY1 = 100;
            int startY2 = 150;
            int startY3 = 200;
            int startY4 = 300;
            int startY5 = 400;
            // only look at one row in the image
            bmp.getPixels(pixels, 0, bmp.getWidth(), 0, startY, bmp.getWidth(), 1); // (array name, offset inside array, stride (size of row), start x, start y, num pixels to read per row, num rows to read)
            bmp.getPixels(pixels1, 0, bmp.getWidth(), 0, startY1, bmp.getWidth(), 1);
            bmp.getPixels(pixels2, 0, bmp.getWidth(), 0, startY2, bmp.getWidth(), 1);
            bmp.getPixels(pixels3, 0, bmp.getWidth(), 0, startY3, bmp.getWidth(), 1);
            bmp.getPixels(pixels4, 0, bmp.getWidth(), 0, startY4, bmp.getWidth(), 1);
            bmp.getPixels(pixels5, 0, bmp.getWidth(), 0, startY5, bmp.getWidth(), 1);

            // pixels[] is the RGBA data (in black an white).
            // instead of doing center of mass on it, decide if each pixel is dark enough to consider black or white
            // then do a center of mass on the thresholded array
            int[] thresholdedPixels = new int[bmp.getWidth()];
            int wbTotal = 0; // total mass
            int wbCOM = 0; // total (mass time position)
            for (int i = 0; i < bmp.getWidth(); i++) {
                // sum the red, green and blue, subtract from 255 to get the darkness of the pixel.
                // if it is greater than some value (600 here), consider it black
                // play with the 600 value if you are having issues reliably seeing the line
                if (255*3-(red(pixels[i])+green(pixels[i])+blue(pixels[i])) > threshold) {
                    thresholdedPixels[i] = 255*3;
                }
                else {
                    thresholdedPixels[i] = 0;
                }
                wbTotal = wbTotal + thresholdedPixels[i];
                wbCOM = wbCOM + thresholdedPixels[i]*i;
            }
            int COM;
            //watch out for divide by 0
            if (wbTotal<=0) {
                COM = bmp.getWidth()/2;
            }
            else {
                COM = wbCOM/wbTotal;
            }

            int[] thresholdedPixels1 = new int[bmp.getWidth()];
            int wbTotal1 = 0; // total mass
            int wbCOM1 = 0; // total (mass time position)
            for (int i = 0; i < bmp.getWidth(); i++) {
                // sum the red, green and blue, subtract from 255 to get the darkness of the pixel.
                // if it is greater than some value (600 here), consider it black
                // play with the 600 value if you are having issues reliably seeing the line
                if (255*3-(red(pixels1[i])+green(pixels1[i])+blue(pixels1[i])) > threshold) {
                    thresholdedPixels1[i] = 255*3;
                }
                else {
                    thresholdedPixels1[i] = 0;
                }
                wbTotal1 = wbTotal1 + thresholdedPixels1[i];
                wbCOM1 = wbCOM1 + thresholdedPixels1[i]*i;
            }
            int COM1;
            //watch out for divide by 0
            if (wbTotal1<=0) {
                COM1 = bmp.getWidth()/2;
            }
            else {
                COM1 = wbCOM1/wbTotal1;
            }

            int[] thresholdedPixels2 = new int[bmp.getWidth()];
            int wbTotal2 = 0; // total mass
            int wbCOM2 = 0; // total (mass time position)
            for (int i = 0; i < bmp.getWidth(); i++) {
                // sum the red, green and blue, subtract from 255 to get the darkness of the pixel.
                // if it is greater than some value (600 here), consider it black
                // play with the 600 value if you are having issues reliably seeing the line
                if (255*3-(red(pixels2[i])+green(pixels2[i])+blue(pixels2[i])) > threshold) {
                    thresholdedPixels2[i] = 255*3;
                }
                else {
                    thresholdedPixels2[i] = 0;
                }
                wbTotal2 = wbTotal2 + thresholdedPixels2[i];
                wbCOM2 = wbCOM2 + thresholdedPixels2[i]*i;
            }
            int COM2;
            //watch out for divide by 0
            if (wbTotal2<=0) {
                COM2 = bmp.getWidth()/2;
            }
            else {
                COM2 = wbCOM2/wbTotal2;
            }

            int[] thresholdedPixels3 = new int[bmp.getWidth()];
            int wbTotal3 = 0; // total mass
            int wbCOM3 = 0; // total (mass time position)
            for (int i = 0; i < bmp.getWidth(); i++) {
                // sum the red, green and blue, subtract from 255 to get the darkness of the pixel.
                // if it is greater than some value (600 here), consider it black
                // play with the 600 value if you are having issues reliably seeing the line
                if (255*3-(red(pixels3[i])+green(pixels3[i])+blue(pixels3[i])) > threshold) {
                    thresholdedPixels3[i] = 255*3;
                }
                else {
                    thresholdedPixels3[i] = 0;
                }
                wbTotal3 = wbTotal3 + thresholdedPixels3[i];
                wbCOM3 = wbCOM3 + thresholdedPixels3[i]*i;
            }
            int COM3;
            //watch out for divide by 0
            if (wbTotal3<=0) {
                COM3 = bmp.getWidth()/2;
            }
            else {
                COM3 = wbCOM3/wbTotal3;
            }

            int[] thresholdedPixels4 = new int[bmp.getWidth()];
            int wbTotal4 = 0; // total mass
            int wbCOM4 = 0; // total (mass time position)
            for (int i = 0; i < bmp.getWidth(); i++) {
                // sum the red, green and blue, subtract from 255 to get the darkness of the pixel.
                // if it is greater than some value (600 here), consider it black
                // play with the 600 value if you are having issues reliably seeing the line
                if (255*3-(red(pixels4[i])+green(pixels4[i])+blue(pixels4[i])) > threshold) {
                    thresholdedPixels4[i] = 255*3;
                }
                else {
                    thresholdedPixels4[i] = 0;
                }
                wbTotal4 = wbTotal4 + thresholdedPixels4[i];
                wbCOM4 = wbCOM4 + thresholdedPixels4[i]*i;
            }
            int COM4;
            //watch out for divide by 0
            if (wbTotal4<=0) {
                COM4 = bmp.getWidth()/2;
            }
            else {
                COM4 = wbCOM4/wbTotal4;
            }

            int[] thresholdedPixels5 = new int[bmp.getWidth()];
            int wbTotal5 = 0; // total mass
            int wbCOM5 = 0; // total (mass time position)
            for (int i = 0; i < bmp.getWidth(); i++) {
                // sum the red, green and blue, subtract from 255 to get the darkness of the pixel.
                // if it is greater than some value (600 here), consider it black
                // play with the 600 value if you are having issues reliably seeing the line
                if (255*3-(red(pixels5[i])+green(pixels5[i])+blue(pixels5[i])) > threshold) {
                    thresholdedPixels5[i] = 255*3;
                }
                else {
                    thresholdedPixels5[i] = 0;
                }
                wbTotal5 = wbTotal5 + thresholdedPixels5[i];
                wbCOM5 = wbCOM5 + thresholdedPixels5[i]*i;
            }
            int COM5;
            //watch out for divide by 0
            if (wbTotal5<=0) {
                COM5 = bmp.getWidth()/2;
            }
            else {
                COM5 = wbCOM5/wbTotal5;
            }
            // draw a circle where you think the COM is
            canvas.drawCircle(COM, startY, 5, paint1);
            canvas.drawCircle(COM1, startY1, 5, paint1);
            canvas.drawCircle(COM2, startY2, 5, paint1);
            canvas.drawCircle(COM3, startY3, 5, paint1);
            canvas.drawCircle(COM4, startY4, 5, paint1);
            canvas.drawCircle(COM5, startY5, 5, paint1);

            // also write the value as text
            canvas.drawText("COM = " + COM, 10, 200, paint1);
            //canvas.drawText("R = " + red(pixels[320]), 10, 220, paint1);
            //canvas.drawText("G = " + green(pixels[320]), 10, 240, paint1);
            //canvas.drawText("B = " + blue(pixels[320]), 10, 260, paint1);
            c.drawBitmap(bmp, 0, 0, null);
            mSurfaceHolder.unlockCanvasAndPost(c);

            // calculate the FPS to see how fast the code is running
            long nowtime = System.currentTimeMillis();
            long diff = nowtime - prevtime;
            mTextView.setText("FPS " + 1000/diff);
            prevtime = nowtime;
        }
    }
}