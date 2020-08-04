package client.view.javafxView;

import client.view.MultiChatView;
import javafx.application.Application;

public class MockController {


    public static void main(String[] args) {
        new Thread(() -> Application.launch(FXEntryPoint.class)).start();
        var view = FXEntryPoint.getCurrentInstance();
        view.display();
    }
}
