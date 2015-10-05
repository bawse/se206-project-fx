package com.softeng206.vidivox;

import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;


public class Vidivox extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("Gui.fxml"));

        Controller controller = fxmlLoader.getController();
        //Use the following command to change to a different JavaFX theme. Still needs to be tested on UG4.
        //System.setProperty( "javafx.userAgentStylesheetUrl", "CASPIAN" );
        primaryStage.setTitle("Vidivox - hcho928 / jpan889");
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(450);
        primaryStage.setScene(new Scene(root, 800, 650));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
