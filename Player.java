package cards;

import java.util.List;
import java.util.Objects;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Thread to represent each player, they are playing concurrently and they're making the moves
 * of drawing and discarding cards, as well as checking if they match
 * @author Aria Noroozi
 */
public class Player extends Thread{

    //attributes
    private int playerIndex;
    private List<Card> cards = new ArrayList<Card>();

    /**this acts as a pointer to the deck the player discards to in a ring topology.
     * it is a pointer, because another player will point to the same deck, but as its pickupDeck
     */
    private CardDeck discardDeck; //discard deck has its index +1 higher than its player index, unless playerIndex == number of players
    /**this acts as a pointer to the deck the player draws from in a ring topology.
     * it is a pointer, because another player will point to the same deck, but as its discardDeck
     */
    private CardDeck pickupDeck; //discard deck has its index equal to its player index

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

    /**
     * works by finding a card in the player's hand matching the face value parameter
     * adding the card to the deck the player would discard to
     * and removing the card from the player's hand (card list)
     * @author Aria Noroozi
     * @param faceValue
     */
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

    /**
     * works by iterating through the card list of the deck the player draws from and finds the first card with
     * the specified face value the player wishes to draw, then adds the card to the player's card list and 
     * removes the card from pickupDeck using the deck's loseCard function
     * @author Aria Noroozi
     * @param faceValue
     */
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

    //methods acting as checks whether to proceed with discarding or the next round
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

    /**
     * resets these boolean variables, so each round player's can wait() for all other players before they proceed
     * with the next round or discarding
     */
    public void resetForNextRound() {
        readyForNextRound = false;
        readyToDiscard = false;
    }

    public Player() {
        playerIndex = ++i;
    }

    /**
     * logic for if all the cards in the player's hand match
     *
     */
    public boolean cardsMatch() {
        int[] array = getCardValues();

        for (int i = 1; i < array.length; i++) {
            if (!Objects.equals(array[0], array[i])) { //compares each element of the array to the first
                return false;
            }
        }
        return true;
    }

    /**
     * returns an integer array of the card values in the player's hand
     * @return cardArray
     */
    public int[] getCardValues() {
        int[] cardArray = new int[4]; //using fixed int size as the player should have exactly 4 cards

        //iterates through and adds cards to the array via their indexes and getCardValue()
        for (int a = 0; a < 4; a++) {
            cardArray[a] = cards.get(a).getCardValue();
        }

        return cardArray;
    }

    /**
     * writes the initial card list the player has as well as creates the file
     */
    public synchronized void initialContents() {

        //the output string detailing player and hand
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

    /**
     * called every round to add the output file created in initialContents()
     * outputs the draw and discard transactions, as well as the hand after that round
     */
    public synchronized void updateContents() {
        String output = "player" + playerIndex + " current hand : ";
        for (Card card : cards) {
            output = output + card.getCardValue() + " ";
        }

        try (FileWriter writer = new FileWriter("player" + playerIndex + "_output.txt", true)) {
            writer.write("\nRound " + x + ": Player " + playerIndex + " takes " + cards.get(cards.size() - 1).getCardValue() + " from deck " + pickupDeck.getDeckIndex());
            writer.write("\nRound " + x + ": Player " + playerIndex + " discards " + discardDeck.getCards().get(discardDeck.getCards().size() - 1).getCardValue() + " to deck " + discardDeck.getDeckIndex());
            writer.write("\n" + output);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //run
    public void run(){
        while (isRunning) {
            try {
                synchronized (pickupDeck) {
                    // Check if `pickupDeck` has 4 elements and take one
                    while (pickupDeck.getCards().size() != 4) {
                        pickupDeck.wait(); // Wait until `pickupDeck` has 4 elements
                    }
                    
                    drawCard(pickupDeck.getCards().get(0).getCardValue());
                    
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
