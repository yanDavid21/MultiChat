package client.view.javafxView;

import client.controller.Features;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class PrivateMessagingController extends AbstractFXMLController {
    private String receiver;
    private String sender;
    private Stage window;
    private Scene scene;

    public PrivateMessagingController() {
        super();
    }

    public void initialize(String receiver, String sender, Features features, Stage window, Scene scene) {
        super.initController();
        this.receiver = receiver;
        this.sender = sender;
        this.features = features;
        this.window = window;
        this.scene = scene;
    }


    @FXML
    private void openFileExplorer() {
       getFile(true, window, receiver, sender);
    }

    @FXML
    private void openImageExplorer() {
        getImage(true, window, receiver, sender);
    }

    public String getReceiver() {
        return receiver;
    }

    public Stage getWindow() {
        return window;
    }

    public Scene getScene() {return scene;}

    @Override
    protected String getPreface() {
        return "/privatemsg " + sender + ": " + receiver + ": ";
    }
}
