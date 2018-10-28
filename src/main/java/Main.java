import com.jfoenix.controls.JFXDecorator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL fxml_file = getClass().getClassLoader().getResource("fxml/interface.fxml");
        Parent root = FXMLLoader.load(fxml_file);
        primaryStage.setTitle("Omega AI (Camiel Kerkhofs i6172266)");

        JFXDecorator decorator = new JFXDecorator(primaryStage, root);
        decorator.setCustomMaximize(true);
//        decorator.setMaximized(true);

        Scene scene = new Scene(decorator);

        String css_uri = getClass().getResource("css/styles.css").toExternalForm();
        scene.getStylesheets().add(css_uri);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
//        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Check java version
        if (!System.getProperty("java.version").startsWith("1.8.")) {
            System.out.println("Invalid Java runtime environment version! Java 8 is required to run this application.\nPlease change your java version or run this jar file from the command line (e.g. 'C:\\java\\jdk1.8.0_144\\bin\\java.exe -jar .\\OmegaAI.jar')");
//            System.exit(1);
            JOptionPane.showMessageDialog(null, "Invalid Java version. Java 8 is required to run this application.\nPlease change your java version or run this jar file from the command line (e.g. 'C:\\java\\jdk1.8.0_144\\bin\\java.exe -jar .\\OmegaAI.jar')", "Java Version Error",  JOptionPane.ERROR_MESSAGE);
        }
        launch(args);
    }
}
