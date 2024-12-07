package cards;

import java.util.List;
import java.util.Objects;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Player extends Thread{

    //attributes
    private int playerIndex;
    private List<Card> cards = new ArrayList<Card>();
    private CardDeck discardDeck;
    private CardDeck pickupDeck;
    private volatile boolean isRunning = true;
    private volatile boolean readyForNextRound = false;
    private volatile boolean readyToDiscard = false;
    private int x = 1;
    private static int i = 0;

    //methods
    public int getPlayerIndex() {return playerIndex;}
    public synchronized List<Card> getPlayerCards() {return cards;}
    public synchronized CardDeck getDiscardDeck() {return discardDeck;}
    public synchronized CardDeck getPickupDeck() {return pickupDeck;}
    public synchronized void addCard(Card newCard) {cards.add(newCard);}
    public synchronized void setDiscardDeck(CardDeck deck) {discardDeck = deck;}
    public synchronized void setPickupDeck(CardDeck deck) {pickupDeck = deck;}

    public synchronized void discard(int faceValue) {
        Card transferredCard = null;

        for (Card card : cards) {
            if (card.getCardValue() == faceValue) {
                transferredCard = card;
                break;
            }
        }
        discardDeck.addCard(transferredCard);
        cards.remove(transferredCard);
    }

    public synchronized void drawCard(int faceValue) {
        Card transferredCard = null;

        for (Card card : pickupDeck.getCards()) {
            if (card.getCardValue() == faceValue) {
                transferredCard = card;
                break;
            }
        }

        cards.add(transferredCard);
        pickupDeck.loseCard(transferredCard);
    }

    public void ready() {
        isRunning = true;
    }

    public void stopRunning() {
        isRunning = false;
    }

    public boolean isReadyForNextRound() {
        return readyForNextRound;
    }

    public boolean isReadyToDiscard() {
        return readyToDiscard;
    }

    public void resetForNextRound() {
        readyForNextRound = false;
        readyToDiscard = false;
    }

    public Player() {
        playerIndex = ++i;
    }

    public boolean cardsMatch() {
        int[] array = getCardValues();

        for (int i = 1; i < array.length; i++) {
            if (!Objects.equals(array[0], array[i])) {
                return false;
            }
        }
        return true;
    }

    public int[] getCardValues() {
        int[] cardArray = new int[4];

        for (int a = 0; a < 4; a++) {
            cardArray[a] = cards.get(a).getCardValue();
        }

        return cardArray;
    }

    public synchronized void initialContents() {

        String output = "player" + playerIndex + " initial hand : ";
        for (Card card : cards) {
            output = output + card.getCardValue() + " ";
        }

        try {
            File myObj = new File("player" + playerIndex + "_output.txt");
            myObj.createNewFile();
            FileWriter writer = new FileWriter(myObj);
            writer.write(output);
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public synchronized void updateContents() {
        String output = "player" + playerIndex + " current hand : ";
        for (Card card : cards) {
            output = output + card.getCardValue() + " ";
        }

        try (FileWriter writer = new FileWriter("player" + playerIndex + "_output.txt", true)) {
            writer.write("Round " + x + ": Player " + playerIndex + " takes " + cards.get(cards.size() - 1).getCardValue() + " from deck " + pickupDeck.getDeckIndex());
            writer.write("Round " + x + ": Player " + playerIndex + " discards " + discardDeck.getCards().get(discardDeck.getCards().size() - 1).getCardValue() + " to deck " + discardDeck.getDeckIndex());
            writer.write(output);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //run
    public void run(){
        System.out.println("Player " + playerIndex + " present! starting with " + cards.size() + " cards");

        while (isRunning) {
            try {
                synchronized (pickupDeck) {
                    // Check if `pickupDeck` has 4 elements and take one
                    while (pickupDeck.getCards().size() != 4) {
                        pickupDeck.wait(); // Wait until `pickupDeck` has 4 elements
                    }
                    
                    drawCard(pickupDeck.getCards().get(0).getCardValue());
                    System.out.println("Round " + x + ": Player " + playerIndex + " takes " + cards.get(cards.size() - 1).getCardValue() + " from deck " + pickupDeck.getDeckIndex());
                    pickupDeck.notifyAll();
                }

                readyToDiscard = true;
                sleep(25);

                synchronized (this) {
                    // Check if `cards` has 5 elements and remove one
                    while (cards.size() < 5 && discardDeck.getCards().size() != 3) {
                        this.wait(); // Wait until `cards` has 5 elements
                    }
                    discard(cards.get(0).getCardValue());
                    System.out.println("Round " + x + ": Player " + playerIndex + " discards " + discardDeck.getCards().get(discardDeck.getCards().size() - 1).getCardValue() + " to deck " + discardDeck.getDeckIndex());
                    this.notifyAll(); // Notify other threads
                }

                readyForNextRound = true;
                updateContents();
                sleep(25);
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " was interrupted.");
            }

            x++;
            
        }
    }
}
