package client.view.javafxView;

import client.controller.Features;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FXMLController extends AbstractFXMLController {
    private Scene scene;
    private final Set<PrivateMessagingController> privateMessagingWindows = new HashSet<>();

    private final ObservableList<String> userList = FXCollections.observableArrayList();
    private final ObservableList<String> serverList = FXCollections.observableArrayList();
    private final Map<String, Color> nameColors = new HashMap<>();

    private File chosenFile;

    // settings
    private boolean isDarkMode = false;
    private boolean isMuted = false;

    private final NewChatPanel newChatPanel = new NewChatPanel();
    private final SettingsPanel settingsPanel = new SettingsPanel();

    @FXML
    private ListView<String> userListView = new ListView<>();
    @FXML
    private ListView<String> serverListView = new ListView<>();

    public void setFeatures(Features features) {
        this.features = features;
    }

    public void initialize(Scene scene) {
        super.initController();
        this.scene = scene;
        this.serverListView.setItems(serverList);
        this.userListView.setItems(userList);
        this.scene.getStylesheets().add(getClass().getResource("Lightmode.css").toExternalForm());
    }


    @FXML
    private void openSettingsPanel() {
        settingsPanel.display();
    }

    @FXML
    private void openNewChatWindow() {
        newChatPanel.display();
    }

    @FXML
    private void openFileExplorer() {
        getFile(false, scene.getWindow(), null, null);
    }

    @FXML
    private void openImageExplorer() {
        getImage(false, scene.getWindow(), null, null);
    }

    @Override
    protected String getPreface() {
        return "";
    }

    public void appendChatLog(String s, String color, boolean hasDate, String protocol) {
        //playNotif();
        if (features.getClientUsername().equals(extractName(s))) {
            color = "white";
        }
        if (protocol.equals("PRIVATEMESSAGE") || protocol.equals("PRIVATEFILE")) {
            appendPrivateChatLog(s, color, hasDate, protocol);
        } else {
            super.appendChatLog(s, color, hasDate, protocol);
        }

    }

    public void setTextFieldEditable(boolean b) {
        chatField.setEditable(b);
    }

    public void setActiveUsers(List<String> activeUsers) {
        setActiveList(activeUsers, this.userList, this.userListView, true);
    }

    public void setActiveServers(List<String> activeServers) {
        setActiveList(activeServers, this.serverList, this.serverListView, false);
    }


    public File showSaveDialog(String fileName) {
        Platform.runLater(() -> {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName(fileName);
            File file = chooser.showSaveDialog(scene.getWindow());
            setChosenFile(file);
        });
        return chosenFile;
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
        for (String name : listOfNames) {
            if (!nameColors.containsKey(name)) {
                nameColors.put(name, randomColor());
            }
        }
    }

    private void appendPrivateChatLog(String s, String color, boolean hasDate, String protocol) {
        String[] components = s.substring(s.indexOf("]") + 2).split(": ");
        String date = s.substring(0, s.indexOf("]") + 1);
        String sender = components[0];
        String receiver = components[1];
        String messageAndReceiver = s.substring(s.indexOf(": ") + 2);
        String message = messageAndReceiver.substring(messageAndReceiver.indexOf(": ") + 2);
        if (sender.equals(features.getClientUsername())) { //if user sent this message
            for (PrivateMessagingController con : privateMessagingWindows) {
                if (con.getReceiver().equals(receiver)) {
                    con.appendChatLog(date + " " + sender + ": " + message, color, hasDate, protocol);
                    return;
                }
            }
        } else { //incoming message
            for (PrivateMessagingController con : privateMessagingWindows) {
                if (con.getReceiver().equals(sender)) {
                    con.appendChatLog(date + " " + sender + ": " + message, color, hasDate, protocol);
                    return;
                }
            }
            Platform.runLater(() -> {
                openPrivateMessagingWindow(sender);
                for (PrivateMessagingController con : privateMessagingWindows) {
                    if (con.getReceiver().equals(sender)) {
                        con.appendChatLog(date + " " + sender + ": " + message, color, hasDate, protocol);
                        return;
                    }
                }
            });
        }
    }

    private static Color randomColor() {
        int red = ((int) (Math.random() * 255)) + 1;
        int green = ((int) (Math.random() * 255)) + 1;
        int blue = ((int) (Math.random() * 255)) + 1;
        return Color.web(String.format("rgb(%d,%d,%d)", red, green, blue));
    }


    private void openPrivateMessagingWindow(String receiver) {
        if (features.getClientUsername().equals(receiver)) {
            appendChatLog("You cannot privately message yourself.", "red", false, "MESSAGEHELP");
            return;
        }
        for (PrivateMessagingController con : privateMessagingWindows) {
            if (con.getReceiver().equals(receiver)) {
                con.getWindow().toFront();
                return;
            }
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PrivateMessaging.fxml"));
            Parent pane = loader.load();
            Stage window = new Stage();
            Scene scene = new Scene(pane);
            window.setScene(scene);
            if (isDarkMode) {
                scene.getStylesheets().add(getClass().getResource("Darkmode.css").toExternalForm());
            } else {
                scene.getStylesheets().add(getClass().getResource("Lightmode.css").toExternalForm());
            }
            PrivateMessagingController controller = loader.getController();
            controller.initialize(receiver, features.getClientUsername(), features, window, scene);
            privateMessagingWindows.add(controller);
            controller.appendChatLog("You are now privately messaging " +
                    receiver + ".", "blue", false, "MESSAGEWELCOME");
            window.sizeToScene();
            window.setResizable(false);
            window.setOnCloseRequest(e -> {
                privateMessagingWindows.remove(controller);
            });
            window.setTitle("Private Messaging - " + receiver);
            window.show();
        } catch (IOException ioe) {
            displayError(true, "Something went wrong: Unable to private message.");
        }
    }

    private void setDarkMode(boolean isSelected) {
        isDarkMode = isSelected;
        if (isSelected) {
            changeStyleSheets(scene, "Lightmode.css", "Darkmode.css");
            for (PrivateMessagingController con : privateMessagingWindows) {
                changeStyleSheets(con.getScene(), "Lightmode.css", "Darkmode.css");
            }
            changeStyleSheets(settingsPanel.getScene(), "Lightmode.css", "Darkmode.css");
            changeStyleSheets(newChatPanel.getScene(), "Lightmode.css", "Darkmode.css");
        } else {
            changeStyleSheets(scene, "Darkmode.css", "Lightmode.css");
            for (PrivateMessagingController con : privateMessagingWindows) {
                changeStyleSheets(con.getScene(), "Darkmode.css", "Lightmode.css");
            }
            changeStyleSheets(settingsPanel.getScene(), "Darkmode.css", "Lightmode.css");
            changeStyleSheets(newChatPanel.getScene(), "Darkmode.css", "Lightmode.css");
        }
    }

    private void changeStyleSheets(Scene scene, String removedStyle, String addedStyle) {
        scene.getStylesheets().remove(getClass().getResource(removedStyle).toExternalForm());
        scene.getStylesheets().add(getClass().getResource(addedStyle).toExternalForm());
    }

    private void setChosenFile(File file) {
        chosenFile = file;
    }


    private class NewChatPanel {
        private final Stage newChatWindow;
        private final Scene scene;
        ObservableList<String> otherUsers = FXCollections.observableArrayList();

        private NewChatPanel() {

            VBox layout = new VBox();
            ImageView banner = new ImageView(new Image(getClass().getResourceAsStream(
                    "/client/resources/logo/multichat_full_logo.png")));
            VBox content = new VBox();
            content.setPadding(new Insets(5, 5, 5, 5));
            content.setSpacing(5);
            Label header = new Label("Please select a user to start chatting with.");
            header.setFont(new Font("Verdana", 12));
            ListView<String> displayNames = new ListView<>();
            displayNames.setItems(otherUsers);

            Button submit = new Button("Create Chat");
            content.getChildren().addAll(header, displayNames, submit);
            content.setAlignment(Pos.CENTER);

            layout.getChildren().addAll(banner, content);
            layout.setAlignment(Pos.CENTER);

            newChatWindow = new Stage();

            submit.setOnAction(e -> {
                String chosen = displayNames.getSelectionModel().getSelectedItem();
                if (chosen != null) {
                    openPrivateMessagingWindow(chosen);
                    newChatWindow.close();
                }
            });

            newChatWindow.initModality(Modality.APPLICATION_MODAL);
            scene = new Scene(layout);
            newChatWindow.setScene(scene);
            newChatWindow.setTitle("Start a new chat");
            newChatWindow.setResizable(false);
            newChatWindow.sizeToScene();
            newChatWindow.setOnCloseRequest(e -> {
                e.consume();
                newChatWindow.hide();
            });
        }

        private void display() {
            otherUsers.clear();
            for (String user : userList) {
                if (!user.equals(features.getClientUsername())) {
                    otherUsers.add(user);
                }
            }
            newChatWindow.showAndWait();
        }

        private Scene getScene() {
            return scene;
        }
    }

    private class SettingsPanel {
        private final Stage settingsWindow;
        private final Scene scene;

        private SettingsPanel() {
            settingsWindow = new Stage();
            VBox layout = new VBox();
            layout.setPadding(new Insets(20, 20, 20, 20));
            layout.setSpacing(20);

            CheckBox mute = new CheckBox("Mute Notification Sounds");
            mute.setSelected(isMuted);
            mute.setOnAction(e -> isMuted = mute.isSelected());

            CheckBox darkMode = new CheckBox("Dark Mode");
            darkMode.setSelected(isDarkMode);
            darkMode.setOnAction(e -> setDarkMode(darkMode.isSelected()));

            layout.getChildren().addAll(mute, new Separator(), darkMode);

            settingsWindow.initModality(Modality.APPLICATION_MODAL);
            scene = new Scene(layout);
            settingsWindow.setScene(scene);
            settingsWindow.setTitle("Settings");
            settingsWindow.setResizable(false);
            settingsWindow.sizeToScene();
            settingsWindow.setOnCloseRequest(e -> {
                e.consume();
                settingsWindow.hide();
            });
        }
        private void display() {
            settingsWindow.showAndWait();
        }

        private Scene getScene() {
            return scene;
        }
    }


    private class Cell extends ListCell<String> {
        private final boolean isUserList;

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
                if (isUserList) {
                    userIcon.setFill(nameColors.get(item));

                    MenuItem privateMessage = new MenuItem("Private Message");
                    privateMessage.setOnAction(e -> openPrivateMessagingWindow(item));

                    MenuItem kick = new MenuItem("Kick");
                    kick.setOnAction(e -> features.sendTextOut("/votekick " + item));

                    MenuItem whisper = new MenuItem("Whisper");
                    whisper.setOnAction(e -> {
                        Platform.runLater(() -> {
                            chatField.setText("/whisper " + item + ": " + chatField.getText());
                            chatField.requestFocus();
                            chatField.positionCaret(chatField.getText().length());
                        });
                    });


                    button = new MenuButton(item, userIcon, privateMessage, whisper, new SeparatorMenuItem(), kick);
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

    private void playNotif() {
        if(!scene.getWindow().isFocused() && !isMuted) {
            URL file = getClass().getResource("/client/resources/sounds/");
            final Media media = new Media(file.toString());
            final MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        }
    }

}