package client.view.javafxView;

import client.controller.Features;
import client.view.MultiChatView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FXEntryPoint extends Application implements MultiChatView {
    private FXMLController controller;
    private Stage window;
    public static FXEntryPoint currentApp;
    private static final CountDownLatch waitForInitLatch = new CountDownLatch(1);
    private static CountDownLatch waitForNameLatch;
    private String name;
    private String prompt;

    public FXEntryPoint() {
        currentApp = this;
    }

    public static FXEntryPoint getCurrentInstance() {
        try {
            waitForInitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return currentApp;
    }


    @Override
    public void giveFeatures(Features feature) {
        controller.setFeatures(feature);
    }

    @Override
    public void display() {
        Platform.runLater(() -> {
            window.show();
            window.setTitle("MultiChat - " + name);
        });

    }

    @Override
    public void appendChatLog(String s, String color, boolean hasDate) {

    }

    @Override
    public void setTextFieldEditable(boolean b) {

    }

    @Override
    public String getName(String prompt) {
        this.prompt = prompt;
        Platform.runLater(new RunDialog());
        waitForNameLatch = new CountDownLatch(1);
        try {
            waitForNameLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return name;
    }

    @Override
    public void setActiveUsers(List<String> activeUsers) {

    }

    @Override
    public void setActiveServers(List<String> activeServers) {

    }

    @Override
    public void dispose() {
       window.close();
    }

    @Override
    public void start(Stage stage) throws Exception {
        window = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MultiChatFXML.fxml"));
        Parent pane = loader.load();
        // Get the Controller from the FXMLLoader
        controller = loader.getController();
        Scene scene = new Scene(pane, 200, 200);
        window.setScene(scene);
        window.getIcons().add(new Image(this.getClass().getResourceAsStream(
                "/client/resources/logo/multichat_logo.png")));
        waitForInitLatch.countDown();
    }

    private class RunDialog implements Runnable {
        @Override
        public void run() {
          new FXGetNameDialog().display(prompt);
        }
    }

    private class FXGetNameDialog {

        public void display(String message) {
            Stage dialogWindow = new Stage();
            dialogWindow.setOnCloseRequest(e -> {
                System.exit(4);
            });
            dialogWindow.setTitle("MultiChat - Name Selection");
            dialogWindow.initModality(Modality.APPLICATION_MODAL);

            VBox root = new VBox();
            root.setAlignment(Pos.CENTER);
            root.setSpacing(10);

            ImageView banner = new ImageView();
            Image fullLogo = new Image(getClass().getResourceAsStream(
                    "/client/resources/logo/multichat_full_logo.png"));
            banner.setImage(fullLogo);

            VBox inputPanel = new VBox();
            inputPanel.setPadding(new Insets(10,30,10,30));
            inputPanel.setAlignment(Pos.CENTER);
            inputPanel.setSpacing(10);

            Label prompt = new Label(message);
            TextField field = new TextField();
            field.setPromptText("Enter your name...");

            HBox buttonPanel = new HBox();
            buttonPanel.setAlignment(Pos.CENTER);
            buttonPanel.setSpacing(35);

            Button submitButton = new Button("Submit");
            Button cancelButton = new Button("Cancel");
            submitButton.setOnAction((e) -> {
                name = field.getText();
                waitForNameLatch.countDown();
                dialogWindow.close();
            });
            cancelButton.setOnAction((e) -> System.exit(3));

            buttonPanel.getChildren().addAll(submitButton, cancelButton);
            inputPanel.getChildren().addAll(prompt, field, buttonPanel);
            root.getChildren().addAll(banner, inputPanel);


            dialogWindow.setScene(new Scene(root));
            dialogWindow.sizeToScene();
            dialogWindow.getIcons().add(new Image(this.getClass().getResourceAsStream(
                    "/client/resources/logo/multichat_logo.png")));
            dialogWindow.setResizable(false);
            banner.requestFocus();
            dialogWindow.showAndWait();
        }
    }

}
