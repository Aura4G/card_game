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
    public List<Card> getPlayerCards() {return cards;}
    public CardDeck getDiscardDeck() {return discardDeck;}
    public CardDeck getPickupDeck() {return pickupDeck;}
    public void addCard(Card newCard) {cards.add(newCard);}
    public void setDiscardDeck(CardDeck deck) {discardDeck = deck;}
    public void setPickupDeck(CardDeck deck) {pickupDeck = deck;}

    public void discard(int faceValue) {
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

    public void drawCard(int faceValue) {
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
        for (int x = 0; x < 5; x++){
            System.out.println(playerIndex);
            try{
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(playerIndex + " was interrupted.");
            }
        }
    }
}
