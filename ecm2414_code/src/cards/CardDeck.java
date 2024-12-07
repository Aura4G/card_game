package cards;

import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Class to create a Deck object to store cards not in the hands of Players
 * Thread safe with synchronised methods to allow for consistency with the players that draw from it
 * @author Aria Noroozi
 */
public class CardDeck{
    
    //attributes
    private int deckIndex;
    private List<Card> cards = Collections.synchronizedList(new ArrayList<Card>());
    private static int i = 0;

    //methods
    public int getDeckIndex(){return deckIndex;}
    public synchronized List<Card> getCards(){return cards;}
    public synchronized void addCard(Card newCard) {
        cards.add(newCard);
        notifyAll();
    }

    /**
     * Takes a card from the list of cards the deck has.
     * This method is called in "Player"'s drawCard method, as the card to be drawn
     * is added to the player's card list
     * @author Aria Noroozi
     * @param cardToBeDrawn
     */
    public synchronized void loseCard(Card cardToBeDrawn) {
        cards.remove(cardToBeDrawn);
        notifyAll(); //notifies the pick up of a card, so players can start discarding when all are ready
    }

    public CardDeck() {
        deckIndex = ++i;
    }

    /**
     * Outputs the card list of the deck to a text file
     * @author Aria Noroozi
     */
    public synchronized void lastDeckContents() {

        //output string initialised, detailing the deck and the cards it has
        String output = "deck" + deckIndex + " contents : ";
        for (Card card : cards) {
            output = output + card.getCardValue() + " ";
        }

        //creates and puts the output onto a text file unique to the deck
        try {
            File myObj = new File("deck" + deckIndex + "_output.txt");
            myObj.createNewFile();
            FileWriter writer = new FileWriter(myObj);
            writer.write(output);
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

