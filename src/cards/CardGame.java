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
    private boolean isFinished = false;
    private int rounds = 1;

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
            player.initialContents();
            player.start();
        }

        try {
            gameLoop();
        } catch (InterruptedException e) {}
    }

    public void gameLoop() throws InterruptedException {
        while (!isFinished) {
            System.out.println("Round: " + rounds);

            // Wait for all players to finish the current round
            for (Player player : players) {
                while (!player.isReadyForNextRound() || !player.isReadyToDiscard()) {
                    Thread.sleep(10); // Polling until the player finishes
                }
            }

            // Reset players for the next round
            for (Player player : players) {
                player.resetForNextRound();
                if (player.cardsMatch()) {
                    isFinished = true;
                    System.out.println("Game over! Player " + player.getPlayerIndex() + " wins!!");
                }
            }

            rounds++;

            if (rounds > 255) {
                System.out.println("Time Out!!");
                for (Player player : players) {
                    player.stopRunning(); // Stop all threads
                }
                isFinished = true;
            }
        }


        for (Player player : players) {
            player.stopRunning(); // Stop all threads
        }

        for (Player player : players) {
            player.join();
        }

        for (CardDeck deck : decks) {
            deck.lastDeckContents();
        }
    }
}
