package com.locker.l0ker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LockerAuthentication extends Application {

    public static void main() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LockerDashboard.class.getResource("auth.fxml"));
        Parent root = fxmlLoader.load();

       // LockerAuthenticationController controller = new LockerAuthenticationController(); // Pass the Stage to the controller
        //fxmlLoader.setController(controller);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Auth");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
