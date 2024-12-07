package cards;

import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

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

    public synchronized void loseCard(Card cardToBeDrawn) {
        cards.remove(cardToBeDrawn);
        notifyAll();
    }

    public CardDeck() {
        deckIndex = ++i;
    }

    public synchronized void lastDeckContents() {

        String output = "deck" + deckIndex + " contents : ";
        for (Card card : cards) {
            output = output + card.getCardValue() + " ";
        }

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

    public void run() {

    }
}

