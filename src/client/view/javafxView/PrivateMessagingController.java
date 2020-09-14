package client.view.javafxView;

import client.controller.Features;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * A class that represents a controller for JavaFX GUI elements in a private messaging window between two users,
 * handling all of the GUI components' functionality. This class holds methods such as expanding a panel to reveal
 * emotes communicating to the server, and sending files.
 */
public class PrivateMessagingController extends AbstractFXMLController {
    private String receiver;
    private String sender;
    private Stage window;
    private Scene scene;

    /**
     * Initializes the controller's properties such as the name of the user receiving the private messages.
     *
     * @param receiver the receiver of the private message
     * @param sender   the sender of the private message (the username of the client)
     * @param features a features object
     * @param window   the main window of the client
     * @param scene    the scene to be displayed in this window
     */
    public void initialize(String receiver, String sender, Features features, Stage window, Scene scene) {
        super.initController();
        this.receiver = receiver;
        this.sender = sender;
        this.features = features;
        this.window = window;
        this.scene = scene;
    }

    //opens a file explorer
    @FXML
    private void openFileExplorer() {
        getFile(true, window, receiver, sender);
    }

    //opens a file explorer that displays only image files (.png, .jpg, .gif)
    @FXML
    private void openImageExplorer() {
        getImage(true, window, receiver, sender);
    }

    /**
     * Returns the receiver of the outgoing private messages
     *
     * @return the username of the person receiving the private messages
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * Returns the window of the application.
     *
     * @return the window of the application
     */
    public Stage getWindow() {
        return window;
    }

    /**
     * Returns the current scene of the window.
     *
     * @return the current scene
     */
    public Scene getScene() {
        return scene;
    }

    @Override
    protected String getPreface() {
        return "/privatemsg " + sender + ": " + receiver + ": ";
    }
}
