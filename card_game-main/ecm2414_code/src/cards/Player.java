package cards;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//Player class encapsulates player threads and data, and runs the threads
//A single thread represents each player, which are concurrently playing with eachother
//Thread-safe with synchronized methods of drawing and discarding cards

public class Player extends Thread {

    //attributes
    private final int playerIndex;
    private final List<Card> cards = new ArrayList<>();
    private final CardDeck pickupDeck; //deck that points to cards
    private final CardDeck discardDeck; //deck that cards points to (index + 1)
    private final CardGame game;
    private volatile boolean isRunning = true; //determines if the thread is running
    private volatile boolean readyForNextRound = false; //determines if the thread is ready for the next round
    private int cardPickedUp; //intermediate card picked up after drawing

    //constructor to create a player thread with its index, associated pickup and discard decks, and CardGame object
    public Player(int playerIndex, CardDeck pickupDeck, CardDeck discardDeck, CardGame game) {

        this.playerIndex = playerIndex;
        this.pickupDeck = pickupDeck;
        this.discardDeck = discardDeck;
        this.game = game;
    }

    //methods
    public synchronized int getPlayerIndex() {return playerIndex;}
    public synchronized List<Card> getPlayerCards() {return cards;}
    public synchronized CardDeck getDiscardDeck() {return discardDeck;}
    public synchronized CardDeck getPickupDeck() {return pickupDeck;}
    public synchronized void addCard(Card newCard) {cards.add(newCard);} //adds a new card to the players card list

    //method to draw a card given a card value
    public synchronized void drawCard(int faceValue) {

        Card transferredCard = null; //creates a null card object 

        //for each card in the deck the player picks up from
        for (Card card : pickupDeck.getCards()) {

            //if the card's value is the same as the value specified
            if (card.getCardValue() == faceValue) {

                transferredCard = card; //card object equals the card
                break;
            }
        }

        //adds the card to the player's hand, removes the card from the pickup deck
        cards.add(transferredCard);
        pickupDeck.loseCard(transferredCard);
    }

    //method to discard a card given a card value
    public synchronized void discard(int faceValue) {

        Card transferredCard = null; //creates a null card object

        //for each card in the player's hand
        for (Card card : cards) {

            //if the card's value is the same as the value specified
            if (card.getCardValue() == faceValue) {

                transferredCard = card; //card object equals the card
                break;
            }
        }

        //adds the card to the discard deck, removes the card from the player's hand
        discardDeck.addCard(transferredCard);            
        cards.remove(transferredCard);
    }

    //method to randomly generate which card to discard from a player's hand, which cannot be of the same denomination as the player's index
    public synchronized int getCardToDiscard() {

        //ensures only one thread can access this
        synchronized (this) {

            List<Card> cardsToDiscard = new ArrayList<>(cards); //defensive i.e. disconnected copy of cards
            cardsToDiscard.removeIf(card -> card.getCardValue() == playerIndex); //remove all cards that have the same value as the player's index    

            Random rand = new Random(); //new Random object
            return cardsToDiscard.get(rand.nextInt(cardsToDiscard.size())).getCardValue(); //randomly selects a card from the list of cards that can be discarded     
        }
    }

    //method to terminate the player thread and to notify all player threads
    public synchronized void stopRunning() {

        isRunning = false;
        this.notifyAll();
    }

    //method to find if the player thread is ready for the next round
    public boolean isReadyForNextRound() {

        return readyForNextRound;
    }

    //method to reset the player's readiness for the next round and to notify all player threads
    public synchronized void resetForNextRound() {

        readyForNextRound = false;
        this.notifyAll();
    }

    //method to check if all the cards in the player's hand match
    public boolean cardsMatch() {

        //for each card in the players hand, not including the first card
        for (int i = 1; i < cards.size(); i++) {

            //if the card not the same as the first card, the method returns false
            if (cards.get(0).getCardValue() != cards.get(i).getCardValue()) {

                return false;
            }
        }

        //returns true if all the cards are the same as the first card
        return true;
    }    

    //method to create an output file containing the player's initial hand. Called once per player thread at game initialisation
    public synchronized void initialContents() {

        String output = "player" + playerIndex + " initial hand : "; //string to show which player the initial hand belongs to
        
        //for every card in the player's hand
        for (Card card : cards) {

            output += card.getCardValue() + " "; //add the card values to the output string
        }
        output += "\n"; //goes to the next line

        //tries to create a new output file
        try {

            File myObj = new File("player" + playerIndex + "_output.txt");
            myObj.createNewFile();

            //tries to access the output file
            try (FileWriter writer = new FileWriter(myObj);) {

                writer.write(output); //writes the initial hand to the file
            }

        //error message outputted when input/output exception
        } catch (IOException e) {

            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    //method to return the value of the last card in the player's hand
    public synchronized int getCardTaken() {

        return cards.get(cards.size() - 1).getCardValue();        
    }

    //method to add to the contents of the player output files. Called every round for every player thread. The logged cardTaken variable is a parameter 
    public synchronized void updateContents(int cardTaken) {

        String output = "player" + playerIndex + " current hand : "; //string to show which player the current hand belongs to
        
        //for every card in the player's hand
        for (Card card : cards) {

            output += card.getCardValue() + " "; //add the card values to the output string
        }
        output += "\n"; //goes to the next line

        //tries to access the output file
        try (FileWriter writer = new FileWriter("player" + playerIndex + "_output.txt", true)) {

            //writes the actions that occurred in the round i.e. what cards were drawn and discarded, and the current hand, to the file
            writer.write("\nplayer " + playerIndex + " draws a " + cardTaken + " from deck " + pickupDeck.getDeckIndex());
            writer.write("\nplayer " + playerIndex + " discards a " + discardDeck.getCards().get(discardDeck.getCards().size() - 1).getCardValue() + " to deck " + discardDeck.getDeckIndex());
            writer.write("\n" + output);

            writer.close(); //closes the writer

        //error message outputted when input/output exception
        } catch (IOException e) {

            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    //method to add the final statements to the player output files. Called once per player thread when the game ends
    public synchronized void finalOutputs() {

        int winnerIndex = game.getWinnerIndex(); //retrieves the index of the winning Player
        
        //tries to access the output file
        try (FileWriter writer = new FileWriter("player" + playerIndex + "_output.txt", true)) {
            
            //if there is a winner
            if (winnerIndex != -1 ) {

                //if the winner is this player thread, write base message
                if (winnerIndex == playerIndex) {

                    writer.write("\nplayer " + winnerIndex + " wins");
                
                //otherwise, write message that includes which player has won
                } else {

                    writer.write("\nplayer " + winnerIndex + " has informed player " + playerIndex + " that player " + winnerIndex + " has won");
                }
            }
        
            writer.write("\nplayer " + playerIndex + " exits"); //message that shows the player exits
            writer.write("\nplayer " + playerIndex + " final hand: "); //message that shows the player's final hand
            
            //for every card in the player's hand
            for (Card card : cards) {

                writer.write(card.getCardValue() + " "); //write the card values
            }
            writer.write("\n"); //goes to the next line

        //error message outputted when input/output exception
        } catch (IOException e) {

            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    //run method of a player thread, manages synchronization
    @Override
    public void run() {

        //continues looping until the player thread is no longer running i.e. the game is finished
        while (isRunning) {

            //ensures only one thread can access game i.e. lock
            synchronized (game) {

                //checks if the game is finished
                if (game.getIsFinished()) {

                    break; //thread exits
                } 
            }

            //tries
            try {

                //ensures only one thread can access pickupDeck
                synchronized (pickupDeck) {

                    //continues waiting until the pickup deck, the discard deck, and the player hand all have 4 cards
                    while (pickupDeck.getCards().size() != 4 && cards.size() != 4 && discardDeck.getCards().size() != 4) {

                        pickupDeck.wait(); 
                    }
                    
                    drawCard(pickupDeck.getCards().get(0).getCardValue()); //draws a card from the top of the pickup deck i.e. first in the deck
                    cardPickedUp = getCardTaken(); //value of the drawn card to log it
                    pickupDeck.notifyAll(); //notifies player threads that they can proceed
                }

                sleep(10); //pause to wait for threads to synchronize manually

                //ensures only one thread can access this
                synchronized (this) {

                    //continues waiting until the player's hand has 5 cards, and the pickup deck and discard deck have 3 cards
                    while (cards.size() != 5 && discardDeck.getCards().size() != 3 && pickupDeck.getCards().size() != 3) {

                        this.wait();
                    }

                    discard(getCardToDiscard()); //discards a random card not of the same denomination as the player's index to the bottom of the discard deck i.e. last in the deck
                    readyForNextRound = true; //player is ready for the next round
                    this.notifyAll(); //notifies player threads that they can proceed
                }

                updateContents(cardPickedUp); //update the contents of the player's hand to an output file, with the logged drawn card
                sleep (10); //pause to wait for threads to synchronize manually
            
            //error message outputted if the current player thread is interrupted
            } catch (InterruptedException e) {

                System.err.println(Thread.currentThread().getName() + " was interrupted.");
            
            //error message outputted when other exception in player
            } catch (Exception e) {

                System.err.println("Error in Player " + playerIndex + ": " + e.getMessage());
            }  
        }
    }
}
