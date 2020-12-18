package com.kmlsiuzdak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/window.fxml"));
        Scene scene = new Scene(root, 800, 500);
        stage.setMaximized(true);
        stage.setTitle("Grain Growth");
        scene.setFill(Color.WHITE);
        stage.setScene(scene);
        stage.show();
    }

}
