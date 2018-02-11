package com.harrisonwelch.blackjack;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity {
    private Wallet wallet;
    private double currentBet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        wallet = new Wallet(1000);
        TextView currentMoney = findViewById(R.id.currentMoney_textview);
        currentMoney.setText(wallet.toString());

        findViewById(R.id.bet_linearLayout).setVisibility(View.VISIBLE);
        SetBetListener setBetListener = new SetBetListener();
        int[] addBetButtonIds = {R.id.betAdd5_button, R.id.betAdd10_button, R.id.betAdd25_button, R.id.betAdd50_button, R.id.betOk_button};
        for (int id : addBetButtonIds){
            findViewById(id).setOnClickListener(setBetListener);
        }


        findViewById(R.id.hit_button).setOnClickListener((View view) ->{
            doGameEnd(true);
        });

    }

    private void doGameEnd(boolean isWin){
        //give money to the player if they won
        if (isWin){
            wallet.addCash(currentBet * 2);
        }


        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        findViewById(R.id.bet_linearLayout).startAnimation(fadeIn);
        findViewById(R.id.bet_linearLayout).setVisibility(View.VISIBLE);
    }

    //Makes the +5, +10, etc buttons work.
    class SetBetListener implements View.OnClickListener{
        EditText betAmount_editText;    //EditText in-menu that shows what user wants to bet
        TextView betAmount_textView;    //TextView in-game that shows what user HAS bet
        Toast errorToast;


        SetBetListener(){
            betAmount_editText = findViewById(R.id.betAmount_editText);
            betAmount_textView = findViewById(R.id.bet_textview);
        }

        @Override
        public void onClick(View view) {
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
            if (view.getId() == R.id.betOk_button){
                setBet(betAmount);
            } else {
                addToBetAmount(view, betAmount);
            }
        }

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

            wallet.removeCash(betAmount);
            currentBet = betAmount;
            betAmount_textView.setText(Wallet.convertDoubleToCashString(betAmount));
            Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout);
            findViewById(R.id.bet_linearLayout).startAnimation(fadeOut);
            findViewById(R.id.bet_linearLayout).setVisibility(View.GONE);
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
}
