package com.rdchandrahas.ui;

import com.rdchandrahas.core.NavigationService;
import com.rdchandrahas.core.Tool;
import com.rdchandrahas.core.ToolRegistry;
import com.rdchandrahas.ui.base.BaseToolController;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.InputStream;

/**
 * DashboardController manages the main entry point of the application.
 * It dynamically populates the UI with tool cards based on the ToolRegistry,
 * handles responsive layout adjustments, and manages navigation to specific PDF tools.
 */
public class DashboardController implements InjectableController {

    @FXML private FlowPane toolFlowPane;
    private NavigationService navigationService;

    @Override
    public void setNavigationService(NavigationService navService) {
        this.navigationService = navService;
    }

    /**
     * Initializes the dashboard by rendering cards and setting up a 
     * responsiveness listener to switch between standard and compact styles.
     */
    @FXML
    public void initialize() {
        renderToolCards();
        
        // Responsive Layout: Adjust card size based on window width
        toolFlowPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            String sizeClass = (newVal.doubleValue() < 600) ? "compact-tool" : "standard-tool";
            toolFlowPane.getChildren().forEach(node -> {
                node.getStyleClass().removeAll("standard-tool", "compact-tool");
                node.getStyleClass().add(sizeClass);
            });
        });
    }

    /**
     * Iterates through all registered tools and adds their visual cards to the FlowPane.
     */
    private void renderToolCards() {
        toolFlowPane.getChildren().clear();
        for (Tool tool : ToolRegistry.getTools()) {
            VBox card = createToolCard(tool);
            toolFlowPane.getChildren().add(card);
        }
    }

    /**
     * Creates a visual card for a specific tool.
     * Logic includes icon resolution (Image -> FontIcon -> Placeholder), 
     * styling, tooltips, and click-to-navigate events.
     */
    private VBox createToolCard(Tool tool) {
        VBox card = new VBox(15); 
        card.getStyleClass().add("tool-card"); 
        card.setAlignment(Pos.CENTER);
        
        Node iconNode = null;

        // 1. Primary: Try loading Image from Path
        if (tool.getIconPath() != null && !tool.getIconPath().trim().isEmpty()) {
            try {
                InputStream imageStream = getClass().getResourceAsStream(tool.getIconPath());
                if (imageStream != null) {
                    ImageView imageView = new ImageView(new Image(imageStream));
                    
                    // 1. MAKE IMAGE LARGER (Covers more button space)
                    imageView.setFitWidth(100);
                    imageView.setFitHeight(100);
                    
                    // 2. OVAL EDGES (Clips the square white corners into an oval)
                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(100, 100);
                    clip.setArcWidth(45); // Adjust for roundness
                    clip.setArcHeight(45);
                    imageView.setClip(clip);

                    imageView.getStyleClass().add("tool-image-icon"); 
                    iconNode = imageView;
                }
            } catch (Exception e) {
                System.err.println("Failed to load icon image: " + tool.getIconPath());
            }
        }

        // 2. Secondary: Fallback to FontIcon (Ikonli)
        if (iconNode == null && tool.getIconCode() != null && !tool.getIconCode().trim().isEmpty()) {
            FontIcon fontIcon = new FontIcon(tool.getIconCode());
            fontIcon.getStyleClass().add("tool-icon");
            iconNode = fontIcon;
        }

        // 3. Absolute Fallback: Placeholder label
        if (iconNode == null) {
            Label placeholder = new Label("?");
            placeholder.setStyle("-fx-font-size: 40px; -fx-text-fill: #0078d7;");
            iconNode = placeholder;
        }
        
        Label title = new Label(tool.getName());
        title.getStyleClass().add("tool-title");

        card.getChildren().addAll(iconNode, title);

        // --- Interaction Logic ---
        card.setOnMouseClicked(e -> {
            try {
                Class<?> controllerClass = tool.getControllerClass();
                if (controllerClass != null) {
                    // Instantiate the specific tool controller and navigate
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
        
        // Add Tooltip for accessibility and clarity
        String description = tool.getDescription();
        if (description != null && !description.isBlank()) {
            Tooltip tooltip = new Tooltip(description);
            tooltip.setStyle("-fx-font-size: 12px;");
            Tooltip.install(card, tooltip);
        }
        
        return card;
    }
}