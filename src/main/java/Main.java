import com.jfoenix.controls.JFXDecorator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL fxml_file = getClass().getClassLoader().getResource("fxml/interface.fxml");
        Parent root = FXMLLoader.load(fxml_file);
        primaryStage.setTitle("Omega AI (Camiel Kerkhofs i6172266)");

        JFXDecorator decorator = new JFXDecorator(primaryStage, root);
        decorator.setCustomMaximize(true);
        decorator.setMaximized(true);

        Scene scene = new Scene(decorator);

        String css_uri = getClass().getResource("css/styles.css").toExternalForm();
        scene.getStylesheets().add(css_uri);
        primaryStage.setScene(scene);
//        primaryStage.setResizable(false);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
