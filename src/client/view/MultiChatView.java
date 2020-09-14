package client.view;

import client.controller.Features;

import java.io.File;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

/**
 * An interface representing the necessary methods of view implementations of the client. This interface allows the
 * controller to command the view and display information from the model or server.
 */
public interface MultiChatView {

    //a map of emote commands to the name of the image, scrubbing the command free of html
    //deprecated, used for java swing gui view
    Map<String, String> HTML_EMOTES = Map.ofEntries(
            entry("&lt;3", "heart.png"),
            entry(":)", "smiley.png"),
            entry(":(", "frowny.png"),
            entry(":/", "confused.png"),
            entry(":D", "excited.png"),
            entry("D:", "anguish.png"),
            entry(":p", "tongue.png"),
            entry("&gt;:(", "angry.png"),
            entry(":O", "wow.png"),
            entry(":|", "neutral.png"),
            entry(";)", "wink.png")
    );

    //a map of emote commands to the name of the image
    Map<String, String> FXEMOTES = Map.ofEntries(
            entry("<3", "heart.png"),
            entry(":)", "smiley.png"),
            entry(":(", "frowny.png"),
            entry(":/", "confused.png"),
            entry(":D", "excited.png"),
            entry("D:", "anguish.png"),
            entry(":p", "tongue.png"),
            entry(">:(", "angry.png"),
            entry(":O", "wow.png"),
            entry(":|", "neutral.png"),
            entry(";)", "wink.png")
    );

    //a map of twitch emote commands to the name of the image
    Map<String, String> TWITCH_EMOTES = Map.ofEntries(
            entry("PepeHands", "Pepehands.png"),
            entry("Pepega", "Pepega.png"),
            entry("Kappa", "Kappa.png"),
            entry("forsenCD", "forsenCD.jpg"),
            entry("pepeD", "pepeD.gif"),
            entry("pepoG", "pepoG.png"),
            entry("WeirdChamp", "WeirdChamp.png"),
            entry("widepeepohappy", "widepeepohappy.png"),
            entry("FeelsBadMan", "FeelsBadMan.png"),
            entry("FeelsGoodMan", "FeelsGoodMan.png"),
            entry("Pog", "Pog.png"),
            entry("OMEGALUL", "OMEGALUL.png"),
            entry("KEKW", "KEKW.png"),
            entry("monkaHmm", "monkaHmm.png"),
            entry("monkaGun", "monkaGun.png"),
            entry("5Head", "5Head.png"),
            entry("PepeLaugh", "PepeLaugh.png"),
            entry("POGGERS", "POGGERS.png"),
            entry("ratirlCoffee", "ratirlCoffee.png")
    );

    /**
     * Requests and returns a name from the user.
     *
     * @param prompt the prompt to be displayed to the user
     * @return the username of the client
     */
    String getName(String prompt);

    /**
     * Gives a reference to the given features object.
     *
     * @param feature a Features object
     */
    void giveFeatures(Features feature);

    /**
     * Sets the text field to be editable based on the given boolean.
     *
     * @param b whether the textfield is editable
     */
    void setTextFieldEditable(boolean b);

    /**
     * Displays the GUI.
     */
    void display();

    /**
     * Disposes the main window of the GUI.
     */
    void dispose();

    /**
     * Appends the given String to the chat log of the client, including information on the protocol, desired color, and
     * if the message has a date.
     *
     * @param s        the message to be appended
     * @param color    the desired color of the message
     * @param hasDate  if there is a date in the message
     * @param protocol the protocol of the message
     */
    void appendChatLog(String s, String color, boolean hasDate, String protocol);

    /**
     * Sets and displays the given list of active usernames.
     *
     * @param names the usernames of the active users in the room
     */
    void setActiveUsers(List<String> names);


    /**
     * Sets and displays the given list of active server names.
     *
     * @param servers the server names of the active servers
     */
    void setActiveServers(List<String> servers);

    /**
     * Display an error pop up dialog.
     *
     * @param remainRunningWhenClosed whether when exiting the dialog, should the program continue running
     * @param errorMessage            the error message to be displayed
     */
    void displayError(boolean remainRunningWhenClosed, String errorMessage);

    /**
     * Opens a file explorer to find a location to save a file to.
     *
     * @param fileName the name of the file to be saved
     * @return an abstract file to be saved to.
     */
    File showSaveDialog(String fileName);
}