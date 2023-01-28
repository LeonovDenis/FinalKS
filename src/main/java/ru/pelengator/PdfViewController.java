package ru.pelengator;


import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pelengator.model.PdfModel;

import static ru.pelengator.App.loadFilePath;

public class PdfViewController implements Initializable {

    private static final Logger LOG = LoggerFactory.getLogger(PdfViewController.class);
    @FXML
    private Pagination pagination;
    @FXML
    private BorderPane mainBorderPane;
    private PdfModel model;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String PATH = loadFilePath("protokol.pdf");//todo исправить на рэ
        model = new PdfModel(Paths.get(PATH));
        ScrollPane scrollPane = new ScrollPane();

        scrollPane.minViewportHeightProperty().bind(mainBorderPane.heightProperty().multiply(0.8));
        scrollPane.minViewportWidthProperty().bind(mainBorderPane.widthProperty().multiply(0.8));
        scrollPane.prefViewportHeightProperty().bind(mainBorderPane.heightProperty().multiply(0.8));
        scrollPane.prefViewportWidthProperty().bind(mainBorderPane.widthProperty().multiply(0.8));
        scrollPane.maxHeightProperty().bind(mainBorderPane.heightProperty().multiply(0.8));
        scrollPane.maxWidthProperty().bind(mainBorderPane.widthProperty().multiply(0.8));
        scrollPane.setPannable(true);

        pagination.minHeightProperty().bind(scrollPane.minViewportHeightProperty().multiply(1.2));
        pagination.minWidthProperty().bind(scrollPane.minViewportWidthProperty().multiply(1.2));

        BorderPane insideBorderPane1 = new BorderPane();
        insideBorderPane1.minHeightProperty().bind(pagination.minHeightProperty().multiply(0.8));
        insideBorderPane1.minWidthProperty().bind(pagination.minWidthProperty().multiply(0.8));

        ImageView imageView = new ImageView();
        insideBorderPane1.setCenter(imageView);
        HBox hBox = new HBox(insideBorderPane1);
        hBox.setAlignment(Pos.CENTER);
        scrollPane.setContent(hBox);

        pagination.setPageCount(model.numPages());
        pagination.setPageFactory(index -> {
            Image image = model.getImage(index);
            imageView.imageProperty().setValue(image);
            return scrollPane;
        });

    }
}
