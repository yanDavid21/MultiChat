package client.controller;

/**
 * An interface representing higher level functions for view implementations instances to use (access specific
 * model methods indirectly). Designed for controller implementations.
 */
public interface Features {

  /**
   * Send the given String to the connected server through the model.
   * @param out the message to be sent to the connected server
   */
  void sendTextOut(String out);

  String getClientUsername();
}
