package client.controller;

import client.model.MultiChatModel;
import client.view.MultiChatView;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * A
 */
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
    while(model.isConnectionRunning()) {
      String line = model.getSocketInput();

      if (line.startsWith("SUBMITNAME")) {
        String username = view.getName("Choose a screen name:");
        model.sendText(username);
      } else if (line.startsWith("SUBMITANOTHERNAME")) {
        String username = view.getName("Please select a different screen name:");
        model.sendText(username);
      } else if (line.startsWith("NAMEACCEPTED")) {
        view.display();
        view.setTextFieldEditable(true);
      } else if (line.startsWith("MESSAGE ")) {
        view.appendChatLog(line.substring(8), "black", true);
      } else if (line.startsWith("MESSAGEUSERJOINED ")) {
        view.appendChatLog(line.substring(18), "green", true);
      } else if (line.startsWith("MESSAGEUSERLEFT ")) {
        view.appendChatLog(line.substring(16), "red", true);
      } else if (line.startsWith("MESSAGEWELCOME ")) {
        view.appendChatLog(line.substring(15), "blue", false);
      } else if (line.startsWith("ACTIVEUSERLIST ")) {
        String[] arr = line.substring(15).split(",");
        List<String> names = Arrays.asList(arr);
        view.setActiveUsers(names);
      } else if (line.startsWith("MESSAGEHELP ")) {
        view.appendChatLog(line.substring(12), "orange", false);
      } else if (line.startsWith("ACTIVESERVERLIST ")) {
        String[] arr = line.substring(17).split(",");
        List<String> servers = Arrays.asList(arr);
        view.setActiveServers(servers);
      } else if (line.startsWith("REQUESTEDNEWROOM ")) {
        try {
          MultiChatModel newModel = model.switchPorts(line.substring(17));
          model.sendText("/quit");
          view.appendChatLog("Successfully left.", "red", false);
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
