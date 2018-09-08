import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;
import de.jensd.fx.fontawesome.AwesomeStyle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL fxml_file = getClass().getClassLoader().getResource("fxml/interface.fxml");
        Parent root = FXMLLoader.load(fxml_file);
        primaryStage.setTitle("Omega AI");

        JFXDecorator decorator = new JFXDecorator(primaryStage, root);
        decorator.setCustomMaximize(true);

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
