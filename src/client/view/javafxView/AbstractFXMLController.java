package client.view.javafxView;

import client.controller.Features;
import client.view.MultiChatView;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * An abstract class representing controller objects that have a chat log and text field in JavaFX. This class holds
 * methods that append messages to the chat log, give functionality to the textfield, and give animation and
 * functionality to the buttons (emotes, send images, send files).
 */
public abstract class AbstractFXMLController {
    @FXML
    protected VBox chatLog;
    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected TextArea chatField;
    @FXML
    protected VBox fileButtons;
    @FXML
    protected GridPane twitchEmotePanel;
    @FXML
    protected GridPane standardEmotePanel;

    //the animations for sliding up, sliding down, and sliding even further up for the panel to send files and emotes
    protected TranslateTransition slideUp;
    protected TranslateTransition slideDown;
    protected TranslateTransition slideFurtherUp;

    protected Features features;

    /**
     * Initializes the fields and properties of the controller. This specifically sets the animation for the emote
     * panels, adds the images to the emotes buttons, and forces the scrollpane to auto scroll as the chatlog grows.
     */
    protected void initController() {
        prepareButtonAnimation();
        scrollPane.vvalueProperty().bind(chatLog.heightProperty());
        initializeEmotePanels(MultiChatView.FXEMOTES,"/client/resources/images/emojis/", standardEmotePanel);
        initializeEmotePanels(MultiChatView.TWITCH_EMOTES,"/client/resources/images/twitch/", twitchEmotePanel);
    }

    /**
     * Returns the command to denote what type of message it is. (ie. "/privatemessage")
     * @return the String that prefaces the message sent to the server from the text field
     */
    protected abstract String getPreface();

    /**
     * Sends the text in the textfield to the server if the given key event is an 'enter key event' and the chatfield
     * is non-empty.
     * @param ke the key event given
     */
    @FXML
    protected void onEnter(KeyEvent ke) {
        String text = chatField.getText();
        if (ke.getCode() == KeyCode.ENTER) {
            if (!text.isBlank()) {
                ke.consume();
                String lastCharacter = text.substring(text.length() - 1);
                if (lastCharacter.equals("\n")) {
                    text = text.substring(0, text.length() - 1);
                }
                features.sendTextOut(getPreface() + text);
            }
            chatField.setText("");
        }
    }

    /**
     * Shows the emote panel. Plays the animation to slide up the button panel even further up to reveal
     * the emote buttons when the panel is slightly hidden and slides the button panel down to the level
     * which only the button panel shows if its fully shown.
     */
    @FXML
    protected void showEmotePanel() {
        if (fileButtons.getTranslateY() == 300) {
            slideUp.play();
        } else {
            slideFurtherUp.play();
        }
    }

    /**
     * Shows the button panel that holds "Upload files", "Upload Images", and "Emotes". Plays the animation to show
     * the button when the panel is hidden and hides the button panel when shown.
     */
    @FXML
    private void showButtonPanel() {
        Platform.runLater(() -> {
            if(fileButtons.getTranslateY() == 450){
                slideUp.play();
            } else{
                slideDown.play();
            }
        });
    }

    /**
     * Adds and stylizes the given String to the chat log based on the given color, boolean, and protocol.
     * @param s the message
     * @param color the desired color of the message
     * @param hasDate whether the message has a date included
     * @param protocol the protocol associated with this message
     */
    protected void updateChatLog(String s, String color, boolean hasDate, String protocol) {
        if (hasDate) {
            appendMessage(formatDate(s), getColor(color), true, protocol);
        } else {
            appendMessage(s, getColor(color), false, protocol);
        }
    }

    /**
     * Display an error message dialog with the given error message. Closing the error message can close the program
     * based on the given boolean
     * @param remainRunningWhenClosed whether the program should keep running when the dialog is closed
     * @param errorMessage the error message to be displayed
     */
    protected void displayError(boolean remainRunningWhenClosed, String errorMessage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage);
            if (!remainRunningWhenClosed) {
                alert.setOnCloseRequest(e -> System.exit(1));
            }
            alert.showAndWait();
        });
    }

    /**
     * Extracts the username from the given message by taking the String in between "]" and ": ".
     * @param msg the message to extract the username from
     */
    protected String extractName(String msg) {
        return msg.substring(msg.indexOf("]") + 2).split(": ")[0];
    }

    /**
     * Opens a file explorer interface that allows a client to select a file (any extension) and sends the file to the
     * server using the model through the controller's features object.
     * @param isPrivate whether the file being sent is in a private message
     * @param window the window of the main application to host the dialog
     * @param receiver the receiver of the file if the file is private, null if if public
     * @param sender the sender of the file if the file is private, null if if public
     */
    protected void getFile(boolean isPrivate, Window window, String receiver, String sender) {
        Platform.runLater(() -> {
            FileChooser dialog = new FileChooser();
            dialog.setTitle("Select a file to upload.");
            deployFileExplorer(dialog, isPrivate, window, receiver, sender);
        });
    }

    /**
     * Opens a file explorer interface that allows a client to select an image (.PNG, .JPG, and .GIF) and sends the
     * file to the server using the model through the controller's features object.
     * @param isPrivate whether the image file being sent is in a private message
     * @param window the window of the main application to host the dialog
     * @param receiver the receiver of the image file if the image file is privately sent, null if if public
     * @param sender the sender of the image file if the image file is privately sent, null if if public
     */
    protected void getImage(boolean isPrivate, Window window, String receiver, String sender) {
        Platform.runLater(() -> {
            FileChooser dialog = new FileChooser();
            dialog.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.png", "*.gif"),
                    new FileChooser.ExtensionFilter("PNG", "*.png"),
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("GIF", "*.gif")
            );
            dialog.setTitle("Select an image to upload.");
            deployFileExplorer(dialog, isPrivate, window, receiver, sender);
        });
    }

    //creates a file explorer dialog and sends a file with the given parameters as properties
    private void deployFileExplorer(FileChooser dialog, boolean isPrivate, Window window, String receiver,
                                    String sender) {
        File selected = dialog.showOpenDialog(window);
        if (!(selected == null)) {
            if (selected.length() < 25000000) {
                try {
                    features.sendFile(selected.getName(), selected.length(), selected, isPrivate, receiver, sender);
                } catch (IOException ioe) {
                    displayError(true, "Something went wrong with sending the file!");
                }

            } else {
                updateChatLog("The file size cannot exceed 25mb", "orange", false, "MESSAGEHELP");
            }
        }
    }

    //appends the given message to the chatlog after styling it based on the parameters (color, protocol, and hasDate)
    //color is used for text fill, hasDate also appends an edited date under the text, and protocol determines whether
    //it should be centered or have a bubble background
    private void appendMessage(String msg, Color c, boolean hasDate, String protocol) {
        String date;
        String[] words;
        if (hasDate) {
            date = retrieveDate(msg);
            // split message after the date by space to check for emotes
            String restOfMessage = msg.substring(msg.indexOf("]") + 1);
            words = restOfMessage.split(" ");
        } else {
            // split message by space to check for emotes
            date = "";
            words = msg.split(" ");
        }

        Platform.runLater(() -> {
            VBox messageContainer = new VBox();
            StackPane bubbleWithMsg = new StackPane(); //stacks the text on top of a chat bubble
            messageContainer.setMaxWidth(Double.MAX_VALUE); //the stackpane fills to the width of chatlog
            bubbleWithMsg.setMaxWidth(Double.MAX_VALUE);

            if (!protocol.equals("MESSAGE") && !protocol.equals("WHISPER") && !protocol.equals("FILE") &&
                    !protocol.equals("PRIVATEMESSAGE") && !protocol.equals("PRIVATEFILE")) {
                messageContainer.setAlignment(Pos.CENTER); //if it is not user text, center it (ie. black text messages)
                bubbleWithMsg.setAlignment(Pos.CENTER); //if it is not user text, center it (ie. black text messages)
            } else if (extractName(msg).equals(features.getClientUsername())) {
                //if it is user text and sent by the user, align to right
                messageContainer.setAlignment(Pos.BASELINE_RIGHT);
                bubbleWithMsg.setAlignment(Pos.BASELINE_RIGHT);
            } else {
                //if it is user text and not sent by the user, align to left
                messageContainer.setAlignment(Pos.BASELINE_LEFT);
                bubbleWithMsg.setAlignment(Pos.BASELINE_LEFT);
            }

            HBox surface;
            if (protocol.equals("FILE") || protocol.equals("PRIVATEFILE")) {
                surface = createHyperLink(msg);
            } else {
                surface = textMessageWithImages(words, c); //contains the texts and images sent
            }

            //holds the surface HBox to ensure stackpane alignment affects all children
            Group message = new Group(surface);
            Rectangle rect;

            //adds a date text underneath the message
            if (hasDate) {
                Text dateText = new Text(date);
                dateText.setFill(Color.GREY);
                dateText.setFont(new Font("Verdana", 8));
                messageContainer.getChildren().addAll(bubbleWithMsg, dateText);
            } else {
                messageContainer.getChildren().addAll(bubbleWithMsg);
            }
            rect = getBubbleGraphic(surface, msg, protocol); //the bubble underneath
            //have the stackpane include text and a bubble underneath
            bubbleWithMsg.getChildren().addAll(rect, message);
            chatLog.getChildren().add(messageContainer); //append the stackpane to the chatlog
        });
    }

    //returns a HBox containing the message after converting any possible keywords into images (emotes)
    private HBox textMessageWithImages(String[] words, Color c) {
        HBox surface = new HBox();
        surface.setPadding(new Insets(5, 5, 5, 5));
        for (String word : words) {
            // if the word equals an emoji name (ex. <3) then replace it with an ImageView of the emoji
            // if the word equals a twitch emoji (ex. PepeHands) then replace it with an ImageView of the twitch emoji
            // otherwise just add the word as plaintext
            if (MultiChatView.FXEMOTES.containsKey(word.trim())) {
                ImageView emoji = getEmote("/client/resources/images/emojis/" +
                        MultiChatView.FXEMOTES.get(word.trim()), 20);
                surface.getChildren().add(emoji);
            } else if (MultiChatView.TWITCH_EMOTES.containsKey(word.trim())) {
                ImageView twitchEmote = getEmote("/client/resources/images/twitch/" +
                        MultiChatView.TWITCH_EMOTES.get(word.trim()), 40);
                surface.getChildren().add(twitchEmote);
            } else {
                Text txt = new Text(word + " ");
                txt.setFill(c);
                txt.setTextAlignment(TextAlignment.RIGHT);
                surface.getChildren().add(txt);
            }
        }
        return surface;
    }

    //returns the date from the given message based on the position of "]"
    private String retrieveDate(String msg) {
        return msg.substring(0, msg.indexOf("]") + 1);
    }

    //returns the bubble that is to be displayed underneath the message, the rectangle's color is based on the
    //protocol and the size is based on the given HBox that contains the text, image, and/or hyperlink of the message
    private Rectangle getBubbleGraphic(HBox surface, String msg, String protocol) {
        Rectangle rect = new Rectangle();
        rect.setX(0);
        rect.setY(0);
        rect.setWidth(surface.prefWidth(-1));
        rect.setHeight(surface.prefHeight(-1));

        rect.setArcWidth(20);
        rect.setArcHeight(20);
        if (protocol.equals("MESSAGE") || protocol.equals("FILE") || protocol.equals("PRIVATEMESSAGE") ||
                protocol.equals("PRIVATEFILE")) {
            if (extractName(msg).equals(features.getClientUsername())) {
                rect.setFill(Color.CORNFLOWERBLUE);
            } else {
                rect.setFill(Color.LIGHTGREY);
            }
        } else if (protocol.equals("WHISPER")) {
            rect.setFill(Color.HOTPINK);
        } else {
            rect.setFill(Color.TRANSPARENT);
        }
        return rect;
    }

    //formats new Date.toString() into more readable dates, removing the seconds and year from the time
    //returns the entire message with the date formatted
    private String formatDate(String message) {
        String date = message.substring(0, message.indexOf("]"));
        String[] dateComponents = date.split(" ");
        String month = dateComponents[1];
        String day = dateComponents[2];
        String time = dateComponents[3];
        time = time.substring(0, time.lastIndexOf(":"));
        String timezone = dateComponents[4];

        StringBuilder buildDate = new StringBuilder();
        buildDate.append("[").append(month).append(" ").append(day).append(" ").append(time).append(" ").append(
                timezone).append("]").append(message.substring(message.indexOf("]") + 1));

        return buildDate.toString();
    }

    //returns a javafx color object based on the String name
    private Color getColor(String color) {
        switch (color) {
            case "blue":
                return Color.CORNFLOWERBLUE;
            case "red":
                return Color.INDIANRED;
            case "green":
                return Color.LIMEGREEN;
            case "orange":
                return Color.ORANGE;
            case "white":
                return Color.WHITE;
            default:
                return Color.BLACK;
        }
    }

    //returns an ImageView object based on the image path given and sets it to the given size
    private ImageView getEmote(String imagePath, int size) {
        ImageView imageView = new ImageView(
                new Image(getClass().getResource(imagePath).toExternalForm())
        );
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        return imageView;
    }

    //returns an HBox containing a hyperlink with the given text as its text
    private HBox createHyperLink(String msg) {
        HBox surface = new HBox();
        surface.setPadding(new Insets(5, 5, 5, 5));
        surface.setAlignment(Pos.CENTER);
        Text name = new Text(extractName(msg) + ": ");
        String filename = msg.substring(msg.indexOf(": ") + 2);
        Hyperlink link = new Hyperlink();
        link.setText(filename);
        if (features.getClientUsername().equals(extractName(msg))) {
            name.setFill(Color.WHITE);
            link.setTextFill(Color.WHITE);
        }
        link.setOnAction(e -> features.sendTextOut("/requestfile " + extractName(msg) + ":" + filename));
        surface.getChildren().addAll(name, link);
        surface.setPrefWidth(new Text(extractName(msg) + ": " + filename).prefWidth(-1) + 20);
        return surface;
    }

    //initializes the animations to slide up the panel holding the buttons to upload files and emotes
    private void prepareButtonAnimation() {
        slideUp = new TranslateTransition(new Duration(300), fileButtons);
        slideUp.setToY(400);
        slideDown = new TranslateTransition(new Duration(300), fileButtons);
        slideDown.setToY(450);
        slideFurtherUp = new TranslateTransition(new Duration(300), fileButtons);
        slideFurtherUp.setToY(300);
    }

    //adds a background image of the emote to the button for every emote
    private void initializeEmotePanels(Map<String, String> map, String path, GridPane panel) {
        int column = 0;
        int row = 0;
        for (String emote : map.keySet()) {
            Button emoteButton = new Button();
            emoteButton.setMinSize(30, 30);
            emoteButton.setMaxSize(30, 30);
            Image image = new Image(getClass().getResource(path + map.get(emote)).toExternalForm(),
                    emoteButton.getWidth(), emoteButton.getHeight(), false, true, true);
            BackgroundImage bImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                    new BackgroundSize(emoteButton.getWidth(), emoteButton.getHeight(), true, true, true, false));

            Background backGround = new Background(bImage);
            emoteButton.setBackground(backGround);
            emoteButton.setOnAction(e -> features.sendTextOut(getPreface() + emote));
            emoteButton.getStyleClass().clear();
            emoteButton.getStyleClass().add("emote-button");
            panel.add(emoteButton, column, row);
            column++;
            if (column >= 16) {
                row++;
                column = 0;
            }
        }
    }
}
