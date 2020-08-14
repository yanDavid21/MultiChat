package client.view.javafxView;

import client.controller.Features;
import client.view.MultiChatView;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FXMLController {
    private Features features;
    private Scene scene;

    @FXML
    private TextArea chatField;
    @FXML
    private VBox chatLog;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private StackPane fileButtons;

    @FXML
    private ListView<String> userListView = new ListView<>();
    @FXML
    private ListView<String> serverListView = new ListView<>();

    private ObservableList<String> userList = FXCollections.observableArrayList();
    private ObservableList<String> serverList = FXCollections.observableArrayList();
    private Map<String, Color> nameColors = new HashMap<>();

    private TranslateTransition slideUp;
    private TranslateTransition slideDown;

    public void setFeatures(Features features) {
        this.features = features;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        scrollPane.vvalueProperty().bind(chatLog.heightProperty());
        this.serverListView.setItems(serverList);
        this.userListView.setItems(userList);
        prepareButtonAnimation();
    }

    public void onEnter(KeyEvent ke) {
        String text = chatField.getText();
        if (ke.getCode() == KeyCode.ENTER) {
            if (text.isBlank()) {
                chatField.setText("");
            } else {
                ke.consume();
                String lastCharacter = text.substring(text.length() - 1);
                if (lastCharacter.equals("\n")) {
                    text = text.substring(0, text.length() - 1);
                }
                features.sendTextOut(text);
                chatField.setText("");
            }
        }
    }

    public void appendChatLog(String s, String color, boolean hasDate, String protocol) {
        if (hasDate) {
            appendMessage(formatDate(s), getColor(color), hasDate, protocol);
        } else {
            appendMessage(s, getColor(color), hasDate, protocol);
        }
    }

    private void appendMessage(String msg, Color c, boolean hasDate, String protocol) {
        String date;
        String restOfMessage;
        String[] words;
        if (hasDate) {
            date = retrieveDate(msg);
            restOfMessage = msg.substring(msg.indexOf("]") + 1);
            words = restOfMessage.split(" ");
        } else {
            // split message by space to check for emotes
            date = "";
            restOfMessage = "";
            words = msg.split(" ");
        }

        Platform.runLater(() -> {
            VBox messageContainer = new VBox();
            StackPane bubbleWithMsg = new StackPane(); //stacks the text on top of a chat bubble
            messageContainer.setMaxWidth(Double.MAX_VALUE); //the stackpane fills to the width of chatlog
            messageContainer.setSpacing(2);
            bubbleWithMsg.setMaxWidth(Double.MAX_VALUE);

            if (!protocol.equals("MESSAGE") && !protocol.equals("WHISPER")) {
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

            HBox surface = textMessageWithImages(words, c); //contains the texts and images sent
            //holds the surface HBox to ensure stackpane alignment affects all children
            Group message = new Group(surface);
            Rectangle rect;

            if (hasDate) {
                Text dateText = new Text(date);
                dateText.setFill(Color.GREY);
                dateText.setFont(new Font("Verdana", 8));
                rect = getBubbleGraphic(surface, restOfMessage, protocol); //the bubble underneath
                //have the stackpane include text and a bubble underneath
                bubbleWithMsg.getChildren().addAll(rect, message);
                if (protocol.equals("WHISPER")){
                    Text fromMessage = new Text(extractName(msg) + " whispered to you: ");
                    fromMessage.setFill(Color.GREY);
                    fromMessage.setFont(new Font("Verdana", 8));
                    messageContainer.getChildren().addAll(fromMessage, bubbleWithMsg, dateText);
                } else {
                    messageContainer.getChildren().addAll(bubbleWithMsg, dateText);
                }
            } else {
                rect = getBubbleGraphic(surface, msg, protocol); //the bubble underneath
                //have the stackpane include text and a bubble underneath
                bubbleWithMsg.getChildren().addAll(rect, message);
                messageContainer.getChildren().addAll(bubbleWithMsg);
            }

            chatLog.getChildren().add(messageContainer); //append the stackpane to the chatlog


        });
    }


    private HBox textMessageWithImages(String[] words, Color c) {
        HBox surface = new HBox();
        surface.setPadding(new Insets(5,5,5,5));
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

    private Rectangle getBubbleGraphic(HBox surface, String msg, String protocol) {
        Rectangle rect = new Rectangle();
        rect.setX(0);
        rect.setY(0);
        rect.setWidth(surface.prefWidth(-1));
        rect.setHeight(surface.prefHeight(-1));
        rect.setArcWidth(20);
        rect.setArcHeight(20);
        if (protocol.equals("MESSAGE")) {
            if (extractName(msg).equals(features.getClientUsername())) {
                rect.setFill(Color.CORNFLOWERBLUE);
            } else {
                rect.setFill(Color.LIGHTGREY);
            }
        } else if(protocol.equals("WHISPER")) {
            rect.setFill(Color.HOTPINK);
        } else {
            rect.setFill(Color.TRANSPARENT);
        }
        return rect;
    }

    private ImageView getEmote(String imagePath, int size) {
        ImageView imageView = new ImageView(
                new Image(getClass().getResource(imagePath).toExternalForm())
        );
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        return imageView;
    }

    private Color getColor(String color) {
        switch (color) {
            case "blue":
                return Color.BLUE;
            case "red":
                return Color.RED;
            case "green":
                return Color.GREEN;
            case "orange":
                return Color.ORANGE;
            default:
                return Color.BLACK;
        }
    }

    public void setTextFieldEditable(boolean b) {
        chatField.setEditable(b);
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
        buildDate.append("[");
        buildDate.append(month);
        buildDate.append(" ");
        buildDate.append(day);
        buildDate.append(" ");
        buildDate.append(time);
        buildDate.append(" ");
        buildDate.append(timezone);
        buildDate.append("]");
        buildDate.append(message.substring(message.indexOf("]") + 1));

        return buildDate.toString();
    }

    //extracts the username of the message
    private String extractName(String msg) {
        return msg.substring(msg.indexOf("]") + 2).split(": ")[0];
    }

    public void setActiveUsers(List<String> activeUsers) {
        setActiveList(activeUsers, this.userList, this.userListView, true);
    }

    public void setActiveServers(List<String> activeServers) {
        setActiveList(activeServers, this.serverList, this.serverListView, false);
    }

    private void setActiveList(List<String> listOfNames, ObservableList<String> observableList,
                               ListView<String> listView, boolean isUserList) {
        Platform.runLater(() -> {
            observableList.clear();
            observableList.addAll(listOfNames);
            this.mapNameToColor(listOfNames);
            listView.setCellFactory(lv -> new Cell(isUserList));
        });
    }

    private void mapNameToColor(List<String> listOfNames) {
        for(String name : listOfNames) {
            if(!nameColors.containsKey(name)) {
                nameColors.put(name, randomColor());
            }
        }
    }

    @FXML
    private void openFileExplorer() {
        Platform.runLater(() -> {
            FileChooser dialog = new FileChooser();
            dialog.setTitle("Select a file to upload.");
            File selected = dialog.showOpenDialog(scene.getWindow());
            if (!(selected == null)) {
                if (selected.length() > 25000000) {

                } else {

                }
            }
        });
    }

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

    private class Cell extends ListCell<String> {
        private boolean isUserList;

        private Cell(boolean isUserList) {
            this.isUserList = isUserList;
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (item != null) {
                MenuButton button;
                Circle userIcon = new Circle(5);
                if(isUserList) {
                    userIcon.setFill(nameColors.get(item));

                    MenuItem privateMessage = new MenuItem("Private Message");
                    MenuItem whisper = new MenuItem("Whisper");
                    MenuItem kick = new MenuItem("Kick");
                    whisper.setOnAction(e -> {
                        Platform.runLater(() -> {
                            chatField.setText("/whisper " + item + ": " + chatField.getText());
                            chatField.requestFocus();
                            chatField.positionCaret(chatField.getText().length());
                        });
                    });
                    kick.setOnAction(e -> features.sendTextOut("/votekick " + item));
                    button = new MenuButton(item, userIcon, privateMessage, whisper, kick);
                } else {
                    int roomNum = Integer.parseInt(item.split(" ")[1]);
                    userIcon.setFill(Color.GREEN);
                    MenuItem join = new MenuItem("Join");
                    join.setOnAction(e -> {
                        features.sendTextOut("/join " + roomNum);
                    });
                    button = new MenuButton(item, userIcon, join);
                }

                button.setMaxWidth(Double.MAX_VALUE);
                setGraphic(button);
            }
        }
    }

    private static Color randomColor() {
        int red = ((int)(Math.random() * 255)) + 1;
        int green = ((int)(Math.random() * 255)) + 1;
        int blue = ((int)(Math.random() * 255)) + 1;
        return Color.web(String.format("rgb(%d,%d,%d)", red, green, blue));
    }

    private String retrieveDate(String msg) {
        return msg.substring(0, msg.indexOf("]") + 1);
    }

    private void prepareButtonAnimation() {
        slideUp = new TranslateTransition(new Duration(300), fileButtons);
        slideUp.setToY(400);
        slideDown = new TranslateTransition(new Duration(300), fileButtons);
        slideDown.setToY(450);
    }

//    public void setDarkMode() {
//        if(darkModeMenuItem.isSelected()) {
//            darkModeMenuItem.setText("Disable Dark Mode");
//            scene.getStylesheets().add(getClass().getResource("Darkmode.css").toExternalForm());
//        } else {
//            darkModeMenuItem.setText("Enable Dark Mode");
//            scene.getStylesheets().remove(getClass().getResource("Darkmode.css").toExternalForm());
//        }
//    }
}