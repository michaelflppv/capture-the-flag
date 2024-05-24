package de.sep.cfp4.application.controller.settings;

import de.sep.cfp4.technicalServices.resource.BoardTheme;
import de.sep.cfp4.technicalServices.database.DatabaseHandler;
import de.sep.cfp4.technicalServices.database.interfaces.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

/**
 * Controller for the settings view.
 * Will be implemented in the future for the second submission.
 * @author dcebulla
 * @version 0.0.1
 */
public class SettingsController {
  @FXML
  private ComboBox<BoardTheme>  boardThemeComboBox;
  private final Database database = DatabaseHandler.getInstance();
  @FXML
  public void initialize() {
    this.boardThemeComboBox.getItems().addAll(BoardTheme.values());
    this.boardThemeComboBox.getSelectionModel().select(BoardTheme.DEFAULT);
    this.boardThemeComboBox.addEventHandler(ActionEvent.ACTION, event -> {
      BoardTheme boardTheme = this.boardThemeComboBox.getSelectionModel().getSelectedItem();
      this.database.setBoardTheme(boardTheme);
    });
  }

}
