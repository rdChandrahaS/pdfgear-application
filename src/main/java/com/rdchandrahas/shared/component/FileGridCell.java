package com.rdchandrahas.shared.component;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.shared.util.PdfThumbnailUtil;

import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class FileGridCell extends ListCell<FileItem> {

    private final ImageView imageView = new ImageView();
    private final VBox container = new VBox(5);
    private final Label nameLabel = new Label();

    public FileGridCell() {

        imageView.setFitWidth(120);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        container.getChildren().addAll(imageView, nameLabel);
        container.setStyle("-fx-alignment: center;");
    }

    @Override
    protected void updateItem(FileItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        nameLabel.setText(item.getName());

        PdfThumbnailUtil.loadThumbnailAsync(
                item.getPath(),
                imageView::setImage
        );

        setGraphic(container);
    }
}
