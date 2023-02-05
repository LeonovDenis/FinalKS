package ru.pelengator.service;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pelengator.API.utils.StatisticsUtils;
import ru.pelengator.MeasController;
import ru.pelengator.model.ExpInfo;
import ru.pelengator.model.StendParams;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ru.pelengator.API.utils.Utils.*;

/**
 * Сервис расчета параметров.
 */
public class MeasService extends Service<Void> {
    /**
     * Логгер.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MeasService.class);

    //Массивы исходных данных
    private double[][] arifmeticMeanValue_0;
    private double[][] arifmeticMeanValue_1;
    private double[][] quadraticMeanValue_0;
    private double[][] quadraticMeanValue_1;
    private double[][] SKOValue;
    private double[][] arifmeticMeanValue_delta;
    private double[][] quadraticMeanValue_delta;
    //Массивы расчитанные
    double[][] voltWatka;
    double[][] eExposure;

    //переменные
    private int sizeY;
    private int sizeX;
    private StatisticsUtils[][] dataArrayStat_0;
    private StatisticsUtils[][] dataArrayStat_1;
    //флаг расчета с/ без учета деф. пикселей
    private boolean noCorrection;
    private StendParams params;
    //итоговые средние значения по ФПУ
    private double arifmeticMean;
    private double quadraticMean;
    private double SKO;
    private double vw;
    private double exposure;
    private byte[] buffToTXT;
    private int bpInCenter;
    private int bpAll;
    private int bpInDiametr;

    private boolean withDefPx;
    private List<BadBigPoint> badBigPoints;
    /**
     * Лист с фреймами.
     */
    private ArrayList<Frame> frList;
    /**
     * Лист с квадратами для печати.
     */
    private ArrayList<BufferedImage> scList;
    /**
     * Ссылка на контроллер.
     */
    private MeasController controller;
    /**
     * Контейер для гистограмм и квадратов.
     */
    private VBox scrlPaneSa;
    private VBox scrlPaneSq;
    private VBox scrlPaneSigma;
    private VBox scrlPaneSu;

    private VBox scrlPaneEp;

    /**
     * Заготовка для отрисовки квадрата.
     */
    private BufferedImage tempImage;


    public MeasService(MeasController controller) {
        this.controller = controller;
    }

    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                LOG.trace("Starting");

                updateMessage("Старт");
                updateProgress(0.0, 1);
                updateMessage("Инициализация данных");
                updateProgress(0.1, 1);
                initParams();
                updateProgress(0.15, 1);
                updateMessage("Расчет исходных данных");

                calculateMeanValuesAndSKO();
                updateProgress(0.2, 1);


                updateMessage("Расчет срендего арифм.");
                arifmeticMean = calculateAndCheckArifmSignal(
                        true,
                        noCorrection);
                updateProgress(0.25, 1);
                updateMessage("Расчет среднего квадрат.");
                quadraticMean = calculateAndCheckQuadraticSignal(
                        true,
                        noCorrection);
                updateProgress(0.3, 1);
                updateMessage("Расчет шума");
                SKO = calculateAndCheckSKO(true,
                        noCorrection);
                updateProgress(0.35, 1);
                updateMessage("Расчет вольтовой чувств.");
                vw = calculateAndCheckVw(true,
                        noCorrection);
                updateProgress(0.45, 1);
                updateProgress(0.75, 1);
                updateMessage("Расчет пороговой облученности");
                exposure = calculateAndCheckExposure(true,
                        noCorrection);
                updateProgress(0.8, 1);
                updateMessage("Расчет итоговой дефектности");
                calculateAllDefects();
                updateMessage("Запись параметров");
                saveExpData();
                updateProgress(0.85, 1);
                updateMessage("Отрисовка результатов");
                showResults();
                updateProgress(1, 1);
                updateMessage("");

                return null;
            }

        };
    }


    /**
     * Расчет облученности и проверка брака
     *
     * @param selected     считаем ли
     * @param noCorrection с учетом бракованных пикселей или нет
     * @return среднее значение по ФПУ
     */
    private double calculateAndCheckExposure(boolean selected, boolean noCorrection) {
        double persent = params.getExposurePersent();
        String colorL = params.getPersentColorExposureL();
        String colorH = params.getPersentColorExposureH();
        DEF_TYPE type = DEF_TYPE.TYPE_EXPOSURE;

        eExposure = exposure(quadraticMeanValue_1, quadraticMeanValue_0, SKOValue,
                params.getExposure());

        if (!selected) {
            return -1;
        }

        return addBpToList(frList.get(4).getBpList(), eExposure, noCorrection,
                persent, colorL, colorH, type);
    }


    /**
     * Расчет вольтовой чувствительности и проверка брака.
     *
     * @param selected     считаем ли.
     * @param noCorrection с учетом бракованных пикселей или нет.
     * @return среднее значение по ФПУ.
     */
    private double calculateAndCheckVw(boolean selected, boolean noCorrection) {
        double persent = params.getVwPersent();
        String colorL = params.getPersentColorVwL();
        String colorH = params.getPersentColorVwH();
        DEF_TYPE type = DEF_TYPE.TYPE_VW;

        voltWatka = voltWatka(quadraticMeanValue_1, quadraticMeanValue_0, params.getPotok());

        if (!selected) {
            return -1;
        }

        return addBpToList(frList.get(3).getBpList(), voltWatka, noCorrection,
                persent, colorL, colorH, type);
    }


    /**
     * Расчет СКО и проверка брака.
     *
     * @param selected     считаем ли.
     * @param noCorrection с учетом бракованных пикселей или нет.
     * @return среднее значение по ФПУ.
     */
    private double calculateAndCheckSKO(boolean selected, boolean noCorrection) {
        double persent = params.getSKOPersent();
        String colorL = params.getPersentColorSKOL();
        String colorH = params.getPersentColorSKOH();
        DEF_TYPE type = DEF_TYPE.TYPE_SKO;

        if (!selected) {
            return -1;
            //  return addBpToList(null, SKOValue, noCorrection,persent, color, type);
        }

        return addBpToList(frList.get(2).getBpList(), SKOValue, noCorrection,
                persent, colorL, colorH, type);
    }

    /**
     * Расчет среднего квадрат. значения и проверка брака.
     *
     * @param selected     считаем ли.
     * @param noCorrection с учетом бракованных пикселей или нет.
     * @return среднее значение по ФПУ.
     */
    private double calculateAndCheckQuadraticSignal(boolean selected, boolean noCorrection) {
        double persent = params.getQuadraticMeanPersent();
        String colorL = params.getPersentColorQuadraticL();
        String colorH = params.getPersentColorQuadraticH();
        DEF_TYPE type = DEF_TYPE.TYPE_QUADRATIC;

        if (!selected) {
            return -1;
            // return addBpToList(null, quadraticMeanValue_delta, noCorrection, persent, color, type);
        }

        return addBpToList(frList.get(1).getBpList(), quadraticMeanValue_delta, noCorrection,
                persent, colorL, colorH, type);
    }

    /**
     * Расчет среднего арифм. значения и проверка брака.
     *
     * @param selected     считаем ли.
     * @param noCorrection с учетом бракованных пикселей или нет.
     * @return среднее значение по ФПУ.
     */
    private double calculateAndCheckArifmSignal(boolean selected,
                                                boolean noCorrection) {
        double persent = params.getArifmeticMeanPersent();
        String colorL = params.getPersentColorArifmL();
        String colorH = params.getPersentColorArifmH();
        DEF_TYPE type = DEF_TYPE.TYPE_ARIFMETIC;

        if (!selected) {
            return -1;
            //   return addBpToList(null, arifmeticMeanValue_delta, noCorrection,persent, color, type);
        }

        return addBpToList(frList.get(0).getBpList(), arifmeticMeanValue_delta, noCorrection,
                persent, colorL, colorH, type);
    }

    /**
     * Расчет среднего значения и ско.
     */
    private void calculateMeanValuesAndSKO() {

        takeStat();

        for (int h = 0; h < sizeY; h++) {
            for (int w = 0; w < sizeX; w++) {
                arifmeticMeanValue_0[h][w] = dataArrayStat_0[h][w].getMean() * MASHTAB / 1000.0;
                quadraticMeanValue_0[h][w] = dataArrayStat_0[h][w].getQvadraricMean() * MASHTAB / 1000.0;
                SKOValue[h][w] = dataArrayStat_0[h][w].getStdDev() * MASHTAB / 1000;

                arifmeticMeanValue_1[h][w] = dataArrayStat_1[h][w].getMean() * MASHTAB / 1000.0;

                quadraticMeanValue_1[h][w] = dataArrayStat_1[h][w].getQvadraricMean() * MASHTAB / 1000.0;
            }
        }
        for (int h = 0; h < sizeY; h++) {
            for (int w = 0; w < sizeX; w++) {
                arifmeticMeanValue_delta[h][w] = arifmeticMeanValue_1[h][w] - arifmeticMeanValue_0[h][w];
                quadraticMeanValue_delta[h][w] = quadraticMeanValue_1[h][w] - quadraticMeanValue_0[h][w];
            }
        }
    }

    /**
     * Инициализация параметров.
     */
    private void initParams() {
        int[][] dta = controller.getController().getSelExp().getDataArray0().get(0);
        sizeY = dta.length;
        sizeX = dta[0].length;
        noCorrection = true;

        arifmeticMeanValue_0 = new double[sizeY][sizeX];
        arifmeticMeanValue_1 = new double[sizeY][sizeX];

        quadraticMeanValue_0 = new double[sizeY][sizeX];
        quadraticMeanValue_1 = new double[sizeY][sizeX];

        SKOValue = new double[sizeY][sizeX];
        arifmeticMeanValue_delta = new double[sizeY][sizeX];
        quadraticMeanValue_delta = new double[sizeY][sizeX];

        voltWatka = new double[sizeY][sizeX];
        eExposure = new double[sizeY][sizeX];

        params = controller.getController().getSelExp().getParams();

        dataArrayStat_0 = new StatisticsUtils[sizeY][sizeX];
        dataArrayStat_1 = new StatisticsUtils[sizeY][sizeX];

        for (int h = 0; h < sizeY; h++) {
            for (int w = 0; w < sizeX; w++) {
                dataArrayStat_0[h][w] = new StatisticsUtils();
            }
        }
        for (int h = 0; h < sizeY; h++) {
            for (int w = 0; w < sizeX; w++) {
                dataArrayStat_1[h][w] = new StatisticsUtils();
            }
        }

        arifmeticMean = 0;
        quadraticMean = 0;
        SKO = 0;
        vw = 0;
        exposure = 0;
        frList = new ArrayList<>();
        scList = new ArrayList<>();
        badBigPoints = new ArrayList<>();

        frList.add(new Frame("Среднее арифметическое сигнала, В",
                sizeX, sizeY));
        frList.add(new Frame("Среднее квадратичное сигнала, В",
                sizeX, sizeY));
        frList.add(new Frame("СКО сигнала (шум), В",
                sizeX, sizeY));
        frList.add(new Frame("Вольтовая чувствительность, В\u00B7Вт\u00AF \u00B9",
                sizeX, sizeY));
        frList.add(new Frame("Пороговая облученность, Вт\u00B7см\u00AF \u00B2",
                sizeX, sizeY));


        scrlPaneSa = controller.getScrlPaneSa();
        scrlPaneSq = controller.getScrlPaneSq();
        scrlPaneSigma = controller.getScrlPaneSigma();
        scrlPaneSu = controller.getScrlPaneSu();
        scrlPaneEp = controller.getScrlPaneEp();

        tempImage = controller.getController().getSelExp().getTempImage();
        bpInCenter = -1;
        bpAll = -1;
        bpInDiametr = -1;
        withDefPx = true;
        buffToTXT = null;
        Platform.runLater(() -> {
            clearPane(scrlPaneSa);
            clearPane(scrlPaneSq);
            clearPane(scrlPaneSigma);
            clearPane(scrlPaneSu);
            clearPane(scrlPaneEp);
        });
        params = controller.getController().getParams();
    }

    /**
     * Набираем массив статистики.
     */
    private void takeStat() {
        ArrayList<int[][]> dataArray0 = controller.getController().getSelExp().getDataArray0();
        ArrayList<int[][]> dataArray1 = controller.getController().getSelExp().getDataArray1();
        int count = dataArray0.size();
        for (int j = 0; j < count; j++) {
            int[][] frame0 = dataArray0.get(j);
            int[][] frame1 = dataArray1.get(j);
            for (int h = 0; h < sizeY; h++) {
                for (int w = 0; w < sizeX; w++) {
                    dataArrayStat_0[h][w].addValue(frame0[h][w]);
                    dataArrayStat_1[h][w].addValue(frame1[h][w]);
                }
            }
        }
    }

    /**
     * Отображение результатов.
     */
    private void showResults() {

        Platform.runLater(() -> {
            fillTextFields();
            fillTextLabels();
            showGistAndImage(scrlPaneSa);
            showGistAndImage(scrlPaneSq);
            showGistAndImage(scrlPaneSigma);
            showGistAndImage(scrlPaneSu);
            showGistAndImage(scrlPaneEp);
        });
    }

    /**
     * Отрисовка гистограммы и малого квадрата.
     *
     * @param pane
     */
    private void showGistAndImage(VBox pane) {


        if (params.getArifmeticMeanPersent() != 0 && pane == scrlPaneSa) {

            RaspredData raspred = makeRaspred(arifmeticMeanValue_delta, "0.000E00", noCorrection, params.getArifmeticMeanPersent());

            showGistAndImageBox(pane, "Среднее арифметическое сигнала, В",
                    "Число диодов", raspred, tempImage, frList.get(0).getBpList(), scList, controller);
        }

        if (params.getQuadraticMeanPersent() != 0 && pane == scrlPaneSq) {

            RaspredData raspred = makeRaspred(quadraticMeanValue_delta, "0.000E00", noCorrection, params.getQuadraticMeanPersent());

            showGistAndImageBox(pane, "Среднее квадратичное сигнала, В",
                    "Число диодов", raspred, tempImage, frList.get(1).getBpList(), scList, controller);
        }
        if (params.getSKOPersent() != 0 && pane == scrlPaneSigma) {

            RaspredData raspred = makeRaspred(SKOValue, "0.000E00", noCorrection, params.getSKOPersent());

            showGistAndImageBox(pane, "СКО сигнала (шум), В",
                    "Число диодов", raspred, tempImage, frList.get(2).getBpList(), scList, controller);
        }
        if (params.getVwPersent() != 0 && pane == scrlPaneSu) {

            RaspredData raspred = makeRaspred(voltWatka, "0.000E00", noCorrection, params.getVwPersent());

            showGistAndImageBox(pane, "Вольтовая чувствительность, В\u00B7Вт\u00AF \u00B9",
                    "Число диодов", raspred, tempImage, frList.get(3).getBpList(), scList, controller);
        }

        if (params.getExposurePersent() != 0 && pane == scrlPaneEp) {

            RaspredData raspred = makeRaspred(eExposure, "0.000E00", noCorrection, params.getExposurePersent());

            showGistAndImageBox(pane, "Пороговая облученность, Вт\u00B7см\u00AF \u00B2",
                    "Число диодов", raspred, tempImage, frList.get(4).getBpList(), scList, controller);
        }
    }

    private void calculateAllDefects() {

        badBigPoints = bedPxToList("000000");
        bpAll = frList.get(3).getBpList().size();
        bpInCenter = bpInCentral(frList, 3, 32);
        bpInDiametr = bpInDiametr(frList, 3, StendParams.getDiametr());
    }

    /**
     * Переделать.
     *
     * @param color цвет пикселя.
     * @return
     */
    private List<BadBigPoint> bedPxToList(String color) {

        String lineseparator = System.getProperty("line.separator");

        List<BadBigPoint> list = null;
        //создаем массив
        BadBigPoint[][] tempMatrix = new BadBigPoint[sizeY][sizeX];
        //перебиваем все кадры
        int x, y = 0;

        //если в кадре есть дефекты,то
        if (!frList.get(3).getBpList().isEmpty()) {
            //список брака в этом кадре
            ArrayList<BadPoint> bpList = frList.get(3).getBpList();
            //перебираем все пиксели
            int w = frList.get(3).getSizeX();
            int h = frList.get(3).getSizeY();

            int nizIndexX = (w - StendParams.getDiametr()) / 2;
            int verhIndexX = (nizIndexX + StendParams.getDiametr() - 1);
            int nizIndexY = (h - StendParams.getDiametr()) / 2;
            int verhIndexY = (nizIndexY + StendParams.getDiametr() - 1);

            for (BadPoint bp :
                    bpList) {
                x = bp.getX();
                y = bp.getY();

                if (bp.getX() >= nizIndexX && bp.getX() <= verhIndexX
                        && bp.getY() >= nizIndexY && bp.getY() <= verhIndexY) {
                    BadBigPoint badbBigPoint = new BadBigPoint(bp, convertcolor(color));
                    tempMatrix[y][x] = badbBigPoint;
                }
            }
        }

        //Если массив имеет попадания, то создаём лист и добавляем точки

        for (int h = 0; h < sizeY; h++) {
            for (int w = 0; w < sizeX; w++) {

                if (tempMatrix[h][w] != null) {
                    if (list == null) {
                        list = new ArrayList<>();
                    }

                    list.add(tempMatrix[h][w]);
                }
            }
        }

        //создаем массив для записи в файл
        buffToTXT = extructTextLine(lineseparator, list);

        return list;
    }

    /**
     * Создание строки дефектных элементов для печати.
     *
     * @param lineseparator
     * @param list
     * @return
     */
    private byte[] extructTextLine(String lineseparator, List<BadBigPoint> list) {
        StringBuilder tempStr = new StringBuilder();
        if (list != null) {
            int count = 0;
            for (BadBigPoint bp : list) {
                tempStr.append(++count).append(".");
                tempStr.append(bp).append(lineseparator);
            }
        } else {
            tempStr.append("Нет дефектных элементов!");
        }

        return tempStr.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Заполнение полей брака.
     */
    private void fillTextLabels() {
        ///////////////////  full

        controller.getLbArifmeticMean().setText(frList.get(0).getBpList().size() == 0 ? "--" : String.valueOf(frList.get(0).getBpList().size()));
        controller.getLbQuadraticMean().setText(frList.get(1).getBpList().size() == 0 ? "--" : String.valueOf(frList.get(1).getBpList().size()));
        controller.getLbSKO().setText(frList.get(2).getBpList().size() == 0 ? "--" : String.valueOf(frList.get(2).getBpList().size()));
        controller.getLbVw().setText(frList.get(3).getBpList().size() == 0 ? "--" : String.valueOf(frList.get(3).getBpList().size()));
        controller.getLbExposure().setText(frList.get(4).getBpList().size() == 0 ? "--" : String.valueOf(frList.get(4).getBpList().size()));

        ////////////////////  32*32

        controller.getLbArifmeticMean1().setText(bpInCentral(frList, 0, 32) == 0 ? "--" : String.valueOf(bpInCentral(frList, 0, 32)));
        controller.getLbQuadraticMean1().setText(bpInCentral(frList, 1, 32) == 0 ? "--" : String.valueOf(bpInCentral(frList, 1, 32)));
        controller.getLbSKO1().setText(bpInCentral(frList, 2, 32) == 0 ? "--" : String.valueOf(bpInCentral(frList, 2, 32)));
        controller.getLbVw1().setText(bpInCentral(frList, 3, 32) == 0 ? "--" : String.valueOf(bpInCentral(frList, 3, 32)));
        controller.getLbExposure1().setText(bpInCentral(frList, 4, 32) == 0 ? "--" : String.valueOf(bpInCentral(frList, 4, 32)));

        ////////////////////  диаметр
        int diametr = StendParams.getDiametr();
        controller.getLbArifmeticMean2().setText(bpInDiametr(frList, 0, diametr) == 0 ? "--" : String.valueOf(bpInDiametr(frList, 0, diametr)));
        controller.getLbQuadraticMean2().setText(bpInDiametr(frList, 1, diametr) == 0 ? "--" : String.valueOf(bpInDiametr(frList, 1, diametr)));
        controller.getLbSKO2().setText(bpInDiametr(frList, 2, diametr) == 0 ? "--" : String.valueOf(bpInDiametr(frList, 2, diametr)));
        controller.getLbVw2().setText(bpInDiametr(frList, 3, diametr) == 0 ? "--" : String.valueOf(bpInDiametr(frList, 3, diametr)));
        controller.getLbExposure2().setText(bpInDiametr(frList, 4, diametr) == 0 ? "--" : String.valueOf(bpInDiametr(frList, 4, diametr)));

    }

    /**
     * Заполнение текстовых полей.
     */
    private void fillTextFields() {

        controller.getTfArifmeticMean().setText(arifmeticMean == -1 ? "--" :
                String.format(Locale.CANADA, "%.3e", arifmeticMean).toUpperCase());

        controller.getTfQuadraticMean().setText(quadraticMean == -1 ? "--" :
                String.format(Locale.CANADA, "%.3e", quadraticMean).toUpperCase());

        controller.getTfSKO().setText(SKO == -1 ? "--" :
                String.format(Locale.CANADA, "%.3e", SKO).toUpperCase());

        controller.getTfVw().setText(vw == -1 ? "--" :
                String.format(Locale.CANADA, "%.3e", vw).toUpperCase());

        controller.getTfExposure().setText(exposure == -1 ? "--" :
                String.format(Locale.CANADA, "%.3e", exposure).toUpperCase());

    }

    /**
     * Сохраняем полученные данные.
     */
    public boolean saveExpData() {
        LOG.trace("Saving");
        ExpInfo exp = controller.getController().getSelExp();
        exp.setArifmeticMean(arifmeticMean);
        exp.setQuadraticMean(quadraticMean);
        exp.setSKO(SKO);
        exp.setVw(vw);
        exp.setExposure(exposure);
        exp.setFrList(frList);
        exp.setScList(scList);
        exp.setBpInCenter(bpInCenter);
        exp.setBpInDiametr(bpInDiametr);
        exp.setBpAll(bpAll);
        exp.setWithDefPx(withDefPx);
        exp.setSizeX(sizeX);
        exp.setSizeY(sizeY);
        exp.setBuffToTXT(buffToTXT);

        return true;
    }


    @Override
    protected void succeeded() {
        super.succeeded();
        controller.getController().save();
    }

    @Override
    protected void cancelled() {
        super.cancelled();
    }

    @Override
    protected void failed() {
        super.failed();
        LOG.error("Failed!");
        Platform.runLater(() -> {
            controller.getController().getLab_exp_status().textProperty().unbind();
            controller.getController().getLab_exp_status().textProperty().setValue("");
        });
    }

    @Override
    public boolean cancel() {
        LOG.error("Canceled!");

        Platform.runLater(() -> {
            controller.getController().getLab_exp_status().textProperty().unbind();
            controller.getController().getLab_exp_status().textProperty().setValue("Отмена записи файла");
        });
        return super.cancel();
    }

    /**
     * Запись листа с матрицами.
     *
     * @return
     */
    public List<double[][]> getList() {
        ArrayList<double[][]> doubles = new ArrayList<>();
        doubles.add(arifmeticMeanValue_delta);
        doubles.add(quadraticMeanValue_delta);
        doubles.add(SKOValue);
        doubles.add(voltWatka);
        doubles.add(eExposure);
        return doubles;
    }
}