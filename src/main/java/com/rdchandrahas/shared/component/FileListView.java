package com.rdchandrahas.shared.component;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.shared.model.ViewMode;
import com.rdchandrahas.shared.util.PdfThumbnailUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import java.io.File;

public class FileListView extends StackPane {

    private final ObservableList<FileItem> items = FXCollections.observableArrayList();
    private final ListView<FileItem> listView = new ListView<>(items);
    private final TilePane gridPane = new TilePane();
    private final ScrollPane gridScroll = new ScrollPane(gridPane);
    private ViewMode currentMode = ViewMode.LIST;

    public FileListView() {
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setPrefTileWidth(180);
        gridPane.setPrefTileHeight(240);
        gridPane.setAlignment(Pos.TOP_LEFT);
        
        // Remove hardcoded background so CSS handles it
        gridPane.getStyleClass().add("grid-pane-container");

        gridScroll.setFitToWidth(true);
        gridScroll.setPannable(true);
        gridScroll.getStyleClass().add("grid-scroll");

        getChildren().addAll(listView, gridScroll);

        setupListView();
        setupGridView();
        setupDesktopDropSupport();
        setViewMode(ViewMode.LIST);
    }

    private void setupListView() {
        listView.setCellFactory(lv -> new FileListCell());
    }

    private void setupGridView() {
        items.addListener((javafx.collections.ListChangeListener<FileItem>) c -> refreshGrid());
        refreshGrid();
    }

    private void refreshGrid() {
        gridPane.getChildren().clear();
        for (int i = 0; i < items.size(); i++) {
            FileItem item = items.get(i);
            int index = i;
            VBox card = createGridCard(item, index);
            gridPane.getChildren().add(card);
        }
    }

    private VBox createGridCard(FileItem item, int index) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(170);
        
        // FIX: Replaced hardcoded style with CSS class
        card.getStyleClass().add("grid-card");

        ImageView image = new ImageView();
        image.setFitWidth(150);
        image.setFitHeight(200);
        
        // FIX: Stops stretching! Images keep their aspect ratio.
        image.setPreserveRatio(true); 

        Label name = new Label(item.getName());
        name.setWrapText(true);
        name.setMaxWidth(160);
        
        // FIX: Removed hardcoded White color. CSS will handle this.
        // name.setTextFill(Color.WHITE); 

        PdfThumbnailUtil.loadThumbnailAsync(item.getPath(), image::setImage);
        card.getChildren().addAll(image, name);
        enableDragReorderGrid(card, index);
        return card;
    }

    // ... (Keep the rest of the methods: setupDesktopDropSupport, enableDragReorderGrid, getters/setters unchanged) ...
    
    private void setupDesktopDropSupport() {
        this.setOnDragOver(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        this.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    if (file.getName().toLowerCase().endsWith(".pdf") || 
                        file.getName().toLowerCase().endsWith(".jpg") || 
                        file.getName().toLowerCase().endsWith(".png")) {
                        items.add(new FileItem(file.getAbsolutePath()));
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void enableDragReorderGrid(VBox card, int index) {
        card.setOnDragDetected(event -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(index));
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
            int draggedIndex = Integer.parseInt(event.getDragboard().getString());
            FileItem draggedItem = items.remove(draggedIndex);
            items.add(index, draggedItem);
            event.setDropCompleted(true);
            event.consume();
        });
    }

    public void setViewMode(ViewMode mode) {
        currentMode = mode;
        listView.setVisible(mode == ViewMode.LIST);
        gridScroll.setVisible(mode == ViewMode.GRID);
    }

    public ObservableList<FileItem> getItems() { return items; }
    public FileItem getSelectedItem() { return listView.getSelectionModel().getSelectedItem(); }
    
    public void sortByName(boolean ascending) {
        items.sort((a, b) -> ascending ? a.getName().compareToIgnoreCase(b.getName()) : b.getName().compareToIgnoreCase(a.getName()));
    }

    public void sortBySize(boolean ascending) {
        items.sort((a, b) -> {
            int result = Long.compare(a.getSize(), b.getSize());
            return ascending ? result : -result;
        });
    }
}