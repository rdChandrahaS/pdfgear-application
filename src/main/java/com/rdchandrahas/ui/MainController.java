package com.rdchandrahas.ui;

import com.rdchandrahas.core.NavigationService;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;

public class MainController implements InjectableController {

    // Must match fx:id in MainLayout.fxml
    @FXML private StackPane contentPane;
    
    // Sidebar Button IDs
    @FXML private Button navDashboard;
    @FXML private Button navMerge;
    @FXML private Button navSplit;
    @FXML private Button navCompress;
    @FXML private Button navConvert;
    @FXML private Button navProtect;
    @FXML private Button navUnlock;
    
    // Theme Toggle IDs
    @FXML private ToggleButton lightModeBtn;
    @FXML private ToggleButton darkModeBtn;

    private NavigationService navigationService;

    @FXML
    public void initialize() {
        // Initialize the NavigationService to use the central StackPane
        this.navigationService = new NavigationService(contentPane);
        
        // 1. Requirement: Start in Dark Mode by default
        // We use runLater to ensure the Scene is fully loaded before applying CSS
        Platform.runLater(this::setDarkTheme);
        
        // 2. Start on the Dashboard
        goHome();
    }

    @Override
    public void setNavigationService(NavigationService nav) {
        // Managed internally for the root layout
    }

    // --- Menu & Theme Handlers ---

    @FXML
    private void handleExit() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About PDFGear");
        alert.setHeaderText("PDFGear Desktop v1.0.0");
        alert.setContentText("A powerful and memory-safe PDF utility built with JavaFX.");
        alert.showAndWait();
    }

    /**
     * Switches the application to Light Mode
     */
    @FXML
    private void setLightTheme() {
        applyTheme("/css/light.css");
        if (lightModeBtn != null) lightModeBtn.setSelected(true);
        if (darkModeBtn != null) darkModeBtn.setSelected(false);
    }

    /**
     * Switches the application to Dark Mode
     */
    @FXML
    private void setDarkTheme() {
        applyTheme("/css/dark.css");
        if (darkModeBtn != null) darkModeBtn.setSelected(true);
        if (lightModeBtn != null) lightModeBtn.setSelected(false);
    }

    /**
     * Helper to clear old styles and apply new theme
     */
    private void applyTheme(String themePath) {
        try {
            if (contentPane.getScene() != null) {
                contentPane.getScene().getStylesheets().clear();
                contentPane.getScene().getStylesheets().add(getClass().getResource(themePath).toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Could not load theme: " + themePath);
        }
    }

    // --- Navigation Methods (Master Layout Pattern) ---

    @FXML private void goHome() { 
        navigationService.navigateTo("/ui/Dashboard.fxml"); 
    }

    @FXML private void goMerge() { 
        navigationService.navigateToTool(new MergeController()); 
    }

    @FXML private void goSplit() { 
        navigationService.navigateToTool(new SplitController()); 
    }

    @FXML private void goCompress() { 
        navigationService.navigateToTool(new CompressController()); 
    }

    @FXML private void goImageToPdf() { 
        navigationService.navigateToTool(new ImageToPdfController()); 
    }

    @FXML private void goProtect() { 
        navigationService.navigateToTool(new ProtectController()); 
    }

    @FXML private void goUnlock() { 
        navigationService.navigateToTool(new UnlockController()); 
    }
}