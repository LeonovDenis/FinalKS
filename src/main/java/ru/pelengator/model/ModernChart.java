package ru.pelengator.model;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.decimal4j.util.DoubleRounder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.fx.overlay.CrosshairOverlayFX;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import static ru.pelengator.API.utils.Utils.MASHTAB;

/**
 * Класс вспомогательных графиков
 */
public class ModernChart {

    static JFreeChart charttt;


    /**
     * Вложенный класс панели графика
     */
    public static class MyPane extends StackPane implements ChartMouseListenerFX {

        private ChartViewer chartViewer;//окно для вывода графика
        private Crosshair xCrosshair;//перекрестие
        private Crosshair yCrosshair;//перекрестие
        private Crosshair yCrosshair2;//перекрестие
        private Crosshair yCrosshair3;//перекрестие

        private boolean FL_BPS = true;

        /**
         * Конструктор
         *
         * @param title  Заголовок
         * @param xLable Подпись по оси Х
         * @param yLable Подпись по оси У
         * @param start  Первое значение
         * @param end    Последнее значение
         * @param mass   Массив данных
         */
        public MyPane(String title, String xLable, String yLable, int start, int end, double[]... mass) {
            XYDataset  dataset = createDataset(start, end, mass);

            JFreeChart chart = createChart(dataset, title, xLable, yLable);
            charttt = chart;
            XYPlot xyPlot = chart.getXYPlot();

      //      final Plot plot = chart.getPlot();
     //       plot.setBackgroundPaint( new Color(130, 189, 66) );

            xyPlot.getDomainAxis().setLowerBound(start - 10);
            xyPlot.getDomainAxis().setUpperBound(end + 10);


            this.chartViewer = new ChartViewer(chart);

            this.chartViewer.addChartMouseListener(this);
            getChildren().add(this.chartViewer);
            CrosshairOverlayFX crosshairOverlay = new CrosshairOverlayFX();
            this.xCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(1f));
            this.xCrosshair.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1, new float[]{5.0f, 5.0f}, 0));
            this.xCrosshair.setLabelVisible(true);
            this.yCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(1f));
            this.yCrosshair.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1, new float[]{5.0f, 5.0f}, 0));
            this.yCrosshair.setLabelVisible(true);

            this.yCrosshair2 = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(1f));
            this.yCrosshair2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1, new float[]{5.0f, 5.0f}, 0));
            this.yCrosshair2.setLabelVisible(true);
            this.yCrosshair2.setLabelFont(new Font("Tahoma", 0, 15));
            this.yCrosshair2.setLabelOutlineVisible(false);
            this.yCrosshair2.setLabelXOffset(5);
            this.yCrosshair2.setLabelYOffset(5);
            this.yCrosshair2.setLabelBackgroundPaint(new Color(0, 0, 0, 0));

            this.yCrosshair3 = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(1f));
            this.yCrosshair3.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1, new float[]{5.0f, 5.0f}, 0));
            this.yCrosshair3.setLabelVisible(true);
            this.yCrosshair3.setLabelFont(new Font("Tahoma", 0, 15));
            this.yCrosshair3.setLabelOutlineVisible(false);
            this.yCrosshair3.setLabelXOffset(5);
            this.yCrosshair3.setLabelYOffset(5);
            this.yCrosshair3.setLabelBackgroundPaint(new Color(0, 0, 0, 0));

            this.yCrosshair.setLabelFont(new Font("Tahoma", 0, 15));
            this.xCrosshair.setLabelFont(new Font("Tahoma", 0, 15));
            this.xCrosshair.setLabelOutlineVisible(false);
            this.xCrosshair.setLabelXOffset(5);
            this.xCrosshair.setLabelYOffset(5);
            this.yCrosshair.setLabelOutlineVisible(false);
            this.yCrosshair.setLabelXOffset(5);
            this.yCrosshair.setLabelYOffset(5);
            this.xCrosshair.setLabelBackgroundPaint(new Color(0, 0, 0, 0));
            this.yCrosshair.setLabelBackgroundPaint(new Color(0, 0, 0, 0));
            crosshairOverlay.addDomainCrosshair(xCrosshair);
            crosshairOverlay.addRangeCrosshair(yCrosshair);

            crosshairOverlay.addRangeCrosshair(yCrosshair2);
            crosshairOverlay.addRangeCrosshair(yCrosshair3);


            Platform.runLater(() -> {
                this.chartViewer.getCanvas().addOverlay(crosshairOverlay);
            });
        }

        @Override
        public void chartMouseClicked(ChartMouseEventFX event) {
            // ignore
        }

        @Override
        public void chartMouseMoved(ChartMouseEventFX event) {
            Rectangle2D dataArea = this.chartViewer.getCanvas().getRenderingInfo().getPlotInfo().getDataArea();
            JFreeChart chart = event.getChart();
            XYPlot plot = (XYPlot) chart.getPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea,
                    RectangleEdge.BOTTOM);
            // убирает перекрестие если указатель за пределами графика
            if (!xAxis.getRange().contains(x)) {
                x = Double.NaN;
            }
            this.xCrosshair.setValue((int) x);

         //   if (plot.getDataset().getSeriesCount() == 2) {
                double y2 = DatasetUtils.findYValue(plot.getDataset(), 1, (int) x);
                this.yCrosshair2.setValue(y2);

            double y3 = DatasetUtils.findYValue(plot.getDataset(), 2, (int) x);
            this.yCrosshair3.setValue(y3);
         //   }

            double y = DatasetUtils.findYValue(plot.getDataset(), 0, (int) x);
            this.yCrosshair.setValue(y);
        }

    }

    /**
     * Создание датасета данных для 30 и 40
     *
     * @param start
     * @param end
     * @param mass
     * @return
     */
    private static XYDataset createDataset(int start, int end, double[]... mass) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        if (mass == null) {
            return dataset;
        }
        String label = "";
        for (int i = 2; i < mass.length; i++) {
            if (i == 2) {
                label = "Main";
            } else if (i == 3){
                label = "Pixel";
            }else{
                label = "Correcting";
            }

            XYSeries series = new XYSeries(label);
            double[] masivY = mass[i];
            double[] masivX = mass[0];
            double[] masivXValues = mass[1];

            for (int j = 0; j < masivX.length; j++) {
                double v = (masivXValues[j]*masivY[0]+ masivY[1])* MASHTAB;
                double round = DoubleRounder.round(v, 0);
                series.add(masivX[j], (int)round);
            }
            dataset.addSeries(series);
        }

        return dataset;
    }


    /**
     * Создание графика
     *
     * @param dataset
     * @param title
     * @param xLable
     * @param yLable
     * @return
     */
    private static JFreeChart createChart(XYDataset dataset, String title, String xLable, String yLable) {
        JFreeChart chart = ChartFactory.createXYLineChart(title, xLable, yLable, dataset);
        return chart;
    }

    /**
     * Точка входа в класс
     *
     * @param winTitle
     * @param title
     * @param xLable
     * @param yLable
     * @param start
     * @param end
     * @param mass
     */
    public void start(String winTitle, String title, String xLable, String yLable, int start, int end, double[]... mass) {
        Scene scene = new Scene(new MyPane(title, xLable, yLable, start, end, mass), 600, 300);
        Stage newWindow = new Stage();
        newWindow.setTitle(winTitle);
        newWindow.setScene(scene);
        newWindow.show();
    }

    public StackPane startView(String title, String xLable, String yLable, int start, int end,double[]... mass) {
        MyPane myPane = new MyPane(title, xLable, yLable, start, end, mass);

        return myPane;
    }

    public static JFreeChart getCharttt() {
        return charttt;
    }

    public static void setCharttt(JFreeChart charttt) {
        ModernChart.charttt = charttt;
    }
}