package com.rdchandrahas.shared.component;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.shared.util.FileUtils;
import com.rdchandrahas.shared.util.PdfThumbnailUtil;

import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FileListCell extends ListCell<FileItem> {

    private final ImageView imageView = new ImageView();
    private final VBox textBox = new VBox(4);
    private final HBox container = new HBox(10);

    public FileListCell() {

        imageView.setFitWidth(60);
        imageView.setFitHeight(80);

        container.getChildren().addAll(imageView, textBox);
    }

    @Override
    protected void updateItem(FileItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        textBox.getChildren().clear();

        Label name = new Label(item.getName());
        Label size = new Label(
                FileUtils.formatSize(item.getSize())
        );

        textBox.getChildren().addAll(name, size);

        imageView.setImage(null);

        PdfThumbnailUtil.loadThumbnailAsync(
                item.getPath(),
                imageView::setImage
        );

        setGraphic(container);
    }
}
