package com.kmlsiuzdak;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class grains_view extends Application {

    private final int xSize;
    private final int ySize;
    private int cells;
    private final Border_transition selection;
    private final int inclusions;
    private final int inclusionLength;
    private final Inclusion_border inclusionBorder;
    private final Inclusion_shape inclusionShape;
    private final int probability;
    private final boolean isShapeControl;
    private final int[][] matrix;
    private PixelWriter pixelWriter;
    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private Map<Integer, Color> colorMap;
    private final List<data_value> borderElements = new LinkedList<>();
    private final List<String> selectedGrainsForStructure = new LinkedList<>();
    private final MenuBar menuBar = new MenuBar();
    private Stage stage;

    public grains_view(int xSize, int ySize, int cells, Border_transition selection, int inclusions, int inclusionLength,
                       int probability, boolean isShapeControl, Inclusion_border inclusionBorder, Inclusion_shape inclusionShape) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.cells = cells;
        this.selection = selection;
        this.inclusions = inclusions;
        this.inclusionLength = inclusionLength;
        this.inclusionBorder = inclusionBorder;
        this.inclusionShape = inclusionShape;
        this.probability = probability;
        this.isShapeControl = isShapeControl;
        matrix = generateMatrix(xSize, ySize);
        colorMap = getColors(cells);
    }


    @Override
    public void start(Stage stage) throws error {
        try {
            this.stage = stage;
            Scene scene = new Scene(get_border_pane(stage), xSize, ySize, Color.WHITE);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
            generate_service generateService = new generate_service(xSize, ySize, cells, selection, inclusions, inclusionLength,
                    probability, inclusionBorder, inclusionShape, matrix, colorMap, pixelWriter);
            generateService.generate(isShapeControl);
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
        setFileMenu(stage);
        setStructureMenu(colorMap);
        setGrainsMenu(colorMap);
        setClearMenu();
        return menuBar;
    }

    private void setFileMenu(Stage stage) {
        Menu file = new Menu("File");
        setExportToFileMenu(file, stage);
        setExportToImageMenu(file, stage);
        menuBar.getMenus().add(0, file);
    }

    private void setExportToFileMenu(Menu menu, Stage stage) {
        MenuItem menuItem = new MenuItem("Export to File");
        setExportToFileEvent(menuItem, stage);
        menu.getItems().add(menuItem);
    }

    private void setExportToImageMenu(Menu menu, Stage stage) {
        MenuItem menuItem = new MenuItem("Export to Image");
        setExportToImageEvent(menuItem, stage);
        menu.getItems().add(menuItem);
    }

    private void setExportToFileEvent(MenuItem menuItem, Stage stage) {
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

    private void setExportToImageEvent(MenuItem menuItem, Stage stage) {
        menuItem.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("BMP", "*.bmp"));
            fc.setInitialFileName("image_of_generation");
            File file = fc.showSaveDialog(stage);
            if (file != null) {
                WritableImage wi = new WritableImage(xSize, ySize);
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(canvas.snapshot(null, wi), null), "png", file);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    private void setClearMenu() {
        Menu clear = new Menu("Clean");
        setClearMenuItems(clear);
        menuBar.getMenus().add(clear);
    }

    private void setClearMenuItems(Menu menu) {
        MenuItem menuItem = new MenuItem("Clean content");
        menuItem.setOnAction(actionEvent -> {
            for (int i = 0; i < xSize; i++) {
                for (int j = 0; j < ySize; j++) {
                    if (matrix[i][j] != -2) {
                        matrix[i][j] = 0;
                        pixelWriter.setColor(i, j, Color.WHITE);
                    }
                }
            }
        });
        menu.getItems().add(menuItem);
    }

    private void setGrainsMenu(Map<Integer, Color> colorMap) {
        Menu grains = new Menu("Grains");
        setGrainsMenuItems(grains, colorMap);
        menuBar.getMenus().add(2, grains);
    }

    private void setGrainsMenuItems(Menu grains, Map<Integer, Color> colorMap) {
        CheckMenuItem selectAll = new CheckMenuItem("Select all");
        selectAll.setOnAction(getSelectAllEvent());
        grains.getItems().add(selectAll);
        for (Map.Entry<Integer, Color> entry : colorMap.entrySet()) {
            if (entry.getKey() != -1) {
                CheckMenuItem menuItem = new CheckMenuItem(toRGBCode(entry.getValue()));
                menuItem.setId(entry.getKey().toString());
                menuItem.setGraphic(new Circle(20, entry.getValue()));
                menuItem.setOnAction(getGrainSelectionEvent());
                grains.getItems().add(menuItem);
            }
        }
    }

    private EventHandler<ActionEvent> getGrainSelectionEvent() {
        return actionEvent -> {
            CheckMenuItem grain = ((CheckMenuItem) actionEvent.getSource());
            if (grain.isSelected()) {
                getBordersForElements(Integer.parseInt(grain.getId()));
                for (data_value data_value : borderElements) {
                    pixelWriter.setColor(data_value.getX(), data_value.getY(), Color.BLACK);
                    matrix[data_value.getX()][data_value.getY()] = -2;
                }
            } else {
                for (data_value data_value : borderElements) {
                    if (data_value.getValue() == Integer.parseInt(grain.getId())) {
                        pixelWriter.setColor(data_value.getX(), data_value.getY(), colorMap.get(data_value.getValue()));
                        matrix[data_value.getX()][data_value.getY()] = data_value.getValue();
                    }
                }
                borderElements.removeIf(indexElement -> indexElement.getValue() == Integer.parseInt(grain.getId()));
            }
        };
    }

    private EventHandler<ActionEvent> getSelectAllEvent() {
        return actionEvent -> {
            CheckMenuItem selectAll = ((CheckMenuItem) actionEvent.getSource());
            if (selectAll.isSelected()) {
                getBorderElements();
                for (data_value data_value : borderElements) {
                    pixelWriter.setColor(data_value.getX(), data_value.getY(), Color.BLACK);
                    matrix[data_value.getX()][data_value.getY()] = -2;
                }
            } else {
                for (data_value data_value : borderElements) {
                    pixelWriter.setColor(data_value.getX(), data_value.getY(), colorMap.get(data_value.getValue()));
                    matrix[data_value.getX()][data_value.getY()] = data_value.getValue();
                }
                borderElements.clear();
            }
        };
    }

    private void getBorderElements() {
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                if (i + 1 != xSize && matrix[i][j] != matrix[i + 1][j]) {
                    borderElements.add(new data_value(i, j, matrix[i][j]));
                }
                if (i - 1 >= 0 && matrix[i][j] != matrix[i - 1][j]) {
                    borderElements.add(new data_value(i, j, matrix[i][j]));
                }
                if (j + 1 != ySize && matrix[i][j] != matrix[i][j + 1]) {
                    borderElements.add(new data_value(i, j, matrix[i][j]));
                }
                if (j - 1 >= 0 && matrix[i][j] != matrix[i][j - 1]) {
                    borderElements.add(new data_value(i, j, matrix[i][j]));
                }
            }
        }
    }

    private void getBordersForElements(int elementId) {
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                if (matrix[i][j] == elementId) {
                    if (i + 1 != xSize && matrix[i][j] != matrix[i + 1][j]) {
                        borderElements.add(new data_value(i, j, matrix[i][j]));
                    }
                    if (i - 1 >= 0 && matrix[i][j] != matrix[i - 1][j]) {
                        borderElements.add(new data_value(i, j, matrix[i][j]));
                    }
                    if (j + 1 != ySize && matrix[i][j] != matrix[i][j + 1]) {
                        borderElements.add(new data_value(i, j, matrix[i][j]));
                    }
                    if (j - 1 >= 0 && matrix[i][j] != matrix[i][j - 1]) {
                        borderElements.add(new data_value(i, j, matrix[i][j]));
                    }
                }
            }
        }
    }

    private void getBordersForSubStructureElement(int elementId) {
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                if (matrix[i][j] == elementId) {
                    if (i + 1 != xSize && matrix[i][j] != matrix[i + 1][j]) {
                        borderElements.add(new data_value(i, j, matrix[i][j]));
                    }
                    if (i - 1 >= 0 && matrix[i][j] != matrix[i - 1][j]) {
                        borderElements.add(new data_value(i, j, matrix[i][j]));
                    }
                    if (j + 1 != ySize && matrix[i][j] != matrix[i][j + 1]) {
                        borderElements.add(new data_value(i, j, matrix[i][j]));
                    }
                    if (j - 1 >= 0 && matrix[i][j] != matrix[i][j - 1]) {
                        borderElements.add(new data_value(i, j, matrix[i][j]));
                    }
                    if (j + 1 != ySize && i + 1 != xSize && matrix[i][j] != matrix[i + 1][j + 1]) {
                        borderElements.add(new data_value(i, j, matrix[i + 1][j + 1]));
                    }
                    if (i - 1 != -1 && j + 1 != ySize && matrix[i][j] != matrix[i - 1][j + 1]) {
                        borderElements.add(new data_value(i, j, matrix[i - 1][j + 1]));
                    }
                    if (i - 1 != -1 && j - 1 != -1 && matrix[i][j] != matrix[i - 1][j - 1]) {
                        borderElements.add(new data_value(i, j, matrix[i - 1][i - 1]));
                    }
                    if (i + 1 != xSize && j - 1 != -1 && matrix[i][j] != matrix[i + 1][j - 1]) {
                        borderElements.add(new data_value(i, j, matrix[i + 1][j - 1]));
                    }
                }
            }
        }
    }

    private void setStructureMenu(Map<Integer, Color> colorMap) {
        Menu structure = new Menu("Structure");
        setStructureMenuItems(structure, colorMap);
        menuBar.getMenus().add(1, structure);
    }

    private void setStructureMenuItems(Menu menu, Map<Integer, Color> colorMap) {
        MenuItem menuItem = new MenuItem("Choose structure");
        setSubStage(menuItem);
        menu.getItems().add(menuItem);
        for (Map.Entry<Integer, Color> entry : colorMap.entrySet()) {
            if (entry.getKey() != -1) {
                CheckMenuItem item = new CheckMenuItem(toRGBCode(entry.getValue()));
                item.setId(entry.getKey().toString());
                item.setGraphic(new Circle(20, entry.getValue()));
                item.setOnAction(getStructureGrainSelectionEvent());
                menu.getItems().add(item);
            }
        }
    }

    private EventHandler<ActionEvent> getStructureGrainSelectionEvent() {
        return actionEvent -> {
            CheckMenuItem grain = ((CheckMenuItem) actionEvent.getSource());
            if (grain.isSelected()) {
                selectedGrainsForStructure.add(grain.getId());
            } else {
                selectedGrainsForStructure.remove(grain.getId());
            }
        };
    }

    private void setSubStage(MenuItem menuItem) {
        menuItem.setOnAction(e -> {
            Stage subStage = new Stage();
            subStage.setTitle("Choose structure");
            subStage.setWidth(350);
            subStage.setHeight(250);
            subStage.initOwner(stage);
            subStage.initModality(Modality.WINDOW_MODAL);
            subStage.setScene(getSubScene(subStage));
            subStage.show();
        });
    }

    private Scene getSubScene(Stage stage) {
        GridPane gridPane = new GridPane();
        Label structureLabel = new Label("Choose structure:");
        gridPane.add(structureLabel, 0, 1);
        ChoiceBox<Structure> structureField = new ChoiceBox<>();
        setOptions(structureField);
        gridPane.add(structureField, 1, 1);
        Label cellsLabel = new Label("Number of cells:");
        gridPane.add(cellsLabel, 0, 2);
        TextField cellsField = new TextField();
        gridPane.add(cellsField, 1, 2);
        Button button = new Button("Generate");
        button.setAlignment(Pos.CENTER);
        button.alignmentProperty().setValue(Pos.CENTER);
        gridPane.add(button, 0, 4);
        button.setOnAction(actionEvent -> {
            int cells = Integer.parseInt(cellsField.getText());
            this.cells = cells + selectedGrainsForStructure.size();
            Structure structure = structureField.getValue();
            Map<Integer, Color> map = getStructureColors(cells, selectedGrainsForStructure, colorMap);
            this.colorMap = map;
            generate_service generateService = new generate_service(xSize, ySize, cells, selection, inclusions, inclusionLength,
                    probability, inclusionBorder, inclusionShape, matrix, colorMap, pixelWriter);
            stage.close();
            prepareMatrixToStructureGrow(structure);
            generateService.setCellsWithIgnoredGrains(selectedGrainsForStructure);
            generateService.growStructureGrains(selectedGrainsForStructure, structure);
            selectedGrainsForStructure.clear();
            menuBar.getMenus().remove(1);
            menuBar.getMenus().remove(1);
            setStructureMenu(map);
            setGrainsMenu(map);
            selectedGrainsForStructure.clear();
        });
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setAlignment(Pos.CENTER);
        Scene scene = new Scene(gridPane, 350, 250);
        scene.getStylesheets().add(this.getClass().getResource("/style.css").toExternalForm());
        return scene;
    }

    private void prepareMatrixToStructureGrow(Structure structure) {
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        switch (structure) {
            case DUAL_PHASE:
                prepareMatrixToDualPhaseGrow();
                break;
            case SUBSTRUCTURE:
                prepareMatrixToSubstructureGrow();
                break;
        }
    }

    private void prepareMatrixToSubstructureGrow() {
        for (String key : selectedGrainsForStructure) {
            getBordersForSubStructureElement(Integer.parseInt(key));
            for (data_value data_value : borderElements) {
                pixelWriter.setColor(data_value.getX(), data_value.getY(), Color.BLACK);
                matrix[data_value.getX()][data_value.getY()] = -2;
            }
        }
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] != -2) {
                    matrix[i][j] = 0;
                }
            }
        }
        selectedGrainsForStructure.clear();
        selectedGrainsForStructure.add("-2");
        borderElements.clear();
    }

    private void prepareMatrixToDualPhaseGrow() {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (selectedGrainsForStructure.contains(String.valueOf(matrix[i][j]))) {
                    int key = Integer.parseInt(selectedGrainsForStructure.get(0));
                    matrix[i][j] = key;
                    pixelWriter.setColor(i, j, colorMap.get(key));
                } else {
                    matrix[i][j] = 0;
                }
            }
        }
        String key = selectedGrainsForStructure.get(0);
        selectedGrainsForStructure.clear();
        selectedGrainsForStructure.add(key);
    }

    private void setOptions(ChoiceBox<Structure> choiceBox) {
        for (Structure structure : Structure.values()) {
            choiceBox.getItems().add(structure);
        }
        choiceBox.setValue(Structure.SUBSTRUCTURE);
    }


    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public Map<Integer, Color> getStructureColors(int cells, List<String> selectedGrainsForStructure,
                                                         Map<Integer, Color> previousMap) {
        Map<Integer, Color> colorMap = new HashMap<>();
        colorMap.put(-1, Color.BLACK);
        for (String key : selectedGrainsForStructure) {
            int mapKey = Integer.parseInt(key);
            colorMap.put(mapKey, previousMap.get(mapKey));
        }
        int var = 1;
        for (int i = 1; i < cells + 1; i++) {
            if (colorMap.containsKey(i)) {
                colorMap.put(cells + var, Color.rgb(getRandomInt(), getRandomInt(), getRandomInt()));
                var++;
            } else {
                colorMap.put(i, Color.rgb(getRandomInt(), getRandomInt(), getRandomInt()));
            }
        }
        return colorMap;
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
        canvas = new Canvas(xSize, ySize);
        graphicsContext = canvas.getGraphicsContext2D();
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
