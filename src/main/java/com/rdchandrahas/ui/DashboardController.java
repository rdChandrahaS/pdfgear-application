package com.rdchandrahas.ui;

import com.rdchandrahas.core.NavigationService;
import com.rdchandrahas.core.Tool;
import com.rdchandrahas.core.ToolRegistry;
import com.rdchandrahas.ui.base.BaseToolController;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.InputStream;

public class DashboardController implements InjectableController {

    @FXML private FlowPane toolFlowPane;
    private NavigationService navigationService;

    @Override
    public void setNavigationService(NavigationService navService) {
        this.navigationService = navService;
    }

    @FXML
    public void initialize() {
        renderToolCards();
        
        toolFlowPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            String sizeClass = (newVal.doubleValue() < 600) ? "compact-tool" : "standard-tool";
            toolFlowPane.getChildren().forEach(node -> {
                node.getStyleClass().removeAll("standard-tool", "compact-tool");
                node.getStyleClass().add(sizeClass);
            });
        });
    }

    private void renderToolCards() {
        toolFlowPane.getChildren().clear();
        for (Tool tool : ToolRegistry.getTools()) {
            VBox card = createToolCard(tool);
            toolFlowPane.getChildren().add(card);
        }
    }

    private VBox createToolCard(Tool tool) {
        VBox card = new VBox(15); 
        card.getStyleClass().add("tool-card"); 
        card.setAlignment(Pos.CENTER);
        
        Node iconNode = null;

        // 1. Try loading Image from Path first
        if (tool.getIconPath() != null && !tool.getIconPath().trim().isEmpty()) {
            try {
                InputStream imageStream = getClass().getResourceAsStream(tool.getIconPath());
                if (imageStream != null) {
                    ImageView imageView = new ImageView(new Image(imageStream));
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    // Add a CSS class if you want to style images specifically
                    imageView.getStyleClass().add("tool-image-icon"); 
                    iconNode = imageView;
                }
            } catch (Exception e) {
                System.err.println("Failed to load icon image: " + tool.getIconPath());
            }
        }

        // 2. Fallback to FontIcon if Image failed or wasn't provided
        if (iconNode == null && tool.getIconCode() != null && !tool.getIconCode().trim().isEmpty()) {
            FontIcon fontIcon = new FontIcon(tool.getIconCode());
            // Size is usually controlled in CSS for font icons, but can be set here if needed
            fontIcon.getStyleClass().add("tool-icon");
            iconNode = fontIcon;
        }

        // 3. Absolute Fallback if neither exists
        if (iconNode == null) {
            Label placeholder = new Label("?");
            placeholder.setStyle("-fx-font-size: 40px; -fx-text-fill: #0078d7;");
            iconNode = placeholder;
        }
        
        Label title = new Label(tool.getName());
        title.getStyleClass().add("tool-title");

        card.getChildren().addAll(iconNode, title);

        // Dynamic Routing 
        card.setOnMouseClicked(e -> {
            try {
                Class<?> controllerClass = tool.getControllerClass();
                if (controllerClass != null) {
                    // FIX: Cast directly to BaseToolController
                    BaseToolController controller = (BaseToolController) controllerClass.getDeclaredConstructor().newInstance();
                    navigationService.navigateToTool(controller);
                } else {
                    System.err.println("Controller class not defined for: " + tool.getName());
                }
            } catch (Exception ex) {
                System.err.println("Failed to open tool: " + tool.getName());
                ex.printStackTrace();
            }
        });
        
        String description = tool.getDescription();
        if (description != null && !description.isBlank()) {
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(description);
            tooltip.setStyle("-fx-font-size: 12px;"); // Optional styling
            javafx.scene.control.Tooltip.install(card, tooltip);
        }
        
        return card;
    }
}