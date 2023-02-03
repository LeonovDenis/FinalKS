package ru.pelengator.model;

import javafx.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pelengator.Controller;

import java.io.*;

public class TwoPointCorrection implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(TwoPointCorrection.class);
    private transient Controller controller;
    private transient ActionEvent event;

    private float[][] fShadow;

    private float[][] fSun;

    private float[][] k;

    public TwoPointCorrection(float[][] fShadow, float[][] fSun) {
        this.fShadow = fShadow;
        this.fSun = fSun;
        this.k = calulateK(fShadow, fSun);
    }

    private float[][] calulateK(float[][] fShadow, float[][] fSun) {
        int h = fShadow.length;
        int w = fShadow[0].length;
        float[][] k = new float[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {

                k[i][j] = (fSun[i][j]) / (fShadow[i][j]);
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

    public void showWindow() {

    }

    public static TwoPointCorrection loadData(String Path) {

        return loadFile(Path);
    }

    public int[][] correct(int[][] data) {
        int h = data.length;
        int w = data[0].length;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {

                data[i][j] = (int) (data[i][j] * k[i][j]);
            }
        }
        return data;
    }


}
