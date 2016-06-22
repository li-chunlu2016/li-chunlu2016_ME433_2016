package com.example.david.tpadv1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TabHost;
import android.widget.Toast;

public class ImageActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.david.tpadv1.MESSAGE";

    RatingBar ratingBar, ratingBar2, ratingBar3, ratingBar4, ratingBar5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        TabHost host = (TabHost)findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Tab One");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Test Image");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Tab Two");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Feedback");
        host.addTab(spec);

//        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
//        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
//            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
//                Toast.makeText(getApplicationContext(),"Selected Ratings: " + String.valueOf(rating), Toast.LENGTH_LONG).show();
//            }
//        });
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, FeedbackActivity.class);

        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
//        intent.putExtra(EXTRA_MESSAGE, message);

        RatingBar readRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        String readBar = String.valueOf(readRatingBar.getRating());

        RatingBar readRatingBar2 = (RatingBar) findViewById(R.id.ratingBar2);
        String readBar2 = String.valueOf(readRatingBar2.getRating());

        RatingBar readRatingBar3 = (RatingBar) findViewById(R.id.ratingBar3);
        String readBar3 = String.valueOf(readRatingBar3.getRating());

        RatingBar readRatingBar4 = (RatingBar) findViewById(R.id.ratingBar4);
        String readBar4 = String.valueOf(readRatingBar4.getRating());

        RatingBar readRatingBar5 = (RatingBar) findViewById(R.id.ratingBar5);
        String readBar5 = String.valueOf(readRatingBar5.getRating());


        intent.putExtra(EXTRA_MESSAGE, readBar + "," + readBar2 + "," + readBar3 + "," + readBar4 + "," + readBar5 + "," + message);

        startActivity(intent);
    }
}
