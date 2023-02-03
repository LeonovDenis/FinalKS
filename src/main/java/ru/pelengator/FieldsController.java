package ru.pelengator;

import at.favre.lib.bytes.Bytes;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.*;

import static ru.pelengator.API.utils.Utils.*;

public class FieldsController implements Initializable {

    /**
     * Логгер.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FieldsController.class);
    /**
     * Кнопка выбора необходимости сохранения Exel.
     */
    @FXML
    private ToggleButton tbExel;
    /**
     * Кнопка выбора необходимости сохранения TXT.
     */
    @FXML
    private ToggleButton tbTxt;
    /**
     * Кнопка выбора необходимости сохранения PDF.
     */
    @FXML
    private ToggleButton tbPdf;
    @FXML
    private TextField tfZakaz;
    @FXML
    private TextField tfDogovor;
    @FXML
    private TextField tfMetodika;
    @FXML
    private TextField tfNomer_0;
    @FXML
    private TextField tfNomer;
    @FXML
    private TextField tfOtk;
    @FXML
    private TextField tfData;
    @FXML
    private TextField TXT_0_0;
    @FXML
    private TextField TXT_0_1;
    @FXML
    private TextField TXT_0_2;
    @FXML
    private TextField TXT_0_3;
       @FXML
    private TextField TXT_0_9;
    @FXML
    private Label tx1;
    @FXML
    private Label tx6;
    @FXML
    private TextField tfComPort;
    @FXML
    private TextField tfVideoPort;
    @FXML
    private Label lbPorts;
    @FXML
    private Label lbSlath;

    /**
     * Ссылка на главный контроллер.
     */
    private Controller mainController;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        LOG.debug("Init fields controller");

    }

    /**
     * Показ панели ethernet, вслучае работы по сети.
     */
    private void showEthernet() {
        ArrayList<Parent> controls = new ArrayList<>();
        controls.add(tfComPort);
        controls.add(tfVideoPort);
        controls.add(lbPorts);
        controls.add(lbSlath);
        String netName = mainController.getParams().getSelNetworkInterface().getName();
        for (Parent c :
                controls) {
            if (netName.startsWith("USB")) {
                c.setVisible(false);
            } else {
                c.setVisible(true);
            }
        }
    }


    public void initController(Controller controller) {
        LOG.debug("Init controller");

        mainController = controller;
        showEthernet();
        fillToolTips();

        tfZakaz.textProperty().bindBidirectional(controller.getParams().zakazProperty());
        tfDogovor.textProperty().bindBidirectional(controller.getParams().dogovorProperty());
        tfMetodika.textProperty().bindBidirectional(controller.getParams().metodikaProperty());
        tfNomer_0.textProperty().bindBidirectional(controller.getParams().nomer_0Property());
        tfNomer.textProperty().bindBidirectional(controller.getParams().nomerProperty());
        tfOtk.textProperty().bindBidirectional(controller.getParams().otkProperty());

        tfData.textProperty().bindBidirectional(controller.getParams().dataProperty());
        String dataWord = constructDataWord(controller);
        tfData.setText(dataWord);

        TXT_0_0.textProperty().bindBidirectional(controller.getParams().TXT_0_0Property());
        TXT_0_1.textProperty().bindBidirectional(controller.getParams().TXT_0_1Property());
        TXT_0_2.textProperty().bindBidirectional(controller.getParams().TXT_0_2Property());
        TXT_0_3.textProperty().bindBidirectional(controller.getParams().TXT_0_3Property());

        TXT_0_9.textProperty().bindBidirectional(controller.getParams().TXT_0_9Property());
        tbExel.selectedProperty().bindBidirectional(controller.getParams().tbExelProperty());
        tbTxt.selectedProperty().bindBidirectional(controller.getParams().tbTxtProperty());
        tbPdf.selectedProperty().bindBidirectional(controller.getParams().tbPdfProperty());
        tfComPort.setText(String.valueOf(controller.getParams().getDetPortCommand()));
        tfVideoPort.setText(String.valueOf(controller.getParams().getDetPortVideo()));


        String str = "Вольтовая чувствительность, В\u00B7Вт\u00AF \u00B9";
        tx1.setText(str);
        str = "Пороговая облученность, Вт\u00B7см\u00AF \u00B2";
        tx6.setText(str);
    }

    /**
     * Заполнение подсказок
     */
    private void fillToolTips() {

    }

    /**
     * Заполнение командного слова DataWord
     *
     * @return 2 байта
     */
    private String constructDataWord(Controller controller) {
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //|Start  |Mode   |GC   | –  |PW(1-0)|I(2-0) |DE(6-0)     |TS(7-0)          |RO(2-0)    |OM1    | – | – |RST|OE |//
        //| 1     | 0     |ку   | 0  | 1 1   | 0 0 0 |смещение    | 0 0 0 0 0 0 0 0 | 0 0 0     | 0     | 0 | 0 | 0 | 1 |//
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        byte[] defValue = new byte[]{(byte) 0x8C, 0x00, 0x00, 0x01};
        Bytes dataWord = Bytes.wrap(defValue, ByteOrder.BIG_ENDIAN);
        BitSet bitSet = dataWord.toBitSet();
        //коэфициент смещения
        if (controller.getParams().isTempKU()) {
            bitSet.set(5, true);//1- если ку 3
        }
        defValue = bitSet.toByteArray();

        //VR0
        byte vR0 = (byte) controller.getParams().getTempVR0();
        defValue[1] = vR0;

        return "0x" + Bytes.wrap(defValue).encodeHex(true);
    }

    public Controller getMainController() {
        return mainController;
    }


    /**
     * Установка IP.
     *
     * @param event
     */
    private void setIP(ActionEvent event) {
        TextField source = (TextField) event.getSource();
        String text = source.getText().trim();
        source.getParent().requestFocus();
        boolean b = ipv4Check(text);
        if (b) {
            mainController.getParams().setDetIP(text);
        } else {
            LOG.error("IP not match");
            setError(source, "Error");
        }
    }

    /**
     * Установка командного порта.
     *
     * @param event
     */
    @FXML
    private void setComPort(ActionEvent event) {

        TextField source = (TextField) event.getSource();
        int i = parseIntText(event, false);
        if (0 < i && i < 64000) {
            mainController.getParams().setDetPortCommand(i);
        } else {
            LOG.error("Command port not match");
            setError(source, "Error");
        }
    }

    /**
     * Установка видео порта.
     *
     * @param event
     */
    @FXML
    private void setVideoPort(ActionEvent event) {
        TextField source = (TextField) event.getSource();
        int i = parseIntText(event, false);
        if (0 < i && i < 64000) {
            mainController.getParams().setDetPortVideo(i);
        } else {
            LOG.error("Video port not match");
            setError(source, "Error");
        }
    }

}
