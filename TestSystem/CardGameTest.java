import cards.CardGame;

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
        testGame.initialiseGame(4, "four_test.txt");

        //test end
        System.out.println("Test end.");
    }
}
