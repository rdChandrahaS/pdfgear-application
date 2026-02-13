package com.rdchandrahas.core;

import com.rdchandrahas.ui.InjectableController;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class NavigationService {
    private final StackPane contentPane;

    public NavigationService(StackPane contentPane) {
        this.contentPane = contentPane;
    }

    public void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            inject(loader.getController());
            contentPane.getChildren().setAll(view);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    /** Master Layout Tool Navigation - Dynamically injects controllers into the shared shell */
    public void navigateToTool(BaseToolController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/ToolLayout.fxml"));
            loader.setController(controller); // KEY: Marries tool logic to the shared UI
            Parent view = loader.load();
            inject(controller);
            contentPane.getChildren().setAll(view);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    private void inject(Object controller) {
        if (controller instanceof InjectableController injectable) {
            injectable.setNavigationService(this);
        }
    }
}