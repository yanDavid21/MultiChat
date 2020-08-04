package client.view.javafxView;

import client.controller.Features;
import client.view.MultiChatView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FXEntryPoint extends Application implements MultiChatView {
    private FXMLController controller;
    private Stage window;
    public static FXEntryPoint currentApp;
    public static final CountDownLatch latch = new CountDownLatch(1);

    public FXEntryPoint() {
        currentApp = this;
    }
//launch obj init start

    public static FXEntryPoint getCurrentInstance() {
        try {
            latch.await();
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
        Platform.runLater(() -> window.show());
    }

    @Override
    public void appendChatLog(String s, String color, boolean hasDate) {

    }

    @Override
    public void setTextFieldEditable(boolean b) {

    }

    @Override
    public String getName(String prompt) {
        return "test";
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
        VBox flowPane = loader.load();
        // Get the Controller from the FXMLLoader
        controller = loader.getController();
        Scene scene = new Scene(flowPane, 200, 200);
        window.setScene(scene);
        latch.countDown();
    }


}
