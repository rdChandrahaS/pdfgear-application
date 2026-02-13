package com.rdchandrahas.ui;

import com.rdchandrahas.core.NavigationService;
import com.rdchandrahas.core.Tool;
import com.rdchandrahas.core.ToolRegistry;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

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
        
        // Dynamic scaling: Cards get slightly smaller if window is very narrow
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
        
        FontIcon icon = new FontIcon(tool.getIconCode());
        icon.getStyleClass().add("tool-icon");
        
        Label title = new Label(tool.getName());
        title.getStyleClass().add("tool-title");

        card.getChildren().addAll(icon, title);

        card.setOnMouseClicked(e -> {
            switch (tool.getName()) {
                case "Merge PDF" -> navigationService.navigateToTool(new MergeController());
                case "Split PDF" -> navigationService.navigateToTool(new SplitController());
                case "Compress PDF" -> navigationService.navigateToTool(new CompressController());
                case "Image to PDF" -> navigationService.navigateToTool(new ImageToPdfController());
                case "Protect PDF" -> navigationService.navigateToTool(new ProtectController());
                case "Unlock PDF" -> navigationService.navigateToTool(new UnlockController());
            }
        });
        
        return card;
    }
}