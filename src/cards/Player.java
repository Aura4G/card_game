package cards;

import java.util.List;
import java.util.ArrayList;

public class Player extends Thread{

    //attributes
    private int playerIndex;
    private List<Card> cards = new ArrayList<Card>();
    private CardDeck discardDeck;
    private CardDeck pickupDeck;

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

    public Player() {
        playerIndex = ++i;
    }

    //run
    public void run(){
        System.out.println("Player " + playerIndex + " present! starting with " + cards.size() + " cards");
        int x = 1;

        try {
            while (true) {

                synchronized (pickupDeck) {
                    // Check if `pickupDeck` has 4 elements and take one
                    while (pickupDeck.getCards().size() < 4) {
                        pickupDeck.wait(); // Wait until `pickupDeck` has 4 elements
                    }
                    
                    drawCard(pickupDeck.getCards().get(0).getCardValue());
                    System.out.println("Round " + x + ": Player " + playerIndex + " takes " + cards.get(cards.size() - 1).getCardValue() + " from deck " + pickupDeck.getDeckIndex());
                    pickupDeck.notifyAll();
                }

                synchronized (this) {
                    // Check if `cards` has 5 elements and remove one
                    while (cards.size() < 5) {
                        this.wait(); // Wait until `cards` has 5 elements
                    }
                    discard(cards.get(0).getCardValue());
                    System.out.println("Round " + x + ": Player " + playerIndex + " discards " + discardDeck.getCards().get(discardDeck.getCards().size() - 1).getCardValue() + " to deck " + discardDeck.getDeckIndex());
                    this.notifyAll(); // Notify other threads
                }

                x++;
                sleep(200);
                
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " was interrupted.");
        }

        System.out.println("Player " + playerIndex);
        for (Card card : cards) {
            System.out.println(card.getCardValue());
        }
    }
}
