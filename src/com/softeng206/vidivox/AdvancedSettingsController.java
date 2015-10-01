package com.softeng206.vidivox;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.awt.*;
import java.awt.peer.TextFieldPeer;

/**
 * Created by jay on 1/10/15.
 */
public class AdvancedSettingsController {
    @FXML public CheckBox overlayAtLocation;
    @FXML public CheckBox overlayTrim;
    @FXML public TextField locationBox;


    public void overlayLocation(){
        if (overlayTrim.isSelected()){
            locationBox.setDisable(false);
            overlayTrim.setSelected(false);
        }
        else if (!overlayAtLocation.isSelected()){
            locationBox.setDisable(true);
        }
        else if (locationBox.isDisabled()){
            locationBox.setDisable(false);
        }

    }
    public void overlayAndTrim(){
        if (overlayAtLocation.isSelected()){
            overlayAtLocation.setSelected(false);
            locationBox.setDisable(true);
        }
    }

}
