package com.rdchandrahas;

import com.rdchandrahas.core.DefaultOSService;
import com.rdchandrahas.core.ExecutionManager;
import com.rdchandrahas.core.OSService;
import com.rdchandrahas.ui.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The MainApp class serves as the core JavaFX Application lifecycle manager.
 * It handles the initialization of the primary UI window, dependency injection 
 * for system services, and global scene management.
 */
public class MainApp extends Application {

    private static Scene scene;

    /**
     * Static setter to resolve SonarQube issue (squid:S2696)
     * "Make the enclosing method 'static' or remove this set."
     */
    private static void setGlobalScene(Scene newScene) {
        scene = newScene;
    }

    /**
     * The main entry point for all JavaFX applications.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Load the primary FXML layout containing the root interface
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ui/MainLayout.fxml")
        );
        Parent root = loader.load();

        // 2. Initialize the OS Abstraction Service
        OSService osService = new DefaultOSService(getHostServices());

        // 3. Inject the Service into the MainController
        MainController controller = loader.getController();
        controller.initService(osService);

        // 4. Setup the Scene and Stage dimensions using the static setter
        setGlobalScene(new Scene(root, 1000, 700));

        primaryStage.setTitle("My PDF File Desktop");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        // 5. Explicitly handle the "X" close button
        primaryStage.setOnCloseRequest(event -> shutdownApplication());
        
        primaryStage.show();
    }

    /**
     * Called automatically by JavaFX when the application is stopping.
     */
    @Override
    public void stop() throws Exception {
        shutdownApplication();
    }

    /**
     * Forces the JVM to terminate. This instantly kills any lingering background 
     * threads (like massive thumbnail generation loops) so the terminal doesn't hang.
     */
    private void shutdownApplication() {
        ExecutionManager.shutdown(); // Gracefully stops the thread pool
        Platform.exit();             // Shuts down the JavaFX thread
        System.exit(0);              // Instantly kills the JVM process
    }

    /**
     * Globally accessible scene reference.
     */
    public static Scene getScene() {
        return scene;
    }

    /**
     * Fallback main method.
     */
    public static void main(String[] args) {
        launch(args);
    }
}