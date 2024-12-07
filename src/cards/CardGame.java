package cards;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.ArrayList;
import java.util.List;

public class CardGame {
    private int totalPlayers;
    private List<Player> players = new ArrayList<Player>();
    private List<CardDeck> decks = new ArrayList<CardDeck>();

    public Player getPlayerFromIndex(int index) {
        for (Player player : players) {
            if (player.getPlayerIndex() == index) {
                return player;
            }
        }
        return null;
    }

    public CardDeck getDeckFromIndex(int index) {
        for (CardDeck deck : decks) {
            if (deck.getDeckIndex() == index) {
                return deck;
            }
        }
        return null;
    }

    public void initialiseGame(int n, String fileText) {
        totalPlayers = n;

        for (int i = 0; i < n; i++) {
            players.add(new Player());
            decks.add(new CardDeck());
        }

        try {
            int index = 0;
            File myObj = new File(fileText);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (index < 4*n) {
                    players.get(index % n).addCard(new Card(Integer.parseInt(data)));
                }
                else {
                    decks.get(index % n).addCard(new Card(Integer.parseInt(data)));
                }
                index++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        for (int i = 0; i < n - 1; i++) {
            players.get(i).setPickupDeck(decks.get(i));
            players.get(i).setDiscardDeck(decks.get(i+1));
        }
        players.get(n-1).setPickupDeck(decks.get(n-1));
        players.get(n-1).setDiscardDeck(decks.get(0));

        for (Player player : players) {
            player.start();
        }
    }
}
