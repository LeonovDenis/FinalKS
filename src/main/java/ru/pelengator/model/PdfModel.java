package ru.pelengator.model;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;


import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class PdfModel {
    private static final Logger LOG = LoggerFactory.getLogger(PdfModel.class);

    private PDDocument document;
    private PDFRenderer renderer;

    public PdfModel(Path path) {
        try {
            document = PDDocument.load(path.toFile());
            renderer = new PDFRenderer(document);
        } catch (IOException ex) {
            LOG.error("PDDocument thorws IOException file=" + path);
            throw new UncheckedIOException("PDDocument thorws IOException file=" + path, ex);
        }
    }

    public int numPages() {
        return document.getPages().getCount();
    }

    public Image getImage(int pageNumber) {
        BufferedImage pageImage;
        try {
            pageImage = renderer.renderImage(pageNumber);
        } catch (IOException ex) {
            LOG.error("PDFRenderer throws IOException");
            throw new UncheckedIOException("PDFRenderer throws IOException", ex);
        }
        return SwingFXUtils.toFXImage(pageImage, null);
    }
}
