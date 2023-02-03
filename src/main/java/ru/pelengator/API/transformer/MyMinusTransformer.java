package ru.pelengator.API.transformer;

import ru.pelengator.API.DetectorImageTransformer;
import ru.pelengator.API.devises.china.ChinaDevice;

import java.awt.*;
import java.awt.image.BufferedImage;

import static ru.pelengator.API.utils.Utils.*;

/**
 * Трансформер для обработки и квантования изображения, полученного с {@link ChinaDevice}
 */

public class MyMinusTransformer implements DetectorImageTransformer {

    private boolean needReverseY;
    private float qvantCount;

    /**
     * Трансформер, реверсирующий строки по оси Y.
     */
    public MyMinusTransformer() {
        this(true);
    }

    /**
     * Трансформер, реверсирующий строки по оси Y, в зависимости от параметра.
     *
     * @param needReverseY true - если нужен реверс, false -если нет.
     */
    public MyMinusTransformer(boolean needReverseY) {
        this(true, ACP);
    }

    /**
     * Трансформер, реверсирующий строки по оси Y, в зависимости от параметра.
     *
     * @param needReverseY true - если нужен реверс, false -если нет.
     * @param ACP          количество отсчетов АСП
     */
    public MyMinusTransformer(boolean needReverseY, float ACP) {
        this.needReverseY = needReverseY;
        this.qvantCount = ACP;
    }

    @Override
    public BufferedImage transform(BufferedImage image) {

        convertImageMinus(image, minusValue);
        return image;
    }

    private static int minusValue = 0;

    private void convertImageMinus(BufferedImage src, int value) {
        int width = src.getWidth();
        int height = src.getHeight();
        int[][] tempData = new int[height][width];


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tempData[y][x] = src.getRGB(x, y) & 0xffffff;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                src.setRGB(x, y, convertValueMinus(tempData[y][x], value));
            }
        }


    }

    private int convertValueMinus(int sourceValue, int valueACP) {
        int BITBYTE = 255;
        double koef = (ACP - valueACP) / (BITBYTE * 4.0 + 1.0);

        sourceValue = (int) ((sourceValue - valueACP) / koef);

        int a = 0xff000000;
        int r = 0;
        int g = 0;
        int b = 0;

        if (sourceValue < 0) {
            new Color(255, 255, 255);
            r = 255;
            g = 255;
            b = 255;
        } else if (sourceValue <= BITBYTE && sourceValue >= 0) {
            new Color(0, 0, BITBYTE);
            r = 0;
            g = sourceValue;
            b = BITBYTE;
        } else if (sourceValue > BITBYTE && sourceValue <= BITBYTE * 2) {
            new Color(0, BITBYTE, BITBYTE);
            r = 0;
            g = BITBYTE;
            b = BITBYTE - (sourceValue - BITBYTE);
        } else if (sourceValue > BITBYTE * 2 && sourceValue <= BITBYTE * 3) {
            new Color(0, BITBYTE, 0);
            r = sourceValue - BITBYTE * 2;
            g = BITBYTE;
            b = 0;
        } else if (sourceValue > BITBYTE * 3 && sourceValue <= BITBYTE * 4) {
            new Color(BITBYTE, BITBYTE, 0);
            r = BITBYTE;
            g = BITBYTE - (sourceValue - BITBYTE * 3);
            b = 0;
        } else {
            new Color(BITBYTE, 0, 0);
            r = BITBYTE;
            g = 0;
            b = 0;
        }

        return a | (r << 16) | (g << 8) | b;
    }


    @Override
    public int convertValueToColor(int value) {
        return convertValueMinus(value, minusValue);
    }


    @Override
    public float getRazryadnost() {
        return this.qvantCount;
    }

    public static int getMinusValue() {
        return minusValue;
    }

    public static void setMinusValue(int minusValue) {
        MyMinusTransformer.minusValue = minusValue;
    }
}
