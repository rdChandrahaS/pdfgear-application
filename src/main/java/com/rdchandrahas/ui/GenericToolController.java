package com.rdchandrahas.ui;

import com.rdchandrahas.core.NavigationService;
import com.rdchandrahas.shared.component.FileListView;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;

public class GenericToolController implements SortableToolController {
	
/*
 * Whenever you create a new FXML for a tool (e.g., UnlockPdf.fxml), simply set its controller to this generic class in the FXML file:
 * 
 * 
 * 
 * 
<VBox fx:controller="com.rdchandrahas.ui.GenericToolController" ...>
	<ComboBox fx:id="sortCombo" onAction="#onSortAction" />
	<ToggleButton fx:id="listViewBtn" onAction="#onListToggle" />
	<ToggleButton fx:id="gridViewBtn" onAction="#onGridToggle" />
	<FileListView fx:id="fileListView" />
</VBox>
 */
    @FXML private FileListView fileListView;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ToggleButton listViewBtn;
    @FXML private ToggleButton gridViewBtn;

    private NavigationService navigationService;

    @FXML
    public void initialize() {
        setupSortAndViews();
    }

    // Implementation of SortableToolController getters
    @Override public FileListView getFileListView() { return fileListView; }
    @Override public ComboBox<String> getSortCombo() { return sortCombo; }
    @Override public ToggleButton getListViewBtn() { return listViewBtn; }
    @Override public ToggleButton getGridViewBtn() { return gridViewBtn; }

    @Override
    public void setNavigationService(NavigationService navService) {
        this.navigationService = navService;
    }

    @FXML
    private void handleBack() {
        navigationService.navigateTo("/ui/Dashboard.fxml");
    }

    // FXML Event Handlers linked to the interface logic
    @FXML private void onSortAction() { handleSort(); }
    @FXML private void onListToggle() { switchToList(); }
    @FXML private void onGridToggle() { switchToGrid(); }
}