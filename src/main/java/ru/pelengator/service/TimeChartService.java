package ru.pelengator.service;

import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import ru.pelengator.Controller;

import java.text.SimpleDateFormat;

import static ru.pelengator.Controller.X_DATA_COUNT;


/**
 * Сервис построения временного графика
 */
public class TimeChartService extends ScheduledService<Void> {


    private static int WINDOW_SIZE;
    private static Controller controller;

    private static FloatProperty srednee = new SimpleFloatProperty(0);
    private static IntegerProperty temp = new SimpleIntegerProperty(0);

    public TimeChartService(Controller controller, int pause) {

        this.controller = controller;
        this.WINDOW_SIZE = X_DATA_COUNT;

        srednee.bind(controller.getParams().sredneeProperty());
        temp.bind(controller.getParams().tempProperty());

    }

    int i = 0;
    int ii = 1;

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (controller.getLineChart_time() != null) {

                    XYChart.Data<Number, Number> data_sred = new XYChart.Data<>(i, getSrednee());//создаем данные
                    XYChart.Data<Number, Number> data_temp = new XYChart.Data<>(i++, getTemp());//создаем данные

                    LineChart baseChart = controller.getLineChart_time().getBaseChart();
                    LineChart lineChart = controller.getLineChart_time().getBackgroundCharts().get(0);


                    ObservableList<XYChart.Series<Number, Number>> data = baseChart.getData();
                    ObservableList<XYChart.Series<Number, Number>> data2 = lineChart.getData();
                    data.get(0).getData().add(data_sred);//добавляем данные в график
                    data2.get(0).getData().add(data_temp);//добавляем данные в график

                    if (data.get(0).getData().size() > WINDOW_SIZE*ii) {  //при переполнении графика удаляем первое значение
                        //ignore
                    }

                }
                return null;
            }
        };
    }

    public static float getSrednee() {
        return srednee.get();
    }

    public static float getTemp() {
        return temp.get();
    }

}