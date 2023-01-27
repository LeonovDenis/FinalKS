package ru.pelengator;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pelengator.API.DetectorDevice;
import ru.pelengator.API.DetectorException;
import ru.pelengator.API.driver.FT_STATUS;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static ru.pelengator.API.utils.Utils.*;

public class LoadDriverController implements Initializable {

    /**
     * Логгер.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LoadDriverController.class);

    /**
     * Кнопка загрузки прошивки
     */
    @FXML
    private Button btnLoad;
    /**
     * Окно деления посылки
     */
    @FXML
    private TextField lb_partSize;


    /**
     * Текстовое поле прогрессбара.
     */
    @FXML
    private Label lab_status;
    /**
     * Прогрессбар.
     */
    @FXML
    private ProgressBar pb_status;
    /**
     * Ссылка на главный контроллер.
     */
    private Controller mainController;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOG.debug("Init LoadDriver controller");
        addButtonOnAction();
    }



    private void addButtonOnAction() {

        btnLoad.setOnAction(event ->
        {
            LOG.debug("Btn load pressed");
            loadFile(event);
        });

    }


    public void initController(Controller controller) {
        LOG.debug("Init controller");
        mainController = controller;
        setStatus(false, "", 0.0);


    }

    private void loadFile(ActionEvent event) {

        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбрать файл для загрузки");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("BIN", "*.bin"),
                new FileChooser.ExtensionFilter("FS", "*.fs"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File loadFile = fileChooser.showOpenDialog(stage);

        if (loadFile != null) {

            try {
                byte[] tempByteData = loadFileFromDisk(loadFile.getAbsolutePath());


                sendDataArray(tempByteData, pb_status, lab_status);

            } catch (IOException e) {
                LOG.error(e.getMessage());
                throw new DetectorException(e);
            }
        }
    }

    /**
     * Отправка массива данных
     *
     * @param tempByteData сам файл
     * @param pb_status    ссылка на прогресс бар
     * @param lab_status   ссылка на текстовое поле
     */
    private void sendDataArray(byte[] tempByteData, ProgressBar pb_status, Label lab_status) {

        //стоп прослушка кадров
        if (!mainController.isPaused()) {
            mainController.stopDetector(null);
        }
        String partS = lb_partSize.textProperty().get();

        int partSize = 0;
        try {
            partSize = Integer.parseInt(partS);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

        if (partSize <= 0) {
            partSize = 1024;
        }
        int finalPartSize = partSize;


        Platform.runLater(() -> {
            lb_partSize.setText(String.valueOf(finalPartSize));
            btnLoad.setStyle(null);
            btnLoad.setText("Загрузка..");
        });


        Thread thread = new Thread(() -> {

            int allLength = tempByteData.length;
            int length = 0;
            int parts = 0;

            ByteArrayInputStream wrappedData = new ByteArrayInputStream(tempByteData);

            FT_STATUS ft_status = null;

            setStatus(true, "Старт отправки драйвера...", 0.0);

            boolean start = true;//статус отправки первого пакета
            while (wrappedData.available() > 0) {

                int size = wrappedData.available() < finalPartSize ? wrappedData.available() : finalPartSize;

                byte[] buff = new byte[size];
                int read = wrappedData.read(buff, 0, size);

                length = length + read;
                parts++;
                //  LOG.debug("Trying to send Array... {} bytes. Msg #{}", size, parts);
                ft_status = ((DetectorDevice.ChinaSource) mainController.getSelDetector().getDevice()).setID(buff, allLength, start);
                //    LOG.debug("MSG # {} sended. Status: {}", parts, ft_status);
                if (ft_status != FT_STATUS.FT_OK) {
                    setStatus(true, "Нет ответа на пакет № " + parts + ". Отправка прервана.", (1.0 * length) / allLength);
                    wrappedData.readAllBytes();
                } else {
                    setStatus(true, "Отправка пакета № " + parts, (1.0 * length) / allLength);
                }
                start = false;
            }

            //       LOG.debug("Send Array Finished. {} bytes, {} msges", length, parts);
            if (ft_status == FT_STATUS.FT_OK) {
                setStatus(true, "Отправлено " + parts + " пакетов. " + allLength + " байт.", (1.0 * length) / allLength);
            }

            FT_STATUS finalFt_status = ft_status;

            Platform.runLater(() -> {
                String txt = "";
                String style = "";
                switch (finalFt_status) {
                    case FT_OK:

                        txt = "Загружено";
                        style = "-fx-background-color: green";
                        break;
                    case FT_BUSY:
                        txt = "Ошибка";
                        style = "-fx-background-color: red";
                        break;
                }

                btnLoad.setText(txt);
                btnLoad.setStyle(style);

                if (mainController.isPaused()) {
                    mainController.stopDetector(null);
                }
            });

        });

        thread.setDaemon(true);
        thread.start();

    }

    private void setStatus(boolean isVisible, String msg, double persent) {
        Platform.runLater(() -> {
            pb_status.setVisible(isVisible);
            lab_status.setVisible(isVisible);
            pb_status.progressProperty().set(persent);
            lab_status.setText(msg);
        });
    }

}
