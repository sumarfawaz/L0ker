package com.locker.l0ker;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.bson.Document;

import org.w3c.dom.Text;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.util.Locale;
import java.util.Optional;

public class LockerDashBoardController {

    @FXML
    private Label profileLetter;

    @FXML
    private Label welcomeLabel;

    @FXML
    private TextField search;

    @FXML
    private Button openFolder;

    @FXML
    private Button encrypt;

    @FXML
    private Button decrypt;


    private static final String SECRET_KEY = "ThisIsA128BitKey"; // Replace this with your secret key




    private MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
    private MongoDatabase db = mongoClient.getDatabase("db_locker");
    private MongoCollection<Document> collection= db.getCollection("user_credentials");

    public void initialize() throws Exception {
        LookForUser();
        profileLetter.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> profileLetter.setCursor(Cursor.HAND));
        //profileLetter.addEventHandler(MouseEvent.MOUSE_EXITED, event -> profileLetter.setCursor(Cursor.DEFAULT));
        welcomeLabel.requestFocus();
        profileLetter.requestFocus();

        search.setFocusTraversable(false);
        search.setDisable(true);

        encrypt.setDisable(true);
        decrypt.setDisable(true);



        //lockFolder("D:\\lockertesting");
       // decryptFolder("D:\\lockertesting");
    }

    public void LookForUser(){
        FindIterable<Document> iterDoc = collection.find();
        MongoCursor<Document> cursor = iterDoc.iterator();

        String user = null;
        while (cursor.hasNext()) {
            Document document = cursor.next();

            user = document.getString("User");

        }

        welcomeLabel.setText("Welcome ".toUpperCase(Locale.ROOT)+user.toUpperCase(Locale.ROOT));
        String userFirstLetter = user.substring(0,1).toUpperCase(Locale.ROOT);
        profileLetter.setText(userFirstLetter);
        //System.out.println(userFirstLetter);
    }

    public void Logout(){
       showConfirmationDialog("Confirmation","Are you sure?","Would you like to logout?",openAuthentication());
    }

    public boolean showConfirmationDialog(String title, String headerText, String contentText, Runnable yesAction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No");

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            // User clicked 'Yes', perform the action
            if (yesAction != null) {
                yesAction.run();
            }
            return true;
        } else {
            // User clicked 'No' or closed the dialog
            return false;
        }
    }

    public Runnable openAuthentication() {
        return () -> {
            Stage stage = (Stage) profileLetter.getScene().getWindow();
            stage.close();
            try {
                LockerAuthentication lockerAuthentication = new LockerAuthentication();
                Stage newStage = new Stage();
                lockerAuthentication.start(newStage);
            } catch (Exception e) {
                e.printStackTrace(); // Handle the exception appropriately
            }
        };
    }


    public void showMessage(String message, String title, String type) {
        Platform.runLater(() -> {
            Alert.AlertType alertType = Alert.AlertType.valueOf(type.toUpperCase());
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }


    public void openFolder(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Encrypt/Decrypt");

        // Get the disk letter from lblFileStore




        // Set the initial directory (optional)
        directoryChooser.setInitialDirectory(new File("D:/"));

        // Show folder chooser dialog
        File selectedFolder = directoryChooser.showDialog(new Stage());

        if (selectedFolder != null) {
            search.setText(selectedFolder.getAbsolutePath().toString());
            encrypt.setDisable(false);
            decrypt.setDisable(false);
        } else {
            //System.out.println("No source folder selected");

        }
    }

    public void lockFolder() throws Exception {
        // Get the folder path from the search text input
        String folderPath = search.getText();

        // Create a File object representing the folder
        File folder = new File(folderPath);

        // Check if the folder exists and is a directory
        if (folder.exists() && folder.isDirectory()) {

            // Check if the folder is already encrypted
            boolean isAlreadyEncrypted = false;
            for (File file : folder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".encrypted")) {
                    // If an encrypted file is found, set the flag to true and break the loop
                    isAlreadyEncrypted = true;
                    break;
                }
            }

            // If the folder is already encrypted, display a message and skip encryption
            if (isAlreadyEncrypted) {
                showMessage("'" + folderPath + "' is already encrypted, skipping encryption", "FOLDER ENCRYPTED ALREADY", "error");
            } else {
                // Generate a secret key for AES encryption
                Key secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

                // Initialize the cipher for encryption in AES mode
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);

                // Encrypt and write each file in the folder
                for (File file : folder.listFiles()) {
                    if (file.isFile()) {
                        try (FileInputStream fis = new FileInputStream(file);
                             FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + ".encrypted");
                             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

                            // Read data from the original file and encrypt it, then write to the encrypted file
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                cos.write(buffer, 0, bytesRead);
                            }

                            // Display a message indicating successful encryption
                            showMessage("'" + folderPath + "' was successfully encrypted and protected", "FOLDER ENCRYPTED", "information");
                        }

                        // Delete the original unencrypted file
                        file.delete();
                    }
                }
            }
        }
    }


    public void decryptFolder() throws Exception {
        // Get the folder path from the search text input
        String folderPath = search.getText();

        // Create a File object representing the folder
        File folder = new File(folderPath);

        // Check if the folder exists and is a directory
        if (folder.exists() && folder.isDirectory()) {
            // Generate a secret key for AES decryption
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

            // Initialize the cipher for decryption in AES mode
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Decrypt and write each encrypted file in the folder
            for (File file : folder.listFiles()) {
                // Check if the file is an encrypted file (ends with ".encrypted" extension)
                if (file.isFile() && file.getName().endsWith(".encrypted")) {
                    try (FileInputStream fis = new FileInputStream(file);
                         CipherInputStream cis = new CipherInputStream(fis, cipher);
                         FileOutputStream fos = new FileOutputStream(file.getAbsolutePath().replace(".encrypted", ""))) {

                        // Read data from the encrypted file, decrypt it, and write to the original file
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = cis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }

                        // Display a message indicating successful decryption
                        showMessage("'" + folderPath + "' was successfully decrypted and is under your supervision", "FOLDER DECRYPTED", "information");
                    }

                    // Delete the encrypted file after decryption
                    file.delete();
                }
            }
        }
    }

}
