package com.kmlsiuzdak;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;


public class controller {

    @FXML
    private TextField number_of_inclusions;
    @FXML
    private ChoiceBox<Inclusion_border> inclusion_border;
    @FXML
    private Button import_button;
    @FXML
    private ChoiceBox<Inclusion_shape> inclusion_shape;
    @FXML
    private TextField number_of_grains;
    @FXML
    private TextField length_of_inclusion;
    @FXML
    private Button submit;
    @FXML
    private TextField y_size;
    @FXML
    private TextField x_size;
    @FXML
    private ChoiceBox<Border_transition> transition;

    public void generate() {
        grains_view grainsView;
        Window window = submit.getScene().getWindow();
        try {
            validateForm();
            grainsView = new grains_view(getIntFromTextField(x_size), getIntFromTextField(y_size),
                    getIntFromTextField(number_of_grains), transition.getValue(), getIntFromTextField(number_of_inclusions),
                    getIntFromTextField(length_of_inclusion), inclusion_border.getValue(), inclusion_shape.getValue());
            grainsView.start(new Stage());
        } catch (validation e) {
            showAlert(Alert.AlertType.ERROR, window, "Error", e.getMessage());
        } catch (error e) {
            System.out.println(e.getMessage());
            showAlert(Alert.AlertType.ERROR, window, "Error", "Something went wrong");
        }
    }

    public void importGeneration() {
        Window window = import_button.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TEXT Files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extensionFilter);
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            import_view importView = new import_view(file);
            try {
                importView.start(new Stage());
            } catch (error e) {
                System.out.println(e.getMessage());
                showAlert(Alert.AlertType.ERROR, window, "Error", "Something went wrong");
            }
        }
    }

    private void validateForm() throws validation {
        validateNumberInput(x_size, "X size");
        validateNumberInput(y_size, "Y size");
        validateNumberInput(number_of_grains, "number of cells");
        validateChoiceBoxInput(transition, "boundary transition");
        validateInclusionInputs();
    }

    private void validateInclusionInputs() throws validation {
        if (!number_of_inclusions.getText().isEmpty() || !length_of_inclusion.getText().isEmpty()) {
            validateNumberInput(number_of_inclusions, "Inclusions");
            validateNumberInput(length_of_inclusion, "Inclusion radius");
        }
    }

    private void showAlert(Alert.AlertType alertType, Window owner, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.show();
    }

    private void validateNumberInput(TextField textField, String attributeName) throws validation {
        if (textField.getText().isEmpty() || textField.getText().isBlank()) {
            throw new validation("Fill the " + attributeName);
        } else if (!textField.getText().matches("^(?!0+$)\\d+$")){
            throw new validation(attributeName + " need to be a non-negative number");
        }
    }

    private void validateChoiceBoxInput(ChoiceBox<?> choiceBox, String attributeName) throws validation {
        if (choiceBox.getValue() == null) {
            throw new validation("Select the " + attributeName);
        }
    }

    private int getIntFromTextField(TextField textField) {
        if (textField.getText().isEmpty() || textField.getText().isBlank()) {
            return 0;
        }
        return Integer.parseInt(textField.getText());
    }

}
