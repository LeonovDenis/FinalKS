package ru.pelengator.service;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.Chart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.multipdf.PDFCloneUtility;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pelengator.API.utils.ImageUtils;
import ru.pelengator.model.ExpInfo;
import ru.pelengator.model.StendParams;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.pelengator.API.utils.Utils.bpInCentral;
import static ru.pelengator.API.utils.Utils.bpInDiametr;
import static ru.pelengator.App.loadFilePath;

/**
 * Заполнение форм в pdf файле.
 */
public class DocMaker {
    /**
     * Логгер.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DocMaker.class);
    private static Map<String, Object> map;
    private static PDDocument pDDocument;
    private static PDAcroForm pDAcroForm;
    private static PDResources pDResources;
    private static String fileName = "protokol.pdf";
    private static File pdfFile;
    private static Node[] src;
    private static ExpInfo exp;
    private static List<String> fontNames;
    private static PDFont myFont;

    public DocMaker(ExpInfo exp, File pdfFile, Node... src) {

        this.map = createList(exp);
        this.pdfFile = pdfFile;
        this.src = src;
        this.exp = exp;
        loadPrepareFile();
        this.myFont = loadTrueTypeFont(pDDocument, "ARIALUNI.TTF");
        this.fontNames = prepareFont(pDDocument, Arrays.asList(myFont,
                PDType1Font.COURIER));

    }


    /**
     * Набивка полей.
     *
     * @return
     */
    private Map<String, Object> createList(ExpInfo exp) {

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("zakaz", exp.getParams().getZakaz());
        hashMap.put("dogovor", exp.getParams().getDogovor());
        hashMap.put("metodika", exp.getParams().getMetodika());
        hashMap.put("nomer_0", exp.getParams().getNomer_0());
        hashMap.put("nomer_1", exp.getParams().getNomer_0());
        hashMap.put("nomer_2", exp.getParams().getNomer_0());
        hashMap.put("nomer_3", exp.getParams().getNomer_0());
        hashMap.put("nomer", exp.getParams().getNomer());
       // hashMap.put("copy", exp.getParams().getCopy());
        hashMap.put("otk", exp.getParams().getOtk());
        hashMap.put("data", exp.getParams().getData());

        //Дата
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-YYYY");
        String format = simpleDateFormat.format(new Timestamp(System.currentTimeMillis()));
        hashMap.put("time", format);


        hashMap.put("fps", String.valueOf(5.0));
        hashMap.put("int", String.valueOf(exp.getParams().getTempInt()));
        hashMap.put("ku", String.valueOf(exp.getParams().isTempKU() ? "3" : "1"));


        hashMap.put("matr", exp.getSizeX() + "*" + exp.getSizeY());
        hashMap.put("area", "900");
        hashMap.put("temp_0", "77");
        hashMap.put("temp_1", String.valueOf(exp.getParams().getTemp1()));
        hashMap.put("temp_2", String.valueOf(exp.getParams().getTemp0()));
        hashMap.put("out", "2");
        hashMap.put("count", String.valueOf(exp.getParams().getCountFrames()));

        ///////

        hashMap.put("VPD", "5.0");
        hashMap.put("VND", "GND");
        hashMap.put("VNEGOUT", "GND");
        hashMap.put("VPOSOUT", "5.0");
        hashMap.put("VDET_ADJ", "-");
        hashMap.put("VOUTREF", "1.6");
        hashMap.put("VREF", "1.6");
        hashMap.put("VOS", String.valueOf(exp.getParams().getTempVOS() / 1000.0));
        hashMap.put("VNEG", "GND");
        hashMap.put("VPOS", "5.0");

        ///////

        //1
        double persent = -1;
        double value = exp.getArifmeticMean();
        if (value < 0 || Double.isNaN(value)) {
            hashMap.put("TXT_0_0", "---");
            hashMap.put("TXT_1_0", "---");
            hashMap.put("TXT_2_0", "---");
            hashMap.put("TXT_3_0", "---");
            hashMap.put("TXT_4_0", "---");
            hashMap.put("TXT_5_0", "---");
        } else {
            hashMap.put("TXT_0_0", exp.getParams().getTXT_0_0());
            hashMap.put("TXT_1_0", String.format(Locale.CANADA, "%.2e", value).toUpperCase());

            persent = exp.getParams().getArifmeticMeanPersent();
            if (persent != 0) {
                hashMap.put("TXT_2_0", String.format(Locale.CANADA, "%.2f", persent));
                hashMap.put("TXT_3_0", String.valueOf(bpInCentral(exp.getFrList(), 0, 32)));
                hashMap.put("TXT_4_0", String.valueOf(exp.getFrList().get(0).getBpList().size()));
                hashMap.put("TXT_5_0", String.valueOf(bpInDiametr(exp.getFrList(), 0, StendParams.getDiametr())));
            } else {
                hashMap.put("TXT_2_0", "---");
                hashMap.put("TXT_3_0", "---");
                hashMap.put("TXT_4_0", "---");
                hashMap.put("TXT_5_0", "---");
            }
        }
        //2
        value = exp.getQuadraticMean();
        if (value < 0 || Double.isNaN(value)) {
            hashMap.put("TXT_0_1", "---");
            hashMap.put("TXT_1_1", "---");
            hashMap.put("TXT_2_1", "---");
            hashMap.put("TXT_3_1", "---");
            hashMap.put("TXT_4_1", "---");
            hashMap.put("TXT_5_1", "---");
        } else {
            hashMap.put("TXT_0_1", exp.getParams().getTXT_0_1());
            hashMap.put("TXT_1_1", String.format(Locale.CANADA, "%.2e", value).toUpperCase());

            persent = exp.getParams().getQuadraticMeanPersent();
            if (persent != 0) {
                hashMap.put("TXT_2_1", String.format(Locale.CANADA, "%.2f", persent));
                hashMap.put("TXT_3_1", String.valueOf(bpInCentral(exp.getFrList(), 1, 32)));
                hashMap.put("TXT_4_1", String.valueOf(exp.getFrList().get(1).getBpList().size()));
                hashMap.put("TXT_5_1", String.valueOf(bpInDiametr(exp.getFrList(), 1, StendParams.getDiametr())));
            } else {
                hashMap.put("TXT_2_1", "---");
                hashMap.put("TXT_3_1", "---");
                hashMap.put("TXT_4_1", "---");
                hashMap.put("TXT_5_1", "---");
            }
        }
        //3
        value = exp.getSKO();
        if (value < 0 || Double.isNaN(value)) {
            hashMap.put("TXT_0_2", "---");
            hashMap.put("TXT_1_2", "---");
            hashMap.put("TXT_2_2", "---");
            hashMap.put("TXT_3_2", "---");
            hashMap.put("TXT_4_2", "---");
            hashMap.put("TXT_5_2", "---");
        } else {
            hashMap.put("TXT_0_2", exp.getParams().getTXT_0_2());
            hashMap.put("TXT_1_2", String.format(Locale.CANADA, "%.2e", value).toUpperCase());

            persent = exp.getParams().getSKOPersent();
            if (persent != 0) {
                hashMap.put("TXT_2_2", String.format(Locale.CANADA, "%.2f", persent));
                hashMap.put("TXT_3_2", String.valueOf(bpInCentral(exp.getFrList(), 2, 32)));
                hashMap.put("TXT_4_2", String.valueOf(exp.getFrList().get(2).getBpList().size()));
                hashMap.put("TXT_5_2", String.valueOf(bpInDiametr(exp.getFrList(), 2, StendParams.getDiametr())));
            } else {
                hashMap.put("TXT_2_2", "---");
                hashMap.put("TXT_3_2", "---");
                hashMap.put("TXT_4_2", "---");
                hashMap.put("TXT_5_2", "---");
            }
        }

        //4
        value = exp.getVw();
        if (value < 0 || Double.isNaN(value)) {
            hashMap.put("TXT_0_3", "---");
            hashMap.put("TXT_1_3", "---");
            hashMap.put("TXT_2_3", "---");
            hashMap.put("TXT_3_3", "---");
            hashMap.put("TXT_4_3", "---");
            hashMap.put("TXT_5_3", "---");
        } else {
            hashMap.put("TXT_0_3", exp.getParams().getTXT_0_3());
            hashMap.put("TXT_1_3", String.format(Locale.CANADA, "%.2e", value).toUpperCase());

            persent = exp.getParams().getVwPersent();
            if (persent != 0) {
                hashMap.put("TXT_2_3", String.format(Locale.CANADA, "%.2f", persent));
                hashMap.put("TXT_3_3", String.valueOf(bpInCentral(exp.getFrList(), 3, 32)));
                hashMap.put("TXT_4_3", String.valueOf(exp.getFrList().get(3).getBpList().size()));
                hashMap.put("TXT_5_3", String.valueOf(bpInDiametr(exp.getFrList(), 3, StendParams.getDiametr())));
            } else {
                hashMap.put("TXT_2_3", "---");
                hashMap.put("TXT_3_3", "---");
                hashMap.put("TXT_4_3", "---");
                hashMap.put("TXT_5_3", "---");
            }
        }
        //5
        value = exp.getExposure();
        if (value < 0 || Double.isNaN(value)) {
            hashMap.put("TXT_0_4", "---");
            hashMap.put("TXT_1_4", "---");
            hashMap.put("TXT_2_4", "---");
            hashMap.put("TXT_3_4", "---");
            hashMap.put("TXT_4_4", "---");
            hashMap.put("TXT_5_4", "---");
        } else {
            hashMap.put("TXT_0_4", exp.getParams().getTXT_0_9());
            hashMap.put("TXT_1_4", String.format(Locale.CANADA, "%.2e", value).toUpperCase());

            persent = exp.getParams().getExposurePersent();
            if (persent != 0) {
                hashMap.put("TXT_2_4", String.format(Locale.CANADA, "%.2f", persent));
                hashMap.put("TXT_3_4", String.valueOf(bpInCentral(exp.getFrList(), 4, 32)));
                hashMap.put("TXT_4_4", String.valueOf(exp.getFrList().get(4).getBpList().size()));
                hashMap.put("TXT_5_4", String.valueOf(bpInDiametr(exp.getFrList(), 4, StendParams.getDiametr())));
            } else {
                hashMap.put("TXT_2_4", "---");
                hashMap.put("TXT_3_4", "---");
                hashMap.put("TXT_4_4", "---");
                hashMap.put("TXT_5_4", "---");
            }
        }

        hashMap.put("WITHBP", exp.isWithDefPx() ? "Примечание - Результаты приведены с учетом дефектных элементов." :
                "Примечание - Результаты приведены без учета дефектных элементов.");

        return hashMap;
    }

    /**
     * Флаг подчистки форм из файла.
     */
    private boolean cleadForms = false;

    /**
     * Метод заполнения отчета
     *
     * @return
     */
    public boolean savePDF() {
        try {
            for (Map.Entry<String, Object> item : map.entrySet()) {
                String key = item.getKey();
                PDField field = pDAcroForm.getField(key);
                if (field != null) {
                    LOG.debug("Form field with placeholder name: '" + key + "' found");

                    if (field instanceof PDTextField) {
                        LOG.debug("(type: " + field.getClass().getSimpleName() + ")");
                        saveField(key, (String) item.getValue(), fontNames);
                        LOG.debug("value is set to: '" + item.getValue() + "'");

                    } else {
                        LOG.error("Unexpected form field type found with placeholder name: '" + key + "'");
                    }
                } else {
                    LOG.error("No field found with name:" + key);
                }
            }
            if (cleadForms) {
                pDAcroForm.flatten();// если нужно убрать отсатки форм
            }
        } catch (IOException e) {
            LOG.error("Error while saving {}", e);
            return false;
        }
        return true;
    }

    /**
     * Сохранение поля.
     *
     * @param name  имя поля.
     * @param value Значение.
     * @throws IOException
     */
    public void saveField(String name, String value, List<String> fontNames) {

        PDField field = pDAcroForm.getField(name);
        COSDictionary dict = field.getCOSObject();
        COSString defaultAppearance = (COSString) dict
                .getDictionaryObject(COSName.DA);
        if (defaultAppearance != null) {
            String appearanceStringstring = defaultAppearance.getString();
            String[] splits = appearanceStringstring.split(" ", 2);
            StringBuilder stringBuilder = new StringBuilder("/");
            stringBuilder.append(fontNames.get(1)).append(" ").append(splits[1]);
            dict.setString(COSName.DA, stringBuilder.toString());
        }
        pDAcroForm.setNeedAppearances(true);

        try {
            field.setValue(value);
        } catch (IOException e) {
            LOG.error("Error in typing fiels {} value {}", name, value);
            throw new RuntimeException(e);
        }
        LOG.debug("saved " + name + ":" + value);
    }

    /**
     * public boolean saveImages() {
     * LOG.trace("Startsaving parse images to PDF file");
     * try {
     * ArrayList<PDImageXObject> list = new ArrayList<>();
     * AtomicBoolean flag = new AtomicBoolean(false);
     * Platform.runLater(() -> {
     * try {
     * <p>
     * VBox vbox = (VBox) src;
     * ObservableList<Node> children = vbox.getChildren();
     * Node[] nodes = children.toArray(new Node[0]);
     * for (int i = 0; i < nodes.length; i = i + 3) {
     * BufferedImage bufImage;
     * if (nodes[i] instanceof HBox) {
     * HBox node = (HBox) nodes[i];
     * Node chartNode = node.getChildren().get(0);
     * if (chartNode instanceof Chart) {
     * BarChart<String, Number> bar_chart = (BarChart<String, Number>) chartNode;
     * bar_chart.getStylesheets().add(this.getClass().getResource("chart.css").toExternalForm());
     * bufImage = SwingFXUtils.fromFXImage(nodes[i].snapshot(null, null), null);
     * bar_chart.getStylesheets().clear();
     * } else {
     * bufImage = SwingFXUtils.fromFXImage(nodes[i].snapshot(null, null), null);
     * }
     * } else {
     * bufImage = SwingFXUtils.fromFXImage(nodes[i].snapshot(null, null), null);
     * }
     * <p>
     * ByteArrayOutputStream bos = new ByteArrayOutputStream();
     * try {
     * ImageIO.write(bufImage, ImageUtils.FORMAT_PNG, bos);
     * bos.flush();
     * } catch (Exception e) {
     * LOG.error("Error in parse chart images", e.getMessage());
     * }
     * PDImageXObject pdImage = null;
     * pdImage = PDImageXObject.createFromByteArray(pDDocument, bos.toByteArray(),
     * nodes[i].getId());
     * list.add(pdImage);
     * bos.close();
     * }
     * } catch (IOException e) {
     * LOG.error("ERROR {}", e.getCause());
     * } finally {
     * flag.set(true);
     * }
     * });
     * while (!flag.get()) {
     * TimeUnit.MILLISECONDS.sleep(500);
     * }
     * <p>
     * for (BufferedImage bi :
     * exp.getScList()) {
     * ByteArrayOutputStream bos = new ByteArrayOutputStream();
     * try {
     * ImageIO.write(bi, ImageUtils.FORMAT_PNG, bos);
     * bos.flush();
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * PDImageXObject pdImage = null;
     * pdImage = PDImageXObject.createFromByteArray(pDDocument, bos.toByteArray(),
     * "");
     * list.add(pdImage);
     * bos.close();
     * }
     * <p>
     * int listSize = list.size();
     * float x = 30;
     * float y = 0;
     * float k = 0;
     * PDPage tempPage = pDDocument.getPage(3);
     * for (int i = 0; i < listSize / 2; i++) {
     * //   if (i % 2 == 0) {
     * PDFCloneUtility cloner = new PDFCloneUtility(pDDocument);
     * COSDictionary pageDictionary = (COSDictionary) cloner.cloneForNewDocument(tempPage);
     * PDPage page2 = new PDPage(pageDictionary);
     * pDDocument.addPage(page2);
     * y = page2.getMediaBox().getHeight() - 150;
     * k = page2.getMediaBox().getWidth();
     * //   }
     * <p>
     * PDImageXObject img = list.get(i);
     * float height = img.getHeight();
     * float width = img.getWidth();
     * try (PDPageContentStream contentStream = new PDPageContentStream(pDDocument,
     * pDDocument.getPage(pDDocument.getNumberOfPages() - 1),
     * PDPageContentStream.AppendMode.APPEND, true)) {
     * contentStream.drawImage(img, (k - width) / 2, y - height, width, height);
     * }
     * y = y - height;
     * <p>
     * PDImageXObject img2 = list.get(i + listSize / 2);
     * height = img2.getHeight();
     * width = img2.getWidth();
     * try (PDPageContentStream contentStream = new PDPageContentStream(pDDocument,
     * pDDocument.getPage(pDDocument.getNumberOfPages() - 1),
     * PDPageContentStream.AppendMode.APPEND, true)) {
     * contentStream.drawImage(img2, (k - width) / 2, y - height, width, height);
     * }
     * <p>
     * }
     * if (listSize % 2 == 1) {
     * //Итоговая страница 4
     * PDPage page = pDDocument.getPage(3);
     * y = page.getMediaBox().getHeight() - 150;
     * k = page.getMediaBox().getWidth();
     * //   }
     * <p>
     * float height = 100;
     * float width = 100;
     * try (PDPageContentStream contentStream = new PDPageContentStream(pDDocument,
     * pDDocument.getPage(3),
     * PDPageContentStream.AppendMode.APPEND, true)) {
     * contentStream.beginText();
     * <p>
     * contentStream.setFont(myFont, 12);
     * contentStream.setLeading(14.5f);
     * contentStream.newLineAtOffset(150, 680);
     * String text1 = "Дефектные элементы по всем выбранным параметрам";
     * contentStream.showText(text1);
     * contentStream.newLineAtOffset(20, -15);
     * contentStream.setFont(myFont, 10);
     * String text2 = "Всего дефектных элементов в центральной зоне 32*32 px: " + exp.getBpInCenter();
     * contentStream.showText(text2);
     * contentStream.newLine();
     * <p>
     * String text3 = String.format("Всего дефектных элементов в зоне %d*%d px: %d",
     * exp.getSizeX(), exp.getSizeY(), exp.getBpAll());
     * contentStream.showText(text3);
     * <p>
     * byte[] bytes = exp.getBuffToTXT();
     * <p>
     * try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));) {
     * float h = 10.5f;
     * contentStream.setLeading(h);
     * <p>
     * contentStream.setFont(myFont, 8);
     * contentStream.newLineAtOffset(-90, -350);
     * String line;
     * int count = 0;
     * for (int i = 0; i < 2; i++) {
     * <p>
     * while ((line = br.readLine()) != null && (count++) < 50) {
     * contentStream.newLine();
     * contentStream.showText(line);
     * if ((count) > 24 && i == 0) {
     * break;
     * }
     * }
     * contentStream.newLineAtOffset(260, h * 25);
     * }
     * <p>
     * } catch (Exception e) {
     * LOG.error("Error while printing list pdf");
     * }
     * <p>
     * contentStream.endText();
     * }
     * <p>
     * y = 320;
     * PDImageXObject img = list.get(listSize - 1);
     * height = img.getHeight();
     * width = img.getWidth();
     * try (PDPageContentStream contentStream = new PDPageContentStream(pDDocument,
     * pDDocument.getPage(3),
     * PDPageContentStream.AppendMode.APPEND, true)) {
     * contentStream.drawImage(img, (k - width) / 2, y, width, height);
     * <p>
     * }
     * } else {
     * <p>
     * pDDocument.removePage(3);
     * }
     * <p>
     * <p>
     * } catch (
     * Exception e) {
     * LOG.error("Exeption in images {}", e.getMessage());
     * return false;
     * <p>
     * } finally {
     * try {
     * <p>
     * if (pDDocument != null) {
     * pDDocument.save(pdfFile);
     * pDDocument.close();
     * }
     * <p>
     * } catch (Exception e) {
     * LOG.error("Exeption in saveFile {}", e.getMessage());
     * return false;
     * }
     * return true;
     * }
     * }
     */
    public boolean saveBoxes() {
        LOG.trace("Startsaving parse images to PDF file");
        try {
            ArrayList<PDImageXObject> list = new ArrayList<>();
            ArrayList<PDImageXObject> listWithLines = new ArrayList<>();
            AtomicBoolean flag = new AtomicBoolean(false);
            Platform.runLater(() -> {
                try {

                    VBox[] nodes = (VBox[]) src;

                    for (int i = 0; i < nodes.length; i++) {
                        BufferedImage bufImage;

                        bufImage = SwingFXUtils.fromFXImage(nodes[i].snapshot(null, null), null);

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try {
                            ImageIO.write(bufImage, ImageUtils.FORMAT_PNG, bos);
                            bos.flush();
                        } catch (Exception e) {
                            LOG.error("Error in parse chart images", e.getMessage());
                        }
                        PDImageXObject pdImage = null;
                        pdImage = PDImageXObject.createFromByteArray(pDDocument, bos.toByteArray(),
                                nodes[i].getId());
                        list.add(pdImage);
                        bos.close();
                    }
                } catch (IOException e) {
                    LOG.error("ERROR {}", e.getCause());
                } finally {
                    flag.set(true);
                }
            });
            while (!flag.get()) {
                TimeUnit.MILLISECONDS.sleep(500);
            }

            for (BufferedImage bi :
                    exp.getScList()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    ImageIO.write(bi, ImageUtils.FORMAT_PNG, bos);
                    bos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PDImageXObject pdImage = null;
                pdImage = PDImageXObject.createFromByteArray(pDDocument, bos.toByteArray(),
                        "");
                listWithLines.add(pdImage);
                bos.close();
            }


            //Cтраница 3

            try (PDPageContentStream contentStream = new PDPageContentStream(pDDocument,
                    pDDocument.getPage(2),
                    PDPageContentStream.AppendMode.APPEND, true)) {
                contentStream.beginText();

                contentStream.setFont(myFont, 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(100, 475);
                String text1 = "Среднее арифметическое сигнала, В";
                contentStream.showText(text1);
                contentStream.newLineAtOffset(275, 0);
                contentStream.setFont(myFont, 12);
                String text2 = "Среднее квадратичное сигнала, В";
                contentStream.showText(text2);
                contentStream.newLineAtOffset(-220, -195);
                contentStream.setFont(myFont, 12);
                String text4 = "СКО сигнала (шум), В";
                contentStream.showText(text4);
                contentStream.newLineAtOffset(220, 0);
                contentStream.setFont(myFont, 12);
                String text5 = "Пороговая облученность, Вт\u00B7см\u00AF \u00B2";
                contentStream.showText(text5);

                contentStream.endText();
            }

            float x = 150;
            float y = 320;

            try (PDPageContentStream contentStream = new PDPageContentStream(pDDocument,
                    pDDocument.getPage(2),
                    PDPageContentStream.AppendMode.APPEND, true)) {
                for (int i = 0; i < list.size(); i++) {

                    if (i == 0) {

                    } else if (i == 1) {
                        x = 390;
                        y = 320;
                    } else if (i == 2) {
                        x = 150;
                        y = 120;
                    } else if (i == 3) {
                        continue;
                    } else if (i == 4) {
                        x = 390;
                        y = 120;
                    }

                    PDImageXObject img = list.get(i);
                    contentStream.drawImage(list.get(i), x, y, img.getWidth(), img.getHeight());
                }


            }


            if (true) {
                //Итоговая страница 4
                PDPage page = pDDocument.getPage(3);
                float y5 = page.getMediaBox().getHeight() - 150;
                float k5 = page.getMediaBox().getWidth();
                //   }

                try (PDPageContentStream contentStream = new PDPageContentStream(pDDocument,
                        pDDocument.getPage(3),
                        PDPageContentStream.AppendMode.APPEND, true)) {
                    contentStream.beginText();

                    contentStream.setFont(myFont, 12);
                    contentStream.setLeading(14.5f);
                    contentStream.newLineAtOffset(150, 680);
                    String text1 = "Дефектные элементы по всем выбранным параметрам";
                    contentStream.showText(text1);
                    contentStream.newLineAtOffset(20, -15);
                    contentStream.setFont(myFont, 10);
                    String text2 = "Всего дефектных элементов в центральной зоне 32*32 px: " + exp.getBpInCenter();
                    contentStream.showText(text2);
                    contentStream.newLine();
                    String text3 = String.format("Всего дефектных элементов в зоне диаметром %d px: %d",
                            140, exp.getBpInDiametr());
                    contentStream.showText(text3);
                    contentStream.newLine();
                    String text4 = String.format("Всего дефектных элементов в зоне %d*%d px: %d",
                            exp.getSizeX(), exp.getSizeY(), exp.getBpAll());
                    contentStream.showText(text4);

                    byte[] bytes = exp.getBuffToTXT();

                    try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));) {
                        float h = 10.5f;
                        contentStream.setLeading(h);

                        contentStream.setFont(myFont, 8);
                        contentStream.newLineAtOffset(-90, -350);
                        String line;
                        int count = 0;
                        for (int i = 0; i < 2; i++) {

                            while ((line = br.readLine()) != null && (count++) < 50) {
                                contentStream.newLine();
                                contentStream.showText(line);
                                if ((count) > 24 && i == 0) {
                                    break;
                                }
                            }
                            contentStream.newLineAtOffset(260, h * 25);
                        }

                    } catch (Exception e) {
                        LOG.error("Error while printing list pdf");
                    }

                    contentStream.endText();
                }

                y5 = 300;
                PDImageXObject img1 = listWithLines.get(3);
              float  height1 = img1.getHeight();
                float  width1 = img1.getWidth();
                try (PDPageContentStream contentStream = new PDPageContentStream(pDDocument,
                        pDDocument.getPage(3),
                        PDPageContentStream.AppendMode.APPEND, true)) {
                    contentStream.drawImage(img1, (k5 - width1) / 2, y5, width1, height1);
                }
            }

        } catch (
                Exception e) {
            LOG.error("Exeption in images {}", e.getMessage());
            return false;

        } finally {
            try {

                if (pDDocument != null) {
                    pDDocument.save(pdfFile);
                    pDDocument.close();
                }

            } catch (Exception e) {
                LOG.error("Exeption in saveFile {}", e.getMessage());
                return false;
            }
            return true;
        }
    }


    /**
     * Добавление шрифтов в документ
     *
     * @param _pdfDocument документ
     * @param fonts        список шрифтов
     * @return перечень шрифтов
     * @throws IOException
     */
    private static List<String> prepareFont(PDDocument _pdfDocument, List<PDFont> fonts) {
        PDDocumentCatalog docCatalog = _pdfDocument.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();
        PDResources res = acroForm.getDefaultResources();
        if (res == null)
            res = new PDResources();
        List<String> fontNames = new ArrayList<>();
        for (PDFont font : fonts) {
            fontNames.add(res.add(font).getName());
        }
        acroForm.setDefaultResources(res);
        return fontNames;
    }

    /**
     * Подгрузка шрифта
     *
     * @param _pdfDocument документ
     * @param resourceName наименование шрифта
     * @return обернутый шрифт
     * @throws IOException
     */
    private PDFont loadTrueTypeFont(PDDocument _pdfDocument, String resourceName) {
        try {
            InputStream fontStream = getClass().getResourceAsStream(resourceName);
            return PDType0Font.load(_pdfDocument, fontStream);
        } catch (Exception e) {
            LOG.error("Font {} not loaded. Error {}", resourceName, e.getMessage());
            throw new RuntimeException(e);
        }

    }

    /**
     * Заполнение pdf файла
     *
     * @return
     */
    public boolean save() {

        if (!savePDF()) {
            return false;
        }

        if (!saveBoxes()) {
            return false;
        }

        return true;
    }

    private void loadPrepareFile() {
        String path = loadFilePath(fileName);
        File file = new File(path);
        try {
            pDDocument = PDDocument.load(file);
            pDAcroForm = pDDocument.getDocumentCatalog().getAcroForm();
            pDResources = pDAcroForm.getDefaultResources();

        } catch (IOException e) {
            LOG.error("PDF file not loaded {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
