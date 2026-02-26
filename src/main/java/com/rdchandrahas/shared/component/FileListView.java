package com.rdchandrahas.shared.component;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.shared.model.ViewMode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.GridView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileListView extends StackPane {

    private final ObservableList<FileItem> items = FXCollections.observableArrayList();
    private final ListView<FileItem> listView = new ListView<>(items);
    
    private final GridView<FileItem> gridView = new GridView<>(items);
    
    private ViewMode currentMode = ViewMode.LIST;

    public FileListView() {
        // Configure GridView UI
        gridView.setCellWidth(180);
        gridView.setCellHeight(240);
        gridView.setHorizontalCellSpacing(20);
        gridView.setVerticalCellSpacing(20);
        gridView.setCellFactory(grid -> new FileGridCell());
        gridView.getStyleClass().add("grid-pane-container");

        // Add both views to the StackPane
        getChildren().addAll(listView, gridView);

        setupListView();
        setupDesktopDropSupport();
        
        setViewMode(ViewMode.LIST);
    }

    private void setupListView() {
        listView.setCellFactory(lv -> new FileListCell());
    }

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
                List<FileItem> droppedItems = new ArrayList<>();
                for (File file : db.getFiles()) {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".pdf") || name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg") || name.endsWith(".webp")) {
                        droppedItems.add(new FileItem(file.getAbsolutePath()));
                    }
                }
                
                if (!droppedItems.isEmpty()) {
                    items.addAll(droppedItems);
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void setViewMode(ViewMode mode) {
        this.currentMode = mode;
        listView.setVisible(mode == ViewMode.LIST);
        gridView.setVisible(mode == ViewMode.GRID);
    }

    public ViewMode getViewMode() { return currentMode; }

    public ObservableList<FileItem> getItems() { return items; }
    
    public FileItem getSelectedItem() { 
        if(currentMode == ViewMode.LIST) {
            return listView.getSelectionModel().getSelectedItem(); 
        }
        return null;
    }
    
    public void sortByName(boolean ascending) {
        items.sort((a, b) -> {
            int result = compareAlphanumeric(a.getName(), b.getName());
            return ascending ? result : -result;
        });
    }

    public void sortBySize(boolean ascending) {
        items.sort((a, b) -> {
            int result = Long.compare(a.getSize(), b.getSize());
            return ascending ? result : -result;
        });
    }

    private int compareAlphanumeric(String s1, String s2) {
        if (s1 == null || s2 == null) return s1 == null ? (s2 == null ? 0 : -1) : 1;

        List<String> chunks1 = extractChunks(s1);
        List<String> chunks2 = extractChunks(s2);

        int minLen = Math.min(chunks1.size(), chunks2.size());
        for (int i = 0; i < minLen; i++) {
            String chunk1 = chunks1.get(i);
            String chunk2 = chunks2.get(i);

            if (isDigit(chunk1.charAt(0)) && isDigit(chunk2.charAt(0))) {
                try {
                    long num1 = Long.parseLong(chunk1);
                    long num2 = Long.parseLong(chunk2);
                    int result = Long.compare(num1, num2);
                    if (result != 0) return result;
                } catch (NumberFormatException e) {
                    int result = chunk1.compareToIgnoreCase(chunk2);
                    if (result != 0) return result;
                }
            } else {
                int result = chunk1.compareToIgnoreCase(chunk2);
                if (result != 0) return result;
            }
        }
        return Integer.compare(chunks1.size(), chunks2.size());
    }

    private List<String> extractChunks(String s) {
        List<String> chunks = new ArrayList<>();
        Matcher matcher = Pattern.compile("(\\d+|\\D+)").matcher(s);
        while (matcher.find()) chunks.add(matcher.group());
        return chunks;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}