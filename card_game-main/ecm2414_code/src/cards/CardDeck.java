package cards;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//CardDeck class encapsulates deck data (not in players hands)
//Thread-safe with synchronized methods when getting deck data and manipulating decks

public class CardDeck {
    
    //attributes
    private final int deckIndex;
    private final List<Card> cards = Collections.synchronizedList(new ArrayList<>());
    private static int i = 0;

    //methods
    public synchronized int getDeckIndex() {return deckIndex;}
    public synchronized List<Card> getCards() {return cards;}

    //method to add a card to the decks card list
    public synchronized void addCard(Card newCard) {

        cards.add(newCard);
        notifyAll(); //notifies player threads that the discard decks have received a card, so players can begin the next round and start drawing when all are ready
    }

   //method to remove a card from the decks card list
    public synchronized void loseCard(Card drawnCard) {

        cards.remove(drawnCard);
        notifyAll(); //notifies player threads that the pickup decks have lost a card, so players can start discarding when all are ready
    }

    //constructor 
    public CardDeck() {

        deckIndex = ++i; //assigns a unique index to each deck 
    }

    //method to output the card list of the deck to a text file
    public void lastDeckContents() {

        //locks the current CardDeck instance
        synchronized (this) {

            String output = "deck" + deckIndex + " contents : "; //initialises the output string unique to the deck
            for (Card card : cards) {
                
                output += card.getCardValue() + " "; //adds the cards in the deck to the output string
            }

            //tries to create a new text file unique to the deck
            try {

                File myObj = new File("deck" + deckIndex + "_output.txt"); 
                myObj.createNewFile();
                try (FileWriter writer = new FileWriter(myObj)){

                    writer.write(output); //writes the output onto the text file 
                }

            //error message outputted when input/output exception
            } catch (IOException e) {

                System.err.println("An error occurred: " + e.getMessage());
            }
        }   
    }
}

