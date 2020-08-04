package client;

import client.controller.MultiChatController;
import client.controller.MultiChatControllerImpl;
import client.model.MultiChatClientModelImpl;
import client.model.MultiChatModel;
import client.view.MultiChatView;
import client.view.javafxView.FXEntryPoint;
import client.view.swingView.MultiChatViewImpl;
import javafx.application.Application;

import java.io.IOException;

/**
 *
 */
public class MultiChatClient {

  /**
   *
   * @param args
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
    } catch (IOException ioe){
      ioe.printStackTrace();
      System.out.print("Failed to connect Server Socket and Client Socket.");
    }
  }

  //TODO: emotes, closing master, secure master server connection

  //added join, documentlistener, dialog visibility, heart and emote help, update title
}
