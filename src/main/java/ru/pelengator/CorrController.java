package ru.pelengator;


import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pelengator.model.TwoPointCorrection;
import ru.pelengator.service.SaveFilesService;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static ru.pelengator.Controller.setCorrection;


public class CorrController implements Initializable {
    /**
     * Логгер.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CorrController.class);

    @FXML
    private Button but_load;

    @FXML
    private ProgressIndicator pb_indicator;


    /**
     * Ссылка на главный контроллер.
     */
    private Controller mainController;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        LOG.debug("Init fields controller");

    }


    public void initController(Controller controller) {
        LOG.debug("Init controller");

        mainController = controller;


    }

    @FXML
    public void loadFile(ActionEvent event) {

        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Загрузить файл коррекции");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CORR DATA", "*.corr"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File file = fileChooser.showOpenDialog(stage);

        if (file == null) {
            exit(stage);
        } else {
            String path = file.getAbsolutePath();

            if (TwoPointCorrection.isCorrectionFileAlive(path))//если файл подходит, то загружаем
            {
                TwoPointCorrection correction = TwoPointCorrection.loadData(path);//если ок, то грузим данные
                setCorrection(correction);
                mainController.getParams().setCorrectionFilePath(path);
                stage.close();
            } else {//иначе возврат к окну выбора
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");

                    alert.setHeaderText(null);
                    alert.setContentText("Файл не содержет требуемой информации." +
                            "\nВыберите другой файл или произведите новую корректировку.");

                    Optional<ButtonType> buttonType = alert.showAndWait();

                });

            }

        }


        //  pb_status.visibleProperty().bind(saveFilesService.runningProperty());
        //  pb_status.progressProperty().bind(saveFilesService.progressProperty());
        //  lab_status.textProperty().bind(saveFilesService.messageProperty());


    }

    private void exit(Stage stage) {
        mainController.getBt_correction().fire();
        stage.close();
    }

}
