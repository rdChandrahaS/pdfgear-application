package com.rdchandrahas.shared.component;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.shared.util.PdfThumbnailUtil;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.controlsfx.control.GridCell;

import java.io.File;

/**
 * FileGridCell is a custom virtualized GridCell used by ControlsFX GridView.
 * It dynamically recycles its UI to save memory when scrolling through thousands of files.
 */
public class FileGridCell extends GridCell<FileItem> {

    private final VBox card = new VBox(10);
    private final ImageView imageView = new ImageView();
    private final Label nameLabel = new Label();
    private final StackPane imageContainer = new StackPane();
    private final HBox actionBox = new HBox(15);
    private String currentFilePath = null;

    public FileGridCell() {
        // 1. Setup main card container
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefSize(170, 240);
        card.setMinSize(170, 240);
        card.setMaxSize(170, 240);
        card.getStyleClass().add("grid-card");

        // 2. Setup image dimensions
        imageView.setFitWidth(150);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        imageContainer.getChildren().add(imageView);
        imageContainer.setPrefSize(150, 180);

        // 3. Setup Hover Action Box (Replace/Remove)
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6); -fx-padding: 8; -fx-background-radius: 5;");
        actionBox.setVisible(false);

        Button replaceBtn = new Button("🔄");
        replaceBtn.setTooltip(new Tooltip("Replace File"));
        replaceBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 18px;");

        Button removeBtn = new Button("❌");
        removeBtn.setTooltip(new Tooltip("Remove File"));
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff6b6b; -fx-cursor: hand; -fx-font-size: 18px;");

        actionBox.getChildren().addAll(replaceBtn, removeBtn);

        imageContainer.setOnMouseEntered(e -> actionBox.setVisible(true));
        imageContainer.setOnMouseExited(e -> actionBox.setVisible(false));
        actionBox.setPickOnBounds(false);
        StackPane.setAlignment(actionBox, Pos.CENTER);
        imageContainer.getChildren().add(actionBox);

        // 4. Setup Filename Label
        nameLabel.setWrapText(false);
        nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        nameLabel.setPrefWidth(160);
        nameLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imageContainer, nameLabel);

        // --- BUTTON ACTIONS ---
        removeBtn.setOnAction(e -> {
            FileItem item = getItem();
            if (item != null && getGridView() != null) {
                getGridView().getItems().remove(item);
            }
        });

        replaceBtn.setOnAction(e -> {
            FileItem item = getItem();
            if (item != null && getGridView() != null) {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Replace File");
                File newFile = chooser.showOpenDialog(card.getScene().getWindow());
                if (newFile != null) {
                    getGridView().getItems().set(getIndex(), new FileItem(newFile.getAbsolutePath()));
                }
            }
        });

        // --- DRAG AND DROP REORDERING ---
        setupDragAndDropReordering();
    }

    private void setupDragAndDropReordering() {
        card.setOnDragDetected(event -> {
            if (getItem() == null) return;
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(getIndex()));
            db.setContent(content);
            event.consume();
        });

        card.setOnDragOver(event -> {
            if (event.getGestureSource() != card && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        card.setOnDragDropped(event -> {
            if (getItem() == null || getGridView() == null) return;
            try {
                int draggedIndex = Integer.parseInt(event.getDragboard().getString());
                int targetIndex = getIndex();

                @SuppressWarnings("unchecked")
                ObservableList<FileItem> items = (ObservableList<FileItem>) getGridView().getItems();
                FileItem draggedItem = items.remove(draggedIndex);
                items.add(targetIndex, draggedItem);

                event.setDropCompleted(true);
            } catch (Exception ex) {
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }

    @Override
    protected void updateItem(FileItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            currentFilePath = null;
            setGraphic(null);
            imageView.setImage(null);
            return;
        }

        nameLabel.setText(item.getName());

        if (!item.getPath().equals(currentFilePath)) {
            currentFilePath = item.getPath();
            imageView.setImage(null); 

            javafx.scene.image.Image cachedImage = com.rdchandrahas.shared.util.ThumbnailCache.get(item.getPath());
            
            if (cachedImage != null) {
                imageView.setImage(cachedImage);
            } else {
                PdfThumbnailUtil.loadThumbnailAsync(
                        item.getPath(),
                        // CANCEL CONDITION: Stop if the cell recycles to a different file
                        () -> !item.getPath().equals(currentFilePath), 
                        img -> {
                            // EXTRA SAFETY: Ensure we are still showing the right file
                            if (item.getPath().equals(currentFilePath)) {
                                imageView.setImage(img);
                            }
                        }
                );
            }
        }
        setGraphic(card);
    }
}