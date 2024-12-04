package cards;

import java.util.List;
import java.util.ArrayList;

public class CardDeck extends Thread{
    
    //attributes
    private int deckIndex;
    private List<Card> cards = new ArrayList<Card>();

    private static int i = 0;

    //methods
    public int getDeckIndex(){return deckIndex;}
    public List<Card> getCards(){return cards;}
    public void addCard(Card newCard) {cards.add(newCard);}

    public void loseCard(Card cardToBeDrawn) {
        cards.remove(cardToBeDrawn);
    }

    public CardDeck() {
        deckIndex = ++i;
    }

    public void run() {

    }
}

