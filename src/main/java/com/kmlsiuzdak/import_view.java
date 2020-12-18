package com.kmlsiuzdak;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class import_view extends Application {

    private final File file;
    private int[][] matrix;
    private int cells;

    public import_view(File file) {
        this.file = file;
    }

    @Override
    public void start(Stage stage) throws error {
        try {
            readMatrix(file);
            Scene scene = new Scene(getScrollPane(), 500, 500, Color.WHITE);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            throw new error(e.getMessage());
        }
    }

    private ScrollPane getScrollPane() {
        ScrollPane scrollPane = new ScrollPane(getStackPane());
        scrollPane.setPannable(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private StackPane getStackPane() {
        Group group = getGroup(getCanvas());
        StackPane stackPane = new StackPane(group);
        setScrollEvent(stackPane, group);
        return stackPane;
    }

    private Group getGroup(Canvas canvas) {
        return new Group(canvas);
    }

    private Canvas getCanvas() {
        Canvas canvas = new Canvas(matrix.length, matrix[0].length);
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        PixelWriter pixelWriter = graphicsContext.getPixelWriter();
        fillCanvas(pixelWriter);
        return canvas;
    }

    private void fillCanvas(PixelWriter pixelWriter) {
        Map<Integer, Color> colorMap = getColors(cells);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                pixelWriter.setColor(i, j, colorMap.get(matrix[i][j]));
            }
        }
        matrix = null;
    }

    private void readMatrix(File file) {
        try {
            Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)));
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().trim().split(" ");
                if (matrix == null) {
                    matrix = new int[Integer.parseInt(line[0])][Integer.parseInt(line[1])];
                    cells = Integer.parseInt(line[2]);
                } else {
                    matrix[Integer.parseInt(line[0])][Integer.parseInt(line[1])] = Integer.parseInt(line[2]);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
