package cards;

/**
 * Card class created to safely encapsulate card data when passed across decks and players
 * @author Aria Noroozi
*/
public class Card extends Thread{
    
    //attributes
    private int faceValue;

    //methods
    public int getCardValue() {return faceValue;}
    public void setCardValue(int newValue) {faceValue = newValue;}

    //constructor
    public Card(int faceValue){
        this.faceValue = faceValue;
    }
}