package cards;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.ArrayList;
import java.util.List;

public class CardGame {
    private int totalPlayers;
    private List<Player> players = new ArrayList<Player>(); //list of all players
    private List<CardDeck> decks = new ArrayList<CardDeck>(); //list of all decks
    private boolean isFinished = false; //variable to denote when the game is finished and a player has a winning hand
    private int rounds = 1; //round counter

    /**
     * retrieves a player instance from their player index
     * @param index
     * @return player
     */
    public Player getPlayerFromIndex(int index) {
        for (Player player : players) {
            if (player.getPlayerIndex() == index) {
                return player;
            }
        }
        return null;
    }

    /**
     * retrieves a deck instance from their deck index
     * @param index
     * @return deck
     */
    public CardDeck getDeckFromIndex(int index) {
        for (CardDeck deck : decks) {
            if (deck.getDeckIndex() == index) {
                return deck;
            }
        }
        return null;
    }

    /**
     * method used to start an instance of CardGame whem given a number of players and a string denoting the text file. 
     * gives each player and deck 4 cards each and links decks with appropriate players
     * @param n the number of players
     * @param fileText the pack file of cards the game will use
     */
    public void initialiseGame(int n, String fileText) {
        totalPlayers = n;

        //loop to create the specified number of players
        for (int i = 0; i < n; i++) {
            players.add(new Player());
            decks.add(new CardDeck()); //there is an equal number of decks to players
        }

        try {
            int index = 0;
            File myObj = new File(fileText); //a file with the matching name is opened and scanned line by line
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) { //iterates until the end of the file, but index still increments
                String data = myReader.nextLine();
                if (index < 4*n) { //the first 4n cards are distributed to players
                    players.get(index % n).addCard(new Card(Integer.parseInt(data))); //gets player based on index modulus player numbers, so would access each player in a cycle
                }
                else { //the remaining 4n go card decks
                    decks.get(index % n).addCard(new Card(Integer.parseInt(data))); //index % n to ensure round robin distribution
                }
                index++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        //loop to allow for the pickupDecks and discardDecks of each player to point to the appropriate decks in the deck list
        for (int i = 0; i < n - 1; i++) {
            players.get(i).setPickupDeck(decks.get(i)); //player i will draw cards from deck i
            players.get(i).setDiscardDeck(decks.get(i+1)); //player i will discard cards to deck (i+1)
        }
        players.get(n-1).setPickupDeck(decks.get(n-1));
        players.get(n-1).setDiscardDeck(decks.get(0));
        //the player with the highest index, where i = n and n being the number of players, cannot discard to deck i+1
        //because there is no deck (n+1). It instead discards to deck 1, to retain ring topology

        //iterate through each player to output their initial hands to their files, and start their threads
        for (Player player : players) {
            player.initialContents();
            player.start();
        }

        try {
            gameLoop(); //once the players and decks have been set up the game can begin
        } catch (InterruptedException e) {}
    }

    public void gameLoop() throws InterruptedException {
        while (!isFinished) { //loops until game declared over

            // Wait for all players to finish the current round
            for (Player player : players) {
                while (!player.isReadyForNextRound() || !player.isReadyToDiscard()) {
                    Thread.sleep(10); // Polling until the player finishes
                }
            }

            // Reset players for the next round
            for (Player player : players) {
                player.resetForNextRound();
                if (player.cardsMatch()) { //also checks each player to see if there is a winning hand
                    isFinished = true; // the game at that point would be over
                    System.out.println("Game over! Player " + player.getPlayerIndex() + " wins!!"); //with a clear winner
                }
            }

            rounds++;

            //if there is no winning hand, a maximum turn has been implemented, to prevent the game running for too long
            if (rounds > 255) {
                System.out.println("Time Out!!");
                for (Player player : players) {
                    player.stopRunning(); // Stop all threads
                }
                isFinished = true;
            }
        }

        //outside of the game loop, which marks the end of the game

        for (Player player : players) {
            player.stopRunning(); // Stop all threads
        }

        //catches all threads up with each other
        for (Player player : players) {
            player.join();
        }

        //writes the decks final cards to their respective output files
        for (CardDeck deck : decks) {
            deck.lastDeckContents();
        }
    }
}
