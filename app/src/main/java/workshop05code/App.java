package workshop05code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
//Included for the logging exercise
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author sqlitetutorial.net
 */
public class App {
    // Start code for logging exercise
    static {
        // must set before the Logger
        // loads logging.properties from the classpath
        try {// resources\logging.properties
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static final String GENERIC_ERROR_MESSAGE = "Something went wrong. Please try starting the game again.";
    // End code for logging exercise
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        System.out.println("Starting Up...");

        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (!wordleDatabaseConnection.checkIfConnectionDefined()) {
            logger.log(Level.SEVERE, "Something went connection to databsse");
            System.out.println(GENERIC_ERROR_MESSAGE);
            System.exit(-1);
        }
        if (!wordleDatabaseConnection.createWordleTables()) {
            logger.log(Level.SEVERE, "Something went wrong creating wordle tables");
            System.out.println(GENERIC_ERROR_MESSAGE);
            System.exit(-1);
        }

        // let's add some words to valid 4 letter words from the data.txt file

        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            System.out.println("Loading word list...");
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (line.length() != 4 || !line.matches("[a-z]{4}")) {
                    logger.log(Level.SEVERE, "Invalid word in data.txt: {}", line);
                    continue;
                }
                logger.log(Level.CONFIG, "Addign word: {}", line);
                wordleDatabaseConnection.addValidWord(i, line);
                i++;
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception while reading word list.", e);
            System.out.println(GENERIC_ERROR_MESSAGE);
            System.exit(-1);
        }

        // let's get them to enter a word

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Done loading.");
            System.out.print("Enter a 4 letter word for a guess or q to quit: ");
            String guess = scanner.nextLine();

            while (!guess.equals("q")) {
                
                if (guess.length() != 4 || !guess.matches("[a-z]{4}")) {
                    System.out.print("Not acceptable input." );
                    System.out.print("Enter a 4 letter word for a guess or q to quit: " );
                    guess = scanner.nextLine();
                    continue;
                }

                System.out.println("You've guessed '" + guess+"'.");

                if (wordleDatabaseConnection.isValidWord(guess)) { 
                    System.out.println("Success! It is in the the list.\n");
                }else{
                    System.out.println("Sorry. This word is NOT in the the list.\n");
                    logger.log(Level.INFO, "Incorrect guess: {}", guess);
                }

                System.out.print("Enter a 4 letter word for a guess or q to quit: " );
                guess = scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.SEVERE, "Exception in maing game loop.", e);
            System.out.println("Something went wrong. Please try starting the game again.");
            System.exit(-1);
        }

    }
}