package com.locker.l0ker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LockerDashboard extends Application {

    public static void main() {
        launch();
    }

    @Override
    public  void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LockerDashboard.class.getResource("lockerdashboard.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Locker");
        primaryStage.setScene(scene);

        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
