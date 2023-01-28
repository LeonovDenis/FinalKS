package ru.pelengator;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static ru.pelengator.API.utils.Utils.calkCRC32;

public class AboutController implements Initializable {

    private static final Logger LOG = LoggerFactory.getLogger(AboutController.class);
    @FXML
    private Label lb0;
    @FXML
    private Label lb1;
    @FXML
    private Label lb2;
    @FXML
    private Label lb3;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        String firstMSG = "Стенд ИС2 v1.2 \n" +
                "JavaFX API v19 by JavaFX runtime v16";
        String secondMSG = "Контрольная сумма приложения CRC-32: "+calkCRC32();
        String thredMSG = "Дизайн и кодировка: Леонов Д.А.";
        String fourMSG = "d.a.leonov@npk-pelengator.ru";

        lb0.setText(firstMSG);
        lb1.setText(secondMSG);
        lb2.setText(thredMSG);
        lb3.setText(fourMSG);
    }
}
