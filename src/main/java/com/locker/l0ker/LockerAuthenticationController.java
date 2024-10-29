package com.locker.l0ker;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import com.mongodb.MongoClient;
import javafx.stage.Stage;
import org.bson.Document;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;


public class LockerAuthenticationController {

    @FXML
    private Button btnSignIn;

    @FXML
    private Label headerTxt;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField rePasswordField;

    @FXML
    private Button btnAuthenticate;

    @FXML
    private Label messageLabel;

    //Mongo Strings
    private MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
    private MongoDatabase db = mongoClient.getDatabase("db_locker");
    private MongoCollection<Document> collection= db.getCollection("user_credentials");

    //Encryption and Decryption Key
    private static final String ENCRYPTION_KEY = "ThisIsABit128Key";


    public  void initialize(){
      //  btnSignIn.requestFocus();
        btnAuthenticate.setVisible(false);
        messageLabel.setVisible(false);
          UserCheck();
          limitTextFieldCharacters(txtUsername,10);
          limitTextFieldCharacters(passwordField,10);
          limitTextFieldCharacters(rePasswordField,10);
    }

    public boolean UserCheck(){
        FindIterable<Document> iterDoc = collection.find();
        int i = 1;
        // Getting the iterator
        Iterator it = iterDoc.iterator();
        if (it.hasNext()){
            rePasswordField.setVisible(false);
            headerTxt.setText("Sign in");
            btnSignIn.setVisible(false);
            btnAuthenticate.setVisible(true);
            messageLabel.setVisible(true);
            return true;
        }
        return false;
    }

    public void Authenticate(){
        if (txtUsername.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()){
            showMessage("Username or Password is incorrect or missing","FAILED","error");
            txtUsername.setText("");
            passwordField.setText("");
            txtUsername.requestFocus();
        }else {
            FindIterable<Document> iterDoc = collection.find();
            MongoCursor<Document> cursor = iterDoc.iterator();

            String dbusername = null;
            String dbpassword = null;
            while (cursor.hasNext()) {
                Document document = cursor.next();

                // Access specific keys and values within each document
                dbusername = document.getString("Username"); // Replace "fieldName" with the key you want to access
                dbpassword = document.getString("Password");
                //System.out.println(username);
                //System.out.println(password);
                // Print the entire document and the specific field value
                //System.out.println("Document: " + document.toJson());
                //System.out.println("Specific Field Value: " + fieldValue);
            }

            //System.out.println(dbusername);
            String enteredUsername = encrypt(txtUsername.getText());
            String enteredPassword = encrypt(passwordField.getText());

            if (enteredUsername.equals(dbusername) && enteredPassword.equals(dbpassword)){
                boolean userClickedClose = showMessageWithCondition("You have been granted access into the locker, Welcome.", "Access Granted", "information");

                if (userClickedClose) {
                    // Get the current stage and close it
                    Stage stage = (Stage) btnAuthenticate.getScene().getWindow();
                    stage.close();
                    openLockerDashboardStage();
                }

            }else{
                showMessage("Username or Password is incorrect or missing","FAILED","error");
                txtUsername.setText("");
                passwordField.setText("");
                txtUsername.requestFocus();
            }
        }
    }

    public void openLockerDashboardStage() {
        try {
            LockerDashboard lockerDashboard = new LockerDashboard();
            Stage newStage = new Stage();
            lockerDashboard.start(newStage);
        } catch (Exception e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    public void SignIn(){
        if (txtUsername.getText().trim().isEmpty()){
            showMessage("Username field is mandatory","REQUIRED FIELD","error");
            txtUsername.requestFocus();
        }else if (passwordField.getText().trim().isEmpty()){
            showMessage("Password field is mandatory","REQUIRED FIELD","error");
            passwordField.requestFocus();
        }else if (rePasswordField.getText().trim().isEmpty()){
            showMessage("Re-enter your password to verify","REQUIRED FIELD","error");
            rePasswordField.requestFocus();
        }else if (!passwordField.getText().equals(rePasswordField.getText())){
            showMessage("Passwords don't match, please re-enter the passwords","PASSWORD MISMATCH","error");
            passwordField.setText("");
            rePasswordField.setText("");
            passwordField.requestFocus();
        }else{
            //UserCreation(txtUsername.getText().toString(),rePasswordField.getText());
            String encryptedUsername = encrypt(txtUsername.getText().toString());
            String encryptedPassword = encrypt(rePasswordField.getText().toString());

            UserCreation(encryptedUsername,encryptedPassword);

        }
    }

    public void UserCreation(String username,String password){
        Document user = new Document("User",txtUsername.getText());
        user.append("Username",username);
        user.append("Password",password);
        collection.insertOne(user);
        boolean userClickedClose = showMessageWithCondition("User Created '" + txtUsername.getText() + "'", "Success", "information");

        if (userClickedClose) {
            // Get the current stage and close it
            Stage stage = (Stage) btnSignIn.getScene().getWindow();
            stage.close();
            openAuth();
        }
    }

    public void openAuth() {
        try {
            LockerAuthentication lockerAuthentication = new LockerAuthentication();
            Stage newStage = new Stage();
            lockerAuthentication.start(newStage);
        } catch (Exception e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    public String encrypt(String data) {
        try {
            // Generate a secret key for AES encryption using the specified ENCRYPTION_KEY
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");

            // Initialize the cipher for encryption in AES mode
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Encrypt the input data
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());

            // Encode the encrypted bytes to Base64 and return the encrypted string
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            // Handle encryption exceptions (print stack trace for debugging)
            e.printStackTrace();
        }
        return null; // Return null if encryption fails
    }


    public String decrypt(String encryptedData) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    public boolean showMessageWithCondition(String message, String title, String type) {
        Alert.AlertType alertType = Alert.AlertType.valueOf(type.toUpperCase());
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(closeButton);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == closeButton;
    }

    public void limitTextFieldCharacters(TextField textField, int maxLength) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > maxLength) {
                textField.setText(oldValue);
                showMessage("You have reached the maximum characters that are allowed for '"+textField.getPromptText()+"'","LIMITATIONS","warning");
            }
        });
    }

    public void limitTextFieldCharacters(PasswordField textField, int maxLength) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > maxLength) {
                textField.setText(oldValue);
                showMessage("You have reached the maximum characters that are allowed for '"+textField.getPromptText()+"'","LIMITATIONS","warning");
            }
        });
    }

}
