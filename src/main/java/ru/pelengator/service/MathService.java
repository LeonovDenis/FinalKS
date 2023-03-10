package ru.pelengator.service;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pelengator.Controller;
import ru.pelengator.MathController;

import java.util.Locale;

import static ru.pelengator.API.utils.Utils.*;

/**
 * Сервис расчета потока.
 */
public class MathService extends Service<Void> {
    /**
     * Логгер.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MathService.class);

    private MathController controller;
    private Controller mainController;
    private ObservableList<TextField> fieldOptions;

    private double plank0;
    private double plank1;
    private double T0;
    private double T1;
    private double epsilin0;
    private double epsilin1;
    private double areaACHT0;
    private double areaACHT1;
    private double L0;
    private double L1;
    private double betta0;
    private double betta1;

    private double lyambdaMin;

    private double lyambdaMax;
    private double fi0;
    private double fi1;
    private double areaFPU0;
    private double areaFPU1;
    private double potok0;
    private double potok1;
    private double potok;
    private double exposure0;
    private double exposure1;
    private double exposure;


    public MathService(MathController controller) {
        this.controller = controller;
        this.mainController = controller.getMainController();
    }

    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                LOG.trace("Start task");
                updateMessage("Старт Сервиса");
                updateProgress(0.0, 1);
                updateMessage("Инициализация данных");
                initParams();
                updateProgress(0.4, 1);
                updateMessage("Расчет");
                math();
                updateProgress(0.7, 1);
                updateMessage("Сохранение");
                saveData();
                updateProgress(0.8, 1);
                updateMessage("Стоп сервис");
                updateMessage("");
                updateProgress(1, 1);
                return null;
            }

        };
    }

    /**
     * Инициализация параметров.
     */
    private void initParams() {
        LOG.trace("Init");

        fieldOptions = controller.getFieldOptions();
        if (fieldOptions == null || fieldOptions.isEmpty()) {
            failed();
        }

        T0 = getValueFromField(1)+273; //приведение в кельвины
        T1 = getValueFromField(2)+273;

        plank0 = getValueFromField(3);
        plank1 = getValueFromField(4);

        epsilin0 = getValueFromField(5);
        epsilin1 = getValueFromField(6);

        areaACHT0 = sCircle(getValueFromField(7)); //приведение в площадь
        areaACHT1 = sCircle(getValueFromField(8));

        L0 = getValueFromField(9)/1000d;//приведение в метры
        L1 = getValueFromField(10)/1000d;//приведение в метры

        betta0 = getValueFromField(11);
        betta1 = getValueFromField(12);

        lyambdaMin = getValueFromField(13);
        lyambdaMax = getValueFromField(14);

        fi0 = calkFiFromTemp(T0,lyambdaMin,lyambdaMax);
        fi1 = calkFiFromTemp(T1,lyambdaMin,lyambdaMax);

      //  fi0 = getValueFromField(15);
     //   fi1 = getValueFromField(16);

        areaFPU0 = getValueFromField(17) * 1.0E-12;// приведение в метры
        areaFPU1 = getValueFromField(18) * 1.0E-12;

    }

    /**
     * Расчетная функция.
     */
    private void math() {

        potok0 = potok(T0, plank0, epsilin0, areaACHT0, L0, betta0*fi0, areaFPU0);
        potok1 = potok(T1, plank1, epsilin1, areaACHT1, L1, betta1*fi1, areaFPU1);
        exposure0 = obluch(T0, plank0, epsilin0,areaACHT0,L0, betta0*fi0);
        exposure1 = obluch(T1, plank1, epsilin1, areaACHT1,L1, betta1*fi1);
        potok = potok1 - potok0;
        exposure = exposure1 - exposure0;

    }

    /**
     * Печать потока.
     *
     * @param controller
     * @param potok0     поток при меньшей засветке.
     * @param potok1     поток при большей засветке.
     * @param potok      итоговый поток.
     * @param exposure   итоговая одлученность.
     */
    private void printPotok(MathController controller, double potok0, double potok1, double potok,
                            double exposure0, double exposure1, double exposure) {

        String ffi0 = String.format(Locale.CANADA, "%.4f", fi0).toUpperCase();
        String ffi1 = String.format(Locale.CANADA, "%.4f", fi1).toUpperCase();

        String fpotok0 = String.format(Locale.CANADA, "%.2e", potok0).toUpperCase();
        String fpotok1 = String.format(Locale.CANADA, "%.2e", potok1).toUpperCase();
        String fpotok = String.format(Locale.CANADA, "%.2e", potok).toUpperCase();

        String fexposure0 = String.format(Locale.CANADA, "%.2e", exposure0).toUpperCase();
        String fexposure1 = String.format(Locale.CANADA, "%.2e", exposure1).toUpperCase();
        String fexposure = String.format(Locale.CANADA, "%.2e", exposure).toUpperCase();

        Platform.runLater(() -> {

            controller.getTfFi0().setText(ffi0);
            controller.getTfFi1().setText(ffi1);

            controller.getLab_potok0().setText(fpotok0);
            controller.getLab_potok1().setText(fpotok1);
            controller.getLab_potok().setText(fpotok);

            controller.getLab_exposure0().setText(fexposure0);
            controller.getLab_exposure1().setText(fexposure1);
            controller.getLab_exposure().setText(fexposure);
        });

    }

    /**
     * Сохраняем полученные данные
     */
    private void saveData() {
        LOG.trace("Printing");
        printPotok(controller, potok0, potok1, potok, exposure0, exposure1, exposure);

        LOG.trace("Saving");
        mainController.getParams().setFi0(fi0);
        mainController.getParams().setFi1(fi1);

        mainController.getParams().setPotok(potok);
        mainController.getParams().setPotok0(potok0);
        mainController.getParams().setPotok1(potok1);
        mainController.getParams().setExposure(exposure);
        mainController.getSelExp().setParams(mainController.getParams());
        controller.saveValuesToParams();

    }


    @Override
    protected void succeeded() {
        super.succeeded();
        controller.getMainController().save();

    }

    @Override
    protected void cancelled() {
        super.cancelled();
    }

    /**
     * Получение значения из текстового поля
     *
     * @param position позиуия поля в списке
     * @return double значение
     */
    private double getValueFromField(int position) {
        String text = fieldOptions.get(position).getText().strip().toUpperCase();
        double value = Double.parseDouble(text);
        return value;
    }
}
