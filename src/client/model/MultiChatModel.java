package client.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public interface MultiChatModel {
  boolean isConnectionRunning();

  String getSocketInput();

  void sendText(String output);

  MultiChatModel switchPorts(String portNumber) throws IOException;

  void setUsername(String name);

  String getUsername();

  void sendFile(String fileName, long fileSize, File file) throws IOException;
}
