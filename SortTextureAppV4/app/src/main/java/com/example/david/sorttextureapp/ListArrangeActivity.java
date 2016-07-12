package com.example.david.sorttextureapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Random;

public class ListArrangeActivity extends AppCompatActivity {
    static Random random = new Random();
    DraggableGridView dgv;
    Button button4;
    ArrayList<String> myorder = new ArrayList<String>();

    int[] textureImages = {R.drawable.image1,R.drawable.image2,R.drawable.image3,R.drawable.image4,R.drawable.image5,R.drawable.image6,R.drawable.image7,R.drawable.image8,R.drawable.image9,R.drawable.image10};
    private int current_image_index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_arrange);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        dgv = ((DraggableGridView)findViewById(R.id.vgv));
        button4 = ((Button)findViewById(R.id.button4));

        setListeners();

        for (current_image_index = 0; current_image_index < textureImages.length; current_image_index++) {
            ImageView view = new ImageView(ListArrangeActivity.this);
            view.setImageBitmap(getThumb(textureImages[current_image_index]));
            myorder.add(String.valueOf(current_image_index+1));
            dgv.addView(view);
        }

    }
    private void setListeners()
    {
        dgv.setOnRearrangeListener(new OnRearrangeListener() {
            public void onRearrange(int oldIndex, int newIndex) {
                String word = myorder.remove(oldIndex);
                if (oldIndex < newIndex)
                    myorder.add(newIndex, word);
                else
                    myorder.add(newIndex, word);
            }
        });
        dgv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //dgv.removeViewAt(arg2);
                //myorder.remove(arg2);
                Intent intent = new Intent(ListArrangeActivity.this, ImageFullScreenActivity.class);
                intent.putExtra("currentImage", arg2); // pass current image ImageFullScreenActivity
                intent.putStringArrayListExtra("passToFullActivity", myorder); // pass order to ImageFullScreenActivity
                startActivity(intent);
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                String finishedOrder = "";
                for (String s : myorder)
                    finishedOrder += s + " ";
                new AlertDialog.Builder(ListArrangeActivity.this)
                        .setTitle("Here is your order")
                        .setMessage(finishedOrder).show();
            }
        });
    }

    private Bitmap getThumb(int img)
    {
        Bitmap bmp = Bitmap.createBitmap(600, 600, Bitmap.Config.RGB_565); // 150, 150
        Bitmap myImg = BitmapFactory.decodeResource(getResources(),img);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();

        canvas.drawRect(new Rect(0, 0, 600, 600), paint);
        canvas.drawBitmap(myImg, 0, 0, null);
        return bmp;
    }
}
