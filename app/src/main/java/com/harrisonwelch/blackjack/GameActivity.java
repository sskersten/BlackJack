package com.harrisonwelch.blackjack;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

public class GameActivity extends Activity {
    private static final String BLACK_JACK_FILE = "blackJack.json";
    private static final String TAG_GAME_ACTIVITY = "GAME_ACT";
    private static final String JSON_PLAYER_CARDS = "player_cards";
    private static final String JSON_DEALER_CARDS = "dealer_cards";

    private JSONObject fileJSON = new JSONObject();
    private Wallet wallet;
    private double currentBet;

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
    boolean isPlayerAceSubracted = false;
    boolean isDealerAceSubracted = false;
    Vector<String> cardsInHand = new Vector<>();
    Vector<Bitmap> cardBitmaps = new Vector<>();
    Bitmap dealersSecretCard;

    Vector<Integer> cardIndexesPlayerHand = new Vector<>();
    Vector<Integer> cardIndexesInDealerHand = new Vector<>();

    private boolean gameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //setup money
        wallet = new Wallet(1000);
        TextView currentMoney = findViewById(R.id.currentMoney_textview);
        currentMoney.setText(wallet.toString());

        initBetStuff();
        setButtons();

        cardBitmaps = makeSpriteSheet();

        resetGame();

    }

    @Override
    protected void onResume() {
        Log.i(TAG_GAME_ACTIVITY,"onResume()");
        readFile();
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG_GAME_ACTIVITY,"onPause()");
        writeFile();
        super.onPause();
    }

    private void doGameEnd(boolean isWin){
        //give money to the player if they won
        if (isWin){
            wallet.addCash(currentBet);
        } else {
            wallet.removeCash(currentBet);
        }
        //disable hit, stand buttons. enable bet, new match buttons.
        toggleButtons();
    }

    //reverses current state of all buttons.
    private void toggleButtons(){
        boolean enabled = findViewById(R.id.btn_hit).isEnabled();
        findViewById(R.id.btn_hit).setEnabled(!enabled);
        findViewById(R.id.btn_stand).setEnabled(!enabled);
        findViewById(R.id.updateBet_button).setEnabled(enabled);
        findViewById(R.id.btn_reset).setEnabled(enabled);
    }

    //Setting up starting bet, the bet textviews, and bet buttons.
    public void initBetStuff(){
        currentBet = 5;
        TextView currentBetValue = findViewById(R.id.bet_textview);
        currentBetValue.setText(Wallet.convertDoubleToCashString(currentBet));
        findViewById(R.id.bet_linearLayout).setVisibility(View.GONE);
        findViewById(R.id.betBackground_view).setVisibility(View.GONE);
        SetBetListener setBetListener = new SetBetListener();
        int[] addBetButtonIds = {R.id.betAdd5_button, R.id.betAdd10_button, R.id.betAdd25_button, R.id.betAdd50_button, R.id.betOk_button, R.id.updateBet_button};
        for (int id : addBetButtonIds){
            findViewById(id).setOnClickListener(setBetListener);
        }
    }

    //Makes the +5, +10, etc buttons work.
    class SetBetListener implements View.OnClickListener{
        private EditText betAmount_editText;    //EditText in-menu that shows what user wants to bet
        private TextView betAmount_textView;    //TextView in-game that shows what user HAS bet
        private Toast errorToast;
        private boolean betMenuShown;

        SetBetListener(){
            betAmount_editText = findViewById(R.id.betAmount_editText);
            betAmount_textView = findViewById(R.id.bet_textview);
            betMenuShown = false;
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.updateBet_button){
                toggleBettingMenu();
                return;
            } else {

                //parse amount they're currently betting
                double betAmount;   //value being bet
                String betText = betAmount_editText.getText().toString();
                if (betText.equals("")) {
                    betAmount = 0;
                } else {
                    betAmount = Double.parseDouble(betAmount_editText.getText().toString());
                }

                //if user pressed ok, set up bet amount. Otherwise, they pressed add button, so add to
                // the current bet amount.
                if (view.getId() == R.id.betOk_button) {
                    setBet(betAmount);
                } else {
                    addToBetAmount(view, betAmount);
                }
            }
        }

        //if betting menu is displayed, remove it. if it isn't displayed, put it up there.
        private void toggleBettingMenu(){
            LinearLayout betMenu = findViewById(R.id.bet_linearLayout);
            if (betMenuShown) {
                Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout);
                betMenu.startAnimation(fadeOut);
                betMenu.setVisibility(View.GONE);
                View background = findViewById(R.id.betBackground_view);
                background.startAnimation(fadeOut);
                background.setVisibility(View.GONE);
                betMenuShown = false;
            } else {

                Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
                betMenu.startAnimation(fadeIn);
                betMenu.setVisibility(View.VISIBLE);
                View background = findViewById(R.id.betBackground_view);
                background.startAnimation(fadeIn);
                background.setVisibility(View.VISIBLE);
                TextView currentMoney = findViewById(R.id.currentMoney_textview);
                currentMoney.setText(wallet.toString());
                betMenuShown = true;
            }
        }

        //change the currently active bet to a specified amount and remove the Bet menu.
        private void setBet(double betAmount){
            //set cash to max they have if the amount input is over what they currently have
            if (betAmount > wallet.getCash()){
                betAmount_editText.setText(wallet.toString());
                showToast(R.string.overMoneyHeldError);
                return;
            }

            if (betAmount <= 0){
                showToast(R.string.notEnoughMoneyBetError);
                return;
            }

            currentBet = betAmount;
            betAmount_textView.setText(Wallet.convertDoubleToCashString(betAmount));
            toggleBettingMenu();
        }

        //called when any of the Add buttons is pressed. Adds to editText of current bet amount.
        private void addToBetAmount(View view, double betAmount){
            //check that we're not at max money
            if (betAmount >= wallet.getCash()){
                showToast(R.string.overMoneyHeldError);
                return;
            }

            //add button selected's money to value
            switch(view.getId()){
                case R.id.betAdd5_button:   betAmount += 5; break;
                case R.id.betAdd10_button:  betAmount += 10; break;
                case R.id.betAdd25_button:  betAmount += 25; break;
                case R.id.betAdd50_button:  betAmount += 50; break;
                default: throw new RuntimeException("Unexpected add to bet total button pressed.");
            }

            //if adding that made us go over total in our wallet, just go all in.
            if (betAmount > wallet.getCash()){
                betAmount = wallet.getCash();
            }

            betAmount_editText.setText(Wallet.convertDoubleToCashString(betAmount));
        }

        //if a toast is being displayed, cancels that one and displays whatever the new one is.
        private void showToast(int messageId){
            if (errorToast != null){
                errorToast.cancel();
            }
            errorToast = Toast.makeText(getApplicationContext(), messageId, Toast.LENGTH_SHORT);
            errorToast.show();
        }
    } //end of SetBetListener

    private void setButtons(){
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

    private Bitmap generateCard(Vector<Bitmap> vec, String person){
        int randInt = (random.nextInt(52));
        Log.i("TAG","vec size : " + vec.size());
        Log.i("TAG","randInt : " + randInt);

        // update the file R/W stuff
        if(person.equals("player")){
            cardIndexesPlayerHand.add(randInt);
        } else if(person.equals("dealer")){
            cardIndexesInDealerHand.add(randInt);
        }

        updateScore(randInt, person);


        return vec.get(randInt);
    }

    private void playerBusted(){
        Toast.makeText(getApplicationContext(), "text", Toast.LENGTH_SHORT);
        standPlayer();
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

        AssetManager assetManager = getApplicationContext().getAssets();
        String filePath = "../../../res/assets/cards_full.png";
        InputStream inputStream;
        Bitmap bitmapStream = null;
        try {
            inputStream = assetManager.open(filePath);
            Log.i("makeSpriteSheet","(YES) found the file " + filePath);
        } catch (Exception e){
            Log.i("makeSpriteSheet","(NO) Could not find file " + filePath);
        }


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

        if( isPlayerAceSubracted || isDealerAceSubracted ){
            cardValue = 1;
        }else if(cardValue == 11){
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

        if(playerHasAce && isBusted("player") && !isPlayerAceSubracted) {
            isPlayerAceSubracted = true;
            playerScore -= 10;
        }
        if(dealerHasAce && isBusted("dealer") && !isDealerAceSubracted){
            isPlayerAceSubracted = true;
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
        cardIndexesPlayerHand.clear();
        cardIndexesInDealerHand.clear();
        toggleButtons();    //re-enable hit, stand, double buttons, disable bet and start new match buttons
        // init board
        initTable();
    }

    private void testWin(){
        if(dealerHiddenScore > 21 || playerScore > dealerHiddenScore) {
            personWin("player");
            doGameEnd(true); //player won
            return;
        }
        if(playerScore > 21 || playerScore < dealerHiddenScore){
            personWin("dealer");
            doGameEnd(false); //player lost
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

    private void writeFile(){
        Log.i(TAG_GAME_ACTIVITY,"writeFile");
        try {
            FileOutputStream fos = openFileOutput(BLACK_JACK_FILE, Context.MODE_PRIVATE);
            OutputStreamWriter opsw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(opsw);
            PrintWriter pw = new PrintWriter(bw);

            try{
                fileJSON.put(JSON_PLAYER_CARDS, cardIndexesPlayerHand);
                fileJSON.put(JSON_DEALER_CARDS, cardIndexesInDealerHand);
                Log.i(TAG_GAME_ACTIVITY,"fileJSON write: " + fileJSON.toString());
            } catch (JSONException e1) {
                Log.i(TAG_GAME_ACTIVITY,"error building JSON");
            }

            pw.println(fileJSON.toString());
            pw.close();
            Log.i(TAG_GAME_ACTIVITY, "fileJSON done printing the file");


        } catch (FileNotFoundException e){
            Log.i("GameAct","BLACK_JACK_FILE found found.");
        }
    }

    private void readFile(){
        Log.i(TAG_GAME_ACTIVITY,"readFile");
        String str = "";

        try {
            FileInputStream fis = openFileInput(BLACK_JACK_FILE);
            Scanner scanner = new Scanner(fis);
            while(scanner.hasNext()){
                str += scanner.nextLine();
                Log.i(TAG_GAME_ACTIVITY,"fileJSON read : '" + str +"'");
            }

            fileJSON = new JSONObject(str);

            Object arr = fileJSON.get(JSON_PLAYER_CARDS);
            Object arr2 = fileJSON.get(JSON_DEALER_CARDS);

            Vector<Integer> vecPlayerCards = stringToVectorInt((String) fileJSON.get(JSON_PLAYER_CARDS));
            Vector<Integer> vecDealerCards = stringToVectorInt((String) fileJSON.get(JSON_DEALER_CARDS));

            Log.i(TAG_GAME_ACTIVITY,"arr : " + arr);
            Log.i(TAG_GAME_ACTIVITY,"arr2 : " + arr2);

            for(int i = 0; i < vecPlayerCards.size() && i < vecDealerCards.size(); i++){
                Log.i(TAG_GAME_ACTIVITY, "vecPlayerCards.get(i) : " + vecPlayerCards.get(i));
                Log.i(TAG_GAME_ACTIVITY, "vecDealerCards.get(i) : " + vecDealerCards.get(i));
            }

            if (vecPlayerCards.size() > 0) fillTableFromIds(vecPlayerCards, "person");
            if (vecDealerCards.size() > 0) fillTableFromIds(vecDealerCards, "dealer");

            Log.i(TAG_GAME_ACTIVITY, "fileJSON (read in coverted) : " + fileJSON);

            Log.i(TAG_GAME_ACTIVITY,"ge" );
        } catch (FileNotFoundException e) {
            Log.i(TAG_GAME_ACTIVITY,"error readFile");
            e.printStackTrace();
        } catch (JSONException je){
            Log.i(TAG_GAME_ACTIVITY,"error JSON conversion");
        }

    }

    private Vector<Integer> stringToVectorInt(String input){
        Vector<Integer> output = new Vector<>();

        String[] strings = input.split("\\[|, |\\]");

        // don't do the first one
        for(int i = 1; i < strings.length; i++){
            output.add(Integer.parseInt(strings[i]));
        }

        return output;
    }

    private void fillTableFromIds(Vector<Integer> cardIndexes, String person){
        Log.i(TAG_GAME_ACTIVITY, "cardIndexes : " + cardIndexes + ", person : " + person);
        for(int i = 0; i < cardIndexes.size(); i++){
            if(person.equals("player")) {
                ((ImageView) findViewById(playerCardSlotIds[i])).setImageBitmap(cardBitmaps.get(i));
            } else if (person.equals("dealer")) {
                ((ImageView) findViewById(dealerCardSlotIds[i])).setImageBitmap(cardBitmaps.get(i));
            }
        }
    }

}
