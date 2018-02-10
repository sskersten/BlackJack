package com.harrisonwelch.blackjack;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class GameActivity extends Activity {
    private Wallet wallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        wallet = new Wallet(1000);

        AddMoneyListener addMoneyListener = new AddMoneyListener();
        int[] addBetButtonIds = {R.id.betAdd5_button, R.id.betAdd10_button, R.id.betAdd25_button, R.id.betAdd50_button};
        for (int id : addBetButtonIds){
            findViewById(id).setOnClickListener(addMoneyListener);
        }

        
    }

    //Makes the +5, +10, etc buttons work.
    class AddMoneyListener implements View.OnClickListener{
        EditText betAmount_editText;

        AddMoneyListener(){
            betAmount_editText = findViewById(R.id.betAmount_editText);
        }

        @Override
        public void onClick(View view) {
            double betAmount;   //value being bet
            String betText = betAmount_editText.getText().toString();
            if (betText.equals("")) {
                betAmount = 0;
            } else {
               betAmount = Double.parseDouble(betAmount_editText.getText().toString());
            }

            //check that we're not at max money
            if (betAmount >= wallet.getCash()){
                Toast.makeText(getApplicationContext(), R.string.overMoneyHeldError, Toast.LENGTH_SHORT).show();
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

            betAmount_editText.setText(Double.toString(betAmount));
        }
    } //end of AddMoneyListener
}
