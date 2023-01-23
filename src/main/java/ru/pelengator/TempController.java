package ru.pelengator;


import javafx.beans.binding.Bindings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.FloatStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

import static javafx.scene.input.KeyEvent.KEY_PRESSED;

public class TempController implements Initializable {

    /**
     * Логгер.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TempController.class);

    /**
     * Напряжение на термодатчике при 77К, мВ
     */
    @FXML
    private TextField tf77;
    /**
     * Калибровочный коэффициент, мВ/К
     */
    @FXML
    private TextField tfK;
    /**
     * Кнопка закрытия
     */
    @FXML
    private Button butClose;

    /**
     * Ссылка на главный контроллер.
     */
    private Controller mainController;
    private boolean isFieldsValid = false;

    private float azot = 0;
    private float koef = 0;

    private ObservableList<TextField> fieldOptions = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        LOG.debug("Init temp controller");
        fillOptions();
        addButtonOnAction();
        setLostFocusAction();


    }

    /**
     * Создание списка полей
     */
    private void fillOptions() {
        fieldOptions.addAll(tf77,tfK);
    }

    /**
     * Проверка значения. Подкрашивание поля
     *
     * @param uzel поле
     * @return true в случае валидного значения
     */

    private boolean proverkaZnacheniy(TextField uzel) {
        boolean isValid = false;
        String text = uzel.getText().trim().toUpperCase();
        String replacedText = text.trim().replace(",", ".");
        if (!text.equals(replacedText)) {
            uzel.setText(replacedText);
            text = replacedText;
        }
        try {
            double value = Double.parseDouble(text);
            if (value > 0) {
                isValid = true;
            }

        } catch (Exception e) {
            LOG.error(e.getMessage());
        } finally {

            if (isValid) {
                String tempText = uzel.getText().toUpperCase();
                uzel.setText(tempText);
                uzel.setStyle("-fx-padding: 2;" +
                        "-fx-border-color: green ;-fx-border-width: 0.5;" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-style: solid inside");
            } else {
                uzel.setStyle("-fx-padding: 2;" +
                        "-fx-border-color: red ;-fx-border-width: 0.5;" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-style: solid inside");
            }

        }
        return isValid;
    }


    /**
     * Описание кнопок
     */
    private void addButtonOnAction() {

        butClose.setOnAction(event -> {
            LOG.trace("Btn close pressed");

            if (checkingFields()) {
                saveValuesToParams();
                Button source = (Button) event.getSource();
                Stage stage = (Stage) source.getScene().getWindow();
                stage.close();
            }
        });
        tfK.setOnAction(event -> proverkaZnacheniy(tfK));
        tf77.setOnAction(event -> proverkaZnacheniy(tf77));



    }
    private void setLostFocusAction() {

        KeyEvent EnterEvent = new KeyEvent(KEY_PRESSED, "", "", KeyCode.ENTER,
                false, false, false, false);

        tf77.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                tf77.fireEvent(EnterEvent);
            }
        });
        tfK.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                tfK.fireEvent(EnterEvent);
            }
        });

    }

    public void saveValuesToParams() {

        if (!isFieldsValid) {
            mainController.getParams().setTempValue_77K(azot);
            mainController.getParams().setTempValue_k(koef);
        }
    }

    public void initController(Controller controller) {
        LOG.debug("Init controller");

        mainController = controller;

        azot = mainController.getParams().getTempValue_77K();
        koef = mainController.getParams().getTempValue_k();

        Bindings.bindBidirectional(tf77.textProperty(), mainController.getParams().tempValue_77KProperty(),
                (StringConverter) new MyConverter());
        Bindings.bindBidirectional(tfK.textProperty(), mainController.getParams().tempValue_kProperty(),
                (StringConverter) new MyConverter());


    }


    /**
     * Проверка полей и выдача разрешения на закрытие
     *
     * @return разрешение
     */
    public boolean checkingFields() {



        for (TextField field : fieldOptions) {

            if (!proverkaZnacheniy(field)) {
                LOG.error("TextField error {}", field.getId());
                isFieldsValid=false;
                return false;
            }
        }
        isFieldsValid=true;
        return true;
    }

    public Controller getMainController() {
        return mainController;
    }

    private static class MyConverter extends FloatStringConverter {

        public MyConverter() {
            super();
        }

        @Override
        public Float fromString(String value) {
            try {
                Float.parseFloat(value);
                return super.fromString(value);
            } catch (NumberFormatException exception) {
                return 0f;
            }
        }

        @Override
        public String toString(Float value) {

            return super.toString(value);
        }
    }
}
