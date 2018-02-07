package com.harrisonwelch.blackjack;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Vector;

public class MainActivity extends Activity {

    private String [] cardNumbers = {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};
    private String [] cardSuits = {"Sp","Di","Ht","Cb"};
    Random random = new Random();

    int handSum = 0;
    Vector<String> cardsInHand = new Vector<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("TAG","onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_debug);

        setButtons();

    }

    private void setButtons(){
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
}
