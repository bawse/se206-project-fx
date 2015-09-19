package com.softeng206.vidivox;

import com.softeng206.vidivox.concurrency.FestivalMp3Worker;
import com.softeng206.vidivox.concurrency.FestivalPreviewWorker;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.io.File;

public class Controller {
    private FestivalPreviewWorker previewWorker;
    private FestivalMp3Worker mp3Worker;
    private FileChooser fc = new FileChooser();

    @FXML public TextArea ttsPreviewText;
    @FXML public Button ttsPreviewButton;
    @FXML public Button ttsCancelPreviewButton;

    private boolean checkTextSuitability() {
        if (ttsPreviewText.getText().split(" ").length > 35) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please use less than 35 words in your text. Long phrases tend to sound unnatural.");
            alert.show();
            return false;
        }

        return true;
    }

    public void ttsPreview() {
        if (!checkTextSuitability()) {
            return;
        }

        previewWorker = new FestivalPreviewWorker(ttsPreviewText.getText());
        previewWorker.setOnFinished(
                WorkerStateEvent -> {
                    ttsCancelPreviewButton.setDisable(true);
                    ttsPreviewButton.setDisable(false);
                }
        );
        ttsPreviewButton.setDisable(true);
        ttsCancelPreviewButton.setDisable(false);

        previewWorker.runTask();
    }

    public void ttsPreviewCancel() {
        if (previewWorker != null) {
            previewWorker.cancel(true);
        }
    }

    public void ttsMp3() {
        if (!checkTextSuitability()) {
            return;
        }

        File target = fc.showSaveDialog(ttsPreviewButton.getScene().getWindow());

        if (target == null) {
            return;
        }

        mp3Worker = new FestivalMp3Worker(ttsPreviewText.getText(), target);
        mp3Worker.setOnSucceeded(
                event -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Done!");
                    alert.setHeaderText(null);
                    alert.setContentText("Your MP3 was saved successfully.");
                    alert.show();
                }
        );

        mp3Worker.setOnFailed(
                event -> {
                    System.out.println(event.toString());
                }
        );

        mp3Worker.runTask();
    }
}
