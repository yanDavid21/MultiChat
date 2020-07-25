package client.controller;

import client.model.MultiChatModel;
import client.view.MultiChatView;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MultiChatControllerImpl implements MultiChatController, Features {
  private MultiChatModel model;
  private MultiChatView view;

  public MultiChatControllerImpl(MultiChatModel model, MultiChatView view) {
    this.model = model;
    this.view = view;
    view.giveFeatures(this);
  }

  @Override
  public void run() {
    while (model.isConnectionRunning()) {
      String line = model.getSocketInput();
      if (line.startsWith("SUBMITNAME")) {
        String username = view.getName("Please select a screen name:");
        model.sendText(username);
      } else if (line.startsWith("SUBMITANOTHERNAME")) {
        String username = view.getName("Please select another screen name:");
        model.sendText(username);
      } else if (line.startsWith("NAMEACCEPTED")) {
        view.setTextFieldEditable(true);
        view.display();
      } else if (line.startsWith("MESSAGE ")) {
        view.appendChatLog(line.substring(8) + "\n", "black", true);
      } else if (line.startsWith("MESSAGEUSERJOINED ")) {
        view.appendChatLog(line.substring(18) + "\n", "green", true);
      } else if (line.startsWith("MESSAGEUSERLEFT ")) {
        view.appendChatLog(line.substring(16) + "\n", "red", true);
      } else if (line.startsWith("MESSAGEWELCOME ")) {
        view.appendChatLog(line.substring(15) + "\n", "blue", false);
      } else if (line.startsWith("ACTIVEUSERLIST ")) {
        List<String> activeUserList = Arrays.asList(line.substring(15).split(","));
        view.setActiveUsers(activeUserList);
      } else if (line.startsWith("MESSAGEHELP ")) {
        view.appendChatLog(line.substring(12) + "\n", "orange", false);
      } else if (line.startsWith("ACTIVESERVERLIST ")) {
        List<String> activeUserList = Arrays.asList(line.substring(17).split(","));
        view.setActiveServers(activeUserList);
      } else if (line.startsWith("REQUESTEDNEWROOM ")) {
        try {
          MultiChatModel newModel = model.switchPorts(line.substring(17));
          model.sendText("/quit");
          view.appendChatLog("Successfully left.\n", "red", false);
          model = newModel;
        } catch (IOException e) {
          model.sendText("UNSUCCESSFULROOMCHANGE Cannot connect to new chat room.");
        } catch (NumberFormatException nfe) {
          model.sendText("UNSUCCESSFULROOMCHANGE Cannot find specified room number.");
        }
      }
    }
    view.dispose();
  }

  @Override
  public void sendTextOut(String out) {
    model.sendText(out);
  }
}
