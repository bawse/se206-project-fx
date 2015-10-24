package com.softeng206.vidivox;

import com.softeng206.vidivox.gui.controllers.Controller;
import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;


public class Vidivox extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("gui/Gui.fxml"));

        Controller controller = fxmlLoader.getController();
        //Use the following command to change to a different JavaFX theme. Still needs to be tested on UG4.
        //System.setProperty( "javafx.userAgentStylesheetUrl", "CASPIAN" );
        primaryStage.setTitle("Vidivox Video Commentator");
        primaryStage.setMinWidth(795);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(new Scene(root, 795, 730));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
