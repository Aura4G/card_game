package cards;

import java.util.List;
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

    public void run() {

    }
}

