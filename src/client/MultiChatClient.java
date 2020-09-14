package client;

import client.controller.MultiChatController;
import client.controller.MultiChatControllerImpl;
import client.model.MultiChatClientModelImpl;
import client.model.MultiChatModel;
import client.view.MultiChatView;
import client.view.javafxView.FXEntryPoint;
import javafx.application.Application;
import java.io.IOException;

/**
 * A texting application that connects to a MultiChat server that supports images, files, and emotes, able to
 * communicate to other users in private messages or in chat rooms. This application contains a MultiChatModel
 * instance to connect and communicate to the server, a MultiChatView to display to the user, and a MultiChatController
 * that controls the flow of this application.
 */
public class MultiChatClient {

    /**
     * Runs the application, creating a model, view, and controller instance where the controller has access to
     * references of the model and view, then running the controller.
     *
     * @param args command line String arguments, where the first is the server IP address and the second is the
     *             room (port) number.
     */
    public static void main(String[] args) {
        if (args.length != 1 && args.length != 2) {
            System.err.println("Pass the server IP as the sole command line argument");
            return;
        }

        try {
            //create a model
            MultiChatModel model = new MultiChatClientModelImpl(args[0], Integer.parseInt(args[1]));

            //create a view
            new Thread(() -> Application.launch(FXEntryPoint.class)).start();
            MultiChatView gui = FXEntryPoint.getCurrentInstance();

            //initialize the controller with the view and model then calls the run method
            MultiChatController controller = new MultiChatControllerImpl(model, gui);
            controller.run();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.print("Failed to connect Server Socket and Client Socket.");
        }
    }
}
