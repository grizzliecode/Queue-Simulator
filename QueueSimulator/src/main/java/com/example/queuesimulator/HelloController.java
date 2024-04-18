package com.example.queuesimulator;

import business_logic.SimulationManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.SelectionPolicy;

import java.net.URL;
import java.util.InputMismatchException;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML
    private ChoiceBox<String> choiceBox;

    @FXML
    private TextField clientInput;

    @FXML
    private Label errorLabel;

    @FXML
    private Button executeButton;

    @FXML
    private TextField queueInput;
    @FXML
    private TextField timeInput;

    @FXML
    private TextField maxArival;

    @FXML
    private TextField maxService;

    @FXML
    private TextField minArival;

    @FXML
    private TextField minService;
    private final String[] choices = new String[]{"ShortestTime", "ShortestQueue"};
    private SelectionPolicy chosen;

    public void onExecute() {
        int simTime = 0, queues = 0, clients = 0, minArrival = 0, maxArrival = 0, minSrv = 2, maxSrv =4; ;
        try {
            clients = Integer.parseInt(clientInput.getText());
            queues = Integer.parseInt(queueInput.getText());
            simTime = Integer.parseInt(timeInput.getText());
            minArrival = Integer.parseInt(minArival.getText());
            maxArrival = Integer.parseInt(maxArival.getText());
            minSrv = Integer.parseInt(minService.getText());
            maxSrv = Integer.parseInt(maxService.getText());
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
        if (choiceBox.getValue().equals("ShortestTime")) {
            chosen = SelectionPolicy.SHORTEST_TIME;
        } else {
            chosen = SelectionPolicy.SHORTEST_QUEUE;
        }
        if(simTime==0 || queues==0 || clients==0 || maxArrival==0)
            errorLabel.setText("With 0 queues or 0 clients or in 0 time a simulation can't be done!");
        else {
            errorLabel.setText("");
            SimulationManager simulationManager = new SimulationManager(simTime, clients, queues, chosen, minArrival, maxArrival, minSrv, maxSrv);
            SimulationFrame simulationFrame = new SimulationFrame(queues,simulationManager);
            Thread t = new Thread(simulationManager);
            t.start();
            executeButton.getScene().getWindow().hide();
            simulationFrame.init();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        choiceBox.getItems().addAll(choices);
    }
}