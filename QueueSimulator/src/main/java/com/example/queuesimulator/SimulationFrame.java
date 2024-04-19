package com.example.queuesimulator;

import business_logic.SimulationManager;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class SimulationFrame {
    private int queueNr;
    private Group root;
    private Stage simulationStage;
    private Scene scene;
    public final int WIDTH = 800;
    public final int HEIGHT = 800;
    private Label myLabel;
    private int threadTime = 0;
    private SimulationManager simulationManager;

    ArrayList<HBox> elem;

    public SimulationFrame(int queueNr,SimulationManager simulationManager) {
        this.queueNr = queueNr;
        root = new Group();
        this.simulationStage = new Stage();
        elem = new ArrayList<HBox>();
        this.simulationManager=simulationManager;
    }

    public void setTime(String message) {
        this.myLabel.setText(message);
    }

    public void init() {
        this.scene = new Scene(root, WIDTH, HEIGHT);
        this.scene.setFill(Color.CYAN);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefHeight(HEIGHT);
        scrollPane.setPrefWidth(WIDTH);
        scrollPane.setFitToWidth(true);
        VBox elements = new VBox();
        scrollPane.setContent(elements);
        Label timeLabel = new Label("Time 0:");
        timeLabel.setPrefWidth(WIDTH);
        timeLabel.setPrefHeight(40);
        timeLabel.setAlignment(Pos.CENTER);
        this.myLabel = timeLabel;
        elements.getChildren().add(timeLabel);
        elements.setPrefHeight(Double.MAX_VALUE);
        root.getChildren().add(scrollPane);
        for (int i = 1; i <= queueNr; i++) {
            HBox horizontal = new HBox();
            horizontal.setPrefWidth(Double.MAX_VALUE);
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("EndOfLine.png")));
            ImageView cashier = new ImageView(image);
            Label endLabel = new Label("Queue" + Integer.toString(i));
            endLabel.setAlignment(Pos.CENTER_LEFT);
            endLabel.setContentDisplay(ContentDisplay.CENTER);
            endLabel.setGraphic(cashier);
            horizontal.getChildren().add(endLabel);
            elements.getChildren().add(horizontal);
            elem.add(horizontal);
        }
        this.simulationStage.setScene(this.scene);
        this.simulationStage.show();
        Thread t =new Thread(()->{
            while (!SimulationManager.endSim.get()) {
                if (SimulationManager.simulationTime.get() == threadTime) {
                    if (SimulationManager.update.get()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                       Platform.runLater(()->{
                           setTime("Time "+threadTime+":");
                           updateGUI(simulationManager.getClients());
                       });
                        threadTime++;
                        SimulationManager.update.set(false);
                    }
                }
            }
            Platform.runLater(()->setTime("Simulation finished"));
        });
        t.setDaemon(true);
        t.start();
    }

    public void updateGUI(ArrayList<ArrayList<String>> clients) {
        for (int i = 0; i < clients.size(); i++) {
            ArrayList<String> humans = clients.get(i);
            if (elem.get(i).getChildren().size() - 1 < humans.size()) {
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("Human.png")));
                ImageView cashier = new ImageView(image);
                Label endLabel = new Label(Integer.toString(i));
                endLabel.setAlignment(Pos.CENTER_LEFT);
                endLabel.setContentDisplay(ContentDisplay.CENTER);
                endLabel.setGraphic(cashier);
                elem.get(i).getChildren().add(endLabel);
            } else if (elem.get(i).getChildren().size() - 1 > humans.size()) {
                elem.get(i).getChildren().removeLast();
            }
            for (int j = 1; j < elem.get(i).getChildren().size(); j++) {
                Label current = (Label) elem.get(i).getChildren().get(j);
                current.setText(humans.get(j-1));
            }
        }
    }
}
