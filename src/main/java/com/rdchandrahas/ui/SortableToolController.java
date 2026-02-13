package com.rdchandrahas.ui;

import com.rdchandrahas.shared.component.FileListView;
import com.rdchandrahas.shared.model.ViewMode;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;

/**
 * Shared logic for tools that require sorting and view toggling.
 */
public interface SortableToolController extends InjectableController {
    
    FileListView getFileListView();
    ComboBox<String> getSortCombo();
    ToggleButton getListViewBtn();
    ToggleButton getGridViewBtn();

    default void setupSortAndViews() {
        getSortCombo().getItems().addAll("Name (A-Z)", "Name (Z-A)", "Size (Smallest)", "Size (Largest)");
        getSortCombo().setOnAction(e -> handleSort());
    }

    default void handleSort() {
        String selection = getSortCombo().getValue();
        if (selection == null) return;

        switch (selection) {
            case "Name (A-Z)" -> getFileListView().sortByName(true);
            case "Name (Z-A)" -> getFileListView().sortByName(false);
            case "Size (Smallest)" -> getFileListView().sortBySize(true);
            case "Size (Largest)" -> getFileListView().sortBySize(false);
        }
    }

    default void switchToList() {
        getFileListView().setViewMode(ViewMode.LIST);
        getListViewBtn().setSelected(true);
        getGridViewBtn().setSelected(false);
    }

    default void switchToGrid() {
        getFileListView().setViewMode(ViewMode.GRID);
        getListViewBtn().setSelected(false);
        getGridViewBtn().setSelected(true);
    }
}