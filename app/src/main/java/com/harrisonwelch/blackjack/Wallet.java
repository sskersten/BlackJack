package com.harrisonwelch.blackjack;

import java.text.DecimalFormat;

/**
 * CSCI 4020
 * Dr. Nicholson
 * William Kersten
 * 1/29/2018
 * basic class to keep track of money that the player has.
 */

public class Wallet {
    private double cash;
    private double maxCashHeld;
    private double totalEarnings;
    private double totalLost;

    private static DecimalFormat decimFormat = new DecimalFormat("0.00");

    /**
     * Converts a double to a string with only two decimal places, like money.
     * @param cash the double to convert to a money-fied string.
     * @return a string with the double having only two numbers after the decimal point.
     */
    public static String convertDoubleToCashString(double cash){
        return decimFormat.format(cash);
    }

    /**
     * Creates a com.harrisonwelch.blackjack.Wallet object with $0.
     */
    public Wallet() {
        cash = 0;
        //in future builds, possibly keep this in a file and grab it back again?
        maxCashHeld = 0;
        totalEarnings = 0;
        totalLost = 0;
    }

    /**
     * creates a com.harrisonwelch.blackjack.Wallet object with an input amount of cash.
     * @param cash how much cash to start the com.harrisonwelch.blackjack.Wallet with.
     */
    public Wallet(double cash){
        this.cash = cash;
        maxCashHeld = cash;
        totalEarnings = 0;
        totalLost = 0;
    }

    /**
     * @return the amount of cash in the wallet, preceded by a $.
     */
    @Override
    public String toString() {
        return(decimFormat.format(cash));
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash){ this.cash = cash; }

    /**
     * Adds an input amount of cash to the wallet, updating totalEarnings and maxCashHeld
     * in the process.
     * @param cash the amount of money to add to the wallet.
     */
    public void addCash(double cash) {
        this.cash += cash;
        this.totalEarnings += cash;
        if (this.cash > maxCashHeld) {
            maxCashHeld = this.cash;
        }
    }

    /**
     * removes an input amount of cash from the wallet, updating totalLost in the process.
     * @param cash the amount of money to remove from the wallet.
     */
    public void removeCash(double cash) {
        this.cash -= cash;
        this.totalLost += cash;
    }

    public double getMaxCashHeld() {
        return maxCashHeld;
    }

    public double getTotalEarnings() {
        return totalEarnings;
    }

    public double getTotalLost() {
        return totalLost;
    }

}
