package org.processmining.multilayeralignment.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class AnomalousPatternDiscoveryVisualization extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Discovery of Anomalous Behavioral Patterns");


     // Create top and bottom TableViews
        TableView<CSVData> topTableView = createTableView("D:/OrganizedFolder/phd/My Thesis/Experiments/chapter_ruleMining_realData_BPI2017/OutpurForThesis/Exp2_SystemDeviatingPatterns.csv");
        TableView<CSVData> bottomTableView = createFilteredTableView("D:/OrganizedFolder/phd/My Thesis/Experiments/chapter_ruleMining_realData_BPI2017/OutpurForThesis/Exp2_UserDeviatingPatterns.csv");

        // Create titles
        Label topTitle = createTitleLabel("Patterns of System Deviating Behavior");
        Label bottomTitle = createTitleLabel("Patterns of User Deviating Behavior");

        // Create labels to display row count
        Label topRowCountLabel = createRowCountLabel1(topTableView);
        Label bottomRowCountLabel = createRowCountLabel2(bottomTableView);

        // Set padding to create space between tables and borders
        VBox.setMargin(topTitle, new Insets(20, 0, 0, 0));
        VBox.setMargin(topTableView, new Insets(0, 0, 10, 0));
        VBox.setMargin(bottomTitle, new Insets(20, 0, 0, 0));

        // Create a VBox to hold the titles, tables, and row count labels
        VBox vbox = new VBox(topTitle, topTableView, topRowCountLabel, bottomTitle, bottomTableView, bottomRowCountLabel);
        vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private TableView<CSVData> createTableView(String filePath) {
        TableView<CSVData> tableView = new TableView<>();
        ObservableList<CSVData> data = FXCollections.observableArrayList();

        // Create columns
        TableColumn<CSVData, String> patternColumn = new TableColumn<>("Pattern");
        TableColumn<CSVData, String> behaviorColumn = new TableColumn<>("Deviant behavior");
        TableColumn<CSVData, String> occurrenceColumn = new TableColumn<>("Occurrence");
        TableColumn<CSVData, String> supportColumn = new TableColumn<>("Support(%)");
        TableColumn<CSVData, String> confidenceColumn = new TableColumn<>("Confidence(%)");

        patternColumn.setCellValueFactory(cellData -> cellData.getValue().patternProperty());
        behaviorColumn.setCellValueFactory(cellData -> cellData.getValue().behaviorProperty());
        occurrenceColumn.setCellValueFactory(cellData -> cellData.getValue().occurrenceProperty());

        // Modify the supportColumn and confidenceColumn to multiply the values by 100
        supportColumn.setCellValueFactory(cellData -> {
            SimpleStringProperty supportProperty = cellData.getValue().supportProperty();
            double supportValue = Double.parseDouble(supportProperty.get());
            return new SimpleStringProperty(String.format("%.2f", supportValue * 100));
        });

        confidenceColumn.setCellValueFactory(cellData -> {
            SimpleStringProperty confidenceProperty = cellData.getValue().confidenceProperty();
            double confidenceValue = Double.parseDouble(confidenceProperty.get());
            return new SimpleStringProperty(String.format("%.2f", confidenceValue * 100));
        });

        tableView.getColumns().addAll(patternColumn, behaviorColumn, occurrenceColumn, supportColumn, confidenceColumn);

        // Read and parse CSV file
        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                // Skip the first line (column names)
                String line = reader.readLine();
                int rowId = 1;

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts.length == 4) {
                        data.add(new CSVData(String.valueOf(rowId), parts[0], parts[1], parts[2], parts[3]));
                        rowId++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("File not found: " + filePath);
        }

        tableView.setItems(data);
        return tableView;
    }

    private TableView<CSVData> createFilteredTableView(String filePath) {
        TableView<CSVData> tableView = new TableView<>();
        ObservableList<CSVData> data = FXCollections.observableArrayList();

        // Create columns
        TableColumn<CSVData, String> patternColumn = new TableColumn<>("Pattern");
        TableColumn<CSVData, String> behaviorColumn = new TableColumn<>("Deviant behavior");
        TableColumn<CSVData, String> occurrenceColumn = new TableColumn<>("Occurrence");
        TableColumn<CSVData, String> supportColumn = new TableColumn<>("Support(%)");
        TableColumn<CSVData, String> confidenceColumn = new TableColumn<>("Confidence(%)");

        patternColumn.setCellValueFactory(cellData -> cellData.getValue().patternProperty());
        behaviorColumn.setCellValueFactory(cellData -> cellData.getValue().behaviorProperty());
        occurrenceColumn.setCellValueFactory(cellData -> cellData.getValue().occurrenceProperty());

        // Modify the supportColumn and confidenceColumn to multiply the values by 100
        supportColumn.setCellValueFactory(cellData -> {
            SimpleStringProperty supportProperty = cellData.getValue().supportProperty();
            double supportValue = Double.parseDouble(supportProperty.get());
            return new SimpleStringProperty(String.format("%.2f", supportValue * 100));
        });

        confidenceColumn.setCellValueFactory(cellData -> {
            SimpleStringProperty confidenceProperty = cellData.getValue().confidenceProperty();
            double confidenceValue = Double.parseDouble(confidenceProperty.get());
            return new SimpleStringProperty(String.format("%.2f", confidenceValue * 100));
        });

        tableView.getColumns().addAll(patternColumn, behaviorColumn, occurrenceColumn, supportColumn, confidenceColumn);

        // Read and parse CSV file, filtering out rows containing "set()"
        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                // Skip the first line (column names)
                String line = reader.readLine();
                int rowId = 1;

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts.length == 4 && !parts[0].contains("set()")) {
                        data.add(new CSVData(String.valueOf(rowId), parts[0], parts[1], parts[2], parts[3]));
                        rowId++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("File not found: " + filePath);
        }

        tableView.setItems(data);
        return tableView;
    }

    private Label createTitleLabel(String text) {
        Label titleLabel = new Label(text);
        titleLabel.setFont(Font.font("Arial", 16));
        titleLabel.setStyle("-fx-font-weight: bold");
        return titleLabel;
    }

    private Label createRowCountLabel1(TableView<CSVData> tableView) {
        Label rowCountLabel = new Label("Based on the configured parameteres, " + tableView.getItems().size() +" frequent anomalous patterns were discovered in system behavior. ");
        rowCountLabel.setFont(Font.font("Arial", 16));
        return rowCountLabel;
    }
    private Label createRowCountLabel2(TableView<CSVData> tableView) {
        Label rowCountLabel = new Label("Based on the configured parameteres, " + tableView.getItems().size() +" frequent anomalous patterns were discovered in user behavior. ");
        rowCountLabel.setFont(Font.font("Arial", 16));
        return rowCountLabel;
    }

    public static class CSVData {
        private final SimpleStringProperty pattern;
        private final SimpleStringProperty behavior;
        private final SimpleStringProperty occurrence;
        private final SimpleStringProperty support;
        private final SimpleStringProperty confidence;

        public CSVData(String pattern, String behavior, String occurrence, String support, String confidence) {
            this.pattern = new SimpleStringProperty(pattern);
            this.behavior = new SimpleStringProperty(behavior);
            this.occurrence = new SimpleStringProperty(occurrence);
            this.support = new SimpleStringProperty(support);
            this.confidence = new SimpleStringProperty(confidence);
        }

        public SimpleStringProperty patternProperty() {
            return pattern;
        }

        public SimpleStringProperty behaviorProperty() {
            return behavior;
        }

        public SimpleStringProperty occurrenceProperty() {
            return occurrence;
        }

        public SimpleStringProperty supportProperty() {
            return support;
        }

        public SimpleStringProperty confidenceProperty() {
            return confidence;
        }
    }
}