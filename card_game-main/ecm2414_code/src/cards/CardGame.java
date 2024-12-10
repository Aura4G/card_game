package cards;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList; 
import java.util.List;
import java.util.Scanner;

//executable CardGame class manages the card game, including interactions between players and decks
//CardGame also factors in the rules of the game and the game loop
//Thread-safe with synchronized game initialisation and game loop methods (wait and notify)

public class CardGame extends Thread {

    //attributes
    private final List<Player> players = new ArrayList<>();
    private final List<CardDeck> decks = new ArrayList<>();
    private volatile boolean isFinished = false;
    private volatile int winnerIndex = -1; //-1 means there is no winner assigned
    private int rounds = 1;

    //method to retrieve a player instance from their player index
    public Player getPlayerFromIndex(int index) {

        for (Player player : players) {

            //if the player index equals the argument
            if (player.getPlayerIndex() == index) {

                return player; //if corresponds to player
            }
        }
        return null; //if no corresponding player
    }

    //method to retrieve a deck instance from their deck index
    public CardDeck getDeckFromIndex(int index) {

        for (CardDeck deck : decks) {

            //if the deck index equals the argument
            if (deck.getDeckIndex() == index) {

                return deck; //if corresponds to deck
            }
        }
        return null; //if no corresponding deck
    }

    //method to retrive the state of the game loop
    public boolean getIsFinished() {

        return isFinished;
    }

    //method to retrieve the player index of the player who won
    public int getWinnerIndex() {

        return winnerIndex;
    }

    //method to determine if the input pack is the correct length and all the values are valid
    public static boolean isValidPack (int totalPlayers, String fileName) {

        int requiredNumberOfLines = 8 * totalPlayers; //the number of lines in the input pack should be 8 * the number of players
        File myObj = new File(fileName);

        //trys to create a new scanner object to scan the input pack and determine if it is valid
        try (Scanner fileChecker = new Scanner (myObj)) {

            int count = 0; //count variable used to iterate through the text file

            //continue looping if there are more lines in the text file
            while (fileChecker.hasNextLine()) {

                String line = fileChecker.nextLine().trim(); //trim removes any whitespace

                //tries to convert the line to an integer
                try {

                    int value = Integer.parseInt(line);
                    
                    //if the integer is negative, output an error message and exits the method
                    if (value < 0) {

                        System.err.println("\nInvalid input pack: contains a negative integer.");
                        return false;
                    }
                
                //error message outputted if the line can not be converted to an integer, exits the method
                } catch (NumberFormatException e) {

                    System.err.println("\nInvalid input pack: contains a non-integer value.");
                    return false;
                }

                count++;
            }

            //if the number of lines does not meet what is required, output an error message and exits the method
            if (count != requiredNumberOfLines) {

                System.err.println("\nInvalid input pack: expected " + requiredNumberOfLines + " lines, but received " + count + ".");
                return false;
            }
        
        //error message outputted if the inputted file name cannot be found, exits the method
        } catch (FileNotFoundException e) {

            System.err.println("\nFile not found: " + e.getMessage());
            return false;
        }

        //input pack is valid if all checks pass
        return true;
    }

    //entry point of the program
    //method to retrieve user input, validate it, and start the game
    public static void main(String[] args) {

        //tries to create a new scanner object to take user input
        try (Scanner userInput = new Scanner(System.in)) {

            //prompts the user for the number of players playing the game, retrieves input
            System.out.println("Please enter the number of players: ");
            int totalPlayers = userInput.nextInt();

            //initialises variables before checking if the inputted input pack is valid
            String fileName = null;
            boolean validPack = false;

            //continues looping until the user enters a valid input pack
            while (!validPack) {

                //prompts the user for the name of the text file, retrieves input
                System.out.println("Please enter location of the input pack to load: ");
                fileName = userInput.next();

                //if the input pack is valid, move on
                if (isValidPack(totalPlayers, fileName)) {

                    validPack = true;

                //if the input pack is invalid, an error message is outputted
                } else {

                    System.err.println("The provided input pack file is invalid. Please enter the location of a valid pack file.\n");
                }
            }

            //a new CardGame object is created and initialised
            CardGame game = new CardGame();
            game.initialiseGame(totalPlayers, fileName);
        
        //error message outputted when exception
        } catch (Exception e) {

            System.err.println("Error reading input: " + e.getMessage());
        }
    }

    //method to initialise an instance of CardGame 
    public void initialiseGame(int n, String file) {    

        //initialise decks
        for (int i = 0; i < n; i++) {

            decks.add(new CardDeck()); //new CardDeck object created and added to decks list
        }

        //initialise players with associated pickup decks and discard decks. One player's pickupDeck is another player's discardDeck
        for (int i = 0; i < n; i++) {

            CardDeck pickupDeck = decks.get(i);
            CardDeck discardDeck = decks.get((i + 1) % n); //mod n as the discard deck of n is the first pickup deck i.e. ring topology
            players.add(new Player(i + 1, pickupDeck, discardDeck, this)); //new Player object created and added to players list
        }

        //tries to open a file with the same name 
        try {

            int index = 0; //for dealing cards to decks
            File myObj = new File(file); 

            //tries the scan the file line by line
            try (Scanner myReader = new Scanner(myObj)) {
                
                //deal the first half of cards to players
                for (int i = 0; i < 4 * n; i++) {

                    //continue scanning if more lines in the file
                    if (myReader.hasNextLine()) {

                        String data = myReader.nextLine();
                        players.get(i % n).addCard(new Card(Integer.parseInt(data))); //creates a Card object from the line scanned and adds it to player's hand
                        //mod is used to get player
                    }
                }
                
                //deal remaining cards to decks, continue scanning if more lines in the file
                while (myReader.hasNextLine()) {

                    String data = myReader.nextLine();
                    decks.get(index % n).addCard(new Card(Integer.parseInt(data))); //creates a Card object from the line scanned and adds it to deck
                    //mod n is used to get correct deck in a round-robin fashion
                    index++;
                }
            }
        
        //error message outputted if the file name cannot be found
        } catch (FileNotFoundException e) {

            System.err.println("An error occurred: " + e.getMessage());
        }

        //for each player
        for (Player player : players) {

            //get initial contents of the players hand
            player.initialContents();
            
            //ensures only one thread can access this
            synchronized (this) {

                //checks if player has a winning hand and makes sure no other player has already won
                if (player.cardsMatch() && (winnerIndex == -1)) {

                    isFinished = true; //game has finished
                    winnerIndex = player.getPlayerIndex(); //assigns the winners index
                    System.out.println("\nplayer " + winnerIndex + " wins\n"); //outputs the winner to the terminal
                    
                    notifyAll(); //notifies player threads that the game is over 
                    
                    //for each player, stop their thread and get final contents
                    for (Player p : players) {

                        p.stopRunning();
                        p.finalOutputs();
                    }

                    //for each deck, outputs the final contents
                    for (CardDeck deck : decks) {

                        deck.lastDeckContents();
                    }
    
                    //exit method as game is over
                    return;
                    }                   
            }             
        }

        //checks if the game is already over
        if (!isFinished) {

            //for each player, start their thread
            for (Player player : players) {

                player.start();
            }
        }  

        //tries to start the main game loop after initialisation
        try {

            gameLoop();

        //error message outputted if the game loop is interrupted
        } catch (InterruptedException e) {

            System.err.println("Game loop interrupted: " + e.getMessage());
        }
    }

    //method to manage the game
    public void gameLoop() throws InterruptedException {

        //tries to run the game loop if it is not finished
        try {

            while (!isFinished) {

                //each player
                for (Player player : players) {

                    //ensures only one thread can access player
                    synchronized (player) {

                        //checks if player is ready for the next round and if the game is still running
                        while (!player.isReadyForNextRound() && !isFinished) {

                            player.wait(); //waits until all players ready 
                        }                    
                    }
                }
                
                //ensures only one thread can access this
                synchronized (this) {

                    //resets each player for the next round, checks for a winning hand
                    for (Player player : players) {

                        player.resetForNextRound();

                        //checks if the player has a winning hand, and if there is not already a winner
                        if (player.cardsMatch() && winnerIndex == -1) {

                            isFinished = true; //game has finished
                            winnerIndex = player.getPlayerIndex(); //assigns the winners index
                            System.out.println("\nplayer " + winnerIndex + " wins\n"); //outputs the winner to the terminal
                            
                            notifyAll(); //notifies player threads that the game is over      
                            break; //exits the loop          
                        }
                    }                
                }
                
                //if the number of rounds goes above 255, the game ends to prevent it from running forever
                if (++rounds > 255) {

                    System.out.println("\nTime out! This input pack file likely does not contain the cards that allow for a winning hand. Exiting game.\n"); //outputs a message to the terminal
                    
                    //ensures only one thread can access this
                    synchronized (this) {

                        isFinished = true; //game has finished
                        notifyAll(); //notifies player threads that the game is over
                    }
                }
            }

            //for each player, stop their thread and get final contents
            for (Player player : players) {

                player.stopRunning();
                player.finalOutputs();
            }
            
            //catches all threads up with eachother
            for (Player player : players) {

                player.join();
            }

            //for each deck, outputs the final contents
            for (CardDeck deck : decks) {

                deck.lastDeckContents();
            }
        
        //error message outputted if the game loop is interrupted
        } catch (InterruptedException e) {

            System.err.println("Game interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); //interrupts the current thread 
        }       
    }
}
