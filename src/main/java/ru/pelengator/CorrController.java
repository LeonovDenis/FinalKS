package ru.pelengator;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pelengator.model.ModernChart;
import ru.pelengator.model.TwoPointCorrection;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static ru.pelengator.API.utils.Utils.parseIntText;


public class CorrController implements Initializable {
    /**
     * Логгер.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CorrController.class);

    @FXML
    private Pane pane;
    @FXML
    private Button but_load;

    @FXML
    private ProgressIndicator pb_indicator;

    @FXML
    private TextField tf_stroka;

    @FXML
    private TextField tf_stolbec;

    @FXML
    private Label lb_pixel;
    @FXML
    private Label lb_final;

    @FXML
    private Label lb_coefic;


    /**
     * Ссылка на главный контроллер.
     */
    private Controller mainController;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        LOG.debug("Init fields controller");


        KeyEvent EnterEvent = new KeyEvent(KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false);

        tf_stolbec.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                tf_stolbec.fireEvent(EnterEvent);
            } else {
                Platform.runLater(() -> {
                    if (tf_stolbec.isFocused() && !tf_stolbec.getText().isEmpty()) {
                        tf_stolbec.selectAll();
                    }
                });
            }
        });
        tf_stroka.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                tf_stroka.fireEvent(EnterEvent);
            } else {
                Platform.runLater(() -> {
                    if (tf_stroka.isFocused() && !tf_stroka.getText().isEmpty()) {
                        tf_stroka.selectAll();
                    }
                });
            }
        });
    }


    public void initController(Controller controller) {
        LOG.debug("Init controller");

        mainController = controller;
        showChart();

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

        } else {
            String path = file.getAbsolutePath();

            if (TwoPointCorrection.isCorrectionFileAlive(path))//если файл подходит, то загружаем
            {
                TwoPointCorrection correction = TwoPointCorrection.loadData(path);//если ок, то грузим данные
                mainController.setTempCorrection(correction);
                mainController.getParams().setCorrectionFilePath(path);

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
        showChart();


    }

    private static int Y = 127;
    private static int X = 0;

    @FXML
    private void setStolbec(ActionEvent event) {
        int i = parseIntText(event, true, mainController.getSelDetector().getViewSize().width - 1);
        if (i < 0) {
            return;
        }
        X = i;
        showChart();
    }

    @FXML
    private void setStroka(ActionEvent event) {
        int i = parseIntText(event, true, mainController.getSelDetector().getViewSize().height - 1);
        if (i < 0) {
            return;
        }
        Y = mainController.getSelDetector().getViewSize().height - 1-i;
        showChart();
    }

    private void showChart() {

        TwoPointCorrection tempCorrection = mainController.getTempCorrection();

        if (tempCorrection == null) {
            return;
        }

        StackPane stackPane = new ModernChart().startView(
                "Pix [" + X+ "][" + (mainController.getSelDetector().getViewSize().height - 1-Y) + "]",
                "Temperature, oC", "Signal, мВ",
                (int) tempCorrection.getTempS()[0], (int) tempCorrection.getTempS()[1],
               ///////////////////////////////////////////////////////////////
                tempCorrection.getTempS(),
                tempCorrection.getTempSValues(),
                tempCorrection.getMedianAB(),
                tempCorrection.getRealAB(X, Y),
                tempCorrection.getCorrAB(X, Y));


        Platform.runLater(() -> {
            if (pane.getChildren().size() > 0) {
                pane.getChildren().clear();
            }
            stackPane.prefHeightProperty().bind(pane.prefHeightProperty());
            stackPane.prefWidthProperty().bind(pane.prefWidthProperty());
            pane.getChildren().add(stackPane);

            String txt = String.format("%.2f*X + %.2f", tempCorrection.getMedianAB()[0], tempCorrection.getMedianAB()[1]);
            lb_final.setText(txt);
            txt = String.format("%.2f*X + %.2f", tempCorrection.getRealAB(X, Y)[0], tempCorrection.getRealAB(X, Y)[1]);
            lb_pixel.setText(txt);

            txt = String.format("%.2f*X + %.2f", tempCorrection.getCorrAB(X, Y)[0], tempCorrection.getCorrAB(X, Y)[1]);
            lb_coefic.setText(txt);

        });

    }

    private void exit(Stage stage) {
        stage.close();
    }

}
