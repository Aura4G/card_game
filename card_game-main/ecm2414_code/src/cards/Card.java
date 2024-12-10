package cards;

//Card class encapsulates card data when passed across decks and players
//Thread-safe when getting card data

 public class Card {
    
    //attributes
    private final int faceValue;

    //methods
    public int getCardValue() {return faceValue;}

    //constructor
    public Card(int faceValue) {
        
        this.faceValue = faceValue;
    }
}