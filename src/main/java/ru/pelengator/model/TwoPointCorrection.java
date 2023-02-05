package ru.pelengator.model;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pelengator.Controller;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class TwoPointCorrection implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(TwoPointCorrection.class);

    private transient ExpInfo exp;

    private ArrayList<int[][]> dataArray0;
    private ArrayList<int[][]> dataArray1;

    /**
     * 0-mean0
     * 1-mean1
     * 2-temp0
     * 3-temp1
     * 4-alfa
     * 5-betta
     * 6-targetAlfa
     * 7-targetBetta
     * 8-alfaFinal
     * 9-bettaFinal
     * 10-alfaReal
     * 11-bettaReal
     */
    private ArrayList<double[][]> tempData;
    private int h = 0;
    private int w = 0;
    private transient Controller controller;


    public TwoPointCorrection(ExpInfo exp, Controller controller) {
        this.exp = exp;
        this.controller = controller;
    }


    public static boolean isCorrectionFileAlive(String Path) {

        if (loadFile(Path) != null) {
            return true;
        } else {
            return false;
        }

    }

    private static TwoPointCorrection loadFile(String Path) {
        TwoPointCorrection correctionFile = null;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(Path))) {

            correctionFile = (TwoPointCorrection) objectInputStream.readObject();

        } catch (FileNotFoundException e) {
            LOG.error("RuntimeException " + e.getMessage());

        } catch (IOException e) {
            LOG.error("RuntimeException " + e.getMessage());

        } catch (ClassNotFoundException e) {
            LOG.error("RuntimeException " + e.getMessage());

        } finally {
            return correctionFile;
        }

    }


    public static TwoPointCorrection loadData(String Path) {
        return loadFile(Path);
    }

    public boolean saveFile(String Path) {

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                new FileOutputStream(Path))) {
            objectOutputStream.writeObject(this);

            controller.getParams().setCorrectionFilePath(Path);
            Platform.runLater(() -> controller.getBt_correction().setDisable(false));
            controller.setTempCorrection(this);
        } catch (FileNotFoundException e) {
            LOG.error("FileNotFoundException", e.getMessage());
            return false;
        } catch (IOException e) {
            LOG.error("IOException", e.getMessage());
            return false;
        }

        return true;
    }



    /**
     * Количество ядер ПК
     */
    private static int MP;

    {
        MP = Runtime.getRuntime().availableProcessors();
    }

    private transient ExecutorService service;

    class MyTask implements Callable<String>, Serializable {

        private int i = 0;

        public MyTask(int i) {
            this.i = i;
        }

        @Override
        public String call() throws Exception {


            int countFrames = dataArray0.size();
            for (int j = 0; j < w; j++) {
                ArrayList<Double> arrayY = new ArrayList<>();
                ArrayList<Double> arrayX = new ArrayList<>();
                for (int k = 0; k < countFrames; k++) {

                    arrayX.add(tempData.get(2)[i][j]);
                    arrayX.add(tempData.get(3)[i][j]);

                    arrayY.add((double) dataArray0.get(k)[i][j]);

                    arrayY.add((double) dataArray1.get(k)[i][j]);

                }

                Double[] X = arrayX.toArray(new Double[0]);
                Double[] Y = arrayY.toArray(new Double[0]);
                LinearRegression linearRegression = new LinearRegression(X, Y);

                tempData.get(4)[i][j] = linearRegression.slope();
                tempData.get(5)[i][j] = linearRegression.intercept();

                tempData.get(10)[i][j] = linearRegression.slope();
                tempData.get(11)[i][j] = linearRegression.intercept();


                //   LOG.debug("Point" + i + ":" + j + "[" + X.length + ":" + Y.length + "]" + " =" + linearRegression);
            }

            return "Done";
        }
    }

    class MyCorrAlfaTask implements Callable<String>, Serializable {

        private int i = 0;

        public MyCorrAlfaTask(int i) {
            this.i = i;
        }

        @Override
        public String call() throws Exception {

            int countFrames = dataArray0.size();

            for (int j = 0; j < w; j++) {

                double alfaValue = tempData.get(4)[i][j];

                if (alfaValue == 0) {

                    tempData.get(4)[i][j] = 0;
                    tempData.get(5)[i][j] = 0;

                } else {

                    ArrayList<Double> arrayY = new ArrayList<>();
                    ArrayList<Double> arrayX = new ArrayList<>();
                    double alfa = tempData.get(6)[i][j] / tempData.get(4)[i][j];//эталон/норм
                    tempData.get(8)[i][j] = alfa;

                    for (int k = 0; k < countFrames; k++) {

                        arrayX.add(tempData.get(2)[i][j]);
                        arrayX.add(tempData.get(3)[i][j]);

                        double v = (double) dataArray0.get(k)[i][j] * alfa;
                        double v1 = (double) dataArray1.get(k)[i][j] * alfa;

                        dataArray0.get(k)[i][j] = (int) v;
                        dataArray1.get(k)[i][j] = (int) v1;

                        arrayY.add(v);
                        arrayY.add(v1);

                    }
                    Double[] X = arrayX.toArray(new Double[0]);
                    Double[] Y = arrayY.toArray(new Double[0]);
                    LinearRegression linearRegression = new LinearRegression(X, Y);

                    tempData.get(4)[i][j] = linearRegression.slope();
                    tempData.get(5)[i][j] = linearRegression.intercept();

                    //   LOG.debug("CorrPoint_ALFA" + i + ":" + j + "[" + X.length + ":" + Y.length + "]" + " =" + linearRegression);

                }

            }

            return "Done";
        }
    }

    class MyCorrBettaTask implements Callable<String>, Serializable {

        private int i = 0;

        public MyCorrBettaTask(int i) {
            this.i = i;
        }

        @Override
        public String call() throws Exception {


            int countFrames = dataArray0.size();

            for (int j = 0; j < w; j++) {

                double alfaValue = tempData.get(4)[i][j];

                if (alfaValue == 0) {

                    tempData.get(4)[i][j] = 0;
                    tempData.get(5)[i][j] = 0;

                } else {

                    ArrayList<Double> arrayY = new ArrayList<>();
                    ArrayList<Double> arrayX = new ArrayList<>();

                    double betta = tempData.get(7)[i][j] - tempData.get(5)[i][j];//эталон-норм
                    tempData.get(9)[i][j] = betta;


                    for (int k = 0; k < countFrames; k++) {

                        arrayX.add(tempData.get(2)[i][j]);
                        arrayX.add(tempData.get(3)[i][j]);

                        arrayY.add((double) dataArray0.get(k)[i][j] + betta);
                        arrayY.add((double) dataArray1.get(k)[i][j] + betta);
                    }

                    Double[] X = arrayX.toArray(new Double[0]);
                    Double[] Y = arrayY.toArray(new Double[0]);

                    LinearRegression linearRegression = new LinearRegression(X, Y);

                    tempData.get(4)[i][j] = linearRegression.slope();
                    tempData.get(5)[i][j] = linearRegression.intercept();

                    //    LOG.debug("CorrPoint_BETTA" + i + ":" + j + "[" + X.length + ":" + Y.length + "]" + " =" + linearRegression);

                }

            }

            return "Done";
        }
    }

    private void calckRegression() {

        service = Executors.newFixedThreadPool(MP);
        for (int i = 0; i < h; i++) {
            MyTask task = new MyTask(i);
            service.submit(task);
        }

        service.shutdown();
        while (!service.isTerminated()) {
            try {
                service.awaitTermination(100, TimeUnit.MILLISECONDS);
                //  LOG.debug("awaitTermination calckRegression");
            } catch (InterruptedException e) {
                LOG.error("Interapted calckRegression");
            }
            //  LOG.debug("Not termetated calckRegression");
        }
        LOG.debug("Regression metod DONE");
    }

    public boolean activation() {

        if (inite()) {
            fillEtalonAB();//0,63 n + 5013,79  (R^2 = 0,000) [127][1]
            calckRegression();//-24.72+5383
            fixA();//-0.02
            fixB();//5151.33
            return true;
        }

        return false;
    }

    private void fixB() {
        service = Executors.newFixedThreadPool(MP);
        for (int i = 0; i < h; i++) {
            MyCorrBettaTask task = new MyCorrBettaTask(i);
            service.submit(task);
        }

        service.shutdown();
        while (!service.isTerminated()) {
            try {
                service.awaitTermination(100, TimeUnit.MILLISECONDS);
                //  LOG.error("awaitTermination fixB");
            } catch (InterruptedException e) {
                LOG.error("Interapted fixB");
            }
            //  LOG.debug("Not termetated fixB");
        }
        LOG.debug("FixB metod DONE");
    }

    private void fixA() {
        service = Executors.newFixedThreadPool(MP);
        for (int i = 0; i < h; i++) {
            MyCorrAlfaTask task = new MyCorrAlfaTask(i);
            service.submit(task);
        }

        service.shutdown();
        while (!service.isTerminated()) {
            try {
                service.awaitTermination(100, TimeUnit.MILLISECONDS);
                //  LOG.debug("awaitTermination fixA");
            } catch (InterruptedException e) {
                LOG.error("Interapted fixA");
            }
            //  LOG.debug("Not termetated fixA");
        }
        LOG.debug("FixA metod DONE");
    }

    private void fillEtalonAB() {

        ArrayList<Double> arrayY = new ArrayList<>();
        ArrayList<Double> arrayX = new ArrayList<>();
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {

                arrayX.add(tempData.get(2)[i][j]);
                arrayX.add(tempData.get(3)[i][j]);

                arrayY.add(tempData.get(0)[i][j]);
                arrayY.add(tempData.get(1)[i][j]);
            }
        }
        Double[] X = arrayX.toArray(new Double[0]);
        Double[] Y = arrayY.toArray(new Double[0]);
        LinearRegression linearRegression = new LinearRegression(X, Y);

        System.out.println(linearRegression);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                tempData.get(6)[i][j] = linearRegression.slope();
                tempData.get(7)[i][j] = linearRegression.intercept();
            }
        }

    }

    private boolean inite() {
        if (exp == null) {
            LOG.error("Exp-null");
            return false;
        }

        tempData = new ArrayList<>();

        tempData.add(exp.getArifmeticMean0());
        tempData.add(exp.getArifmeticMean1());

        dataArray0 = exp.getDataArray0();
        dataArray1 = exp.getDataArray1();

        h = exp.getArifmeticMean0().length;
        w = exp.getArifmeticMean0()[0].length;

        double temp0Value = exp.getParams().getTemp0();
        double[][] temp0 = new double[h][w];
        fill(temp0, temp0Value);
        tempData.add(temp0);

        double temp1Value = exp.getParams().getTemp1();
        double[][] temp1 = new double[h][w];
        fill(temp1, temp1Value);
        tempData.add(temp1);

        double[][] alfa = new double[h][w];
        double[][] betta = new double[h][w];
        tempData.add(alfa);
        tempData.add(betta);

        double[][] alfaMean = new double[h][w];
        double[][] bettaMean = new double[h][w];
        tempData.add(alfaMean);
        tempData.add(bettaMean);

        double[][] alfaFinal = new double[h][w];
        double[][] bettaFinal = new double[h][w];
        tempData.add(alfaFinal);
        tempData.add(bettaFinal);

        double[][] alfaReal = new double[h][w];
        double[][] bettaReal = new double[h][w];
        tempData.add(alfaReal);
        tempData.add(bettaReal);
        return true;
    }

    private void fill(double[][] array, double value) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                array[i][j] = value;
            }
        }
    }

    public double[][] getAlfaForCorrection() {

        return tempData.get(8);
    }

    public double[][] getBettaForCorrection() {

        return tempData.get(9);
    }

    public double[] getCorrAB(int X, int Y) {
        double[] values = new double[2];
        values[0] = tempData.get(8)[Y][X];
        values[1] = tempData.get(9)[Y][X];
        return values;
    }


    public double[] getRealAB( int X,int Y) {
        double[] values = new double[2];
        values[0] = tempData.get(10)[Y][X];
        values[1] = tempData.get(11)[Y][X];
        return values;
    }


    public double[] getMedianAB() {
        double[] values = new double[2];
        values[0] = tempData.get(6)[0][0];
        values[1] = tempData.get(7)[0][0];
        return values;
    }

    public double[] getTempS() {
        double[] values = new double[2];
        values[0] = tempData.get(2)[0][0];
        values[1] = tempData.get(3)[0][0];
        return values;
    }
    public int[][] correct(int[][] data) {
        int h = data.length;
        int w = data[0].length;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                double alfa = getCorrAB(j, i)[0];
                if (alfa == 0) {
                    data[i][j] =(int)  findNearValue(j,i,  data);
                } else {
                    data[i][j] = (int) (data[i][j] * alfa + getCorrAB(j, i)[1]);
                }
            }
        }
        return data;
    }
    private double findNearValue(int x, int y, int[][] data) {
        double value;

        int k = 0;
        do {
            k++;
            try {
                value = getCorrAB(x - k, y - k)[0];
                if (value != 0) {
                    return (value * data[y - k][x - k]
                            + getCorrAB(x - k, y - k)[1]);
                }
            } catch (Exception e) {
                //ignore
            }

            try {
                value = (int) getCorrAB(x - k, y)[0];
                if (value != 0) {
                    return (value * data[y][x - k]
                            + getCorrAB(x - k, y)[1]);
                }
            } catch (Exception e) {
                //ignore
            }
            try {
                value =  getCorrAB(x - k, y + k)[0];
                if (value != 0) {
                    return  (value * data[y + k][x - k]
                            + getCorrAB(x - k, y + k)[1]);
                }
            } catch (Exception e) {
                //ignore
            }
            try {
                value = getCorrAB(x, y - k)[0];
                if (value != 0) {
                    return  (value * data[y - k][x]
                            + getCorrAB(x, y - k)[1]);
                }
            } catch (Exception e) {
                //ignore
            }
            try {
                value =  getCorrAB(x, y + k)[0];
                if (value != 0) {
                    return (value * data[y + k][x]
                            + getCorrAB(x, y + k)[1]);
                }
            } catch (Exception e) {
                //ignore
            }
            try {
                value =  getCorrAB(x + k, y - k)[0];
                if (value != 0) {
                    return (value * data[y - k][x + k]
                            + getCorrAB(x + k, y - k)[1]);
                }
            } catch (Exception e) {
                //ignore
            }
            try {
                value =  getCorrAB(x + k, y)[0];
                if (value != 0) {
                    return (value * data[y][x + k]
                            + getCorrAB(x + k, y)[1]);
                }
            } catch (Exception e) {
                //ignore
            }
            try {
                value = getCorrAB(x + k, y + k)[0];
                if (value != 0) {
                    return  (value * data[y + k][x + k]
                            + getCorrAB(x + k, y + k)[1]);
                }
            } catch (Exception e) {
                //ignore
            }

        } while (k<=data.length);

        return 0;
    }
}
