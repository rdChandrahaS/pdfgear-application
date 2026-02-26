package com.rdchandrahas.ui.base;

import com.rdchandrahas.core.ExecutionManager;
import com.rdchandrahas.core.NavigationService;
import com.rdchandrahas.core.PdfOperation;
import com.rdchandrahas.core.PdfService;
import com.rdchandrahas.ui.SortableToolController;
import com.rdchandrahas.shared.component.FileListView;
import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.shared.util.LogManager;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * BaseToolController serves as the foundation for all PDF tool controllers.
 * It provides common UI management, asynchronous task execution, and 
 * safe PDF handling methods to ensure consistency across the application.
 */
public abstract class BaseToolController implements SortableToolController {

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
    private final PdfService pdfService = new PdfService(); // Instantiate core service

    @FXML
    public void initialize() {
        setupSortAndViews();
        
        // --- SMART BUTTON LISTENER ---
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
    protected void updateActionBtnState() {
        actionBtn.getStyleClass().removeAll("action-button", "success-button", "danger-button", "button");

        if (fileListView.getItems().isEmpty()) {
            actionBtn.setDisable(true);
            actionBtn.getStyleClass().add("button");
        } else if (isInputValid()) {
            actionBtn.setDisable(false);
            actionBtn.getStyleClass().add("action-button");
        } else {
            actionBtn.setDisable(true);
            actionBtn.getStyleClass().add("danger-button");
        }
    }

    protected boolean isInputValid() {
        return !fileListView.getItems().isEmpty();
    }

    // --- UI Configuration Helpers ---
    protected void setTitle(String title) { toolTitleLabel.setText(title); }
    protected void setActionText(String text) { actionBtn.setText(text); }
    protected void addToolbarItem(Node... nodes) { customToolbarArea.getChildren().addAll(nodes); }

    /**
     * Utility method for controllers to launch a FileChooser and append items.
     * Includes batch-addition to prevent UI freezing on massive selections.
     */
    protected void addFiles(String filterName, String... extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Files");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName, extensions));

        List<File> files = chooser.showOpenMultipleDialog(actionBtn.getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            
            // FIX: Convert files to FileItems in memory first
            List<FileItem> newItems = files.stream()
                    .map(f -> new FileItem(f.getAbsolutePath()))
                    .toList();

            // FIX: Add all items at once. This ensures the ListChangeListener 
            // in FileListView only triggers ONE refresh cycle instead of thousands.
            fileListView.getItems().addAll(newItems);
            
            updateActionBtnState();
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
                logError("Execution failed: " + e.getMessage());
                Platform.runLater(() -> {
                    setBusy(false, actionBtn);
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                });
            }
        });
    }

    // --- DEPENDENCY INJECTION / TEMPLATE METHOD (The Gold Standard) ---
    /**
     * Centralized execution method. 
     * Injects the global UI memory settings dynamically and handles closing streams automatically.
     */
    protected void processPdfSafely(File inputFile, File outputFile, PdfOperation operation) throws IOException, GeneralSecurityException {
        try (PDDocument document = PDDocument.load(inputFile, PdfService.getGlobalMemorySetting())) {
            operation.execute(document);
            document.save(outputFile);
        }
    }

    // --- PDF Safe Fallback Methods ---
    // For edge-cases where a child controller MUST manage the document lifecycle manually
    protected PDDocument loadDocumentSafe(String path) throws IOException {
        return PDDocument.load(new File(path), PdfService.getGlobalMemorySetting());
    }
    
    protected PDDocument loadDocumentSafe(String path, String pass) throws IOException {
        return PDDocument.load(new File(path), pass, PdfService.getGlobalMemorySetting());
    }
    
    protected PDDocument createDocumentSafe() {
        return new PDDocument(PdfService.getGlobalMemorySetting());
    }
    
    /**
     * Delegates to the highly-optimized PdfService to prevent OS file limits
     * and memory exhaustion on massive batches.
     */
    protected void mergeDocumentsSafe(List<String> paths, File dest) throws IOException, GeneralSecurityException {
        try {
            pdfService.merge(paths, dest.getAbsolutePath());
        } catch (Exception e) {
            throw new IOException("Merge operation failed", e);
        }
    }

    // --- Interface Implementations & Helpers ---
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
        void execute(File destination) throws IOException, GeneralSecurityException; 
    }

    protected void logInfo(String message) {
        LogManager.log("INFO", message);
    }

    protected void logError(String message) {
        LogManager.log("ERROR", message);
    }
}