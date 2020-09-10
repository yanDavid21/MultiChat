package client.model;

import java.io.File;
import java.io.IOException;

/**
 * An interface representing the necessary methods of model implementations of the client. This interface allows the
 * controller to command the model and receive the client's properties through specific methods.
 */
public interface MultiChatModel {

  /**
   * Returns whether there is an active connection to a server.
   * @return whether there is an active connection to the MultiChat server
   */
  boolean isConnectionRunning();

  /**
   * Returns the next input from the server.
   * @return the next line from the socket connected to the server
   */
  String getSocketInput();

  /**
   * Sends the given String to the server by socket.
   * @param output the given String to be sent to the server
   */
  void sendText(String output);

  /**
   * Connects to a different port of the MultiChatServer.
   * @param portNumber the desired port number of the MultiChatServer
   * @return the new MultiChatModel that is created when connecting to the new port
   * @throws IOException when there is an error connecting to the new port
   */
  MultiChatModel switchPorts(String portNumber) throws IOException;

  /**
   * Sets and stores the username of the client.
   * @param name the username of the client
   */
  void setUsername(String name);

  /**
   * Gets the username of the client.
   * @return the username of the client
   */
  String getUsername();

  /**
   * Sends a file to the server through the socket.
   * @param fileName the name of the file
   * @param fileSize the size of the file
   * @param file the file to be sent
   * @throws IOException when there is an error accessing the file to be sent or writing to the server
   */
  void sendFile(String fileName, long fileSize, File file) throws IOException;

  /**
   * Sends a file to the server through the socket and signifies to the server that the file is to be private between
   * specified users.
   * @param fileName the name of the file
   * @param fileSize the size of the file
   * @param file the file to be sent
   * @param receiver the receiver of the file
   * @param sender the sender of the file
   * @throws IOException when there is an error accessing the file to be sent or writing to the server
   */
  void sendPrivateFile(String fileName, long fileSize, File file, String receiver, String sender) throws IOException;

  /**
   * Saves the file locally.
   * @param file the file to be saved
   * @param fileSize the size of the file to be saved
   * @throws IOException when there is an error saving the file
   */
  void saveFile(File file, long fileSize) throws IOException;
}
