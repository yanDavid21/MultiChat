package client.model;

import java.io.IOException;

public interface MultiChatModel {
  boolean isConnectionRunning();

  String getSocketInput();

  void sendText(String output);

  MultiChatModel switchPorts(String portNumber) throws IOException;
}
