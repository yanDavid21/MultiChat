package client.view.javafxView;

import client.controller.Features;
import client.view.MultiChatView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class FXMLController {
    private Features features;
    private Scene scene;

    @FXML
    private TextArea chatField;
    @FXML
    private TextFlow chatLog;
    @FXML
    private ScrollPane scrollPane;

    public void setFeatures(Features features) {
        this.features = features;
        System.out.print("features:  " + this.features);
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        scrollPane.vvalueProperty().bind(chatLog.heightProperty());
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

    public void appendChatLog(String s, String color, boolean hasDate) {
        if (hasDate) {
            appendMessage(formatDate(s), getColor(color));
        } else {
            appendMessage(s, getColor(color));
        }
    }

    private void appendMessage(String msg, Color c) {
        // split message by space
        String[] words = msg.split(" ");

        HBox surface = textMessageWithImages(words, c); //contains the texts and images sent
        Rectangle rect = getBubbleGraphic(surface, msg, c); //the bubble underneath

        StackPane bubbleWithMsg = new StackPane(); //stacks the text on top of a chat bubble
        bubbleWithMsg.getChildren().addAll(rect, surface);
        Platform.runLater(() -> chatLog.getChildren().add(bubbleWithMsg));
        Platform.runLater(() -> chatLog.getChildren().add(new Text("\n")));
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
                surface.getChildren().add(txt);
            }
        }
        return surface;
    }

    private Rectangle getBubbleGraphic(HBox surface, String msg, Color c) {
        Rectangle rect = new Rectangle();
        rect.setX(0);
        rect.setY(0);
        rect.setWidth(surface.prefWidth(-1));
        rect.setHeight(surface.prefHeight(-1));
        rect.setArcWidth(20);
        rect.setArcHeight(20);
        if (c.equals(Color.BLACK)) {
            if (extractName(msg).equals(features.getClientUsername())) {
                rect.setFill(Color.CORNFLOWERBLUE);
            } else {
                rect.setFill(Color.LIGHTGREY);
            }
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

    private String extractName(String msg) {
        return msg.substring(msg.indexOf("]") + 2).split(": ")[0];
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