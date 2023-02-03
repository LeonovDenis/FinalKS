package ru.pelengator.model;

import javafx.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pelengator.API.utils.Utils;
import ru.pelengator.Controller;

import java.io.*;

public class TwoPointCorrection implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(TwoPointCorrection.class);
    private transient Controller controller;
    private transient ActionEvent event;

    private double[][] fShadow;

    private double[][] fSun;

    private float[][] k;

    private float[][] A;//Y=A*X+B
    private float[][] B;


    private double shMEAN=0;

    public TwoPointCorrection(double[][] fShadow, double[][] fSun) throws  Exception{
        this.fShadow = fShadow;
        this.fSun = fSun;
        this.shMEAN= Utils.makeMaxMeanMin(fShadow,true,0)[1];
      //  this.k = calulateK(fShadow, fSun);
    }

    private float[][] calulateK(double[][] fShadow, double[][] fSun) {
        int h = fShadow.length;
        int w = fShadow[0].length;
        float[][] k = new float[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {




            //    k[i][j] = (fSun[i][j]) / (fShadow[i][j]);
            }
        }
        return k;
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

    public  boolean saveFile(String Path) {

        try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                new FileOutputStream(Path))){
            objectOutputStream.writeObject(this);
        } catch (FileNotFoundException e) {
            LOG.error("FileNotFoundException",e.getMessage());
            return false;
        } catch (IOException e) {
            LOG.error("IOException",e.getMessage());
            return false;
        }

        return true;
    }

    public int[][] correct(int[][] data) {
        int h = data.length;
        int w = data[0].length;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {

             //   data[i][j] = (int) (data[i][j] * k[i][j]);
            }
        }
        return data;
    }


}
