package com.kmlsiuzdak;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class grains_view extends Application {

    private final int xSize;
    private final int ySize;
    private final int cells;
    private final Border_transition selection;
    private final int inclusions;
    private final int inclusionLength;
    private final Inclusion_border inclusionBorder;
    private final Inclusion_shape inclusionShape;
    private final int[][] matrix;
    private PixelWriter pixelWriter;
    private final Map<Integer, Color> colorMap;

    public grains_view(int xSize, int ySize, int cells, Border_transition selection, int inclusions, int inclusionLength,
                       Inclusion_border inclusionBorder, Inclusion_shape inclusionShape) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.cells = cells;
        this.selection = selection;
        this.inclusions = inclusions;
        this.inclusionLength = inclusionLength;
        this.inclusionBorder = inclusionBorder;
        this.inclusionShape = inclusionShape;
        matrix = generateMatrix(xSize, ySize);
        colorMap = getColors(cells);
    }


    @Override
    public void start(Stage stage) throws error {
        try {
            Scene scene = new Scene(get_border_pane(stage), xSize, ySize, Color.WHITE);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
            generate_service generateService = new generate_service(xSize, ySize, cells, selection, inclusions, inclusionLength,
                    inclusionBorder, inclusionShape, matrix, colorMap, pixelWriter);
            generateService.generate();
        } catch (Exception e) {
            throw new error(e.getMessage());
        }
    }

    private BorderPane get_border_pane(Stage stage) {
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(getMenu(stage));
        borderPane.setCenter(getScrollPane());
        return borderPane;
    }

    private MenuBar getMenu(Stage stage) {
        Menu menu = new Menu("Export to file");
        MenuItem menuItem = new MenuItem("Export");
        setExportEvent(menuItem, stage);
        menu.getItems().add(menuItem);
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu);
        return menuBar;
    }

    private void setExportEvent(MenuItem menuItem, Stage stage) {
        menuItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TEXT Files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extensionFilter);
            fileChooser.setInitialFileName("generation");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                writeMatrix(file.getAbsolutePath(), matrix, cells);
            }
        });
    }

    private ScrollPane getScrollPane() {
        ScrollPane scrollPane = new ScrollPane(getStackPane());
        scrollPane.setPannable(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private StackPane getStackPane() {
        Group group = getGroup();
        StackPane stackPane = new StackPane(group);
        setScrollEvent(stackPane, group);
        stackPane.setAlignment(Pos.CENTER);
        return stackPane;
    }

    private Canvas getCanvas() {
        Canvas canvas = new Canvas(xSize, ySize);
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        pixelWriter = graphicsContext.getPixelWriter();
        return canvas;
    }

    private Group getGroup() {
        return new Group(getCanvas());
    }

    private Map<Integer, Color> getColors(int cells) {
        Map<Integer, Color> colorMap = new HashMap<>();
        colorMap.put(-1, Color.BLACK);
        for (int i = 1; i < cells + 1; i++) {
            colorMap.put(i, Color.rgb(getRandomInt(), getRandomInt(), getRandomInt()));
        }
        return colorMap;
    }

    private int getRandomInt() {
        Random random = new Random();
        return random.nextInt(255);
    }

    private int[][] generateMatrix(int xSize, int ySize) {
        int[][] matrix = new int[xSize][ySize];
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                matrix[i][j] = 0;
            }
        }
        return matrix;
    }

    private void writeMatrix(String path, int[][] matrix, int cells) {
        File file = new File(path);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(getMatrixString(matrix, cells).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private StringBuilder getMatrixString(int[][] matrix, int cells) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(matrix.length).append(" ").append(matrix[0].length).append(" ").append(cells);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                stringBuilder.append("\n").append(i).append(" ").append(j).append(" ").append(matrix[i][j]);
            }
        }
        return stringBuilder;
    }

    private void setScrollEvent(StackPane stackPane, Group group) {
        final double scaleDelta = 1.1;
        stackPane.setOnScroll(event -> {
            event.consume();
            if (event.getDeltaY() == 0) {
                return;
            }
            double scaleFactor = (event.getDeltaY() > 0) ? scaleDelta : 1 / scaleDelta;
            group.setScaleX(group.getScaleX() * scaleFactor);
            group.setScaleY(group.getScaleY() * scaleFactor);
        });
    }

}
