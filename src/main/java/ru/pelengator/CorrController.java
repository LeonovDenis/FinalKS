package ru.pelengator;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;


public class CorrController implements Initializable {
    /**
     * Логгер.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CorrController.class);

    @FXML
    private Button but_load;

    @FXML
    private ProgressIndicator pb_indicator;




    /**
     * Ссылка на главный контроллер.
     */
    private Controller mainController;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        LOG.debug("Init fields controller");

    }


    public void initController(Controller controller) {
        LOG.debug("Init controller");

        mainController = controller;

    }

}
