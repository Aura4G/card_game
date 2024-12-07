package cards;

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

    //run
}