/*
Author: Reece Pieri
ID: M087496
Date: 14/10/2020
Assessment: Java III - Portfolio AT2 Q6
*/

package csvreadwrite;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CSVReadWrite extends Application {
    private Stage stage;
    private CSVReadWriteController controller;
    private CSVReader reader;
    private String currentFile;
    private String[] headers;
    public boolean changes;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CSVReadWrite.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.csvrw = this;
        controller.setupMenuItems();
        controller.setMouseEvents();
        controller.stage = stage;
        
        stage.setTitle("CSV Read & Write");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> {
            savePromptOnClose(event);
        });
        this.stage = stage;
        stage.show();
    }

    // Load a CSV file
    public void loadCSV() {
        FileChooser openFile = new FileChooser();
        openFile.setTitle("Open File");
        openFile.setInitialDirectory(new File(Paths.get(".").toAbsolutePath().normalize().toString()));
        openFile.getExtensionFilters().add(new ExtensionFilter("CSV Files", "*.csv"));
        
        File file = openFile.showOpenDialog(stage);
        
        if (file != null) {
            readCSV(file);
            changes = false;
        }
    }
    
    // Save a CSV file
    public void saveCSV(String currentFile) {
        try (PrintWriter writer = new PrintWriter(new File(currentFile))) {
            String string = "";
            
            // Write headers to file
            for (String header : headers) {
                string = string + header + ",";
            }
            string = string.substring(0, string.length() - 1);
            writer.write(string + "\n");
            
            // Write table data to file
            for (var record : controller.tblOutput.getItems()) {
                string = "";
                
                for (int i = 0; i < ((List<String>)record).size(); i++) {
                    string = string + ((List<String>)record).get(i) + ",";
                }

                string = string.substring(0, string.length() - 1);
                writer.write(string + "\n");
            }
            
            changes = false;
        } catch (IOException e) {
            
        }
    }
    
    // Save a CSV file as...
    public void saveCSVAs() {
        FileChooser saveFile = new FileChooser();
        saveFile.setTitle("Save File");
        saveFile.setInitialDirectory(new File(Paths.get(".").toAbsolutePath().normalize().toString()));
        saveFile.getExtensionFilters().add(new ExtensionFilter("CSV Files", "*.csv"));
        File file = saveFile.showSaveDialog(stage);
        if (file != null) {
            saveCSV(file.getPath());
            currentFile = file.getPath();
            controller.updateFileInfo(currentFile);
        }
    }
    
    // Save prompt on program close
    public void savePromptOnClose(Event event) {
        if (currentFile != null) {
            ButtonType yes = new ButtonType("Yes");
            ButtonType no = new ButtonType("No");
            ButtonType cancel = new ButtonType("Cancel");
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save the current file?", yes, no, cancel);
            alert.setTitle("Save File");
            alert.setHeaderText(null);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(stage);
            alert.showAndWait().ifPresent(response -> {
                if (response == yes) {
                    saveCSV(currentFile);
                    Platform.exit();
                } else if (response == no) {
                    Platform.exit();
                } else {
                    event.consume();
                }
            });
        } else {
            Platform.exit();
        }
    }
    
    // Read a CSV file
    private void readCSV(File file) {
        currentFile = file.getPath();

        try {
            reader = new CSVReader(new FileReader(currentFile));
            int count = 0;
            List<List<String>> data = new ArrayList();
            String[] line;

            clearTables();
            while((line = reader.readNext()) != null) {
                if (count == 0) {
                    headers = line;
                    tblOutputAddColumns(line);
                    tblModifyAddColumns(line);
                } else {
                    storeRowData(data, line);
                }
                count++;
            }

            populateOutputTable(data);
            controller.updateFileInfo(currentFile);
        } catch (CsvValidationException | IOException e) {
            
        }
    }
    
    // Add columns to output tableview
    private void tblOutputAddColumns(String[] line) {
        int count = 0;
        for (String header : line) {
            final int count2 = count;
            TableColumn<ObservableList, String> column = new TableColumn<>(header);
            column.setCellValueFactory(param -> {
                return new SimpleStringProperty(param.getValue().get(count2).toString());
            });
            controller.tblOutput.getColumns().add(column);
            count++;
        }
    }
    
    // Store row data
    private void storeRowData(List<List<String>> data, String[] line) {
        List<String> row = new ArrayList();
        row.addAll(Arrays.asList(line));
        data.add(row);
    }
    
    // Populate output table with row data
    private void populateOutputTable(List<List<String>> data) {
        if (!data.isEmpty()) {
            ObservableList<ObservableList> csvData = FXCollections.observableArrayList();

            for (List<String> dataList : data) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (String rowData : dataList) {
                    row.add(rowData);
                }
                csvData.add(row);
            }
            controller.tblOutput.setItems(csvData);
        }
    }
    
    // Add row
    public void addRow() {
        List<String> newRow = FXCollections.observableArrayList();

        for (int i = 0; i < headers.length; i++) {
            newRow.add("-");
        }

        controller.tblOutput.getItems().add(newRow);
        controller.tblOutput.getSelectionModel().select(controller.tblOutput.getItems().size() - 1);
        controller.updateRowsInfo();
    }
    
    // Delete row
    public void deleteRow() {
        int selectedIndex = controller.tblOutput.getSelectionModel().getSelectedIndex();
        controller.tblOutput.getItems().remove(selectedIndex);
        controller.updateRowsInfo();
        clearModifyTextFields();
    }
    
    
    // Add columns to modify table
    private void tblModifyAddColumns(String[] line) {
        int count = 0;
        for (String header : line) {
            Label label = new Label(header);
            label.setAlignment(Pos.CENTER);
            label.setMinWidth(100.0);
            label.setPrefWidth(150.0);
            controller.grdModify.addColumn(count, label);
            
            TextField textField = new TextField();
            textField.setAlignment(Pos.CENTER);
            textField.setPrefWidth(90.0);
            controller.grdModify.add(textField, count, 1);
            textField.setOnKeyReleased(event -> {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    controller.btnModify_OnAction(null);
                }
            });
            
            count++;
        }
    }
    
    // Clear modify table text fields
    public void clearModifyTextFields() {
        for (var node : controller.grdModify.getChildren()) {
            if (node instanceof TextField) {
                ((TextField) node).setText("");
            }
        }
    }
    
    // Clear tables
    private void clearTables() {
        controller.tblOutput.getColumns().clear();
        controller.grdModify.getChildren().clear();
    }
    
    // Clear data
    public void clearData() {
        controller.tblOutput.getItems().clear();
        clearModifyTextFields();
    }
    
    public String getCurrentFile() {
        return currentFile;
    }
}
