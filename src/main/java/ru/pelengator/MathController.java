package ru.pelengator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pelengator.service.MathService;

import java.net.URL;
import java.util.*;

import static javafx.scene.input.KeyEvent.KEY_PRESSED;

public class MathController implements Initializable {

    /**
     * Логгер.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MathController.class);


    /**
     * Количество отсчетов.
     */
    @FXML
    private TextField tfFrameCount;
    /**
     * Температура 0.
     */
    @FXML
    private TextField tfTemp0;
    /**
     * Температура 1.
     */
    @FXML
    private TextField tfTemp1;
    /**
     * Постоянная Больцмана 0.
     */
    @FXML
    private TextField tfPlank0;
    /**
     * Постоянная Больцмана 1.
     */
    @FXML
    private TextField tfPlank1;
    /**
     * Коэффициент излучения 0.
     */
    @FXML
    private TextField tfEps0;
    /**
     * Коэффициент излучения 1.
     */
    @FXML
    private TextField tfEps1;
    /**
     * Площадь отверстия диафрагмы АЧТ 0.
     */
    @FXML
    private TextField tfAreaACHT0;
    /**
     * Площадь отверстия диафрагмы АЧТ 1.
     */
    @FXML
    private TextField tfAreaACHT1;
    /**
     * Расстояние между диафрагмой АЧТ и плоскостью фоточувствительного элемента испытуемого образца 0.
     */
    @FXML
    private TextField tfRasst0;
    /**
     * Расстояние между диафрагмой АЧТ и плоскостью фоточувствительного элемента испытуемого образца 1.
     */
    @FXML
    private TextField tfRasst1;
    /**
     * Коэффициент поправки 0.
     */
    @FXML
    private TextField tfBetta0;
    /**
     * Коэффициент поправки 1.
     */
    @FXML
    private TextField tfBetta1;
    /**
     * Коэффициент поправки 0.
     */
    @FXML
    private TextField tfFi0;
    /**
     * Коэффициент поправки 1.
     */
    @FXML
    private TextField tfFi1;
    /**
     * Эффективная фоточувствительная площадь испытуемого образца 0.
     */
    @FXML
    private TextField tfAreaFPU0;
    /**
     * Эффективная фоточувствительная площадь испытуемого образца 1.
     */
    @FXML
    private TextField tfAreaFPU1;
    /**
     * Нижняя граница спектра пропускания.
     */
    @FXML
    private TextField tfMinLyambda;
    /**
     * Верхняя граница спектра пропускания.
     */
    @FXML
    private TextField tfMaxLyambda;
    /**
     * Действующее значение потока излучения 0.
     */
    @FXML
    private Label lab_potok0;
    /**
     * Действующее значение потока излучения 1.
     */
    @FXML
    private Label lab_potok1;
    /**
     * Облученность 0
     */
    @FXML
    private Label lab_exposure0;
    /**
     * Облученность 1
     */
    @FXML
    private Label lab_exposure1;

    /**
     * Итоговый поток излучения.
     */
    @FXML
    private Label lab_potok;
    /**
     * Итоговая облученность.
     */
    @FXML
    private Label lab_exposure;

    /**
     * Кнопка старта эксперимента.
     */
    @FXML
    private Button btnStart;

    /**
     * Кнопка сброса эксперимента
     */
    @FXML
    private Button btnReset;
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

    //размерности
    @FXML
    private Label ib_N;
    @FXML
    private Label ib_T;
    @FXML
    private Label ib_Sig;
    @FXML
    private Label ib_Eps;
    @FXML
    private Label ib_D;
    @FXML
    private Label ib_L;
    @FXML
    private Label ib_Betta;
    @FXML
    private Label ib_Fi;
    @FXML
    private Label ib_S;
    @FXML
    private Label ib_Fe;
    @FXML
    private Label ib_Ee;
    @FXML
    private Label ib_Fe1;
    @FXML
    private Label ib_Ee1;
    @FXML
    private Label ib_MinL;
    @FXML
    private Label ib_MaxL;
    ////размерности

    /**
     * Сервис расчета потока.
     */
    private MathService service;
    /**
     * Ссылка на главный контроллер.
     */
    private Controller mainController;

    private ObservableList<TextField> fieldOptions = FXCollections.observableArrayList();

    private boolean isFieldsValid = false;

    private volatile ArrayList<String> values = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        LOG.debug("Init potok controller");

        fillOptions();
        fillDimensions();
        addButtonOnAction();
        setLostFocusAction();
        disableFields();
    }

    /**
     * Блокировка полей
     */
    private void disableFields() {

        fieldOptions.get(3).setDisable(true);
        fieldOptions.get(4).setDisable(true);

        fieldOptions.get(8).setDisable(true);
        fieldOptions.get(6).setDisable(true);

        fieldOptions.get(10).setDisable(true);
        fieldOptions.get(12).setDisable(true);

        fieldOptions.get(15).setDisable(true);
        fieldOptions.get(16).setDisable(true);
        fieldOptions.get(17).setDisable(true);
        fieldOptions.get(18).setDisable(true);
    }

    /**
     * Создание списка полей
     */
    private void fillOptions() {
        fieldOptions.addAll(tfFrameCount,//0
                tfTemp0, tfTemp1,//1 2
                tfPlank0, tfPlank1,// 3 4
                tfEps0, tfEps1,// 5 6
                tfAreaACHT0, tfAreaACHT1,// 7 8
                tfRasst0, tfRasst1, //9 10
                tfBetta0, tfBetta1, // 11 12
                tfMinLyambda, // 13
                tfMaxLyambda, // 14
                tfFi0, tfFi1, //15 16
                tfAreaFPU0, tfAreaFPU1); //17 18
    }

    /**
     * Проверка значения. Подкрашивание поля
     *
     * @param uzel поле
     * @return true в случае валидного значения
     */
    private boolean proverkaZnacheniy(TextField uzel) {
        boolean isValid = false;
        String text = uzel.getText().trim().toUpperCase();
        String replacedText = text.trim().replace(",", ".");
        if (!text.equals(replacedText)) {
            uzel.setText(replacedText);
            text = replacedText;
        }
        try {
            if (uzel.getId().equals("tfFrameCount")) {
                int value = Integer.parseInt(text);
                if (value > 1) {
                    isValid = true;
                }
            } else {
                double value = Double.parseDouble(text);
                if (value > 0) {
                    isValid = true;
                }
            }

        } catch (Exception e) {
            LOG.error(e.getMessage());
        } finally {

            if (isValid) {
                String tempText = uzel.getText().toUpperCase();
                uzel.setText(tempText);
                uzel.setStyle("-fx-padding: 2;" +
                        "-fx-border-color: green ;-fx-border-width: 0.5;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-color: lightGreen;" +
                        "-fx-border-style: solid inside");
            } else {
                uzel.setStyle("-fx-padding: 2;" +
                        "-fx-border-color: red ;-fx-border-width: 0.5;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-color: lightRed;" +
                        "-fx-border-style: solid inside");
            }

        }
        return isValid;
    }


    /**
     * Описание кнопок
     */
    private void addButtonOnAction() {


        /**
         * Кнопка страрт.
         */
        btnStart.setOnAction(event -> {

            LOG.trace("Btn start pressed");

            if (service.getState() == Worker.State.RUNNING) {
                service.cancel();
                return;
            }

            isFieldsValid = checkingFields();
            if (isFieldsValid) {
                service.restart(); //Стартуем сервис
            }

        });

        btnReset.setOnAction(event -> {
            LOG.trace("Btn reset pressed");
            if (service.getState() == Worker.State.RUNNING) {
                service.cancel();
            }
            initService();
            autoConfig(false);
        });

    }

    private void autoConfig(boolean b) {
        fieldOptions.get(1).setText(b ? "300.0" : "25.0");
        fieldOptions.get(2).setText(b ? "300.0" : "50.0");
        fieldOptions.get(5).setText(b ? "1.0" : "0.95");
        fieldOptions.get(6).setText(b ? "1.0" : "0.95");
        fieldOptions.get(7).setText(b ? "0.390" : "56.0");
        fieldOptions.get(8).setText(b ? "0.448" : "56.0");
        fieldOptions.get(9).setText(b ? "1857.2" : "470.0");
        fieldOptions.get(10).setText(b ? "1857.2" : "470.0");
        fieldOptions.get(11).setText(b ? "1.0" : "1.0");
        fieldOptions.get(12).setText(b ? "1.0" : "1.0");
        fieldOptions.get(13).setText(b ? "1.0" : "3.45");
        fieldOptions.get(14).setText(b ? "1.0" : "5.35");
        fieldOptions.get(15).setText(b ? "0.1136" : "0.0197");
        fieldOptions.get(16).setText(b ? "0.1136" : "0.0318");
        fieldOptions.get(17).setText(b ? "900" : "900");
        fieldOptions.get(18).setText(b ? "900" : "900");
        resetItogFields();
    }


    public void saveValuesToParams() {
        int i = 0;
        if (isFieldsValid) {
            mainController.getParams().setCountFrames(Integer.parseInt(values.get(i++)));

            mainController.getParams().setTemp0(Double.parseDouble(values.get(i++)));
            mainController.getParams().setTemp1(Double.parseDouble(values.get(i++)));

            mainController.getParams().setPlank0(Double.parseDouble(values.get(i++)));
            mainController.getParams().setPlank1(Double.parseDouble(values.get(i++)));

            mainController.getParams().setEpsilin0(Double.parseDouble(values.get(i++)));
            mainController.getParams().setEpsilin1(Double.parseDouble(values.get(i++)));

            mainController.getParams().setAreaACHT0(Double.parseDouble(values.get(i++)));
            mainController.getParams().setAreaACHT1(Double.parseDouble(values.get(i++)));

            mainController.getParams().setRasstACHTfpu0(Double.parseDouble(values.get(i++)));
            mainController.getParams().setRasstACHTfpu1(Double.parseDouble(values.get(i++)));

            mainController.getParams().setBetta0(Double.parseDouble(values.get(i++)));
            mainController.getParams().setBetta1(Double.parseDouble(values.get(i++)));

            mainController.getParams().setLyambdaMin(Double.parseDouble(values.get(i++)));
            mainController.getParams().setLyambdaMax(Double.parseDouble(values.get(i++)));

            mainController.getParams().setFi0(Double.parseDouble(values.get(i++)));
            mainController.getParams().setFi1(Double.parseDouble(values.get(i++)));

            mainController.getParams().setAreaFPU0(Double.parseDouble(values.get(i++)));
            mainController.getParams().setAreaFPU1(Double.parseDouble(values.get(i++)));
        }
    }

    private void resetItogFields() {
        Platform.runLater(() -> {
            lab_potok0.setText("--");
            lab_potok1.setText("--");
            lab_potok.setText("--");
            lab_exposure.setText("--");
            lab_exposure0.setText("--");
            lab_exposure1.setText("--");
        });
    }

    /**
     * Заполнение полей размерностей
     */
    private void fillDimensions() {
        ib_N.setText("кадр");
        ib_T.setText("\u2103");
        ib_Sig.setText("Вт\u00B7м\u00AF \u00B2\u00B7К\u00AF \u2074");
        ib_Eps.setText("отн. ед.");
        ib_D.setText("мм");
        ib_L.setText("мм");
        ib_Betta.setText("отн. ед.");
        ib_Fi.setText("отн. ед.");
        ib_S.setText("мкм\u00B2");
        ib_MinL.setText("мкм");
        ib_MaxL.setText("мкм");
        ib_Fe.setText("Вт");
        ib_Ee.setText("Вт\u00B7см\u00AF \u00B2");
        ib_Fe1.setText("Вт");
        ib_Ee1.setText("Вт\u00B7см\u00AF \u00B2");
    }


    /**
     * Инициализация сервиса.
     */
    public void initService() {
        service = new MathService(this);

        pb_status.visibleProperty().bind(service.runningProperty());
        pb_status.progressProperty().bind(service.progressProperty());
        lab_status.textProperty().bind(service.messageProperty());

        for (TextField field : fieldOptions) {
            field.setStyle(null);//todo под вопросом
        }

    }

    public void initController(Controller controller) {
        LOG.debug("Init controller");

        mainController = controller;
        initService();
        fillToolTips();

        tfEps1.textProperty().bindBidirectional(tfEps0.textProperty());
        tfAreaFPU1.textProperty().bindBidirectional(tfAreaFPU0.textProperty());

        tfFrameCount.setText(String.valueOf(controller.getParams().getCountFrames()));
        tfTemp0.setText(String.format(Locale.CANADA, "%.1f", controller.getParams().getTemp0()).toUpperCase());
        tfTemp1.setText(String.format(Locale.CANADA, "%.1f", controller.getParams().getTemp1()).toUpperCase());

        tfAreaACHT0.setText(String.format(Locale.CANADA, "%.1f", controller.getParams().getAreaACHT0()).toUpperCase());
        tfAreaACHT1.setText(String.format(Locale.CANADA, "%.1f", controller.getParams().getAreaACHT1()).toUpperCase());
        tfAreaFPU0.setText(String.format(Locale.CANADA, "%.0f", controller.getParams().getAreaFPU0()).toUpperCase());
        tfAreaFPU1.setText(String.format(Locale.CANADA, "%.0f", controller.getParams().getAreaFPU1()).toUpperCase());
        tfRasst0.setText(String.format(Locale.CANADA, "%.0f", controller.getParams().getRasstACHTfpu0()).toUpperCase());
        tfRasst1.setText(String.format(Locale.CANADA, "%.0f", controller.getParams().getRasstACHTfpu1()).toUpperCase());
        tfEps0.setText(String.format(Locale.CANADA, "%.2f", controller.getParams().getEpsilin0()).toUpperCase());
        tfEps1.setText(String.format(Locale.CANADA, "%.2f", controller.getParams().getEpsilin1()).toUpperCase());
        tfPlank0.setText(String.format(Locale.CANADA, "%.2e", controller.getParams().getPlank0()).toUpperCase());
        tfPlank1.setText(String.format(Locale.CANADA, "%.2e", controller.getParams().getPlank1()).toUpperCase());
        tfBetta0.setText(String.format(Locale.CANADA, "%.2f", controller.getParams().getBetta0()).toUpperCase());
        tfBetta1.setText(String.format(Locale.CANADA, "%.2f", controller.getParams().getBetta1()).toUpperCase());

        tfFi0.setText(String.format(Locale.CANADA, "%.4f", controller.getParams().getFi0()).toUpperCase());
        tfFi1.setText(String.format(Locale.CANADA, "%.4f", controller.getParams().getFi1()).toUpperCase());

        tfMinLyambda.setText(String.format(Locale.CANADA, "%.2f", controller.getParams().getLyambdaMin()).toUpperCase());
        tfMaxLyambda.setText(String.format(Locale.CANADA, "%.2f", controller.getParams().getLyambdaMax()).toUpperCase());

        btnStart.fire();
    }

    /**
     * Заполнение подсказок
     */
    private void fillToolTips() {
        int i = 0;
        fieldOptions.get(i++).setTooltip(new Tooltip("Количество отсчетов (кадров) в эксперименте"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Температура источника излучения"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Температура источника излучения"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Постоянная Стефана-Больцмана"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Постоянная Стефана-Больцмана"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Коэффициент черноты"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Коэффициент черноты"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Диаметр диафрагмы источника излучения"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Диаметр диафрагмы источника излучения"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Расстояние между диафрагмой излучателя" +
                " и плоскостью фоточувствительного элемента испытуемого образца"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Расстояние между диафрагмой излучателя" +
                " и плоскостью фоточувствительного элемента испытуемого образца"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Коэффициент, учитывающий потери ИК излучения фильтрами"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Коэффициент, учитывающий потери ИК излучения фильтрами"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Нижняя граница спектра пропускания"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Верхняя граница спектра пропускания"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Коэффициент, учитывающий потери ИК излучения от полосы пропускания"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Коэффициент, учитывающий потери ИК излучения от полосы пропускания"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Площадь фоточувствительного элемента матрицы"));
        fieldOptions.get(i++).setTooltip(new Tooltip("Площадь фоточувствительного элемента матрицы"));


        lab_potok0.setTooltip(new Tooltip("Действующее значение потока излучения"));
        lab_potok1.setTooltip(new Tooltip("Действующее значение потока излучения"));
        lab_potok.setTooltip(new Tooltip("Итоговый поток излучения"));
        lab_exposure0.setTooltip(new Tooltip("Действующее значение облученности"));
        lab_exposure1.setTooltip(new Tooltip("Действующее значение облученности"));
        lab_exposure.setTooltip(new Tooltip("Итоговая облученность"));


    }

    /**
     * Проверка полей и выдача разрешения на запуск сервиса
     *
     * @return разрешение
     */
    public boolean checkingFields() {

        values.clear();

        for (TextField field : fieldOptions) {
            if (!proverkaZnacheniy(field)) {
                LOG.error("TextField error {}", field.getId());
                return false;
            } else {
                values.add(field.getText());
            }
        }
        return true;
    }


    public Controller getMainController() {
        return mainController;
    }

    /**
     * По нажатию ентера выделение текста и запуск сервиса
     *
     * @param event
     */
    @FXML
    private void startServiceOnTap(ActionEvent event) {
        TextField source = (TextField) event.getSource();
        source.selectAll();
        btnStart.fire();
    }


    private void setLostFocusAction() {

        KeyEvent EnterEvent = new KeyEvent(KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false);

        for (TextField tx :
                fieldOptions) {
            tx.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (!t1) {
                    tx.fireEvent(EnterEvent);
                }
            });
        }
    }

    public ObservableList<TextField> getFieldOptions() {
        return fieldOptions;
    }

    public Label getLab_potok0() {
        return lab_potok0;
    }

    public Label getLab_potok1() {
        return lab_potok1;
    }

    public Label getLab_exposure0() {
        return lab_exposure0;
    }

    public Label getLab_exposure1() {
        return lab_exposure1;
    }

    public Label getLab_potok() {
        return lab_potok;
    }

    public TextField getTfFi0() {
        return tfFi0;
    }

    public TextField getTfFi1() {
        return tfFi1;
    }

    public Label getLab_exposure() {
        return lab_exposure;
    }
}
