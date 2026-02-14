package com.rdchandrahas.ui.base;

import com.rdchandrahas.core.ExecutionManager;
import com.rdchandrahas.core.NavigationService;
import com.rdchandrahas.ui.InjectableController;
import com.rdchandrahas.ui.SortableToolController;
import com.rdchandrahas.shared.component.FileListView;
import com.rdchandrahas.shared.model.FileItem;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * BaseToolController serves as the foundation for all PDF tool controllers.
 * It provides common UI management, asynchronous task execution, and 
 * safe PDF handling methods to ensure consistency across the application.
 */
public abstract class BaseToolController implements SortableToolController, InjectableController {

    @FXML protected Label toolTitleLabel;
    @FXML protected Button addFilesBtn;
    @FXML protected HBox customToolbarArea; 
    @FXML protected FileListView fileListView;
    @FXML protected ComboBox<String> sortCombo;
    @FXML protected ToggleButton listViewBtn;
    @FXML protected ToggleButton gridViewBtn;
    @FXML protected ProgressIndicator progressIndicator;
    @FXML protected Button actionBtn;

    protected NavigationService navigationService;

    /**
     * Creates a dynamic memory setting for PDF processing.
     * This can be further linked to the memory limit set in your MainController.
     */
    protected MemoryUsageSetting getMemorySetting() {
        // Uses the 500MB mixed strategy you mentioned, or falls back to disk if needed
        return MemoryUsageSetting.setupMixed(500L * 1024L * 1024L);
    }

    @FXML
    public void initialize() {
        setupSortAndViews();
        
        // --- SMART BUTTON LISTENER ---
        // Listens to the file list and updates the button color/state instantly
        fileListView.getItems().addListener((ListChangeListener<FileItem>) c -> {
            updateActionBtnState();
        });

        onInitialize(); 
        
        // Set the initial state of the button
        updateActionBtnState(); 
    }

    /**
     * Hook for subclasses to perform specific initialization logic.
     */
    protected abstract void onInitialize();

    // --- Dynamic UI State Management ---

    /**
     * Updates the Action Button styling based on current validation.
     */
    protected void updateActionBtnState() {
        actionBtn.getStyleClass().removeAll("action-button", "success-button", "danger-button", "button");

        if (fileListView.getItems().isEmpty()) {
            // EMPTY -> Neutral grey, Disabled
            actionBtn.setDisable(true);
            actionBtn.getStyleClass().add("button");
        } else if (isInputValid()) {
            // ALL FILES OK -> Green (action-button style), Enabled
            actionBtn.setDisable(false);
            actionBtn.getStyleClass().add("action-button");
        } else {
            // ERROR / INVALID FILES -> Red (danger-button style), Disabled
            actionBtn.setDisable(true);
            actionBtn.getStyleClass().add("danger-button");
        }
    }

    /**
     * Logic for file validation. Override in specific tool controllers.
     */
    protected boolean isInputValid() {
        return !fileListView.getItems().isEmpty();
    }

    // --- UI Configuration Helpers ---
    protected void setTitle(String title) { toolTitleLabel.setText(title); }
    protected void setActionText(String text) { actionBtn.setText(text); }
    protected void addToolbarItem(Node... nodes) { customToolbarArea.getChildren().addAll(nodes); }

    /**
     * Opens a FileChooser and adds selected files to the list using BATCH processing
     * to prevent UI lag when adding thousands of files at once.
     */
    protected void addFiles(String filterName, String... extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select " + filterName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName, extensions));
        List<File> files = chooser.showOpenMultipleDialog(addFilesBtn.getScene().getWindow());
        
        if (files != null && !files.isEmpty()) {
            List<FileItem> newItems = new ArrayList<>();
            for (File file : files) {
                newItems.add(new FileItem(file.getAbsolutePath()));
            }
            // Pushing all at once ensures the validation listener only runs once
            fileListView.getItems().addAll(newItems);
        }
    }

    // --- Abstract Handlers ---
    @FXML protected abstract void handleAddFiles();
    @FXML protected abstract void handleAction();

    // --- Common UI Handlers ---
    @FXML protected void handleBack() { navigationService.navigateTo("/ui/Dashboard.fxml"); }
    @FXML protected void onSortAction() { handleSort(); }
    @FXML protected void onListToggle() { switchToList(); }
    @FXML protected void onGridToggle() { switchToGrid(); }
    
    @FXML 
    protected void handleRemove() { 
        fileListView.getItems().clear(); 
        updateActionBtnState(); 
    }

    /**
     * Standardized background processing workflow for PDF tasks.
     */
    protected void processWithSaveDialog(String title, String defaultName, ToolTask task) {
        if (!isInputValid()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please check your file requirements.");
            return;
        }
        
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.setInitialFileName(defaultName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File dest = chooser.showSaveDialog(actionBtn.getScene().getWindow());

        if (dest == null) return;

        setBusy(true, actionBtn);
        ExecutionManager.submit(() -> {
            try {
                task.execute(dest);
                Platform.runLater(() -> {
                    setBusy(false, actionBtn);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Saved to: " + dest.getName());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setBusy(false, actionBtn);
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                });
            }
        });
    }

    // --- PDF Safe Methods (Hybrid Memory Optimized) ---
    protected PDDocument loadDocumentSafe(String path) throws IOException {
        return PDDocument.load(new File(path), getMemorySetting());
    }
    
    protected PDDocument loadDocumentSafe(String path, String pass) throws IOException {
        return PDDocument.load(new File(path), pass, getMemorySetting());
    }
    
    protected PDDocument createDocumentSafe() {
        return new PDDocument(getMemorySetting());
    }
    
    protected void mergeDocumentsSafe(List<String> paths, File dest) throws IOException {
        PDFMergerUtility m = new PDFMergerUtility();
        m.setDestinationFileName(dest.getAbsolutePath());
        for (String p : paths) m.addSource(new File(p));
        m.mergeDocuments(getMemorySetting());
    }

    // --- Interface Implementations ---
    @Override public FileListView getFileListView() { return fileListView; }
    @Override public ComboBox<String> getSortCombo() { return sortCombo; }
    @Override public ToggleButton getListViewBtn() { return listViewBtn; }
    @Override public ToggleButton getGridViewBtn() { return gridViewBtn; }
    @Override public void setNavigationService(NavigationService nav) { this.navigationService = nav; }

    protected void setBusy(boolean b, Button btn) {
        progressIndicator.setVisible(b);
        btn.setDisable(b);
        if(!b) updateActionBtnState(); 
    }

    protected void showAlert(Alert.AlertType t, String title, String content) {
        Alert a = new Alert(t); 
        a.setTitle(title); 
        a.setHeaderText(null); 
        a.setContentText(content); 
        a.show();
    }

    @FunctionalInterface 
    public interface ToolTask { 
        void execute(File destination) throws Exception; 
    }
}