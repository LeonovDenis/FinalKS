package ru.pelengator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.management.MonitorInfo;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import org.decimal4j.util.DoubleRounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pelengator.API.*;
import ru.pelengator.API.devises.china.ChinaDriver;
import ru.pelengator.API.devises.china.ChinaDriverEthernet;
import ru.pelengator.API.driver.FT_STATUS;
import ru.pelengator.API.transformer.MyChinaRgbImageTransformer;
import ru.pelengator.model.*;
import ru.pelengator.service.DataService;
import ru.pelengator.service.TimeChartService;


import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static ru.pelengator.API.utils.Utils.*;
import static ru.pelengator.API.driver.ethernet.NetUtils.findInterfaces;

public class Controller implements Initializable, DetectorDiscoveryListener {
    /**
     * Логгер.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

    /**
     * Чекбокс на центральную зону.
     */
    @FXML
    private CheckBox cbKvadrat;
    /**
     * Размер центральной зоны по ширине.
     */

    private TextField tfKvadratWidth;
    /**
     * Размер центральной зоны по высоте.
     */

    private TextField tfKvadratHeight;
    /**
     * Поле ввода диаметра
     */

    private TextField tf_diam;
    ////////////////////////////////////////////////////////////////испытания
    public MultipleAxesLineChart timechart;
    private TimeChartService median_chart_service;


    ///////////////////////////////////////////////////////////испытания

    /**
     * Скролпейн.
     */

    private ScrollPane myPane;
    @FXML
    private Pane paneExp;

    private ScrollPane myLeftPane;
    /**
     * Сценарий остановки картинки.
     */

    private Button btnStartStop;
    /**
     * Кнопка расчета потока от источника света.
     */

    private Button btnPotok;
    /**
     * Кнопка сбора кадров.
     */
    @FXML
    private Button btnGetData;

    /**
     * Кнопка температуры.
     */
    @FXML
    private Label lb_Temp;

    @FXML
    private Label lb_Temp1;

    /**
     * Кнопка таймера.
     */

    private Button btnTimer;

    /**
     * Кнопка подачи газа
     */
    @FXML
    private Button btnGaz;
    /**
     * Кнопка расчета параметров.
     */

    private Button btnParams;
    /**
     * Список подключенных детекторов.
     */
    @FXML
    private ComboBox<DetectorInfo> cbDetectorOptions;
    /**
     * Комбобокс выбора сетевого драйвера.
     */
    @FXML
    private ComboBox<NetworkInfo> cbNetworkOptions;
    /**
     * Центральное окно с картинкой.
     */
    @FXML
    private BorderPane bpDetectorPaneHolder;
    /**
     * Панель ско.
     */

    private VBox pnFlash;
    /**
     * Поле вывода картинки.
     */
    @FXML
    private SwingNode snDetectorCapturedImage;
    /**
     * Панель, обслуживающая детектор.
     */
    private DetectorPanel detectorPanel;
    /**
     * Кнопка питания.
     */
    @FXML
    private CheckBox chPower;
    /**
     * Поле инта.
     */
    @FXML
    private TextField tfInt;
    /**
     * Поле инта.
     */
    @FXML
    private Label lb_fps;
    /**
     * Поле VOS.
     */
    @FXML
    private TextField tfVOS;
    /**
     * Поле VR0.
     */
    @FXML
    private TextField tfVR0;
    /**
     * Доступные разрешения.
     */
    private ComboBox<String> cbExpOptions;
    /**
     * Доступные разрешения.
     */
    @FXML
    private ComboBox<String> cbDimOptions;
    /**
     * Доступные усиления.
     */
    @FXML
    private ComboBox<String> cbCCCOptions;
    /**
     * Задержка опроса платы.
     */
    private TextField tfSpeedPlata;
    /**
     * Поле текущего FPS.
     */
    @FXML
    private TextField tfFPS;
    /**
     * Панелька гистограммы.
     */
    @FXML
    private Pane pnGist;

    @FXML
    private StackPane spGist;
    /**
     * Среднее значение отклонения сигнала.
     */
    @FXML
    private ImageView iv_okStatus;
    @FXML
    private ImageView iv_errorStatus;


    @FXML
    private Label lbSKO;
    /**
     * Среднее значение сигнала.
     */
    @FXML
    private Label lbAverageSignal;
    /**
     * Максимальный сигнал.
     */
    @FXML
    private Label lbMax;
    /**
     * Минимальный сигнал.
     */
    @FXML
    private Label lbMin;
    /**
     * Поле ввода номера столбца
     */
    @FXML
    private TextField tf_stolbec;
    /**
     * Поле для ввода строки
     */
    @FXML
    private TextField tf_stroka;
    /**
     * Гистограмма верх.
     */
    @FXML
    private ImageView iwGist;
    /**
     * Полоса.
     */
    @FXML
    private ImageView ivPolosa;//w=600; h=20;
    /**
     * Гистограмма низ. Распределение по строкам.
     */
    @FXML
    private ImageView iwGistSKO_H;
    /**
     * Гистограмма низ. Распределение по столбцам.
     */
    @FXML
    private ImageView iwGistSKO_V;

    /**
     * Гистограмма распределения сигнала по строкам.
     */
    @FXML
    private ImageView iwGistSignal_H;
    /**
     * Гистограмма распределения сигнала по столбцам.
     */
    @FXML
    private ImageView iwGistSignal_V;
    /**
     * Прогрессбар.
     */
    @FXML
    private ProgressBar pb_exp;
    /**
     * Дежурная строка.
     */
    @FXML
    private Label lab_exp_status;
    /**
     * Режим рисования DRAW_NONE
     */
///////////////////////////////////////////////////меню////////////

    @FXML
    private Menu menu_Settings_M;
    @FXML
    private MenuItem menu_Potok;
    @FXML
    private MenuItem menu_Kalibr;
    @FXML
    private Menu menu_Driver_M;
    @FXML
    private RadioMenuItem menu_USB;
    @FXML
    private RadioMenuItem menu_Ethernet;
    @FXML
    private MenuItem menu_loadProgram;

    @FXML
    private Menu menu_Exp_M;
    @FXML
    private MenuItem menu_LoadExp;
    @FXML
    private MenuItem menu_SaveExp;

    @FXML
    private Menu menu_Order_M;
    @FXML
    private MenuItem menu_fields;

    @FXML
    private Menu menu_Window_M;
    @FXML
    private MenuItem menu_fullSize;
    @FXML
    private MenuItem menu_iconed;
    @FXML
    private MenuItem menu_close;

    @FXML
    private Menu menu_Help_M;
    @FXML
    private MenuItem menu_RE;
    @FXML
    private MenuItem menu_about;

    ///////////////////////////////////////////////////меню////////////

    private ToggleButton tb_none;
    /**
     * Режим рисования DRAW_FILL
     */

    private ToggleButton tb_fill;
    /**
     * Режим рисования DRAW_FIT
     */

    private ToggleButton tb_fit;
    /**
     * Группа ддля режимов рисования.
     */
    private ToggleGroup growModeGroup = new ToggleGroup();
    @FXML
    private ToggleGroup driverGroup = new ToggleGroup();
    /**
     * Режим зеркалирования
     */

    private ToggleButton tb_mirror;
    /**
     * Режим показа отладочной информации
     */

    private ToggleButton tb_debug;
    /**
     * Сглаживание картинки.
     */

    private ToggleButton tb_antialising;
    /**
     * Поворот изображения на угол кратный 90 градусов.
     */

    private ChoiceBox<String> cb_flip;

    private ToggleButton tb_rgb;

    private ToggleButton tb_gray;

    private ToggleButton tb_norm;

    private Label lb_online;


    private Button btnLookUp;


    private TitledPane tp_debug;


    private VBox vb_comPanel;

    private VBox vb_expPanel;

    private boolean isEthrnetWorking;


    /**
     * Пакет ресурсов.
     */
    private final Properties properties = new Properties();
    /**
     * Текущее разрешение картинки.
     */
    private volatile Dimension viewSize;
    /**
     * Формат целого числа.
     */
    private static final String DEFAULT_FORMAT = "0";
    private static final NumberFormat FORMATTER = new DecimalFormat(DEFAULT_FORMAT);
    /**
     * Флаг остановки вывода картинки.
     */
    private static boolean paused = false;
    /**
     * Масштаб оцифровки АЦП.
     */
    private static final float MASHTAB = (5000 / (float) (Math.pow(2, 14)));
    /**
     * Параметры стенда.
     */
    private static StendParams params;

    /**
     * Запись параметров при выходе.
     */
    public void save() {
        params.save();
    }

    /**
     * Картинка для гистограммы верх.
     */
    private BufferedImage grabbedImage;
    /**
     * Картинка для гистограммы низ верт.
     */
    private BufferedImage grabbedImageV;
    /**
     * Картинка для гистограммы низ гориз
     */
    private BufferedImage grabbedImageH;
    /**
     * Картинка для гистограммы сигнала столбцы.
     */
    private BufferedImage grabbedImageSignalV;
    /**
     * Картинка для гистограммы сигнала строки
     */
    private BufferedImage grabbedImageSignalH;


    /**
     * Свойства для отображения картинки гистограммы.
     */
    private final ObjectProperty<Image> gistImageProperty = new SimpleObjectProperty<Image>();
    /**
     * Свойства для отображения картинки гистограммы низ верт.
     */
    private final ObjectProperty<Image> gistImagePropertyV = new SimpleObjectProperty<Image>();
    /**
     * Свойства для отображения картинки гистограммы низ гориз.
     */
    private final ObjectProperty<Image> gistImagePropertyH = new SimpleObjectProperty<Image>();
    /**
     * Свойства для отображения картинки гистограммы столбцов.
     */
    private final ObjectProperty<Image> gistImagePropertySignalV = new SimpleObjectProperty<Image>();
    /**
     * Свойства для отображения картинки гистограммы строк.
     */
    private final ObjectProperty<Image> gistImagePropertySignalH = new SimpleObjectProperty<Image>();


    /**
     * Активный детектор.
     */
    private Detector selDetector = null;
    /**
     * Активный эксперимент.
     */
    private final ExpInfo selExp = new ExpInfo();
    /**
     * Активный интерфейс драйвера сети.
     */
    private NetworkInfo selNetworkInterface = null;
    /**
     * Флаг работы.
     */
    private boolean stopVideo = false;
    /**
     * FPS по умолчанию.
     */
    private final double FPSVideo = 25;
    /**
     * Подсказка в список детекторов.
     */
    private final String detectorListPromptText = "Выбрать";
    /**
     * Подсказка в список эксп.
     */
    private final String expListPromptText = "Нет данных";
    /**
     * Подсказка в список драйверов.
     */
    private final String networkListPromptText = "Сетевой интерфейс";
    /**
     * Список для меню детекторов.
     */
    private final ObservableList<DetectorInfo> options = FXCollections.observableArrayList();
    ;
    /**
     * Список для меню экспериментов.
     */
    private final ObservableList<ExpInfo> optionsExp = FXCollections.observableArrayList();
    /**
     * Список для меню драйвера.
     */
    private final ObservableList<NetworkInfo> optionsNetwork = FXCollections.observableArrayList();
    /**
     * Список коэф. усиления.
     */
    private final ObservableList<String> optionsCCC = FXCollections.observableArrayList("1", "3");
    /**
     * Список для допустимых экспериментов.
     */
    private final ObservableList<String> optionsExperim = FXCollections.observableArrayList("1. Время выхода на режим",
            "2. Подтверждение кадровой частоты", "3. Проверка фотоэлектрических характеристик", "4. Проверка динам. диапазона",
            "5. Проверка поддержания температуры криостатирования", "6. Время непрерывной работы");
    /**
     * Список для допустимых разрешений.
     */
    private final ObservableList<String> optionsDimension = FXCollections.observableArrayList("128*128", "92*90");
    /**
     * Список допустимых поворотов картинки.
     */
    private final ObservableList<String> optionsFlip = FXCollections.observableArrayList("Нет поворота", "Поворот: +90\u00B0", "Поворот: -90\u00B0", "Поворот: 180\u00B0");

    /**
     * Счетчик экспериментов.
     */
    private static int expCounter = 0;
    /**
     * Создание списка детекторов.
     */
    private static final AtomicBoolean isImageFrash = new AtomicBoolean(false);
    private static int detectorCounter = 0;

    private final DetectorImageTransformer imageTransformer = new MyChinaRgbImageTransformer();


    private boolean async = false;
    private boolean fpsLimited = false;

    /**
     * Инициализация всего и вся.
     *
     * @param arg0
     * @param arg1
     */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        params = new StendParams(this);

        /**
         * Установка полей в прошлое состояние.
         */
        params.loadParams("props.properties");

        setLostFocusAction();// инициализация отработки потери фокуса полей ввода данных

        Detector.addDiscoveryListener(this);//добавка в слушатели

        createNetworcOptions(); //заполнение списка сетей


        /**
         * Установка подсказки в комбобокс.
         */
        cbDetectorOptions.setPromptText(detectorListPromptText);
        /**
         * Создание списка экспериментов.
         */
        /**
         * Заполнение списка экспериментов.
         */
        ExpInfo expInfo = new ExpInfo();
        expInfo.setExpIndex(expCounter);
        expInfo.setExpName("Пустой эксперимент");
        optionsExp.add(expInfo);
        expCounter++;

        /**
         * Подключение слушателя на выбор элемента из списка.
         */
        cbNetworkOptions.getSelectionModel().selectedItemProperty().addListener((arg012, arg112, newValue) -> {
            if (newValue != null) {
                if (newValue.getName().equals("USB 3.0")) {
                    /**
                     * Регистрация драйвера детектора для USB 3.0.
                     */
                    Detector.setDriver(new ChinaDriver(params));
                    async = false;
                    isEthrnetWorking = false;
                } else {
                    /**
                     * Регистрация драйвера детектора для ethernet.
                     */
                    Detector.setDriver(new ChinaDriverEthernet(params));
                    async = false;
                    fpsLimited = true;
                    //   btnLookUp.setVisible(true);
                    isEthrnetWorking = true;
                }

                /**
                 * Передача индекса интерфейса в инициализатор.
                 */
                initializeNetwork(newValue.getIndex());
                /**
                 * Заполнение списка детекторов.
                 */
                fillDetectors();
                cbNetworkOptions.setDisable(true);
            }

            scanEthernetAndfireDetector();

        });

        changeIconStatusVisible(false, false);

        /**
         * Подключение слушателя на выбор элемента из списка детекторов.
         */
        cbDetectorOptions.getSelectionModel().selectedItemProperty().addListener((arg01, arg11, newValue) -> {

            if (newValue != null) {
                String detectorName = newValue.getDetectorName();
                LOG.trace("Detector Index: " + newValue.getDetectorIndex() + ": Detector Name: " + detectorName + " choosed");
                /**
                 * Передача индекса детектора в инициализатор.
                 */
                saveDetIp(detectorName);
                //    btnLookUp.setVisible(false);
                initializeDetector(newValue.getDetectorIndex());
            }

        });


        /**
         * Подгонка размеров окна отображения картинки.
         */
        Platform.runLater(() -> setImageViewSize());
        /**
         * Инициализация списка коэф. усиления.
         */
        cbCCCOptions.setItems(optionsCCC);

        /**
         * Обработка смены коэф. усиления
         */
        cbCCCOptions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (selDetector == null) {
                return;//todo проверить на корректность
            }
            if ("1".equals(newValue)) {
                if (selDetector.getDevice() instanceof DetectorDevice.ChinaSource) {
                    ((DetectorDevice.ChinaSource) selDetector.getDevice()).setССС(false);
                    params.setTempKU(false);
                }

            } else {
                if (selDetector.getDevice() instanceof DetectorDevice.ChinaSource) {
                    ((DetectorDevice.ChinaSource) selDetector.getDevice()).setССС(true);
                    params.setTempKU(true);
                }
            }

        });
        /**
         * Установка списка экспериментов
         */
        //    cbExpOptions.setItems(optionsExperim);

        /**
         * Установка списка разрешений.
         */
        cbDimOptions.setItems(optionsDimension);
        /**
         * Активация разрешения.
         */
        if (viewSize == DetectorResolution.CHINA.getSize()) {
            cbDimOptions.getSelectionModel().select(optionsDimension.get(0));
        } else if (viewSize == DetectorResolution.CHINALOW.getSize()) {
            cbDimOptions.getSelectionModel().select(optionsDimension.get(1));
        } else {
            cbDimOptions.getSelectionModel().select(optionsDimension.get(0));
        }
        boolean tempKU;

        if (tempKU = params.isTempKU()) {
            cbCCCOptions.getSelectionModel().select(tempKU ? 1 : 0);
        }

        /**
         * Обработка  меню экспериментов
         */
        /**   cbExpOptions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

         System.out.println("Эксперимент: " + newValue);

         });*/

        /**
         * Обработка отклика на смену разрешения.
         */
        cbDimOptions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (selDetector == null) {
                return;//todo проверить на корректность
            }
            if ("92*90".equals(newValue)) {
                if (selDetector.getDevice() instanceof DetectorDevice.ChinaSource) {
                    ((DetectorDevice.ChinaSource) selDetector.getDevice()).setDim(false);
                    params.setDimention(newValue);
                }
            } else {
                if (selDetector.getDevice() instanceof DetectorDevice.ChinaSource) {
                    ((DetectorDevice.ChinaSource) selDetector.getDevice()).setDim(true);
                    params.setDimention(newValue);
                }
            }
            setSignalFielsdToZero();

            Dimension resolution = getSelDetector().getDevice().getResolution();

            Platform.runLater(() -> {
                params.setWidth(resolution.width);
                params.setHeigth(resolution.height);
            });

        });

        /**
         * Обработка отклика на нажатие кнопки вкл.
         */
        chPower.selectedProperty().addListener((observable, oldValue, newValue) -> {

            if (selDetector == null) {
                newValue = oldValue;
                return;
            }
            /**
             * Запрос на статус
             */
            if (newValue) {
                if (selDetector.getDevice() instanceof DetectorDevice.ChinaSource) {

                    params.setTempPower(true);
                    /**
                     *Установка стартовых параметров
                     */
                    if (isEthrnetWorking) {
                        //  ((DetectorDevice.ChinaSource) selDetector.getDevice()).setPower(false);
                        //ignore
                    } else {
                        ((DetectorDevice.ChinaSource) selDetector.getDevice()).setPower(true);

                    }
                    extStartSession();//поток на установку выбранных параметров
                }
            } else {
                if (selDetector.getDevice() instanceof DetectorDevice.ChinaSource) {
                    params.setTempPower(false);
                    if (isEthrnetWorking) {
                        extStopSession();
                    } else {
                        ((DetectorDevice.ChinaSource) selDetector.getDevice()).setPower(false);
                    }

                }
            }

        });
        /**
         * Определение кнопки газа
         */

        Bindings.bindBidirectional(lb_fps.textProperty(), params.intFPSProperty(), (StringConverter) new MyFPSConverter());

        Bindings.bindBidirectional(btnGaz.textProperty(), params.mPowerValueProperty(), (StringConverter) new MyGazConverter());

        btnGaz.setOnAction(actionEvent -> {
            if (selDetector == null) {
                return;//todo проверить на корректность
            }
            int i = params.getmPowerValue();
            if (i == 0) {
                i = 0x18; // 24 вольта
                startTimer();
            } else {
                i = 0x00;// 0 вольт
                stopTimer();
            }

            if (selDetector.getDevice() instanceof DetectorDevice.ChinaSource) {
                ((DetectorDevice.ChinaSource) selDetector.getDevice()).setMPower((byte) i);
            }

            params.setmPowerValue(i);

        });

        /**
         * Определение вывода температуры
         */
        Bindings.bindBidirectional(lb_Temp.textProperty(), params.tempProperty(), (StringConverter) new MyTempConverter());
        Bindings.bindBidirectional(lb_Temp1.textProperty(), params.tempProperty(), (StringConverter) new MyTempCelsConverter());


        /**
         * Определение времени выхода
         */

        //  Bindings.bindBidirectional(btnTimer.textProperty(), params.timeProperty(), (StringConverter) new MyTimeConverter());


        Platform.runLater(() -> setImageViewSize());

        /**
         *Сброс строки состояния
         */

        pb_exp.setVisible(false);

        lab_exp_status.setText("");

        lbAverageSignal.textProperty().bind(StendParams.sredneeProperty().asString("%.0f"));

        lb_fps.setVisible(false);

        setMenuItems();//настройка менюшек
    }

    private void createNetworcOptions() {
        /**
         * Создание списка.
         */
        int interfaceCounter = 0;
        /**
         * Заполнение списка интерфейсов.
         */
        optionsNetwork.add(new NetworkInfo(interfaceCounter++));
        for (NetworkInterface networkInterface : findInterfaces().toArray(new NetworkInterface[0])) {
            NetworkInfo networkInfo = new NetworkInfo(networkInterface.getName(), networkInterface, interfaceCounter);
            optionsNetwork.add(networkInfo);
            interfaceCounter++;
        }

        /**
         * Установка списка в комбобокс.
         */
        cbNetworkOptions.setItems(optionsNetwork);

        /**
         * Установка подсказки в комбобокс.
         */
        cbNetworkOptions.setPromptText(networkListPromptText);
    }

    private void setMenuItems() {


        initeMenu();

        driverGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioMenuItem item = (RadioMenuItem) newValue;
                String id = item.getId();

                switch (id) {
                    case "menu_USB":
                        params.setDriver("USB");
                        disposeDetector(null);
                        initeMenu();
                        break;
                    case "menu_Ethernet":
                        params.setDriver("ETHERNET");
                        disposeDetector(null);
                        initeMenu();
                        break;
                }

            }
        });


        options.addListener((ListChangeListener<DetectorInfo>) c -> {

            if (c.getList().size() != 1) {


            } else {//Если только USB
                if (tm1 != null) {
                    tm1.cancel();
                }
                cbDetectorOptions.getSelectionModel().select(0);
            }

        });


        menu_fullSize.setOnAction(event -> {
            Stage stage = (Stage) lb_fps.getScene().getWindow();
            stage.setFullScreen(true);
        });
        menu_iconed.setOnAction(event -> {
            Stage stage = (Stage) lb_fps.getScene().getWindow();
            stage.setIconified(true);
        });
        menu_close.setOnAction(event -> {
            Stage stage = (Stage) lb_fps.getScene().getWindow();
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });


        styleMenus();


    }

    private void styleMenus() {

        HashMap<MenuItem, String> list = new HashMap<>();
        list.put(menu_Settings_M, "menu/settings.png");
        list.put(menu_Potok, "menu/sensor.png");
        list.put(menu_Kalibr, "menu/freezing.png");
        list.put(menu_Driver_M, "menu/plug.png");
        list.put(menu_USB, "menu/usb.png");
        list.put(menu_Ethernet, "menu/lan.png");
        list.put(menu_loadProgram, "menu/loading.png");
        list.put(menu_Exp_M, "menu/exp.png");
        list.put(menu_LoadExp, "menu/dowload.png");
        list.put(menu_SaveExp, "menu/save.png");

        list.put(menu_Order_M, "menu/clipboard.png");
        list.put(menu_fields, "menu/filling-form.png");
        list.put(menu_Window_M, "menu/window.png");
        list.put(menu_fullSize, "menu/resize.png");
        list.put(menu_iconed, "menu/iconed.png");
        list.put(menu_close, "menu/close-window.png");
        list.put(menu_Help_M, "menu/help.png");
        list.put(menu_RE, "menu/manual.png");
        list.put(menu_about, "menu/about.png");


        for (Map.Entry<MenuItem, String> item : list.entrySet()) {

            Image image = new Image(getClass().getResourceAsStream(item.getValue()));
            ImageView openView = new ImageView(image);
            openView.setFitWidth(30);
            openView.setFitHeight(30);
            item.getKey().setGraphic(openView);


            item.getKey().setStyle(" -fx-padding: 5 5 5 5;" +
                    " -fx-font-size : 14px;" +
                    "-fx-text-fill: #112e1d;");
            //  item.getKey().setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));

        }

    }

    private void initeMenu() {

        if ("ETHERNET".equals(params.getDriver()) && onLine.getValue()) {
            menu_loadProgram.setDisable(false);
        } else {
            menu_loadProgram.setDisable(true);
        }

        if (tm1 != null) {
            tm1.cancel();
            tm1 = null;
            timerTask1 = null;
        }

        String driver = params.getDriver();

        int select0 = 0;
        switch (driver) {
            case "USB":
                cbDetectorOptions.setVisible(false);
                cbNetworkOptions.setVisible(false);

                driverGroup.selectToggle(menu_USB);
                select0 = 0;
                cbNetworkOptions.getSelectionModel().select(select0);
                break;
            case "ETHERNET":
                cbDetectorOptions.setVisible(false);
                cbNetworkOptions.setVisible(true);
                driverGroup.selectToggle(menu_Ethernet);
                if (optionsNetwork.size() == 2) {
                    select0 = 1;
                    cbNetworkOptions.getSelectionModel().select(select0);
                } else {
                    cbNetworkOptions.setDisable(false);
                    cbNetworkOptions.getSelectionModel().clearSelection();

                }
                break;
        }

    }

    private TimerTask timerTask1;
    private Timer tm1;

    private void scanEthernetAndfireDetector() {

        if (options.size() > 0) {
            options.clear();
        }

        tm1 = new Timer("Таймер поиска детекторов");

        timerTask1 = new TimerTask() {
            @Override
            public void run() {
                Detector.getDiscoveryService().scan();
            }
        };

        tm1.schedule(timerTask1, 0, 1000);

    }


    private void setSignalFielsdToZero() {
        KeyEvent EnterEvent = new KeyEvent(KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tf_stolbec.setText("0");
                tf_stolbec.fireEvent(EnterEvent);
                tf_stroka.setText("0");
                tf_stroka.fireEvent(EnterEvent);
            }
        });

    }

    private void setLostFocusAction() {

        KeyEvent EnterEvent = new KeyEvent(KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false);

        tfInt.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                tfInt.fireEvent(EnterEvent);
            }
        });
        tfVR0.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                tfVR0.fireEvent(EnterEvent);
            }
        });
        tfVOS.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                tfVOS.fireEvent(EnterEvent);
            }
        });

        /**
         tfKvadratWidth.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
         if (!t1) {
         tfKvadratWidth.fireEvent(EnterEvent);
         }
         });
         tfKvadratHeight.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
         if (!t1) {
         tfKvadratHeight.fireEvent(EnterEvent);
         }
         });

         tfSpeedPlata.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
         if (!t1) {
         tfSpeedPlata.fireEvent(EnterEvent);
         }
         });*/
        tf_stolbec.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                tf_stolbec.fireEvent(EnterEvent);
            }
        });
        tf_stroka.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                tf_stroka.fireEvent(EnterEvent);
            }
        });
    }

    /**
     * Запись ip выбранного детектора
     *
     * @param detectorName
     */
    private void saveDetIp(String detectorName) {
        String[] strings = detectorName.split(" ");
        boolean driverSelect = cbNetworkOptions.getSelectionModel().isSelected(0);
        if (!driverSelect) {
            params.setDetIP(strings[0]);
        }
    }

    private void setAllAnotherButtons() {
/**
 tb_antialising.selectedProperty().addListener((observable, oldValue, newValue) -> detectorPanel.setAntialiasingEnabled(newValue));
 tb_mirror.selectedProperty().addListener((observable, oldValue, newValue) -> detectorPanel.setMirrored(newValue));
 tb_debug.selectedProperty().addListener((observable, oldValue, newValue) -> {
 detectorPanel.setFPSDisplayed(newValue);
 detectorPanel.setImageSizeDisplayed(newValue);
 detectorPanel.setDisplayDebugInfo(newValue);

 });

 cb_flip.setItems(optionsFlip);
 cb_flip.getSelectionModel().select(optionsFlip.get(0));
 cb_flip.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

 switch (newValue) {
 case "Нет поворота":
 DetectorPanel.setFlipper(null);
 break;
 case "Поворот: +90\u00B0":
 DetectorPanel.setFlipper(new JHFlipFilter(FLIP_90CW));
 break;
 case "Поворот: -90\u00B0":
 DetectorPanel.setFlipper(new JHFlipFilter(FLIP_90CCW));
 break;
 case "Поворот: 180\u00B0":
 DetectorPanel.setFlipper(new JHFlipFilter(FLIP_180));
 break;
 }
 });


 tb_rgb.selectedProperty().addListener((observable, oldValue, newValue) -> {
 if (newValue) {
 detectorPanel.setImageTransformer(new MyChinaRgbImageTransformer());
 tb_gray.selectedProperty().setValue(false);
 BufferedImage bufferedImage = fillPolosa();
 ivPolosa.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
 } else {
 tb_gray.selectedProperty().setValue(true);
 }

 });

 tb_gray.selectedProperty().addListener((observable, oldValue, newValue) -> {
 if (newValue) {
 detectorPanel.setImageTransformer(new MyChinaGrayTramsformer());
 tb_rgb.selectedProperty().setValue(false);
 BufferedImage bufferedImage = fillPolosa();
 ivPolosa.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
 } else {
 tb_rgb.selectedProperty().setValue(true);
 }
 });

 tb_norm.selectedProperty().addListener((observable, oldValue, newValue) -> {
 if (newValue) {
 detectorPanel.setNormalayzer(new JHNormalizeFilter());
 } else {
 detectorPanel.setNormalayzer(null);
 }
 });*/
    }

    /**
     * Настройка кнопок рисования.
     */
    private void setDrawButton() {

        ArrayList<ToggleButton> toggleMode = new ArrayList<>();
        toggleMode.add(tb_none);
        toggleMode.add(tb_fill);
        toggleMode.add(tb_fit);
        int i = 0;
        for (ToggleButton tb :
                toggleMode) {
            tb.setToggleGroup(growModeGroup);
            tb.setUserData(i++);
        }
        growModeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if ((int) newValue.getUserData() == 0) {
                    detectorPanel.setDrawMode(DetectorPanel.DrawMode.NONE);
                } else if ((int) newValue.getUserData() == 1) {
                    detectorPanel.setDrawMode(DetectorPanel.DrawMode.FILL);
                } else {
                    detectorPanel.setDrawMode(DetectorPanel.DrawMode.FIT);
                }
            } else {
                growModeGroup.selectToggle(tb_none);
            }
        });
    }

    /**
     * Скан детекторов и заполнение списка.
     */
    private void fillDetectors() {

        /**
         * Заполнение списка детекторов.
         */
        Detector.getDetectors();

        /**
         * Установка списка в комбобокс.
         */
        cbDetectorOptions.setItems(options);
    }


    /**
     * Небольшая пауза.
     */
    private void waitNewImage() {
        if (isEthrnetWorking) {

        } else {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Инициализация работы детектора.
     */
    private void extStartSession() {
        Thread thread = new Thread(() -> {

            if (isEthrnetWorking) {//для сети
                ((DetectorDevice.ChinaSource) selDetector.getDevice()).setID();
                waitNewImage();
            }

            boolean selectedFullScr = cbDimOptions.getSelectionModel().isSelected(0);

            waitNewImage();
            if (FT_STATUS.FT_OK != ((DetectorDevice.ChinaSource) selDetector.getDevice()).setDim(selectedFullScr)) {
                return;
            }

            waitNewImage();
            if (setInt() != FT_STATUS.FT_OK) {
                return;
            }

            waitNewImage();

            if (!isEthrnetWorking) {//для сети
                if (setReference() != FT_STATUS.FT_OK) {
                    return;
                }


                waitNewImage();
                if (setVOS() != FT_STATUS.FT_OK) {
                    return;
                }
                waitNewImage();
            }

            if (setVR0() != FT_STATUS.FT_OK) {
                return;
            }
            boolean selectedCcc = cbCCCOptions.getSelectionModel().isSelected(1);
            waitNewImage();
            if (FT_STATUS.FT_OK != ((DetectorDevice.ChinaSource) selDetector.getDevice()).setССС(selectedCcc)) {
                return;
            }
            waitNewImage();

            if (isEthrnetWorking) {//для сети
                ((DetectorDevice.ChinaSource) selDetector.getDevice()).setPower(true);
                waitNewImage();
            }

            if (isEthrnetWorking) {//для сети
                ((DetectorDevice.ChinaSource) selDetector.getDevice()).setSpecPower(params.getTempVR0(),
                        params.getTempREF(), params.getTempREF(), params.getTempVOS());
                waitNewImage();
            }


        });
        thread.setDaemon(true);
        thread.start();
    }

    private void extStopSession() {
        Thread thread = new Thread(() -> {


            if (isEthrnetWorking) {//для сети
                ((DetectorDevice.ChinaSource) selDetector.getDevice()).setPower(false);
                waitNewImage();
            }

            if (isEthrnetWorking) {//для сети
                ((DetectorDevice.ChinaSource) selDetector.getDevice()).setSpecPower(0, 0, 0, 0);
                waitNewImage();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Заполнение (отрисовка) полосы.
     *
     * @return
     */
    private BufferedImage fillPolosa(ImageView ivPolosa, Pane pnGist) {
        int w = (int) pnGist.getWidth();
        int h = (int) (pnGist.getHeight() / 10.0);
        int max = 100;
        int count = 100;

        float[] floats = new float[count];
        for (int i = 0; i < count; i++) {
            floats[i] = max;
        }
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage bi = gc.createCompatibleImage(w, h);
        Graphics2D g2 = ge.createGraphics(bi);
        g2.setBackground(new Color(204, 204, 204, 255));
        g2.clearRect(0, 0, w, h);

        for (int i = 0; i < count; i++) {
            drawRect(g2, max, i, count, h, w);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.dispose();
        bi.flush();

        ivPolosa.setFitHeight(h);
        ivPolosa.setFitWidth(w);
        ivPolosa.prefHeight(h);
        ivPolosa.prefWidth(w);
        ivPolosa.setPreserveRatio(true);

        return bi;
    }

    /**
     * Выставление размеров (свойств) картинки относительно размера ImageView.
     */
    protected void setImageViewSize() {
        double height = pnGist.getHeight();
        double width = pnGist.getWidth();
        iwGist.setFitHeight(height);
        iwGist.setFitWidth(width);
        iwGist.prefHeight(height);
        iwGist.prefWidth(width);
        iwGist.setPreserveRatio(true);
    }

    /**
     * Инициализация детектора.
     *
     * @param detectorIndex индекс найденного детектора.
     */
    protected void initializeDetector(final int detectorIndex) {

        Task<Void> detectorIntilizer = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                /**
                 * Если нет активного детектора, тогда инициализировать выбранный.
                 */
                if (selDetector == null) {
                    initPanel(detectorIndex, FPSVideo);
                } else {
                    /**
                     * Если уже есть активный детектор, тогда закрыть активный и инициализировать выбранный.
                     */
                    closeDetector();
                    initPanel(detectorIndex, FPSVideo);
                }
                /**
                 * Старт  видеопотока.
                 */
                startDetectorStream();
                return null;
            }
        };
        new Thread(detectorIntilizer).start();
        /**
         * Активировать панель с кнопками.
         */
        // btnGetData.setDisable(true);
        //  btnParams.setDisable(true);
        //  Platform.runLater(() -> showFPS());
    }

    /**
     * Инициализация панели.
     *
     * @param detectorIndex индекс детектора
     * @param FPS           ограничение кадровой частоты
     */
    private void initPanel(int detectorIndex, double FPS) {
        selDetector = Detector.getDetectors().get(detectorIndex);
        viewSize = selDetector.getViewSize();
        detectorPanel = new DetectorPanel(selDetector, viewSize, true, async, this);
        detectorPanel.setFPSLimited(fpsLimited);
        detectorPanel.setImageTransformer(imageTransformer);
        detectorPanel.setPause(params.getPAUSE());
        snDetectorCapturedImage.setContent(detectorPanel);
        initSHowStatusSservice();
        initStatServiceForNew();
        BufferedImage bufferedImage = fillPolosa(ivPolosa, pnGist);

        ivPolosa.setImage(SwingFXUtils.toFXImage(bufferedImage, null));


    }

    /**
     * Старт видеопотока.
     */
    protected void startDetectorStream() {
        /**
         * Флаг работы потока
         */
        stopVideo = false;

        if (paused) {
            detectorPanel.resume();
            //  initStatService();
            initStatServiceForNew();
            paused = false;
        }
    }

    /**
     * Отработка закрытия детектора.
     */
    private void closeDetector() {
        if (selDetector != null) {
            detectorPanel.stop();
        }
    }

    /**
     * Отработка кнопки старт/стоп.
     *
     * @param event
     */
    public void stopDetector(ActionEvent event) {
        if (!paused) {
            stopVideo = true;
            detectorPanel.pause();
            btnStartStop.setText("Старт");
            paused = true;
        } else {
            stopVideo = false;
            startDetectorStream();
            btnStartStop.setText("Стоп");
        }
    }

    /**
     * Отработка кнопки сброса.
     *
     * @param event
     */
    public void disposeDetector(ActionEvent event) {
        stopVideo = true;
        closeDetector();
    }

    /**
     * Отработка установки задержки опроса платы
     *
     * @param event
     */
    public void setPauseOnPlate(ActionEvent event) {
        TextField source = (TextField) event.getSource();
        try {
            String text = source.getText();
            int value = Integer.parseInt(text);
            detectorPanel.setPause(value);
            source.setText(text);
            params.setPAUSE(value);
        } catch (Exception e) {
            setError(source, "Error");
            LOG.error("Integer processing error", e);
        }

        source.selectAll();
        source.getParent().requestFocus();
    }

    private static BooleanProperty onLine = new SimpleBooleanProperty(true);

    private Timer tm2;
    private TimerTask timerTask2;

    /**
     * Сервис отображения FPS.
     */
    private void initSHowStatusSservice() {

        onLine.addListener((observable, oldValue, newValue) -> {
                    changeIconStatusVisible(newValue, !newValue);
                }
        );

        tm2 = new Timer();
        timerTask2 = new TimerTask() {
            @Override
            public void run() {
                onLine.setValue(((DetectorDevice.ChinaSource) selDetector.getDevice()).isOnline());
            }
        };

        tm2.schedule(timerTask2, 0, 1000);

    }

    private void changeIconStatusVisible(boolean okVisible, boolean errorVisible) {
        Platform.runLater(() -> {
            iv_okStatus.setVisible(okVisible);
            iv_errorStatus.setVisible(errorVisible);
        });
    }

    private ScheduledExecutorService executor = null;
    private static int stolbec = 0;
    private static int stroka = 0;

    private static float mean = 0;


    private void initStatServiceForNew() {
        Runnable task = new Runnable() {
            @Override
            public void run() {

                //   while (!stopVideo) {
                try {
                    DetectorDevice.ChinaSource device = (DetectorDevice.ChinaSource) selDetector.getDevice();
                    if (device == null) {
                        return;
                    }
                    if (stopVideo) {
                        return;
                    }
                    int[][] frameData = device.getFrame();
                    if (frameData != null) {
                        StatData statData = new StatData(frameData);
                        float[] dataArray = statData.getDataArray();
                        float[] skoArray = statData.getSKOArray();
                        float[] skoArrayHorisontal = statData.getSKOArrayHorisontal();
                        float[] signalFromStroka = statData.getSignalFromStroka(stroka);
                        float[] signalFromStolbec = statData.getSignalFromStolbec(stolbec);


                        mean = statData.getMean();
                        grabbedImage = drawMainGist(dataArray, pnGist);

                        grabbedImageH = drawLowGist(skoArray, true, true, spGist);
                        grabbedImageV = drawLowGist(skoArrayHorisontal, true, false, spGist);

                        grabbedImageSignalH = drawLowGist(signalFromStroka, false, false, spGist);
                        grabbedImageSignalV = drawLowGist(signalFromStolbec, false, false, spGist);

                        Platform.runLater(() -> {
                            //Отображение статистики в полях
                            params.setSrednee(mean);

                            //Отображение гистограмм
                            /**
                             * Главная гистограмма.
                             */
                            if (grabbedImage != null) {
                                final Image gistIamgeToFX = SwingFXUtils
                                        .toFXImage(grabbedImage, null);
                                gistImageProperty.set(gistIamgeToFX);
                            }
                            /**
                             * Горизонтальная гистограмма.
                             */
                            if (grabbedImageH != null) {
                                final Image gistHIamgeToFXH = SwingFXUtils
                                        .toFXImage(grabbedImageH, null);
                                gistImagePropertyH.set(gistHIamgeToFXH);
                            }
                            /**
                             * Вертикальная гистограмма.
                             */
                            if (grabbedImageV != null) {
                                final Image gistVIamgeToFXV = SwingFXUtils
                                        .toFXImage(grabbedImageV, null);
                                gistImagePropertyV.set(gistVIamgeToFXV);
                            }

                            /**
                             * Строчная гистограмма.
                             */
                            if (grabbedImageSignalH != null) {
                                final Image gistSignalHIamgeToFXH = SwingFXUtils
                                        .toFXImage(grabbedImageSignalH, null);
                                gistImagePropertySignalH.set(gistSignalHIamgeToFXH);
                            }
                            /**
                             * Столбцовая гистограмма.
                             */
                            if (grabbedImageSignalV != null) {
                                final Image gistSignalVIamgeToFXV = SwingFXUtils
                                        .toFXImage(grabbedImageSignalV, null);
                                gistImagePropertySignalV.set(gistSignalVIamgeToFXV);
                            }


                        });

                        if (grabbedImage != null) {
                            grabbedImage.flush();
                        }


                        if (grabbedImageH != null) {
                            grabbedImageH.flush();
                        }

                        if (grabbedImageV != null) {
                            grabbedImageV.flush();
                        }


                        if (grabbedImageSignalH != null) {
                            grabbedImageSignalH.flush();
                        }
                        if (grabbedImageSignalV != null) {
                            grabbedImageSignalV.flush();
                        }

                    }
                } catch (Exception e) {
                    LOG.error("Error in staticService {}", e);
                }
                // }
            }
        };

        iwGist.imageProperty().bind(gistImageProperty);

        iwGistSKO_H.imageProperty().bind(gistImagePropertyH);
        iwGistSKO_V.imageProperty().bind(gistImagePropertyV);

        iwGistSignal_H.imageProperty().bind(gistImagePropertySignalH);
        iwGistSignal_V.imageProperty().bind(gistImagePropertySignalV);

        executor = Executors.newScheduledThreadPool(2);
        executor.scheduleWithFixedDelay(task, 0, 120, TimeUnit.MILLISECONDS);
    }


    /**
     * Перерасчет, исходя из архитектуры ФПУ, времени накопления в FPS, в герцы
     */
    private void showFPS() {

        try {
            int i = Integer.parseInt(tfInt.getText());
            if (i <= 0) {
                throw new NumberFormatException();
            }
            lb_fps.setBorder(null);

            Dimension resolution = getSelDetector().getDevice().getResolution();

            double v = 10E+06 / (2.0 * (((resolution.getWidth() + 2.0) * ((resolution.getHeight() / 4.0) + 16)) + 1));
            double max = Math.min(v, 1000_000.0 / i);

            lb_fps.setText(String.format("%.1f", max));

        } catch (NumberFormatException e) {

            lb_fps.setBorder(new Border(new BorderStroke(javafx.scene.paint.Color.RED,
                    BorderStrokeStyle.SOLID, new CornerRadii(3),
                    new BorderWidths(2), new Insets(-2))));
            lb_fps.setText("Err.");

        } catch (NullPointerException e) {

            //ignore
        }


    }


    /**
     * Отработка установки инта.
     *
     * @param event
     */
    public void setInt(ActionEvent event) {
        int i = parseIntText(event, true);
        if (i < 0) {
            return;
        }
        params.setTempInt(i);

        calkFeffect(i);

        setInt();

        //    resetBTNS();

        /**
         * Отображение пересчета в ФПС
         */
        //    Platform.runLater(() -> showFPS());

    }

    /**
     * Расчет эфективной полосы пропускания, Гц
     *
     * @param i - время накопления, мкс
     */
    private void calkFeffect(int i) {
        double fEfect = 1.0 / ((1.0E-06) * (2.0) * i);
        params.setfEfect(fEfect);
    }

    /**
     * Сброс цвета кнопок.
     */
    private void resetBTNS() {
        btnGetData.setDisable(true);
        //    btnParams.setDisable(true);
        btnGetData.setStyle("-fx-background-color:  orange");
        //    btnParams.setStyle("-fx-background-color:  orange");
        //    btnPotok.setStyle("-fx-background-color:  orange");
    }

    /**
     * Установка инта.
     */
    public FT_STATUS setInt() {

        if (selDetector.getDevice() instanceof DetectorDevice.ChinaSource) {
            FT_STATUS ft_status = ((DetectorDevice.ChinaSource) selDetector.getDevice()).setInt(params.getTempInt());
            params.setIntForFPS(params.getTempInt());

            if (!lb_fps.isVisible()) {
                lb_fps.setVisible(true);
            }

            return ft_status;
        }

        return null;
    }


    /**
     * Отработка установки VOS/скимменг.
     *
     * @param event
     */
    public void setVOS(ActionEvent event) {
        int i = parseIntText(event, true);
        if (i < 0) {
            return;
        }
        params.setTempVOS(i);
        setVOS();
        //    resetBTNS();
    }

    /**
     * Установка VOS.
     */
    public FT_STATUS setVOS() {

        if (selDetector.getDevice() instanceof DetectorDevice.ChinaSource) {

            if (isEthrnetWorking) {//для сети
                FT_STATUS ft_status = ((DetectorDevice.ChinaSource) selDetector.getDevice()).setSpecPower(params.getTempVR0(),
                        params.getTempREF(), params.getTempREF(), params.getTempVOS());
                return ft_status;
            } else {
                FT_STATUS ft_status = ((DetectorDevice.ChinaSource) selDetector.getDevice()).setVOS(params.getTempVOS());
                return ft_status;
            }
        }
        return null;
    }

    /**
     * Отработка установки VREF и VOUTREF.
     *
     * @param event
     */
    public void setReference(ActionEvent event) {

        int i = parseIntText(event, true);
        if (i < 0) {
            return;
        }
        params.setTempREF(i);
        setReference();
        //   resetBTNS();
    }

    /**
     * Установка VREF и VOUTREF.
     */
    public FT_STATUS setReference() {
        if (selDetector.getDevice() instanceof DetectorDevice.ChinaSource) {
            FT_STATUS ft_status = ((DetectorDevice.ChinaSource) selDetector.getDevice()).setReference(params.getTempREF());
            return ft_status;
        }
        return null;
    }


    /**
     * Отработка установки VR0/смещения.
     *
     * @param event
     */
    public void setVR0(ActionEvent event) {
        int i = parseIntText(event, true);
        if (i < 0) {
            return;
        }
        params.setTempVR0(i);
        setVR0();
        //  resetBTNS();
    }

    /**
     * Установка VR0/смещение.
     */
    public FT_STATUS setVR0() {
        if (selDetector.getDevice() instanceof DetectorDevice.ChinaSource) {
            FT_STATUS ft_status = ((DetectorDevice.ChinaSource) selDetector.getDevice()).setVR0(params.getTempVR0());
            return ft_status;
        }
        return null;
    }

    /**
     * Установка числа отсчетов.
     *
     * @param event
     */
    public void setCountFrames(ActionEvent event) {
        int i = parseIntText(event, false);
        if (i < 0) {
            return;
        }
        params.setCountFrames(i);
        //  resetBTNS();
    }

    /**
     * Установка размеров окна центральной части.
     *
     * @param event
     */
    public void setKvadratSize(ActionEvent event) {
        int i = parseIntText(event, false);
        if (i < 0) {
            return;
        }
        TextField source = (TextField) event.getSource();
        String id = source.getId();
        Dimension viewSize = selDetector.getViewSize();
        if (detectorPanel != null) {
            if (id.equals("tfKvadratHeight")) {
                detectorPanel.setAimHeight(i + 2 <= viewSize.getHeight() ? i : (int) viewSize.getHeight() - 1);
            } else {
                detectorPanel.setAimWidth(i + 2 <= viewSize.getWidth() ? i : (int) viewSize.getWidth() - 1);
            }
        }
    }

    @FXML
    private void setStolbec(ActionEvent event) {
        int i = parseIntText(event, true, selDetector.getViewSize().width - 1);
        if (i < 0) {
            return;
        }
        stolbec = i;
    }

    @FXML
    private void setStroka(ActionEvent event) {
        int i = parseIntText(event, true, selDetector.getViewSize().height - 1);
        if (i < 0) {
            return;
        }
        stroka = i;
    }

    /**
     * Отрисовка главной гистограммы распределения.
     *
     * @param data входной массив данных.
     * @return картинка.
     */
    private BufferedImage drawMainGist(float[] data, Pane pnGist) {

        int height = (int) pnGist.getHeight();
        int width = (int) pnGist.getWidth();
        if (height <= 0 || width <= 0) {
            return null;
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage bi = gc.createCompatibleImage(width, height);

        Graphics2D g2 = ge.createGraphics(bi);

        g2.setBackground(new Color(204, 204, 204, 255));
        g2.clearRect(0, 0, width, height);

        drowColomn(data, g2, height, width);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.dispose();
        bi.flush();
        return bi;
    }

    int maxVisota = -1;
    int maxVhogrenie = -1;
    int bochka = -1;
    int length = 0;

    /**
     * Отрисовка столбцов.
     *
     * @param data
     * @param g2
     * @param height
     * @param width
     */
    private void drowColomn(float[] data, Graphics2D g2, int height, int width) {
        maxVisota = -1;
        maxVhogrenie = -1;
        bochka = -1;
        length = data.length;
        int[] ints = takeCountInside(data, true, height, width);
        for (int i = 0; i < ints.length; i++) {

            drawRect(g2, ints[i], i, ints.length, height, width);
            if (ints[i] > maxVhogrenie) {
                maxVhogrenie = ints[i];
                bochka = i;
            }
        }
    }

    /**
     * Отрисовка примоугольника.
     *
     * @param g2
     * @param value
     * @param i
     * @param length
     */
    private void drawRect(Graphics2D g2, int value, int i, int length, int heigth, int width) {

        int shaG = width / length;
        float acp = detectorPanel.getImageTransformer().getRazryadnost();
        int color = detectorPanel.getImageTransformer()
                .convertValueToColor((int) ((acp / length) * (i)));
        g2.setColor(new Color(color));

        int visota = (int) (heigth * value * 0.01);
        g2.fillRect(shaG * i, heigth - visota, shaG, visota);

    }

    ///////////////////

    /**
     * Расчет количества вхождений.
     *
     * @param dataArray      входные параметры.
     * @param enableMasshtab флагмасштабирования.
     * @param h              высота поля.
     * @param w              ширина.
     * @return
     */
    private int[] takeCountInside(float[] dataArray, boolean enableMasshtab, int h, int w) {

        double maxValue = Double.MIN_VALUE;
        double minValue = Double.MAX_VALUE;

        for (int i = 0; i < dataArray.length; i++) {
            if (dataArray[i] > maxValue) {
                maxValue = dataArray[i];
            }
            if (dataArray[i] < minValue) {
                minValue = dataArray[i];
            }
        }

        int countOtrezkov = 100;
        float acp = detectorPanel.getImageTransformer().getRazryadnost();

        double delta = acp / countOtrezkov;
        int[] tempArray = new int[countOtrezkov];
        int length = dataArray.length;
        //отработка вхождений
        for (int i = 0; i < dataArray.length; i++) {
            boolean entered = false;
            for (int j = 0; j < countOtrezkov; j++) {

                if (((delta * j) <= dataArray[i]) && (dataArray[i] < (delta * (j + 1)))) {
                    tempArray[j] += 1;
                    entered = true;
                }
            }
            if (!entered) {
                tempArray[countOtrezkov - 1] += 1;
            }
        }
        float koef = (float) ((1.0 * h) / length);
        if (enableMasshtab) {
            for (int i = 0; i < countOtrezkov; i++) {
                tempArray[i] = (int) DoubleRounder.round((tempArray[i] * koef), 0);
            }
        }
        return tempArray;
    }

    /**
     * Отрисовка гистограмм распределения по вертикали и горизонтали.
     *
     * @param data         исходные данные.
     * @param TYPE_DIAGRAM true- СКО, false - Сигнал
     * @param REVERSX      true- прямая, false - обратная последовательность по оси Х.
     * @return картинка.
     */
    private BufferedImage drawLowGist(float[] data, boolean TYPE_DIAGRAM, boolean REVERSX, Pane parent) {

        if (!REVERSX) {
            float[] tempData = new float[data.length];
            for (int i = 0; i < data.length; i++) {
                tempData[i] = data[data.length - 1 - i];
            }
            data = tempData;
        }

        int height = (int) parent.getHeight();
        int width = (int) parent.getWidth();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage bi = gc.createCompatibleImage(width, height);

        Graphics2D g2 = ge.createGraphics(bi);

        g2.setBackground(new Color(204, 204, 204, 255));
        g2.clearRect(0, 0, width, height);

        applySKO(data, g2, height, width, TYPE_DIAGRAM, REVERSX);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.dispose();
        bi.flush();
        return bi;
    }

    /**
     * Отрисовка гистограмм распределения по вертикали и горизонтали.
     *
     * @param data         исходные данные.
     * @param TYPE_DIAGRAM true- вертикаль, false -горизонталь.
     * @return картинка.
     */
    private BufferedImage drawLowGist(float[] data, boolean TYPE_DIAGRAM) {

        if (!TYPE_DIAGRAM) {
            float[] tempData = new float[data.length];
            for (int i = 0; i < data.length; i++) {
                tempData[i] = data[data.length - 1 - i];
            }
            data = tempData;
        }


        int height = (int) pnFlash.getHeight() / 2;
        int width = (int) pnFlash.getWidth();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage bi = gc.createCompatibleImage(width, height);

        Graphics2D g2 = ge.createGraphics(bi);

        g2.setBackground(new Color(204, 204, 204, 255));
        g2.clearRect(0, 0, width, height);

        applySKO(data, g2, height, width, TYPE_DIAGRAM);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.dispose();
        bi.flush();
        return bi;
    }

    /**
     * Расчет распределения для гистограмм строк и столбцов.
     *
     * @param data         данные.
     * @param g2           среда.
     * @param height       высота поля.
     * @param width        ширина поля.
     * @param TYPE_DIAGRAM тип диаграммы. true- вертикаль, false -горизонталь.
     */
    private void applySKO(float[] data, Graphics2D g2, int height, int width, boolean TYPE_DIAGRAM) {
        maxVisotaSKO = -1;
        maxVhogrenieSKO = -1;
        bochkaSKO = -1;
        lengthSKO = data.length;
        for (int i = 0; i < data.length; i++) {

            if (maxVisotaSKO < data[i]) {
                maxVisotaSKO = data[i];
                bochkaSKO = i;
            }
        }
        float koef = height / maxVisotaSKO;
        for (int i = 0; i < data.length; i++) {
            drawLitleColomn(g2, (int) (data[i] * koef), i, data.length, height, width);
        }
        drawMaxValueOnGist(g2, data.length - 1 - bochkaSKO, maxVisotaSKO, data.length,
                height, width, TYPE_DIAGRAM);
    }

    /**
     * Отрисовка значения на истограмме.
     *
     * @param g2
     * @param bochka       номер столбца с максимальным значением.
     * @param maxVhogrenie максимальное значение.
     * @param size         количество столбцов.
     * @param height       высота.
     * @param width        ширина.
     * @param TYPE_DIAGRAM тип диаграммы: строка, столбец.
     */
    private void drawMaxValueOnGist(Graphics2D g2, int bochka, float maxVhogrenie, int size, int height, int width,
                                    boolean TYPE_DIAGRAM) {
        Font font = new Font("sans-serif", Font.BOLD, 10);
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics(font);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int shaG = width / size;
        double pats = size / 4.0;
        for (int i = 4; i >= 0; i = i - 1) {
            String s;
            if (i == 0) {
                s = (i) + "";
            } else {
                s = (int) (i * pats - 1) + "";
            }
            int w = width - 24 - shaG * (size - 1 - (int) (i * pats)) - (metrics.stringWidth(s) - shaG) / 2;
            int h = height - 5;
            int sw = w;
            int sh = h;
            g2.setColor(Color.BLACK);
            g2.drawString(s, sw + 1, sh + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(s, sw, sh);
        }
        font = new Font("sans-serif", Font.BOLD, 14);
        g2.setFont(font);
        metrics = g2.getFontMetrics(font);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String s;
        if (TYPE_DIAGRAM) {
            s = "Макс: " + (int) maxVhogrenie + " мВ " + (bochka) + " строка";
        } else {
            s = "Макс: " + (int) maxVhogrenie + " мВ " + (bochka) + " столбец";
        }
        int w = width;
        int h = height;
        int sw = (w - metrics.stringWidth(s)) - 25;
        int sh = (h - metrics.getHeight()) - 20;
        g2.setColor(Color.BLACK);
        g2.drawString(s, sw + 1, sh + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(s, sw, sh);
    }

    float maxVisotaSKO = -1;
    int maxVhogrenieSKO = -1;
    int bochkaSKO = -1;
    int lengthSKO = -1;

    /**
     * Расчет распределения для гистограмм строк и столбцов.
     *
     * @param data         данные.
     * @param g2           среда.
     * @param height       высота поля.
     * @param width        ширина поля.
     * @param TYPE_DIAGRAM тип диаграммы. true- СКО, false - Сигнал.
     * @param REVERSX      true- прямая, false -обратная последовательность.
     */
    private void applySKO(float[] data, Graphics2D g2, int height, int width,
                          boolean TYPE_DIAGRAM, boolean REVERSX) {
        maxVisotaSKO = -1;
        maxVhogrenieSKO = -1;
        bochkaSKO = -1;
        lengthSKO = data.length;
        for (int i = 0; i < data.length; i++) {

            if (maxVisotaSKO < data[i]) {
                maxVisotaSKO = data[i];
                bochkaSKO = i;
            }
        }
        float koef = height / maxVisotaSKO;
        for (int i = 0; i < data.length; i++) {
            drawLitleColomn(g2, (int) (data[i] * koef), i, data.length, height, width);
        }
        drawMaxValueOnGist(g2, data.length - 1 - bochkaSKO, maxVisotaSKO, data.length,
                height, width, TYPE_DIAGRAM, REVERSX);
    }

    /**
     * Отрисовка маленьких столбцов c показом максимума.
     *
     * @param g2
     * @param value  значение.
     * @param i      номер столбца с максимальным значением.
     * @param length количество столбцов.
     * @param height высота поля.
     * @param width  ширина поля.
     */
    private void drawLitleColomn(Graphics2D g2, int value, int i, int length, int height, int width) {

        int shaG = width / length;
        if (i == bochkaSKO) {
            g2.setColor(new Color(0, 0, 0));
        } else {
            g2.setColor(new Color(68, 133, 3));
        }
        int visota = height - value;
        g2.fillRect(width - 24 - shaG * i, visota, shaG, value);
    }

    /**
     * Отрисовка значения на истограмме.
     *
     * @param g2
     * @param bochka       номер столбца с максимальным значением.
     * @param maxVhogrenie максимальное значение.
     * @param size         количество столбцов.
     * @param height       высота.
     * @param width        ширина.
     * @param TYPE_DIAGRAM тип диаграммы. true- СКО, false - Сигнал.
     * @param REVERSX      true- прямая, false -обратная последовательность.
     */
    private void drawMaxValueOnGist(Graphics2D g2, int bochka, float maxVhogrenie, int size, int height, int width,
                                    boolean TYPE_DIAGRAM, boolean REVERSX) {
        Font font = new Font("sans-serif", Font.BOLD, 10);
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics(font);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int shaG = width / size;
        double pats = size / 4.0;
        for (int i = 4; i >= 0; i = i - 1) {
            String s;
            if (i == 0) {
                s = (i) + "";
            } else {
                s = (int) (i * pats - 1) + "";
            }
            int w = width - 24 - shaG * (size - 1 - (int) (i * pats)) - (metrics.stringWidth(s) - shaG) / 2;
            int h = height - 5;
            int sw = w;
            int sh = h;
            g2.setColor(Color.BLACK);
            g2.drawString(s, sw + 1, sh + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(s, sw, sh);
        }
        font = new Font("sans-serif", Font.BOLD, 14);
        g2.setFont(font);
        metrics = g2.getFontMetrics(font);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String s;
        if (REVERSX) {
            if (TYPE_DIAGRAM) {
                s = "Макс: " + (int) maxVhogrenie + " мВ " + (bochka) + " строка";
            } else {
                s = "Макс: " + (int) maxVhogrenie + " мВ " + (bochka) + " pix";
            }
        } else {
            if (TYPE_DIAGRAM) {
                s = "Макс: " + (int) maxVhogrenie + " мВ " + (bochka) + " столбец";
            } else {
                s = "Макс: " + (int) maxVhogrenie + " мВ " + (bochka) + " pix";
            }
        }

        int w = width;
        int h = height;
        int sw = (w - metrics.stringWidth(s)) - 25;
        int sh = (h - metrics.getHeight()) - 20;
        g2.setColor(Color.BLACK);
        g2.drawString(s, sw + 1, sh + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(s, sw, sh);
    }

    /**
     * Старт расчета потока.
     *
     * @param event
     */
    @FXML
    private void startPotok(ActionEvent event) throws IOException {
        Stage stage = new Stage();

        potokFxmlLoader = new FXMLLoader(getClass().getResource("mathPage.fxml"));
        Parent root = potokFxmlLoader.load();
        MathController mathController = potokFxmlLoader.getController();
        mathController.initController(this);
        stage.setOnCloseRequest(t -> mathController.saveValuesToParams());
        Scene scene = new Scene(root);
        stage.setTitle("Расчет параметров потока");
        stage.setScene(scene);
        //stage.centerOnScreen();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        stage.show();

    }


    /**
     * Старт окна для загрузки драйвера.
     *
     * @param event
     */
    @FXML
    private void startLoadDriver(ActionEvent event) throws IOException {
        Stage stage = new Stage();

        driverFxmlLoader = new FXMLLoader(getClass().getResource("loadDriverPage.fxml"));
        Parent root = driverFxmlLoader.load();
        LoadDriverController loadController = driverFxmlLoader.getController();
        loadController.initController(this);

        Scene scene = new Scene(root);
        stage.setTitle("Загрузка драйвера");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.show();

    }

    /**
     * Старт окна для ввода полей отчета.
     *
     * @param event
     */
    @FXML
    private void startFields(ActionEvent event) throws IOException {
        Stage stage = new Stage();

        fieldsFxmlLoader = new FXMLLoader(getClass().getResource("fieldsPage.fxml"));
        Parent root = fieldsFxmlLoader.load();
        FieldsController fieldsController = fieldsFxmlLoader.getController();
        fieldsController.initController(this);

        Scene scene = new Scene(root);
        stage.setTitle("Подготовка отчета");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.show();
    }


    /**
     * Старт расчета параметров.
     *
     * @param event
     */
    @FXML
    private void startParams(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        paramsFxmlLoader = new FXMLLoader(getClass().getResource("paramsPage.fxml"));
        Parent root = paramsFxmlLoader.load();
        ParamsController paramsController = paramsFxmlLoader.getController();
        paramsController.initController(this);
        Scene scene = new Scene(root);
        stage.setTitle("Расчет характеристик ФПУ");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    private DataService dataService;

    /**
     * Старт сервиса набора данных.
     *
     * @param event
     */
    @FXML
    private void startGetData(ActionEvent event) {
        if (dataService == null) {
            initDataservice();
        }
        getPb_exp().visibleProperty().bind(dataService.runningProperty());
        getPb_exp().progressProperty().bind(dataService.progressProperty());
        getLab_exp_status().textProperty().bind(dataService.messageProperty());
        if (dataService.getState() == Worker.State.RUNNING) {
            dataService.cancel();
            dataService = null;
        } else {
            dataService.restart();
        }
    }

    /**
     * Инициализация сервиса сбора данных.
     */
    public void initDataservice() {
        dataService = new DataService(this);
    }

    FXMLLoader driverFxmlLoader;

    FXMLLoader potokFxmlLoader;
    FXMLLoader fieldsFxmlLoader;
    FXMLLoader tempFxmlLoader;
    FXMLLoader paramsFxmlLoader;

    public FXMLLoader getParamsFxmlLoader() {
        return paramsFxmlLoader;
    }

    public ComboBox<String> getCbDimOptions() {
        return cbDimOptions;
    }

    public static float getMASHTAB() {
        return MASHTAB;
    }

    public ProgressBar getPb_exp() {
        return pb_exp;
    }

    public Label getLab_exp_status() {
        return lab_exp_status;
    }

    public Detector getSelDetector() {
        return selDetector;
    }

    public TextField getTfInt() {
        return tfInt;
    }

    public StendParams getParams() {
        return params;
    }

    public Button getBtnPotok() {
        return btnPotok;
    }

    public Button getBtnGetData() {
        return btnGetData;
    }

    public Button getBtnParams() {
        return btnParams;
    }

    public ExpInfo getSelExp() {
        return selExp;
    }

    public ObservableList<ExpInfo> getOptionsExp() {
        return optionsExp;
    }

    public static int getExpCounter() {
        return expCounter++;
    }

    public TextField getTfVOS() {
        return tfVOS;
    }

    public TextField getTfVR0() {
        return tfVR0;
    }

    public ComboBox<String> getCbCCCOptions() {
        return cbCCCOptions;
    }

    public TextField getTfSpeedPlata() {
        return tfSpeedPlata;
    }

    public Label getLb_online() {
        return lb_online;
    }

    public static boolean isPaused() {
        return paused;
    }

    /**
     * Инициализация сети.
     *
     * @param index индекс найденного интрерфейса.
     */
    protected void initializeNetwork(final int index) {
        selNetworkInterface = optionsNetwork.get(index);
        params.setSelNetworkInterface(selNetworkInterface);
    }

    @Override
    public void detectorFound(DetectorDiscoveryEvent event) {

        int size = options.size();
        Detector detector = event.getDetector();
        DetectorInfo detectorInfo = new DetectorInfo();
        detectorInfo.setDetectorIndex(size);
        detectorInfo.setDetectorName(detector.getName());

        Platform.runLater(() -> {
            options.add(detectorInfo);
            //      btnLookUp.setStyle("-fx-background-color:  green");
        });

    }


    @Override
    public void detectorGone(DetectorDiscoveryEvent event) {
        LOG.debug("Детектор ушел");
    }

    /**
     * Выключение панельки
     *
     * @param paneID ID панельки, которую необходимо выключить
     */
    private void disableTitledPane(String paneID) {
        ObservableList<Node> children = vb_comPanel.getChildren();
        for (Node node :
                children) {
            if (paneID.equals(node.getId())) {
                children.remove(node);
                return;
            }
        }
    }

    ///////////////////////////////////////////////// Дебаг окно
    @FXML
    private Button bt_debug_download;

    @FXML
    private Button bt_debug_startStopVideo;

    @FXML
    private Button bt_debug_kalibrovka;

    @FXML
    private Button bt_debug_testFrame;

    @FXML
    private TextField tf_debug_kalibrovka_0;

    @FXML
    private TextField tf_debug_kalibrovka_1;

    @FXML
    private TextField tf_debug_kalibrovka_2;

    @FXML
    private TextField tf_debug_kalibrovka_3;

    @FXML
    private TextField tf_debug_testFrameCount;

    @FXML
    private ToggleButton tb_debug_testFrame_0;

    @FXML
    private ToggleButton tb_debug_testFrame_1;

    @FXML
    private ToggleButton tb_debug_testFrame_2;

    @FXML
    private ProgressBar pb_debug_progress;

    @FXML
    private Label lb_debug_status;

    private static ArrayList<String> status = new ArrayList<>();

    static {

        status.add("видео идет");
        status.add("калибровка не выполнена");

    }

    /**
     * Статус в строку
     *
     * @return строка
     */
    private String statusToString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String txt :
                status) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append(" ");
            } else {
                stringBuilder.append("; ");
            }
            stringBuilder.append(" ").append(txt);
        }

        if (stringBuilder.length() > 0) {
            stringBuilder.append(".");
        }
        return stringBuilder.toString();
    }

    /**
     * Печать строки в лейбле
     */
    private void printStatus() {
        lb_debug_status.setText("Статус:" + statusToString());
    }

    /**
     * Инициирование debug панели
     */
    private void initDebugPanel() {

        pb_debug_progress.setVisible(false);

        printStatus();
    }


    private class MyFPSConverter extends DoubleStringConverter {
        public MyFPSConverter() {
            super();
        }

        @Override
        public Double fromString(String value) {
            try {
                Double.parseDouble(value);
                return super.fromString(value);
            } catch (NumberFormatException exception) {
                return Double.valueOf(0);
            }
        }

        @Override
        public String toString(Double value) {
            if (value >= 0) {
                return String.format("%.1f", value);
            } else {
                return "Err.";
            }
        }
    }


    /**
     * Конвертер для надписи кнопки "Газ"
     */
    private static class MyGazConverter extends IntegerStringConverter {
        public MyGazConverter() {
            super();
        }

        @Override
        public Integer fromString(String value) {
            try {
                Integer.parseInt(value);
                return super.fromString(value);
            } catch (NumberFormatException exception) {
                return 0;
            }
        }

        @Override
        public String toString(Integer value) {
            if (value == 0) {
                return "Старт";
            } else {
                return "Стоп";
            }
        }
    }

    /**
     * Конвертер для вывода значения температуры в кельвинах
     */
    private static class MyTempConverter extends IntegerStringConverter {

        public MyTempConverter() {
            super();
        }

        @Override
        public String toString(Integer value) {
            if (value < 0) {
                return "Err.";
            } else {
                return value + " К";
            }
        }
    }

    /**
     * Конвертер для вывода значения температуры в градусах
     */
    private static class MyTempCelsConverter extends IntegerStringConverter {

        public MyTempCelsConverter() {
            super();
        }

        @Override
        public String toString(Integer value) {
            if (value < 0 || value > 500) {
                return "Err.";
            } else {
                return (value - 273) + " \u2103";
            }

        }
    }

    @FXML
    private void setTempParams(ActionEvent event) throws IOException {
        Stage stage = new Stage();

        tempFxmlLoader = new FXMLLoader(getClass().getResource("tempPage.fxml"));
        Parent root = tempFxmlLoader.load();
        TempController tempController = tempFxmlLoader.getController();
        tempController.initController(this);
        stage.setOnCloseRequest(t -> tempController.saveValuesToParams());
        Scene scene = new Scene(root);
        stage.setTitle("Калибровка термодатчика");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.show();
    }

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
    private static final SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("m мин s сек");

    private static class MyTimeConverter extends LongStringConverter {


        public MyTimeConverter() {
            super();
        }

        @Override
        public Long fromString(String value) {
            return super.fromString(value);
        }

        @Override
        public String toString(Long value) {
            String str_date = simpleDateFormat.format(value);
            return str_date;
        }

    }

    private static boolean isTimerStarted = false;
    private static Thread serviceSbora;

    /**
     * Старт таймера времени выхода и отрисовки графика
     *
     * @return
     */

    private String startTimer() {

        int timeTick = 1000;//градация шкалы, мс

        initMedian_chart_service(timeTick);//создание сервиса графика

        if (((DetectorDevice.ChinaSource) selDetector.getDevice()).isOnline() && !isTimerStarted) {

            Long startTime = System.currentTimeMillis(); //время старта

            initTimer(startTime);   //запуск самого таймера

            isTimerStarted = !isTimerStarted;   //фиксания события
        } else {
            //игнор
            return "Not CNCT";
        }

        if (timechart == null) {
            timechart = getNew_chart();     //  создание основного графика
            new Thread(() -> {              //  запуск отрисовки графика
                median_chart_service.restart();
            }).start();

        }
        return "str_date";
    }

    private String stopTimer() {

        if (((DetectorDevice.ChinaSource) selDetector.getDevice()).isOnline() && isTimerStarted) {

            if (median_chart_service.isRunning()) {
                median_chart_service.cancel();
            }
            isTimerStarted = !isTimerStarted;   //фиксания события
        }

        return "stoped";
    }

    public static int i = 900;//тестовые данные
    public static int TARGET_TEMP = 80;//целевая температура, К

    /**
     * Инициализация таймера
     *
     * @param startTime начальное время. Время старта
     */
    private void initTimer(Long startTime) {

        if (tm != null) {
            tm.cancel();
            tm = null;
            return;
        }
        tm = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {

                params.setTempValue(i++);//тест
                if (params.getTemp() <= TARGET_TEMP) {
                    String txt = "Таймер остановлен!\n" +
                            "Время выхода на рабочий режим: " + simpleDateFormat2.format(params.getTime()) + ".";
                    String txt2 = "Рабочий температурный режим: <" + TARGET_TEMP + " К.\n" +
                            "Время выхода на рабочий температурный режим не должно превышать 2 мин.";
                    showAlert(txt, txt2);
                    tm.cancel();
                    tm = null;
                    isTimerStarted = !isTimerStarted;
                } else {
                    params.setTempValue(i++);
                    long l = System.currentTimeMillis() - startTime;
                    params.setTime(l);
                }
            }
        };
        tm.schedule(timerTask, 0, 1000);

    }

    private static void showAlert(String alertTxt, String txt2) {

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Внимание!");
            alert.setHeaderText(alertTxt);
            alert.setContentText(txt2);
            Rectangle2D bounds = Screen.getPrimary().getBounds();
            alert.setX(bounds.getMaxX() / 2 - alert.getDialogPane().getWidth() / 2);
            alert.setY(bounds.getMaxY() / 2 + alert.getDialogPane().getHeight() * 2);
            alert.show();
        });
    }

    static private Timer tm;
    static private TimerTask timerTask;

    /**
     * Запуск графика наработки
     */
    private void initMedian_chart_service(int millis_timer_medianChart) {

        median_chart_service = new TimeChartService(this, millis_timer_medianChart);
        median_chart_service.setPeriod(Duration.millis(millis_timer_medianChart));
        median_chart_service.setRestartOnFailure(true);
    }

    public MultipleAxesLineChart getLineChart_time() {
        return timechart;
    }

    public final static int X_DATA_COUNT = 3 * 60;

    private XYChart.Series<Number, Number> prepareSeries(String name) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);
        for (int i = 0; i < X_DATA_COUNT; i++) {
            series.getData().add(new XYChart.Data<>(i, 0));
        }
        return series;
    }

    private long tick = TimeUnit.SECONDS.toMillis(1);

    public MultipleAxesLineChart getNew_chart() {

        BorderPane borderPane = new BorderPane();
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        //  xAxis.setUpperBound(X_DATA_COUNT);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {

            private final SimpleDateFormat format = new SimpleDateFormat("H:mm:ss");
            private final long hours = TimeUnit.HOURS.toMillis(3);

            @Override
            public String toString(Number object) {


                System.out.println();
                return format.format(new Date((tick * object.longValue()) - hours));
            }

            @Override
            public Number fromString(String string) {

                return null;
            }
        });


        yAxis.setLabel("Среднее значение, мВ");
        LineChart baseChart = new LineChart(xAxis, yAxis);

        XYChart.Series<Number, Number> baseSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> ser_temp = new XYChart.Series<>();

        baseSeries.setName("Среднее значение, мВ");
        // baseSeries.prepareSeries("Среднее значение, мВ");

        ser_temp.setName("Температура термодатчика, К");

        baseChart.getData().add(baseSeries);
        //  baseChart.getData().add(prepareSeries("Среднее значение, мВ"));

        baseChart.setAnimated(false);
        MultipleAxesLineChart chart = new MultipleAxesLineChart(baseChart, javafx.scene.paint.Color.RED);
        chart.addSeries(ser_temp, javafx.scene.paint.Color.BLUE);
        borderPane.setCenter(chart);
        borderPane.setBottom(chart.getNewLegend());

        Stage stage = new Stage();
        stage.setOnCloseRequest(t -> {

            timechart = null;


            if (median_chart_service.isRunning()) {
                median_chart_service.cancel();
            }

        });
        Scene scene = new Scene(borderPane, 800, 500);
        stage.setTitle("Временная диаграмма");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(true);
        stage.show();


        return chart;
    }

    @FXML
    private void loadExp(ActionEvent event) throws IOException {

        //todo Загрузка файла.
        //todo Парсинг.
    }

    @FXML
    private void saveExp(ActionEvent event) throws IOException {

        //todo Вызов окна сохранения файла
    }

    @FXML
    private void showManual(ActionEvent event) throws IOException {

        String PATH = "PdfView.fxml";

        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PATH));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Руководство по эксплуатации");
        stage.setScene(scene);
        Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize ();
        stage.setHeight(sSize.getHeight()*.8);
        stage.setWidth(sSize.getWidth()*.8);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setAlwaysOnTop(true);
        stage.show();

    }

    @FXML
    private void showAbout(ActionEvent event) throws IOException {

        String PATH = "aboutPage.fxml";

        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PATH));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setTitle("О программе ИС2");
        stage.setScene(scene);

        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();

    }
}