module com.locker.l0ker {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires mongo.java.driver;


    opens com.locker.l0ker to javafx.fxml;
    exports com.locker.l0ker;

}