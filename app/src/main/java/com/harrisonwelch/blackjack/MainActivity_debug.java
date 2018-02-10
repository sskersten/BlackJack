package com.harrisonwelch.blackjack;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Vector;

public class MainActivity_debug extends Activity {

    private String [] cardNumbers = {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};
    private String [] cardSuits = {"Sp","Di","Ht","Cb"};
    Random random = new Random();

    int handSum = 0;
    Vector<String> cardsInHand = new Vector<>();
    Vector<Bitmap> cardBitmaps = new Vector<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("TAG","onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_debug);

        setButtons();

        cardBitmaps = makeSpriteSheet();

        hitPlayer();

    }

    private void setButtons(){
        findViewById(R.id.toNormalMain_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
        Button b = findViewById(R.id.btn_hit);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hitPlayer();
            }
        });
        b = findViewById(R.id.btn_stand);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                standPlayer();
            }
        });
    }

    // gives the player a card
    private void hitPlayer(){
        Log.i("TAG","msg");
        TextView handTv = findViewById(R.id.tv_hand);
        String handStr = handTv.getText().toString();

        // add new card to the table
        handStr += generateCard(cardNumbers,cardSuits);
        handTv.setText(handStr);
        calcTotal();
        ( (ImageView) findViewById(R.id.cardView)).setImageBitmap(generateCardImage(cardBitmaps));
//        ( (ImageView) findViewById(R.id.cardView)).setImageBitmap(cardBitmaps.get(17));
    }

    private void standPlayer(){

    }

    // generates a card based on two arrays of nums and suits
    private String generateCard(String [] nums, String [] suits){

        String str = "";

        // random card number
        String randCardNum = "";
//        Log.i("TAG","random.nextInt() % nums.length + 1 : " + (random.nextInt( nums.length)));
        randCardNum = nums[ random.nextInt(nums.length) ];

        str += randCardNum;

        // then random card face
        String randCardSuit = "";
        randCardSuit = suits[ random.nextInt(suits.length) ];

        str += randCardSuit;

        Log.i("TAG","str : " + str);

        cardsInHand.add(str);

        return str;
    }

    private Bitmap generateCardImage(Vector<Bitmap> vec){
        int randInt = (random.nextInt(52));
        Log.i("TAG","vec size : " + vec.size());
        Log.i("TAG","randInt : " + randInt);
        return vec.get(randInt);
    }

    private void playerBusted(){
        Toast.makeText(getApplicationContext(), "text", Toast.LENGTH_SHORT);
    }

    private void calcTotal(){
        int total = 0;
        for ( int i = 0; i < cardsInHand.size(); i++){
            String str = cardsInHand.get(i);
            str = Character.toString(str.charAt(0));
            int addNum = 0;
            switch (str){
                case "1":
                case "J":
                case "Q":
                case "K":
                case "A":
                    addNum = 10;
                    break;
                default:
                    addNum = Integer.parseInt(str);
            }
            total += addNum;
        }

        ((TextView) findViewById(R.id.tv_total)).setText(total+"");

    }

    private Vector<Bitmap> makeSpriteSheet() {

        double ampFactor = 3.52;
        // image size 800*332
        int cardHeight = (int)((332/4) * ampFactor);
        int cardWidth = (int)(((800 - 800*((float)(1/14))) / 14) * ampFactor);
        int numberFramesInRows = 13;
        int totalFrames = 54;

        int xStart = 0;
        int yStart = 0;
        Vector<Bitmap> vec = new Vector<Bitmap>();

        Bitmap cardSpriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.cards_full);

        for(int i = 0; i < totalFrames; i++){
            xStart = ( i % numberFramesInRows) * cardWidth;
            yStart = ( i / numberFramesInRows) * cardHeight;
            Log.i("TAG","xStart : " + xStart + ", yStart : "+ yStart);
            Bitmap bitmap = Bitmap.createBitmap(cardWidth,cardHeight,Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            Rect src = new Rect(xStart,yStart,xStart+cardWidth,yStart+cardHeight);
            Rect dst = new Rect(0,0,cardWidth,cardHeight);
            c.drawBitmap(cardSpriteSheet,src,dst,null);
            vec.add(bitmap);
        }
        return vec;
    }
}
