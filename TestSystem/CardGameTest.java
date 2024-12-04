import cards.CardGame;
import cards.Card;

public class CardGameTest {
    /**
	 * Test method.
	 * 
	 * @param args not used
	 */

    public static void main(String[] args) {
        System.out.println("The system compiled and started the execution...");

        //creates instance of a card game
        CardGame testGame = new CardGame();

        //initialises the game with 4 players
        testGame.initialiseGame(4, "four.txt");

        //draw and discard
        testGame.getPlayerFromIndex(4).drawCard(14); //adds 14
        testGame.getPlayerFromIndex(4).discard(8); //removes 8

        for (Card card : testGame.getPlayerFromIndex(4).getPlayerCards()) {
            System.out.println(card.getCardValue()); //2, 7, 4, 14
        }
        for (Card card : testGame.getDeckFromIndex(4).getCards()) {
            System.out.println(card.getCardValue()); //5, 1, 9
        }
        for (Card card : testGame.getDeckFromIndex(1).getCards()) {
            System.out.println(card.getCardValue()); //12, 14, 1, 12, 8
        }
        

        //test end
        System.out.println("Test end.");
    }
}
