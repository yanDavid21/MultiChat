package client.controller;

import java.io.File;
import java.io.IOException;

/**
 * An interface representing higher level functions for view implementations instances to use (access specific
 * model methods indirectly). Designed for controller implementations.
 */
public interface Features {

    /**
     * Send the given String to the connected server through the model.
     *
     * @param out the message to be sent to the connected server
     */
    void sendTextOut(String out);

    /**
     * Returns the username of this client.
     *
     * @return the username of this client
     */
    String getClientUsername();

    /**
     * Requests the model to send a file to the connected server.
     *
     * @param fileName  the name of the file being sent
     * @param filesize  the size of the file being sent
     * @param file      the file being sent
     * @param isPrivate whether the file being sent was in a private message
     * @param receiver  the receiver of the file if in a private message (null if it wasn't privately messaged)
     * @param sender    the sender of the file if in a private message (null if it wasn't privately messaged)
     * @throws IOException when there is an error reading the desired file
     */
    void sendFile(String fileName, long filesize, File file, boolean isPrivate, String receiver, String sender)
            throws IOException;
}
