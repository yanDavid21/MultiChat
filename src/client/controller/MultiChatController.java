package client.controller;

/**
 * An interface representing the necessary methods of controller implementations of the client.
 */
public interface MultiChatController {

    /**
     * Starts the client and server interaction, handles the server's communication and delegates the controller's
     * view and model objects to properly handle the server commands.
     */
    void run();
}
