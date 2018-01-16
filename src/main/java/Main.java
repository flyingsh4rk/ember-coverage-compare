import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by sic.org on 1/15/2018.
 */
public class Main extends Application {
    private Scene scene;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Ember coverage compare");
        Parameters p = this.getParameters();
        List<String> params = p.getUnnamed();

        scene = new Scene(new Browser(params.get(0), params.get(1)), 900, 600, Color.web("#666970"));
        stage.setScene(scene);
        stage.show();
    }
}

class Browser extends Region {

    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();
    final Button reloadBnt = new Button("Reload");
    final Button syncBnt = new Button("Sync");
    final Button diffBnt = new Button("Diff");
    final Label currentFrom = new Label("-");
    final TextField fromInput = new TextField();
    final Label currentTo = new Label("-");
    final TextField toInput = new TextField();
    private final HBox toolBar;
    private Document cachedDoc;

    {
        reloadBnt.setOnAction((ActionEvent e) -> {
                    String fromUrl = fromInput.getText();
                    this.reload(fromUrl);
                }
        );

    }


    public Browser(String fromUrl, String toUrl) {

        // create the toolbar
        toolBar = new HBox();
        toolBar.setPadding(new Insets(5, 5, 5, 5));
        toolBar.setSpacing(10);
        toolBar.setAlignment(Pos.CENTER);
        toolBar.getChildren().add(reloadBnt);
        toolBar.getChildren().add(syncBnt);
        toolBar.getChildren().add(diffBnt);
        toolBar.getChildren().add(new Label("From"));
        toolBar.getChildren().add(fromInput);
        toolBar.getChildren().add(new Label("To"));
        toolBar.getChildren().add(toInput);
        toolBar.getChildren().add(new Label("From"));
        toolBar.getChildren().add(currentFrom);
        toolBar.getChildren().add(new Label("To"));
        toolBar.getChildren().add(currentTo);
        toolBar.getChildren().add(createSpacer());


        // process page loading
        webEngine.getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends State> ov, State oldState,
                 State newState) -> {
                    if (newState == State.SUCCEEDED) {
                        webEngine.executeScript("var script = document.createElement(\"script\");\n" +
                                "script.src = \"" + Main.class.getClassLoader().getResource("initscript.js") + "\", " +
                                "document.getElementsByTagName(\"head\")[0].appendChild(script);");
                        webEngine.executeScript("function defer(method) {\n" +
                                "    if (window.jQuery && updateRow) {\n" +
                                "        method();\n" +
                                "    } else {\n" +
                                "        setTimeout(function() { defer(method) }, 50);\n" +
                                "    }\n" +
                                "}");
                        webEngine.executeScript("updateRow = undefined;");
                        if (!StringUtils.isEmpty(toInput.getText())) {
                            update(toUrl, true);
                        }

                    }
                });

        // load the home page
        //System.out.println(Paths.get(fromUrl).toUri().toString());
        this.reload(fromUrl);
        fromInput.setText(fromUrl);
        toInput.setText(toUrl);

        //add components
        getChildren().add(toolBar);
        getChildren().add(browser);
    }

    private void update(String url, Boolean isReload) {
        url = getUrl(url);
        if (isReload || cachedDoc == null) {
            try {
                cachedDoc = Jsoup.connect(url).get();
            } catch (IOException e) {
                System.out.println("Can't load url: " + url);
                e.printStackTrace();
            }
        }

        if (cachedDoc == null) {
            return;
        }


        Elements rows = cachedDoc.select("table[class=\"coverage-summary\"] > tbody > tr");
        rows.forEach(row -> {
            String args = row.select("td").stream().map(td -> td.text()).reduce("[", (s1, s2) -> s1 + "'" + s2 + "',");
            final String fnArgs = args.substring(0, args.length() - 1) + "]";
            webEngine.executeScript("defer(function () { updateRow(" + fnArgs + ");})");
        });


    }

    private void reload(String fromUrl) {
        webEngine.load(getUrl(fromUrl));
        currentFrom.setText(getUrl(fromUrl));
    }

    public String getUrl(String source) {
        return ResourceUtils.isUrl(source) ? source : Paths.get(source).toUri().toString();
    }


    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double tbHeight = toolBar.prefHeight(w);
        layoutInArea(browser, 0, 0, w, h - tbHeight, 0, HPos.CENTER, VPos.CENTER);
        layoutInArea(toolBar, 0, h - tbHeight, w, tbHeight, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height) {
        return 900;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 600;
    }
}
