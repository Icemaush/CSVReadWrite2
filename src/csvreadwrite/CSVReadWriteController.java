/*
Author: Reece Pieri
ID: M087496
Date: 14/10/2020
Assessment: Java III - Portfolio AT2 Q6
*/

package csvreadwrite;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CSVReadWriteController {
    public Stage stage;
    public CSVReadWrite csvrw;
    @FXML public GridPane grdModify;
    @FXML private Label lblFileName;
    @FXML private Label lblRows;
    @FXML public TableView tblOutput;
    @FXML private MenuItem menuLoad;
    @FXML private MenuItem menuSave;
    @FXML private MenuItem menuSaveAs;
    @FXML private MenuItem menuClose;
    @FXML private MenuItem menuClearData;
    @FXML private MenuItem menuAbout;
    @FXML private MenuItem menuHowTo;
    @FXML private Button btnModify;
    
    // Set mouse events
    public void setMouseEvents() {
        tblOutput.setOnMouseClicked(event -> {
            List<String> fields = (List<String>)tblOutput.getSelectionModel().getSelectedItem();
            if (fields != null) {
                updateModifyTable(fields);
            }
        });
    }
    
    // Update modify table
    private void updateModifyTable(List<String> fields) {
        List<TextField> textFields = new ArrayList();
        for (var node : grdModify.getChildren()) {
            if (node instanceof TextField) {
                textFields.add((TextField)node);
            }
        }
        
        for (int i = 0; i < fields.size(); i++) {
            textFields.get(i).setText(fields.get(i));
        }
    }
    
    // Modify a record
    public void btnModify_OnAction(ActionEvent event) {
        if (tblOutput.getSelectionModel().getSelectedItem() != null) {
            ObservableList<String> modifiedFields = FXCollections.observableArrayList();
            int selectedIndex = tblOutput.getSelectionModel().getSelectedIndex();

            for (var node : grdModify.getChildren()) {
                if (node instanceof TextField) {
                    modifiedFields.add(((TextField) node).getText());
                }
            }
            
            tblOutput.getItems().set(selectedIndex, modifiedFields);
            tblOutput.getSelectionModel().select(selectedIndex);
            csvrw.changes = true;
        }
    }
    
    // Add a record
    public void btnAddRow_OnAction(ActionEvent event) {
        if (csvrw.getCurrentFile() != null) {
            csvrw.addRow();
            List<String> fields = (List<String>)tblOutput.getSelectionModel().getSelectedItem();
            updateModifyTable(fields);
            focusTextField();
        }
    }
    
    // Delete a record
    public void btnDeleteRow_OnAction(ActionEvent event) {
        csvrw.deleteRow();
        tblOutput.requestFocus();
    }
    
    // Set focus to first modify table text field
    private void focusTextField() {
        for (var node : grdModify.getChildren()) {
            if (node instanceof TextField) {
                ((TextField)node).requestFocus();
                return;
            }
        }
    }
    
    // Update file information
    public void updateFileInfo(String currentFile) {
        String[] fileParts = org.apache.commons.lang3.StringUtils.split(currentFile, "\\");
        String fileName = fileParts[fileParts.length - 1];
        lblFileName.setText(fileName);
        updateRowsInfo();
    }
    
    // Update rows information
    public void updateRowsInfo() {
        lblRows.setText(String.valueOf(tblOutput.getItems().size()));
    }
    
    // Setup menubar functions
    public void setupMenuItems() {
        menuLoad.setStyle("-fx-max-width: 100px");
        menuLoad.setOnAction(event -> {
            csvrw.loadCSV();
        });
        
        menuSave.setOnAction(event -> {
            csvrw.saveCSV(csvrw.getCurrentFile());
        });
        
        menuSaveAs.setOnAction(event -> {
            csvrw.saveCSVAs();
        });

        menuClose.setOnAction(event -> {
            if (csvrw.changes) {
                csvrw.saveCSV(csvrw.getCurrentFile());
            }
            Platform.exit();
        });
        
        menuClearData.setOnAction(event -> {
            csvrw.clearData();
            updateRowsInfo();
        });
        
        menuHowTo.setOnAction(event -> {
            try {
                File file = new java.io.File("./HowTo.html").getAbsoluteFile();
                Desktop.getDesktop().open(file); 
            } catch (IOException ex) {
                
            }
        });
        
        menuAbout.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, 
                "CSV Read & Write\n"
                + "Developed as a tool to modify CSV files.\n\n"
                + "Created by Reece Pieri (M087496).\n"
                + "Date: 17/10/2020\n"
                + "South Metropolitan TAFE\n"
                + "Diploma in Software Development, Java III - Portfolio Activity 6");
            alert.setTitle("About");
            alert.setHeaderText(null);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(stage);
            alert.showAndWait();
        });
    }
}