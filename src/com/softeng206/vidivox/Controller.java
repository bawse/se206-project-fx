package com.softeng206.vidivox;

import com.softeng206.vidivox.concurrency.FestivalPreviewWorker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class Controller {
    private FestivalPreviewWorker previewWorker;

    @FXML public TextArea ttsPreviewText;
    @FXML public Button ttsPreviewButton;
    @FXML public Button ttsCancelPreviewButton;

    public void ttsPreview() {
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
}
