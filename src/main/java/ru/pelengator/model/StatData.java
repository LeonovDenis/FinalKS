package ru.pelengator.model;

import ru.pelengator.API.utils.StatisticsUtils;
import ru.pelengator.Controller;

/**
 * Статистические данные.
 */
public class StatData {

    private int[][] data = null;
    private float[] dataArray = null;
    private float[] skoArray;
    private StatisticsUtils statisticsUtils;

    /**
     * Конструктор.
     *
     * @param data входящий массив данных.
     */
    public StatData(int[][] data) {
        assert data != null;
        this.data = data;
        init(data);
    }

    /**
     * Возврат массива СКО.
     * i- Y; k- X
     *
     * @return массив СКО.
     */
    public synchronized float[] getSKOArray() {
        assert data != null;
        StatisticsUtils[] statSKO = new StatisticsUtils[data.length];
        float[] statSKOfloat = new float[data.length];

        for (int i = 0; i < data.length; i++) {
            statSKO[i] = new StatisticsUtils();
            for (int k = 0; k < data[0].length; k++) {
                statSKO[i].addValue(data[i][k]);
            }
            statSKOfloat[i] = (float) statSKO[i].getStdDev() * Controller.getMASHTAB();
        }
        skoArray = statSKOfloat;
        return statSKOfloat;
    }

    /**
     * Возврат массива СКО по столбцам.
     *
     * @return массив СКО.
     */
    public synchronized float[] getSKOArrayHorisontal() {
        assert data != null;
        StatisticsUtils[] statSKO = new StatisticsUtils[data[0].length];
        float[] statSKOfloat = new float[data[0].length];

        for (int i = 0; i < data[0].length; i++) {
            statSKO[i] = new StatisticsUtils();
            for (int k = 0; k < data.length; k++) {
                statSKO[i].addValue(data[k][i]);
            }
            statSKOfloat[i] = (float) statSKO[i].getStdDev() * Controller.getMASHTAB();
        }
        skoArray = statSKOfloat;
        return statSKOfloat;
    }


    /**
     * Инициализация массива данных.
     *
     * @param data массив данных.
     */
    private synchronized void init(int[][] data) {
        assert data != null;
        int i = 0;
        statisticsUtils = new StatisticsUtils();
        dataArray = new float[data.length * data[0].length];
        for (int h = 0; h < data.length; h++) {
            for (int w = 0; w < data[0].length; w++) {
                statisticsUtils.addValue(data[h][w]);
                dataArray[i++] = data[h][w];
            }
        }
    }

    /**
     * Перевод двумерного массива в список.
     *
     * @return список исходных данных.
     */
    public synchronized float[] getDataArray() {
        return dataArray;
    }

    /**
     * Средний сигнал по выборке.
     *
     * @return мВ.
     */
    public synchronized float getMean() {

        float value = (float) (statisticsUtils.getMean() * Controller.getMASHTAB());

        return value;
    }

    /**
     * Среднее СКО по выборке.
     *
     * @return мВ.
     */
    public synchronized float getSKO() {
        float value;
        StatisticsUtils statisticsUtils1 = new StatisticsUtils();
        for (float data :
                skoArray) {
            statisticsUtils1.addValue((long) data);
        }
        value = (float) (statisticsUtils1.getMean());
        return value;
    }


    /**
     * Максимальный сигнал по выборке.
     *
     * @return мВ.
     */
    public synchronized float getMAX() {
        float value = statisticsUtils.getMax() * Controller.getMASHTAB();

        return value;
    }

    /**
     * Минимальный сигнал по выборке.
     *
     * @return мВ.
     */
    public synchronized float getMin() {
        float value = statisticsUtils.getMin() * Controller.getMASHTAB();

        return value;
    }

    public synchronized float[] getSignalFromStroka(int number) {
        assert data != null;

        int lengthX = data[0].length;
        int lengthY = data.length;

        float[] statSignalfloat = new float[lengthX];

        if (number > lengthY) {
            number=lengthY-1;
        }

        for (int i = 0; i < lengthX; i++) {

            statSignalfloat[i] = (data[lengthY - number - 1][i] * Controller.getMASHTAB());
        }
        return statSignalfloat;
    }

    public synchronized float[]  getSignalFromStolbec(int number) {
        assert data != null;

        int lengthX = data[0].length;
        int lengthY = data.length;

        float[] statSignalfloat = new float[lengthY];

        if (number > lengthX) {
            number=lengthX-1;
        }

        for (int i = 0; i < lengthY; i++) {

            statSignalfloat[i] = (data[lengthY - i - 1][number] * Controller.getMASHTAB());
        }
        return statSignalfloat;
    }
}