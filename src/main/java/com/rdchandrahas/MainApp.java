package com.rdchandrahas;

import com.rdchandrahas.core.DefaultOSService;
import com.rdchandrahas.core.OSService;
import com.rdchandrahas.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Scene scene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Load the FXML
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ui/MainLayout.fxml")
        );
        Parent root = loader.load();

        // 2. Initialize the OS Abstraction Service
        // This takes the JavaFX HostServices to handle browser opening properly on Linux/Windows
        OSService osService = new DefaultOSService(getHostServices());

        // 3. Inject the Service into the MainController
        // This triggers the font scanning and UI setup
        MainController controller = loader.getController();
        controller.initService(osService);

        // 4. Setup the Scene and Stage
        scene = new Scene(root, 1000, 700);

        primaryStage.setTitle("My PDF File Desktop");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    /**
     * Globally accessible scene reference for simple utility calls
     */
    public static Scene getScene() {
        return scene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}