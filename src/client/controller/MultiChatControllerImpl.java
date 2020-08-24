package client.controller;

import client.model.MultiChatModel;
import client.view.MultiChatView;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * An implementation of the client's controller object, possessing both model and view objects of the client, delegating
 * each to handle the server's communication.
 */
public class MultiChatControllerImpl implements MultiChatController, Features {
  private MultiChatModel model;
  private MultiChatView view;

  /**
   * Constructs an instance of a MultiChatControllerImpl, providing this object with instances of a MultiChatView and
   * MultiChatModel
   * @param model MultiChatModel instance to be used for the client's logic
   * @param view MultiChatView instance to be used for displaying the client
   */
  public MultiChatControllerImpl(MultiChatModel model, MultiChatView view) {
    this.model = model;
    this.view = view;
    view.giveFeatures(this); //give itself to the view as a Features object, allowing limited and specific method access
  }

  @Override
  public void run() {
    String username = "";

    while(model.isConnectionRunning()) { //while the server and this client is still connected

      String line = model.getSocketInput(); //listen and capture what the server just communicated
      System.out.println(line);

      //handle what the server communicated based on the pre-determined protocols (ie. SUBMITNAME & NAMEACCEPTED)
      if (line.startsWith("SUBMITNAME")) {
        String name = view.getName("Choose a screen name:");
        username = name;
        model.sendText(username);
      } else if (line.startsWith("SUBMITANOTHERNAME")) {
        String name = view.getName("Please select a different screen name:");
        username = name;
        model.sendText(username);
      } else if (line.startsWith("NAMEACCEPTED")) {
        model.setUsername(username);
        view.display();
        view.setTextFieldEditable(true);
      } else if (line.startsWith("MESSAGE ")) {
        view.appendChatLog(line.substring(8), "black", true, "MESSAGE");
      } else if (line.startsWith("MESSAGEUSERJOINED ")) {
        view.appendChatLog(line.substring(18), "green", true, "MESSAGEUSERJOINED");
      } else if (line.startsWith("MESSAGEUSERLEFT ")) {
        view.appendChatLog(line.substring(16), "red", true, "MESSAGEUSERLEFT");
      } else if (line.startsWith("MESSAGEWELCOME ")) {
        view.appendChatLog(line.substring(15), "blue", false, "MESSAGEWELCOME");
      } else if (line.startsWith("ACTIVEUSERLIST ")) {
        String[] arr = line.substring(15).split(",");
        List<String> names = Arrays.asList(arr);
        view.setActiveUsers(names);
      } else if (line.startsWith("MESSAGEHELP ")) {
        view.appendChatLog(line.substring(12), "orange", false, "MESSAGEHELP");
      } else if(line.startsWith("VOTEKICK ")) {
        view.appendChatLog(line.substring(9), "orange", false, "VOTEKICK");
      } else if(line.startsWith("FAILEDVOTEKICK ")) {
        view.appendChatLog(line.substring(15), "red", false, "FAILEDVOTEKICK");
      } else if (line.startsWith("SUCCESSFULVOTEKICK ")) {
        view.appendChatLog(line.substring(19), "red", false, "SUCCESSFULVOTEKICK");
      } else if (line.startsWith("ACTIVESERVERLIST ")) {
        String[] arr = line.substring(17).split(",");
        List<String> servers = Arrays.asList(arr);
        view.setActiveServers(servers);
      } else if(line.startsWith("WHISPER ")) {
        view.appendChatLog(line.substring(8), "white", true, "WHISPER");
      } else if(line.startsWith("PRIVATEMESSAGE ")) {
        view.appendChatLog(line.substring(15), "black", true, "PRIVATEMESSAGE");
      } else if(line.startsWith("FILE ")) {
        view.appendChatLog(line.substring(5), "black", true, "FILE");
      } else if(line.startsWith("FAILEDFILETRANSFER ")) {
        view.appendChatLog(line.substring(19), "red", false, "FAILEDFILETRANSFER");
      } else if(line.startsWith("FILEDATA ")) {
        File file = view.showSaveDialog(line.substring(line.indexOf(":") + 1));
        long fileSize = Long.parseLong(line.substring(9, line.indexOf(":")));
        model.saveFile(file, fileSize);
      } else if (line.startsWith("REQUESTEDNEWROOM ")) {
        try {
          MultiChatModel newModel = model.switchPorts(line.substring(17));
          model.sendText("/quit");
          view.appendChatLog("Successfully left.", "red", false, "REQUESTEDNEWROOM");
          model = newModel;
        } catch (IOException e) {
          model.sendText("UNSUCCESSFULROOMCHANGE Cannot connect to new chat room.");
        } catch (NumberFormatException nfe) {
          model.sendText("UNSUCCESSFULROOMCHANGE Cannot find specified room number.");
        }
      }
    }
    view.dispose(); //once connection is closed, end the GUI
  }

  @Override
  public void sendTextOut(String out) {
    model.sendText(out);
  }

  @Override
  public String getClientUsername() {
    return model.getUsername();
  }

  @Override
  public void sendFile(String fileName, long fileSize, File file) throws IOException{
    model.sendFile(fileName, fileSize, file);
  }
}
