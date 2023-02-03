package ru.pelengator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pelengator.model.TwoPointCorrection;
import ru.pelengator.service.MeasService;
import ru.pelengator.service.SaveFilesService;

import java.io.File;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.*;

import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static ru.pelengator.API.utils.Utils.clearPane;
import static ru.pelengator.API.utils.Utils.parseDoubleText;

public class MeasController implements Initializable {

    /**
     * Логгер.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MeasController.class);

    /**
     * Лейбл размера окна.
     */
    @FXML
    private Label lbFullWindow;

    /**
     * Среднее арифметическое сигнала.
     */
    @FXML
    private TextField tfArifmeticMean;
    /**
     * Среднее квадратическое сигнала.
     */
    @FXML
    private TextField tfQuadraticMean;
    /**
     * СКО сигнала.
     */
    @FXML
    private TextField tfSKO;
    /**
     * Вольтовая чувствительность.
     */
    @FXML
    private TextField tfVw;
    /**
     * Пороговая облученность.
     */
    @FXML
    private TextField tfExposure;

    @FXML
    private TextField tfArifmeticMeanPersent;//Процент брака по параметру
    @FXML
    private TextField tfQuadraticMeanPersent;
    @FXML
    private TextField tfSKOPersent;
    @FXML
    private TextField tfVwPersent;
    @FXML
    private TextField tfExposurePersent;

    @FXML
    private Label lbArifmeticMean;//Результирующие значения
    @FXML
    private Label lbArifmeticMean1;//Результирующие значения
    @FXML
    private Label lbArifmeticMean2;//Результирующие значения
    @FXML
    private Label lbQuadraticMean;
    @FXML
    private Label lbQuadraticMean1;
    @FXML
    private Label lbQuadraticMean2;
    @FXML
    private Label lbSKO;
    @FXML
    private Label lbSKO1;
    @FXML
    private Label lbSKO2;
    @FXML
    private Label lbVw;
    @FXML
    private Label lbVw1;
    @FXML
    private Label lbVw2;
    @FXML
    private Label lbExposure;
    @FXML
    private Label lbExposure1;
    @FXML
    private Label lbExposure2;

    @FXML
    private ColorPicker cpArifmeticMeanH;//Колорпикер для битых пикселей
    @FXML
    private ColorPicker cpQuadraticMeanH;
    @FXML
    private ColorPicker cpSKOH;
    @FXML
    private ColorPicker cpVwH;
    @FXML
    private ColorPicker cpExposureH;

    @FXML
    private ColorPicker cpArifmeticMeanL;//Колорпикер для битых пикселей
    @FXML
    private ColorPicker cpQuadraticMeanL;
    @FXML
    private ColorPicker cpSKOL;
    @FXML
    private ColorPicker cpVwL;
    @FXML
    private ColorPicker cpExposureL;


    @FXML
    private Label lab_ArifmeticMean;//функции
    @FXML
    private Label lab_QuadraticMean;
    @FXML
    private Label lab_SKO;
    @FXML
    private Label lab_Vw;
    @FXML
    private Label lab_Exposure;

    /**
     * Панель вывода гистограмм и кадров.
     */
    @FXML
    private VBox scrlPaneSa;
    /**
     * Панель вывода гистограмм и кадров.
     */
    @FXML
    private VBox scrlPaneSq;

    @FXML
    private VBox scrlPaneSigma;
    @FXML
    private VBox scrlPaneSu;
    @FXML
    private VBox scrlPaneEp;

    @FXML
    private Button btnStart;
    @FXML
    private Button btnReset;
    @FXML
    private Button btnSaveExp;

    @FXML
    private Button btnSaveProt;

    @FXML
    private Button btnCorr;


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
     * Прогрессиндикатор.
     */
    @FXML
    private ProgressIndicator pIndicatorSaveExp;
    @FXML
    private ProgressIndicator pIndicatorSavePDF;

    @FXML
    private ProgressIndicator prIndicatorCorr;
    @FXML
    private ComboBox cbExpOptions;
    /**
     * Сервис расчетов.
     */
    private MeasService service;

    private ObservableList<TextField> persentFieldOptions = FXCollections.observableArrayList();

    private ObservableList<TextField> expdOptions = FXCollections.observableArrayList();


    /**
     * Ссылка на контроллер.
     */

    private Controller controller;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOG.trace("Init");

        String str = "Вольтовая чувствительность, В\u00B7Вт\u00AF \u00B9";
        lab_Vw.setText(str);

        str = "Пороговая облученность, Вт\u00B7см\u00AF \u00B2";
        lab_Exposure.setText(str);


        /**
         * Кнопка страрт
         */
        btnStart.setOnAction(event -> {
            LOG.trace("Start Service");
            controller.getSelExp().setParams(controller.getParams());
            service.restart();//Стартуем сервис
        });

        btnReset.setOnAction(event -> {
            LOG.trace("Stop service");
            if (service.getState() == Worker.State.RUNNING) {
                service.cancel();
            }
            initService();
            Platform.runLater(() -> {
                setMinus();
                clearPane(scrlPaneSa);
                clearPane(scrlPaneSq);
                clearPane(scrlPaneSigma);
                clearPane(scrlPaneSu);
                clearPane(scrlPaneEp);
                setAllPersent20();
            });

        });

        btnSaveExp.setOnAction(event -> {
            LOG.debug("Save exp pressed");
        });

        btnSaveProt.setOnAction(event -> {
            LOG.debug("Save file pressed");
            saveAllInFile(event);
        });

        setLostFocusAction();

        cbExpOptions.setPromptText("Выберите испытание");

    }

    /**
     * Сброс процентовки на 20%
     */
    private void setAllPersent20() {
        controller.getParams().setArifmeticMeanPersent(20.0);
        tfArifmeticMeanPersent.setText("20.0");
        controller.getParams().setQuadraticMeanPersent(20.0);
        tfQuadraticMeanPersent.setText("20.0");
        controller.getParams().setSKOPersent(20.0);
        tfSKOPersent.setText("20.0");
        controller.getParams().setVwPersent(20.0);
        tfVwPersent.setText("20.0");
        controller.getParams().setExposurePersent(20.0);
        tfExposurePersent.setText("20.0");
    }

    /**
     * Сервис сохранения в файл.
     */
    private SaveFilesService saveFilesService;

    /**
     * Сохранение в файл.
     *
     * @param event эвент для внутрянки.
     */
    private void saveAllInFile(ActionEvent event) {

        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить протокол измерений характеристик ФПУ");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File pdfFile = fileChooser.showSaveDialog(stage);


        if (saveFilesService == null) {
            saveFilesService = new SaveFilesService(this, pdfFile);
        }

        pb_status.visibleProperty().bind(saveFilesService.runningProperty());
        pb_status.progressProperty().bind(saveFilesService.progressProperty());
        lab_status.textProperty().bind(saveFilesService.messageProperty());

        if (saveFilesService.getState() == Worker.State.RUNNING) {
            saveFilesService.cancel();
        } else {
            saveFilesService.restart();
        }
        saveFilesService = null;
    }

    /**
     * Отработка колорпикера.
     */
    private void addCBListeners() {
        cpArifmeticMeanL.valueProperty().addListener((observable, oldValue, newValue) ->
                controller.getParams().setPersentColorArifmL(newValue.toString()));
        cpQuadraticMeanL.valueProperty().addListener((observable, oldValue, newValue) ->
                controller.getParams().setPersentColorQuadraticL(newValue.toString()));
        cpSKOL.valueProperty().addListener((observable, oldValue, newValue) ->
                controller.getParams().setPersentColorSKOL(newValue.toString()));
        cpVwL.valueProperty().addListener((observable, oldValue, newValue) ->
                controller.getParams().setPersentColorVwL(newValue.toString()));
        cpExposureL.valueProperty().addListener((observable, oldValue, newValue) ->
                controller.getParams().setPersentColorExposureL(newValue.toString()));

        cpArifmeticMeanH.valueProperty().addListener((observable, oldValue, newValue) ->
                controller.getParams().setPersentColorArifmH(newValue.toString()));
        cpQuadraticMeanH.valueProperty().addListener((observable, oldValue, newValue) ->
                controller.getParams().setPersentColorQuadraticH(newValue.toString()));
        cpSKOH.valueProperty().addListener((observable, oldValue, newValue) ->
                controller.getParams().setPersentColorSKOH(newValue.toString()));
        cpVwH.valueProperty().addListener((observable, oldValue, newValue) ->
                controller.getParams().setPersentColorVwH(newValue.toString()));
        cpExposureH.valueProperty().addListener((observable, oldValue, newValue) ->
                controller.getParams().setPersentColorExposureH(newValue.toString()));

    }


    /**
     * Инициализация сервиса.
     */
    public void initService() {
        service = new MeasService(this);
        controller.getPb_exp().visibleProperty().bind(pb_status.visibleProperty());
        controller.getPb_exp().progressProperty().bind(pb_status.progressProperty());
        controller.getLab_exp_status().textProperty().bind(lab_status.textProperty());

        pb_status.visibleProperty().bind(service.runningProperty());
        pb_status.progressProperty().bind(service.progressProperty());
        lab_status.textProperty().bind(service.messageProperty());
    }

    /**
     * Инициализация контроллера.
     *
     * @param controller
     */
    public void initController(Controller controller) {
        LOG.trace("Init controller");
        this.controller = controller;
        initService();
        lbFullWindow.setText(controller.getParams().getDimention());

        tfArifmeticMeanPersent.setText(String.format(Locale.CANADA, "%.2f",
                controller.getParams().getArifmeticMeanPersent()));
        tfQuadraticMeanPersent.setText(String.format(Locale.CANADA, "%.2f",
                controller.getParams().getQuadraticMeanPersent()));
        tfSKOPersent.setText(String.format(Locale.CANADA, "%.2f",
                controller.getParams().getSKOPersent()));
        tfVwPersent.setText(String.format(Locale.CANADA, "%.2f",
                controller.getParams().getVwPersent()));
        tfExposurePersent.setText(String.format(Locale.CANADA, "%.2f",
                controller.getParams().getExposurePersent()));

        setMinus();

        cpArifmeticMeanL.valueProperty().setValue(
                Color.web(controller.getParams().getPersentColorArifmL()));
        cpQuadraticMeanL.valueProperty().setValue(
                Color.web(controller.getParams().getPersentColorQuadraticL()));
        cpSKOL.valueProperty().setValue(
                Color.web(controller.getParams().getPersentColorSKOL()));
        cpVwL.valueProperty().setValue(
                Color.web(controller.getParams().getPersentColorVwL()));
        cpExposureL.valueProperty().setValue(
                Color.web(controller.getParams().getPersentColorExposureL()));

        cpArifmeticMeanH.valueProperty().setValue(
                Color.web(controller.getParams().getPersentColorArifmH()));
        cpQuadraticMeanH.valueProperty().setValue(
                Color.web(controller.getParams().getPersentColorQuadraticH()));
        cpSKOH.valueProperty().setValue(
                Color.web(controller.getParams().getPersentColorSKOH()));
        cpVwH.valueProperty().setValue(
                Color.web(controller.getParams().getPersentColorVwH()));
        cpExposureH.valueProperty().setValue(
                Color.web(controller.getParams().getPersentColorExposureH()));

        addCBListeners();

        btnStart.fire();
    }

    /**
     * Установка минусиков.
     */
    private void setMinus() {

        tfArifmeticMean.setText("--");
        tfQuadraticMean.setText("--");
        tfSKO.setText("--");
        tfVw.setText("--");
        tfExposure.setText("--");

        ArrayList<Label> labels = new ArrayList<>(Arrays.asList(lbArifmeticMean, lbArifmeticMean1, lbArifmeticMean2, lbQuadraticMean, lbQuadraticMean1, lbQuadraticMean2, lbSKO,
                lbSKO1, lbSKO2, lbVw, lbVw1, lbVw2, lbExposure, lbExposure1, lbExposure2));
        for (Label l : labels) {
            l.setText("--");
        }
    }

    public Controller getController() {
        return controller;
    }

    //Отработка нажатий в полях
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @FXML
    public void setArifmeticMeanPersent(ActionEvent event) {
        double d = parseDoubleText(event);
        controller.getParams().setArifmeticMeanPersent(d);

        startServiceOnTap(event);
    }

    @FXML
    public void setQuadraticMeanPersent(ActionEvent event) {
        double d = parseDoubleText(event);
        controller.getParams().setQuadraticMeanPersent(d);
        startServiceOnTap(event);
    }

    @FXML
    public void setSKOPersent(ActionEvent event) {
        double d = parseDoubleText(event);
        controller.getParams().setSKOPersent(d);
        startServiceOnTap(event);
    }

    @FXML
    public void setVwPersent(ActionEvent event) {
        double d = parseDoubleText(event);
        controller.getParams().setVwPersent(d);
        startServiceOnTap(event);
    }

    @FXML
    public void setExposurePersent(ActionEvent event) {
        double d = parseDoubleText(event);
        controller.getParams().setExposurePersent(d);
        startServiceOnTap(event);
    }


    @FXML
    public void saveTempDataExp(ActionEvent event) {
        //  cbExpOptions
        LOG.debug("saveTempDataExp pushed");
    }
    private double[][] arifmeticMeanValue_0;
    private double[][] arifmeticMeanValue_1;

    @FXML
    public void saveCorrData(ActionEvent event) {
        Platform.runLater(() -> {
            prIndicatorCorr.setVisible(true);
        });
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить данные корректировки");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CORR DATA", "*.corr"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        boolean b = false;

        try {
            TwoPointCorrection coreFile = new TwoPointCorrection(getArifmeticMeanValue_0(), getArifmeticMeanValue_1());
            b = coreFile.saveFile(file.getAbsolutePath());

        } catch (Exception e) {
            // ignore
        } finally {
            if (!b) {
                Button btn = (Button) source;
                Platform.runLater(() -> {
                    btn.setStyle("-fx-background-color: red");
                    prIndicatorCorr.setVisible(false);
                //    lab_status.textProperty().setValue("Ошибка при записи файла");
                //    lab_status.visibleProperty().setValue(true);
                });
            } else {
                Button btn = (Button) source;
                controller.getParams().setCorrectionFilePath(file.getAbsolutePath());
                Platform.runLater(() -> {
                    btn.setStyle("-fx-background-color: green");
                    prIndicatorCorr.setVisible(false);
                 //   lab_status.textProperty().setValue("Файл успешно записан");
                 //   lab_status.visibleProperty().setValue(true);
                });
            }


        }

    }

    public TextField getTfArifmeticMean() {
        return tfArifmeticMean;
    }

    public TextField getTfQuadraticMean() {
        return tfQuadraticMean;
    }

    public TextField getTfSKO() {
        return tfSKO;
    }

    public TextField getTfVw() {
        return tfVw;
    }

    public TextField getTfExposure() {
        return tfExposure;
    }

    public Label getLbArifmeticMean1() {
        return lbArifmeticMean1;
    }

    public Label getLbQuadraticMean1() {
        return lbQuadraticMean1;
    }

    public Label getLbSKO1() {
        return lbSKO1;
    }

    public Label getLbVw1() {
        return lbVw1;
    }

    public Label getLbExposure1() {
        return lbExposure1;
    }

    public Label getLbArifmeticMean2() {
        return lbArifmeticMean2;
    }

    public void setLbArifmeticMean2(Label lbArifmeticMean2) {
        this.lbArifmeticMean2 = lbArifmeticMean2;
    }

    public Label getLbQuadraticMean2() {
        return lbQuadraticMean2;
    }

    public void setLbQuadraticMean2(Label lbQuadraticMean2) {
        this.lbQuadraticMean2 = lbQuadraticMean2;
    }

    public Label getLbSKO2() {
        return lbSKO2;
    }

    public void setLbSKO2(Label lbSKO2) {
        this.lbSKO2 = lbSKO2;
    }

    public Label getLbVw2() {
        return lbVw2;
    }

    public void setLbVw2(Label lbVw2) {
        this.lbVw2 = lbVw2;
    }

    public Label getLbExposure2() {
        return lbExposure2;
    }

    public void setLbExposure2(Label lbExposure2) {
        this.lbExposure2 = lbExposure2;
    }

    public Label getLbArifmeticMean() {
        return lbArifmeticMean;
    }

    public Label getLbQuadraticMean() {
        return lbQuadraticMean;
    }

    public Label getLbSKO() {
        return lbSKO;
    }

    public Label getLbVw() {
        return lbVw;
    }

    public Label getLbExposure() {
        return lbExposure;
    }

    public Button getBtnStart() {
        return btnStart;
    }

    public VBox getScrlPaneEp() {
        return scrlPaneEp;
    }

    public VBox[] getScrlPaneS() {
        return new VBox[]{scrlPaneSa, scrlPaneSq, scrlPaneSigma, scrlPaneSu, scrlPaneEp};
    }

    public Button getBtnSaveProt() {
        return btnSaveProt;
    }

    public VBox getScrlPaneSa() {
        return scrlPaneSa;
    }

    public void setScrlPaneSa(VBox scrlPaneSa) {
        this.scrlPaneSa = scrlPaneSa;
    }

    public VBox getScrlPaneSq() {
        return scrlPaneSq;
    }

    public void setScrlPaneSq(VBox scrlPaneSq) {
        this.scrlPaneSq = scrlPaneSq;
    }

    public VBox getScrlPaneSigma() {
        return scrlPaneSigma;
    }

    public void setScrlPaneSigma(VBox scrlPaneSigma) {
        this.scrlPaneSigma = scrlPaneSigma;
    }

    public VBox getScrlPaneSu() {
        return scrlPaneSu;
    }

    public void setScrlPaneSu(VBox scrlPaneSu) {
        this.scrlPaneSu = scrlPaneSu;
    }

    public void setScrlPaneEp(VBox scrlPaneEp) {
        this.scrlPaneEp = scrlPaneEp;
    }

    public Label getLab_status() {
        return lab_status;
    }

    public MeasService getService() {
        return service;
    }

    public void setService(MeasService service) {
        this.service = service;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public ProgressIndicator getpIndicatorSavePDF() {
        return pIndicatorSavePDF;
    }

    private void startServiceOnTap(ActionEvent event) {
        TextField source = (TextField) event.getSource();
        source.selectAll();
        btnStart.fire();
    }

    private void setLostFocusAction() {

        KeyEvent EnterEvent = new KeyEvent(KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false);

        persentFieldOptions.addAll(
                tfArifmeticMeanPersent,
                tfQuadraticMeanPersent,
                tfSKOPersent,
                tfVwPersent,
                tfExposurePersent);

        for (TextField tx :
                persentFieldOptions) {
            tx.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (!t1) {
                    tx.fireEvent(EnterEvent);
                }
            });
        }

    }

    public double[][] getArifmeticMeanValue_0() {
        return arifmeticMeanValue_0;
    }

    public void setArifmeticMeanValue_0(double[][] arifmeticMeanValue_0) {
        this.arifmeticMeanValue_0 = arifmeticMeanValue_0;
    }

    public double[][] getArifmeticMeanValue_1() {
        return arifmeticMeanValue_1;
    }

    public void setArifmeticMeanValue_1(double[][] arifmeticMeanValue_1) {
        this.arifmeticMeanValue_1 = arifmeticMeanValue_1;
    }
}
