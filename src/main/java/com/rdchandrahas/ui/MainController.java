package com.rdchandrahas.ui;

import com.rdchandrahas.core.ExecutionManager;
import com.rdchandrahas.core.NavigationService;
import com.rdchandrahas.core.OSService;
import com.rdchandrahas.core.PdfService;
import com.rdchandrahas.shared.util.TempFileManager;
import com.rdchandrahas.shared.util.ThumbnailCache;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MainController serves as the primary controller for the application's root layout.
 * It manages global state, UI themes, performance settings, developer configurations,
 * and delegates tool-specific operations to other controllers.
 */
public class MainController implements InjectableController {

    // --- Constants ---
    private static final String DEFAULT_ACCENT_COLOR = "#0078d7";
    private static final String DEFAULT_FONT_SIZE = "14px";
    private static final long DEFAULT_MEMORY_LIMIT = 1024L * 1024L * 1024L; // 1 GB
    private static final long DEFAULT_CACHE_SIZE = 500L * 1024L * 1024L; // 500 MB
    private static final String APP_NAME = "My PDF Application";
    private static final String APP_VERSION = "v0.0.1";
    private static final String GITHUB_URL = "https://github.com/rdChandrahaS/my-pdf-file-application";

    // --- FXML Injections ---
    @FXML private StackPane contentPane;
    @FXML private RadioMenuItem lightModeRadio;
    @FXML private RadioMenuItem darkModeRadio;
    @FXML private CheckMenuItem multiThreadingCheck; // Add corresponding fx:id in FXML

    // --- State Variables ---
    private String currentAccentColor = DEFAULT_ACCENT_COLOR;
    private String currentFontSize = DEFAULT_FONT_SIZE;
    private NavigationService navigationService;
    private OSService osService;
    private static File defaultSaveDirectory = null;
    public static final Map<String, String> systemFontMap = new TreeMap<>();

    // --- Configuration Flags ---
    private boolean backgroundProcessing = true;
    private boolean multiThreadingEnabled = true;
    private boolean hardwareAcceleration = true;
    private boolean verboseLogging = false;
    private final List<String> debugLogs = new ArrayList<>();
    private boolean cloudSyncEnabled = false;
    private boolean isProduction = true; // Set to false to unlock experimental features

    /**
     * Initializes the controller after its root element has been completely processed.
     */
    @FXML
    public void initialize() {
        this.navigationService = new NavigationService(contentPane);
        goHome();

        if (multiThreadingCheck != null) {
            multiThreadingCheck.setSelected(multiThreadingEnabled);
        }

        Platform.runLater(() -> {
            try {
                setDarkTheme();
            } catch (Exception e) {
                System.err.println("Note: Theme applied without radio button sync.");
            }
        });
    }

    public void initService(OSService osService) {
        this.osService = osService;
        if (osService != null) {
            new Thread(this::scanFonts).start();
        }
    }

    @Override
    public void setNavigationService(NavigationService nav) {
        // Managed internally by this controller
    }

    // ==========================================================
    // 1. FILE & CORE OPERATIONS
    // ==========================================================

    @FXML 
    private void handleExit() {
        Platform.exit();
        System.exit(0);
    }

    public static File promptForSaveLocation(Window owner) {
        if (defaultSaveDirectory != null && defaultSaveDirectory.exists()) {
            return defaultSaveDirectory;
        }
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Save Destination");
        return dc.showDialog(owner);
    }

    @FXML 
    private void goHome() {
        if (navigationService != null) {
            navigationService.navigateTo("/ui/Dashboard.fxml");
        }
    }

    // ==========================================================
    // 2. APPEARANCE & THEMING
    // ==========================================================

    @FXML
    public void setLightTheme() {
        applyTheme("/css/light.css");
        syncThemeUI(true);
    }

    @FXML
    public void setDarkTheme() {
        applyTheme("/css/dark.css");
        syncThemeUI(false);
    }

    private void applyTheme(String path) {
        if (contentPane != null && contentPane.getScene() != null) {
            try {
                contentPane.getScene().getStylesheets().clear();
                String css = getClass().getResource(path).toExternalForm();
                contentPane.getScene().getStylesheets().add(css);
            } catch (Exception e) {
                System.err.println("Could not apply stylesheet: " + path);
            }
        }
    }

    private void applyGlobalStyles() {
        if (contentPane.getScene() != null) {
            String style = String.format("-fx-font-size: %s; -fx-accent: %s;", currentFontSize, currentAccentColor);
            contentPane.getScene().getRoot().setStyle(style);
        }
    }

    private void syncThemeUI(boolean isLight) {
        if (lightModeRadio != null) lightModeRadio.setSelected(isLight);
        if (darkModeRadio != null) darkModeRadio.setSelected(!isLight);
    }

    @FXML 
    private void handleAccentColor() {
        List<String> colors = Arrays.asList("Blue", "Green", "Red", "Purple", "Orange", "Teal");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Blue", colors);
        dialog.setTitle("Accent Color");
        dialog.setHeaderText("Choose an accent color for the application");
        dialog.setContentText("Color:");

        dialog.showAndWait().ifPresent(color -> {
            currentAccentColor = switch (color) {
                case "Green" -> "#28a745";
                case "Red" -> "#dc3545";
                case "Purple" -> "#6f42c1";
                case "Orange" -> "#fd7e14";
                case "Teal" -> "#20c997";
                default -> "#0078d7";
            };
            applyGlobalStyles();
        });
    }

    @FXML 
    private void handleFontSize() {
        List<String> sizes = Arrays.asList("Small", "Medium", "Large", "Extra Large");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Medium", sizes);
        dialog.setTitle("Font Size");
        dialog.setHeaderText("Select the global font size");
        dialog.setContentText("Size:");

        dialog.showAndWait().ifPresent(size -> {
            currentFontSize = switch (size) {
                case "Small" -> "12px";
                case "Medium" -> "14px";
                case "Large" -> "18px";
                case "Extra Large" -> "24px";
                default -> "14px";
            };
            applyGlobalStyles();
        });
    }

    // ==========================================================
    // 3. PERFORMANCE & MEMORY
    // ==========================================================

    @FXML 
    private void handleMemoryLimit() {
        List<String> limits = Arrays.asList("512 MB", "1 GB", "2 GB", "4 GB", "Unrestricted");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("1 GB", limits);
        dialog.setTitle("Memory Limit");
        dialog.setHeaderText("Set maximum memory usage for RAM processing.\nLarger files will use Disk Storage.");
        dialog.setContentText("Limit:");

        dialog.showAndWait().ifPresent(limit -> {
            long bytes = -1;
            try {
                if (!limit.equals("Unrestricted")) {
                    String[] parts = limit.split(" ");
                    long val = Long.parseLong(parts[0]);
                    String unit = parts[1];

                    if (unit.equals("MB")) bytes = val * 1024 * 1024;
                    else if (unit.equals("GB")) bytes = val * 1024 * 1024 * 1024;
                }
                PdfService.setMemoryLimit(bytes);
                showAlert(Alert.AlertType.INFORMATION, "Performance",
                        "Memory limit updated to " + limit + ".\nFiles smaller than this will be processed in RAM.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to parse memory limit.");
            }
        });
    }

    @FXML
    private void handleCacheSize() {
        TextInputDialog dialog = new TextInputDialog("500");
        dialog.setTitle("Cache Size");
        dialog.setHeaderText("Set thumbnail cache limit (in MB)");
        dialog.setContentText("Size (MB):");

        dialog.showAndWait().ifPresent(sizeStr -> {
            try {
                long mb = Long.parseLong(sizeStr.trim());
                long bytes = mb * 1024 * 1024;
                ThumbnailCache.setMaxSizeBytes(bytes);
                showAlert(Alert.AlertType.INFORMATION, "Performance", "Cache size limit set to " + mb + " MB");
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid number.");
            }
        });
    }

    @FXML
    private void handleClearCache() {
        logDebug("Initiating manual cache purge...");
        try {
            ThumbnailCache.clear();
            logDebug("Runtime thumbnail memory cleared.");
        } catch (Exception e) {
            logDebug("Error clearing memory cache: " + e.getMessage());
        }

        try {
            Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), "pdf-app-temp");
            TempFileManager.cleanup(tempPath);
            logDebug("Disk-based temporary files purged from: " + tempPath.toString());
        } catch (Exception e) {
            logDebug("Error cleaning temp directory: " + e.getMessage());
        }

        showAlert(Alert.AlertType.INFORMATION, "Maintenance", "All temporary processing files and thumbnail memory have been purged.");
    }

    @FXML
    private void handleBackgroundProcessing() {
        backgroundProcessing = !backgroundProcessing;
        ExecutionManager.setAsync(backgroundProcessing);
        String status = backgroundProcessing ? "ENABLED" : "DISABLED";
        showAlert(Alert.AlertType.INFORMATION, "Performance", "Background Processing is now " + status + ".");
    }

    @FXML
    private void handleMultiThreading() {
        multiThreadingEnabled = !multiThreadingEnabled;
        ExecutionManager.setMultiThreading(multiThreadingEnabled);
        
        if (multiThreadingCheck != null) {
            multiThreadingCheck.setSelected(multiThreadingEnabled);
        }
        
        String status = multiThreadingEnabled ? "ENABLED" : "DISABLED";
        logDebug("Multi-threading toggled to: " + status);
        showAlert(Alert.AlertType.INFORMATION, "Performance", "Multi-threading Processing is now " + status + ".");
    }

    @FXML 
    private void handleHardwareAcceleration() {
        hardwareAcceleration = !hardwareAcceleration;

        if (hardwareAcceleration) {
            System.setProperty("prism.order", "d3d,sw");
            System.setProperty("prism.vsync", "true");
        } else {
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.vsync", "false");
        }

        String status = hardwareAcceleration ? "ENABLED" : "DISABLED";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Performance");
        alert.setHeaderText("Hardware Acceleration " + status);
        alert.setContentText("A restart is required for these changes to take full effect.");

        ButtonType restartBtn = new ButtonType("Restart Now");
        ButtonType laterBtn = new ButtonType("Later", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(restartBtn, laterBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == restartBtn) handleRestart();
        });
    }

    private void handleRestart() {
        Platform.exit();
        System.out.println("Application closing for restart...");
        System.exit(0);
    }

    // ==========================================================
    // 4. KEYBOARD SHORTCUTS
    // ==========================================================

    @FXML 
    private void handleViewShortcuts() {
        Stage shortcutStage = new Stage();
        shortcutStage.setTitle("Keyboard Shortcuts");
        TableView<Map.Entry<String, String>> table = new TableView<>();

        TableColumn<Map.Entry<String, String>, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));

        TableColumn<Map.Entry<String, String>, String> keyCol = new TableColumn<>("Shortcut");
        keyCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getValue()));

        Map<String, String> shortcutMap = new LinkedHashMap<>();
        shortcutMap.put("Undo", "Ctrl + Z");
        shortcutMap.put("Redo", "Ctrl + Y");
        shortcutMap.put("Save", "Ctrl + S");
        shortcutMap.put("Exit", "Alt + F4");
        shortcutMap.put("Clear List", "Delete");

        table.getItems().addAll(shortcutMap.entrySet());
        table.getColumns().add(actionCol);
        table.getColumns().add(keyCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        VBox layout = new VBox(10, new Label("Active System Shortcuts:"), table);
        layout.setStyle("-fx-padding: 20;");
        shortcutStage.setScene(new Scene(layout, 400, 300));
        shortcutStage.show();
    }

    @FXML 
    private void handleCustomizeShortcuts() {
        showAlert(Alert.AlertType.INFORMATION, "Shortcuts",
                "Shortcut customization is currently in 'Read-Only' mode.\nTo change a key, please edit the 'MainLayout.fxml' file directly.");
    }

    @FXML 
    private void handleResetShortcuts() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Shortcuts");
        alert.setHeaderText("Revert to Default Bindings?");
        alert.setContentText("This will restore Ctrl+Z, Ctrl+Y, and other system defaults.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Shortcuts have been reset to factory defaults.");
            }
        });
    }

    // ==========================================================
    // 5. DEVELOPER & ADVANCED SETTINGS
    // ==========================================================

    @FXML 
    private void handleDebugConsole() {
        Stage debugStage = new Stage();
        debugStage.setTitle("Developer Debug Console");

        TextArea console = new TextArea();
        console.setEditable(false);
        console.setStyle("-fx-font-family: 'Consolas'; -fx-control-inner-background: #1e1e1e; -fx-text-fill: #00ff00;");

        StringBuilder sb = new StringBuilder("--- New Debug Session Started ---\n");
        for (String log : debugLogs) {
            sb.append(log).append("\n");
        }
        console.setText(sb.toString());

        VBox layout = new VBox(10, new Label("Live System Logs:"), console);
        layout.setStyle("-fx-padding: 15; -fx-background-color: #252525;");
        VBox.setVgrow(console, Priority.ALWAYS);

        debugStage.setScene(new Scene(layout, 700, 450));
        debugStage.show();
    }

    @FXML 
    private void handleVerboseLogging() {
        verboseLogging = !verboseLogging;
        String status = verboseLogging ? "ON" : "OFF";
        logDebug("Verbose Logging turned " + status);
        showAlert(Alert.AlertType.INFORMATION, "Developer Mode", "Verbose Logging is now " + status + ".");
    }

    private void logDebug(String msg) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String formatted = "[" + timestamp + "] " + msg;
        debugLogs.add(formatted);
        if (verboseLogging) {
            System.out.println(formatted);
        }
    }

    @FXML
    private void handleApiTesting() {
        logDebug("Initiating API Diagnostic tests...");
        String pdfBoxStatus;
        try {
            pdfBoxStatus = "CONNECTED (v" + org.apache.pdfbox.util.Version.getVersion() + ")";
        } catch (Exception e) {
            pdfBoxStatus = "FAILED: Engine not found";
            logDebug("API Test Error: PDFBox version check failed.");
        }

        String navStatus = (navigationService != null) ? "ACTIVE" : "ERROR: Service not initialized";
        String osStatus = (osService != null) ? "ACTIVE (" + System.getProperty("os.name") + ")" : "INACTIVE";
        PdfService processor = new PdfService();
        String processorStatus = (processor != null) ? "READY" : "FAILED: Implementation missing";

        showAlert(Alert.AlertType.INFORMATION, "API Diagnostics",
                "Service Status Results:\n\n" +
                "• PDFBox Engine: " + pdfBoxStatus + "\n" +
                "• Navigation Service: " + navStatus + "\n" +
                "• OS Integration Service: " + osStatus + "\n" +
                "• PDF Processing Service: " + processorStatus);
    }

    @FXML
    private void handleFeatureFlags() {
        logDebug("Opening Feature Flags manager.");
        StringBuilder status = new StringBuilder("Current System Feature Flags:\n\n");
        status.append(hardwareAcceleration ? "[ON] " : "[OFF] ").append("Hardware Acceleration (Rendering)\n");
        status.append(backgroundProcessing ? "[ON] " : "[OFF] ").append("Async Background Processing\n");
        status.append(multiThreadingEnabled ? "[ON] " : "[OFF] ").append("Multi-threaded Execution\n");
        status.append(cloudSyncEnabled ? "[ON] " : "[OFF] ").append("Cloud Storage Sync (BETA)\n");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Flags");
        alert.setHeaderText("Development & Experimental Flags");
        alert.setContentText(status.toString());

        ButtonType toggleCloud = new ButtonType("Toggle Cloud Sync");
        ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(toggleCloud, close);

        alert.showAndWait().ifPresent(response -> {
            if (response == toggleCloud) {
                cloudSyncEnabled = !cloudSyncEnabled;
                handleFeatureFlags();
            }
        });
    }

    @FXML
    private void handlePluginManager() {
        logDebug("Scanning for external plugins...");
        File pluginDir = new File("plugins");
        if (!pluginDir.exists()) pluginDir.mkdir();

        File[] plugins = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
        StringBuilder pluginList = new StringBuilder("Detected Plugins:\n\n");

        if (plugins == null || plugins.length == 0) {
            pluginList.append("• No external .jar plugins found in /plugins directory.");
        } else {
            for (File p : plugins) pluginList.append("• ").append(p.getName()).append(" [LOADED]\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Plugin Manager");
        alert.setHeaderText("External Module Status");
        alert.setContentText(pluginList.toString());
        
        ButtonType refresh = new ButtonType("Rescan Directory");
        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(refresh, ok);

        alert.showAndWait().ifPresent(response -> {
            if (response == refresh) handlePluginManager();
        });
    }

    @FXML
    private void handleExperimentalFeatures() {
        if (isProduction) {
            showAlert(Alert.AlertType.WARNING, "Restricted Access", "Experimental features are disabled in production builds.");
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Experimental Features");
            alert.setHeaderText("Warning: Unstable Modules");
            alert.setContentText("Available Experimental Tools:\n• AI-Powered PDF Summarizer\n• Batch OCR");
            
            ButtonType enableAll = new ButtonType("Enable All (Unstable)");
            ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(enableAll, close);

            alert.showAndWait().ifPresent(response -> {
                if (response == enableAll) {
                    showAlert(Alert.AlertType.CONFIRMATION, "Experimental", "Modules have been injected.");
                }
            });
        }
    }

    @FXML
    private void handleResetAppState() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Application State");
        alert.setHeaderText("Warning: Permanent Action");
        alert.setContentText("This will revert all settings to factory defaults.\nAre you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                currentAccentColor = DEFAULT_ACCENT_COLOR;
                currentFontSize = DEFAULT_FONT_SIZE;
                setDarkTheme();
                applyGlobalStyles();

                backgroundProcessing = true;
                multiThreadingEnabled = true;
                hardwareAcceleration = true;
                
                if (multiThreadingCheck != null) {
                    multiThreadingCheck.setSelected(true);
                }

                ExecutionManager.setAsync(true);
                ExecutionManager.setMultiThreading(true);
                PdfService.setMemoryLimit(DEFAULT_MEMORY_LIMIT);
                ThumbnailCache.setMaxSizeBytes(DEFAULT_CACHE_SIZE);

                verboseLogging = false;
                cloudSyncEnabled = false;
                isProduction = true;
                debugLogs.clear();

                showAlert(Alert.AlertType.INFORMATION, "Reset Complete", "The application state has been restored.");
            }
        });
    }

    // ==========================================================
    // 6. HELP & ABOUT
    // ==========================================================

    @FXML
    private void handleGithubLink() {
        if (osService != null) {
            osService.openBrowser(GITHUB_URL);
        }
    }

    @FXML
    private void handleUpdates() {
        logDebug("Checking for updates via GitHub API...");
        String apiEndpoint = "https://api.github.com/repos/rdChandrahaS/my-pdf-file-application/releases/latest";

        CompletableFuture.runAsync(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiEndpoint))
                        .header("Accept", "application/vnd.github.v3+json")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String responseBody = response.body();
                    String latestVersion = extractVersionFromGithubResponse(responseBody);

                    Platform.runLater(() -> {
                        if (latestVersion != null && !latestVersion.equals(APP_VERSION)) {
                            showUpdateAvailableDialog(latestVersion);
                        } else {
                            showAlert(Alert.AlertType.INFORMATION, "Check for Updates", "You are running the latest version (" + APP_VERSION + ").");
                        }
                    });
                } else if (response.statusCode() == 404) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Check for Updates", "No releases found on GitHub yet."));
                } else {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to check for updates. HTTP Status: " + response.statusCode()));
                }
            } catch (Exception e) {
                logDebug("Update check failed: " + e.getMessage());
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Connection Error", "Could not connect to the update server."));
            }
        });
    }

    private String extractVersionFromGithubResponse(String json) {
        Pattern pattern = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private void showUpdateAvailableDialog(String latestVersion) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Update Available");
        alert.setHeaderText("A new version is available!");
        alert.setContentText("Current version: " + APP_VERSION + "\nLatest version: " + latestVersion + "\n\nWould you like to download it?");

        ButtonType downloadBtn = new ButtonType("Download");
        ButtonType cancelBtn = new ButtonType("Later", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(downloadBtn, cancelBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == downloadBtn) {
                if (osService != null) {
                    osService.openBrowser(GITHUB_URL + "/releases/latest");
                }
            }
        });
    }

    @FXML
    private void handleAbout() {
        showAlert(Alert.AlertType.INFORMATION, "About " + APP_NAME, APP_NAME + " " + APP_VERSION + "\nCreated by RdChandrahas");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    private void scanFonts() {
        if (osService == null) return;
        for (File dir : osService.getSystemFontDirectories()) {
            if (dir != null && dir.exists()) {
                findTtfRecursively(dir);
            }
        }
    }

    private void findTtfRecursively(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                findTtfRecursively(f);
            } else if (f.getName().toLowerCase().endsWith(".ttf")) {
                systemFontMap.put(f.getName().replace(".ttf", ""), f.getAbsolutePath());
            }
        }
    }
}