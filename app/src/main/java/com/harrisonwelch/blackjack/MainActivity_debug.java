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
    private String [] pointValues = {"2","3","4","5","6","7","8","9","10","10","10","10","11"};
    private String [] cardSuits = {"Sp","Di","Ht","Cb"};
    private int [] dealerCardSlotIds = {R.id.iv_table1,R.id.iv_table2,R.id.iv_table3,R.id.iv_table4,R.id.iv_table5,R.id.iv_table6,R.id.iv_table7,R.id.iv_table8,R.id.iv_table9,R.id.iv_table10};
    private int [] playerCardSlotIds = {R.id.iv_card1,R.id.iv_card2,R.id.iv_card3,R.id.iv_card4,R.id.iv_card5,R.id.iv_card6,R.id.iv_card7,R.id.iv_card8,R.id.iv_card9,R.id.iv_card10};
    Random random = new Random();

    int playerScore = 0;
    int playerCards = 0;
    int dealerHiddenScore = 0;
    int dealerVisibleScore = 0;
    int dealerCards = 0;
    boolean playerHasAce = false;
    boolean dealerHasAce = false;
    Vector<String> cardsInHand = new Vector<>();
    Vector<Bitmap> cardBitmaps = new Vector<>();
    Bitmap dealersSecretCard;

    private boolean gameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("TAG","onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_debug);

        setButtons();

        cardBitmaps = makeSpriteSheet();

        resetGame();
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
                giveCard("player");
            }
        });
        b = findViewById(R.id.btn_stand);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 standPlayer();
            }
        });
        b = findViewById(R.id.btn_reset);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });
    }

    // gives the player a card
    private void giveCard(String person){

        Bitmap randCard = generateCard(cardBitmaps, person);

        if(person.equals("dealer")){
            if(dealerCards == 0){
                ( (ImageView) findViewById(R.id.iv_table1)).setImageResource(R.drawable.card_back);
                dealersSecretCard = randCard;
                dealerCards++;

            } else if (dealerCards < dealerCardSlotIds.length) {
                ( (ImageView) findViewById(dealerCardSlotIds[dealerCards])).setImageBitmap(randCard);
                dealerCards++;
            }

        } else if(person.equals("player")){
            if(playerCards < playerCardSlotIds.length){
                ( (ImageView) findViewById(playerCardSlotIds[playerCards])).setImageBitmap(randCard);
                playerCards++;
            }
        }
        Log.i("TAG","playerScore: " + playerScore + ", dealerHiddenScore: " + dealerHiddenScore);
    }

    private void standPlayer(){
        // loop through the dealer drawing cards
        // game ends now
        while(dealerHiddenScore < 17){
            giveCard("dealer");
        }
        revealDealerCard();
        testWin();
        gameOver = true;
    }

    private Bitmap generateCard(Vector<Bitmap> vec,String person){
        int randInt = (random.nextInt(52));
        Log.i("TAG","vec size : " + vec.size());
        Log.i("TAG","randInt : " + randInt);

        updateScore(randInt, person);


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

//        ((TextView) findViewById(R.id.tv_total)).setText(total+"");

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

    private void initTable(){
        // initialize the table to give the dealer 1 face down card and the player 2 face up cards

        ((TextView) findViewById(R.id.tv_dealerScore)).setText("0");
        ((TextView) findViewById(R.id.tv_playerScore)).setText("0");

        // set the the dealer handslot 1 to card back, and slot 2 to rand card
        giveCard("dealer");
        giveCard("dealer");

        // set player hand slot 1 and 2 to rand card
        giveCard("player");
        giveCard("player");

    }
    private void updateScore(int i, String person){

        int cardValue = Integer.parseInt(pointValues[i%pointValues.length]);

        if(cardValue == 11){
            if(person.equals("player")){
                playerHasAce = true;
            } else if (person.equals("dealer")) {
                dealerHasAce = true;
            }
        }

        if(person.equals("dealer")){
            // update the hidden Dealer score

            dealerHiddenScore += cardValue;
            if(dealerCards > 0){
                dealerVisibleScore += cardValue;
            }

            //update the UI to reflect the observable dealer score(not including the first card.
            ((TextView) findViewById(R.id.tv_dealerScore)).setText(dealerVisibleScore +"");

        } else if(person.equals("player")) {
            //update the handSum and update UI to reflect
            playerScore += cardValue;
            ((TextView) findViewById(R.id.tv_playerScore)).setText(playerScore+"");
        }

        if(playerHasAce && isBusted("player")) playerScore -= 10;
        if(dealerHasAce && isBusted("dealer")){
            dealerVisibleScore -= 10;
            dealerHiddenScore -= 10;
        }

        testBust();

    }
    private boolean isBusted(String person){
        if(person.equals("player")) return (playerScore > 21);
        if(person.equals("dealer")) return (playerScore > 21);
        return false;
    }
    private void testBust(){
        if (playerScore > 21){
            Log.i("MainAct","busted");
            gameOver = true;
            Toast.makeText(getApplicationContext(),"Player 1 busted",Toast.LENGTH_LONG).show();
        }
        if (dealerHiddenScore > 21){
            Log.i("MainAct","busted");
            gameOver = true;
            Toast.makeText(getApplicationContext(),"The Dealer busted",Toast.LENGTH_LONG).show();
        }

    }
    private void resetGame(){
        gameOver = true;

        double ampFactor = 3.52;
        int cardHeight = (int)((332/4) * ampFactor);
        int cardWidth = (int)(((800 - 800*((float)(1/14))) / 14) * ampFactor);
        // clear all slots
        for(int i = 0; i < dealerCardSlotIds.length; i++){
            // set to blank bitmaps
            ( ( ImageView ) findViewById(dealerCardSlotIds[i])).setImageBitmap(Bitmap.createBitmap(cardWidth,cardHeight,Bitmap.Config.ARGB_8888));
        }
        for(int i = 0; i < playerCardSlotIds.length; i++){
            ( ( ImageView ) findViewById(playerCardSlotIds[i])).setImageBitmap(Bitmap.createBitmap(cardWidth,cardHeight,Bitmap.Config.ARGB_8888));
            //
        }

        playerScore = 0;
        playerCards = 0;
        dealerHiddenScore = 0;
        dealerVisibleScore = 0;
        dealerCards = 0;
        // init board
        initTable();
    }

    private void testWin(){
        if(dealerHiddenScore > 21 || playerScore > dealerHiddenScore) {
            personWin("player");
            return;
        }
        if(playerScore > 21 || playerScore < dealerHiddenScore){
            personWin("dealer");
            return;
        }
    }

    private void personWin(String person){
        Toast.makeText(getApplicationContext(),person + " won!!!",Toast.LENGTH_SHORT).show();

    }

    private void revealDealerCard(){
        // show the secret card
        ((ImageView) findViewById(R.id.iv_table1)).setImageBitmap(dealersSecretCard);

        // replace the visible with hidden score
        ((TextView) findViewById(R.id.tv_dealerScore)).setText(dealerHiddenScore +"");

    }

}
