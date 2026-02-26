package com.rdchandrahas.shared.component;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.shared.util.FileUtils;
import com.rdchandrahas.shared.util.PdfThumbnailUtil;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * FileListCell is a custom ListCell implementation used for displaying PDF files
 * in a traditional list format. It displays a small thumbnail on the left, 
 * with the filename and file size stacked vertically on the right.
 */
public class FileListCell extends ListCell<FileItem> {

    private final ImageView imageView = new ImageView();
    private final VBox textBox = new VBox(4);
    private final HBox container = new HBox(10);
    private String currentFilePath = null;

    /**
     * Constructs a new FileListCell with a horizontal layout and fixed thumbnail scaling.
     */
    public FileListCell() {
        // Configure the thumbnail size for the list view
        imageView.setFitWidth(60);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        // Build the horizontal layout: [Thumbnail] [Text Metadata]
        container.getChildren().addAll(imageView, textBox);
        container.setStyle("-fx-alignment: center-left;");
    }

    /**
     * Updates the cell's visual content based on the provided FileItem.
     * * @param item  The FileItem data for the current row.
     * @param empty Whether the row is empty.
     */
    @Override
    protected void updateItem(FileItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            currentFilePath = null;
            setText(null);
            setGraphic(null);
            return;
        }

        textBox.getChildren().clear();
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label sizeLabel = new Label(com.rdchandrahas.shared.util.FileUtils.formatSize(item.getSize()));
        sizeLabel.getStyleClass().add("secondary-label");
        textBox.getChildren().addAll(nameLabel, sizeLabel);

        if (!item.getPath().equals(currentFilePath)) {
            currentFilePath = item.getPath();
            imageView.setImage(null);

            javafx.scene.image.Image cachedImage = com.rdchandrahas.shared.util.ThumbnailCache.get(item.getPath());
            if (cachedImage != null) {
                imageView.setImage(cachedImage);
            } else {
                PdfThumbnailUtil.loadThumbnailAsync(
                        item.getPath(),
                        () -> !item.getPath().equals(currentFilePath),
                        img -> {
                            if (item.getPath().equals(currentFilePath)) {
                                imageView.setImage(img);
                            }
                        }
                );
            }
        }
        setGraphic(container);
    }
}