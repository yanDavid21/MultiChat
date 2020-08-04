package client.view.javafxView;

import client.controller.Features;
import javafx.scene.Scene;


public class FXMLController  {
    private Features features;
    private Scene root;
    private String name;

    public void setFeatures(Features features) {
        this.features = features;
        System.out.print(this.features);
    }

}