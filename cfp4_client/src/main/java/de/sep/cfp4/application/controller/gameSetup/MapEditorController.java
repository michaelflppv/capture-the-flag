package de.sep.cfp4.application.controller.gameSetup;

import de.sep.cfp4.application.exceptions.MapTemplateNameTaken;
import de.sep.cfp4.application.model.BoardModel;
import de.sep.cfp4.application.model.MapEditorModel;
import de.sep.cfp4.ui.customComponents.BoardView;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.map.PlacementType;
import java.io.IOException;
import java.net.URI;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;

/**
 * Controller for the map editor view. Will be implemented in the future for the second submission.
 *
 * @author dcebulla
 * @version 0.0.2
 */
public class MapEditorController {

  @FXML
  private TextField mapNameTextField;
  @FXML
  private ComboBox<Integer> mapSizeComboBox;
  @FXML
  private ComboBox<Integer> obstaclesComboBox;
  @FXML
  private ComboBox<Integer> numberTeamsComboBox;
  @FXML
  private ComboBox<Integer> numberFlagsComboBox;
  @FXML
  private ComboBox<PlacementType> piecePlacementComboBox;
  @FXML
  private ComboBox<Integer> totalGameTimeComboBox;
  @FXML
  private ComboBox<Integer> timePerMoveComboBox;
  @FXML
  private AnchorPane mapPreview;
  @FXML
  private ServerListController serverListController;
  private final MapEditorModel mapEditorModel = MapEditorModel.getInstance();

  /**
   * Initializes the map editor view with the default values for the combo boxes.
   */
  @FXML
  public void initialize() {
    this.setUpView();
    this.addMapItemChangeListener();
  }

  private void setUpView() {
    this.mapSizeComboBox.getItems().addAll(4, 5, 6, 7, 8, 9, 10);
    this.mapSizeComboBox.getSelectionModel().selectLast();
    this.mapSizeComboBox.setCellFactory(param -> new ListCell<>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else {
          setText(item + "x" + item);
        }
      }
    });
    this.mapSizeComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(Integer object) {
        return object + "x" + object;
      }

      @Override
      public Integer fromString(String string) {
        return Integer.parseInt(string.split("x")[0]);
      }
    });

    this.obstaclesComboBox.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    this.obstaclesComboBox.getSelectionModel().selectFirst();
    this.obstaclesComboBox.setCellFactory(param -> new ListCell<>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else {
          if (item == 0) {
            setText("None");
          } else {
            setText(item.toString());
          }
        }
      }
    });
    this.obstaclesComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(Integer object) {
        if (object == 0) {
          return "None";
        } else {
          return object.toString();
        }
      }

      @Override
      public Integer fromString(String string) {
        return string.equals("None") ? 0 : Integer.parseInt(string);
      }
    });

    this.numberTeamsComboBox.getItems().addAll(2, 3, 4);
    this.numberTeamsComboBox.getSelectionModel().selectFirst();

    this.numberFlagsComboBox.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    this.numberFlagsComboBox.getSelectionModel().selectFirst();

    this.piecePlacementComboBox.getItems().addAll(PlacementType.values());
    this.piecePlacementComboBox.getSelectionModel().selectFirst();
    this.piecePlacementComboBox.setCellFactory(param -> new ListCell<>() {
      @Override
      protected void updateItem(PlacementType item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else {
          setText(item.toString().replace("_", " "));
        }
      }
    });
    this.piecePlacementComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(PlacementType object) {
        return object.toString().replace("_", " ");
      }

      @Override
      public PlacementType fromString(String string) {
        return PlacementType.valueOf(string.replace(" ", "_"));
      }
    });

    this.totalGameTimeComboBox.getItems().addAll(-1, 180, 300, 480, 600, 720, 900);
    this.totalGameTimeComboBox.getSelectionModel().selectFirst();
    this.totalGameTimeComboBox.setCellFactory(param -> new ListCell<>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else if (item == -1) {
          setText("No Time Limit");
        } else {
          setText(item / 60 + " min");
        }
      }
    });
    this.totalGameTimeComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(Integer object) {
        if (object == -1) {
          return "No Time Limit";
        } else {
          return object / 60 + " min";
        }
      }

      @Override
      public Integer fromString(String string) {
        return Integer.parseInt(string.substring(0, string.length() - 4)) * 60;
      }
    });

    this.timePerMoveComboBox.getItems().addAll(-1, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60);
    this.timePerMoveComboBox.getSelectionModel().selectFirst();
    this.timePerMoveComboBox.setCellFactory(param -> new ListCell<>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else if (item == -1) {
          setText("No Time Limit");
        } else {
          setText(item + " s");
        }
      }
    });
    this.timePerMoveComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(Integer object) {
        if (object == -1) {
          return "No Time Limit";
        } else {
          return object + " s";
        }
      }

      @Override
      public Integer fromString(String string) {
        return Integer.parseInt(string.substring(0, string.length() - 2));
      }
    });
  }

  /**
   * Adds a listener to the selected map item property of the map editor model.
   */
  private void addMapItemChangeListener() {
    this.mapEditorModel.selectedMapProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        this.mapNameTextField.setText(newValue.getMapName());

        MapTemplate mapTemplate = newValue.getMapTemplate();
        this.mapSizeComboBox.getSelectionModel().select(mapTemplate.getGridSize()[0] - 4);
        this.obstaclesComboBox.getSelectionModel().select(mapTemplate.getBlocks());
        this.numberTeamsComboBox.getSelectionModel().select(mapTemplate.getTeams() - 2);
        this.numberFlagsComboBox.getSelectionModel().select(mapTemplate.getFlags() - 1);
        this.piecePlacementComboBox.getSelectionModel().select(mapTemplate.getPlacement());
        this.totalGameTimeComboBox.getSelectionModel().select(Integer.valueOf(mapTemplate.getTotalTimeLimitInSeconds()));
        this.timePerMoveComboBox.getSelectionModel().select(Integer.valueOf(mapTemplate.getMoveTimeLimitInSeconds()));
      }
    });
  }


  /**
   * Creates a new map template with the specified parameters.
   */
  @FXML
  public void createMapTemplate() {
    if (this.mapNameTextField.getText().isEmpty()) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("No Map Name");
      alert.setHeaderText(null);
      alert.setContentText("Please enter a name for the map template.");
      alert.show();
      return;
    } else if (this.mapEditorModel.getSelectedPiecesList().isEmpty()) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("No Pieces Selected");
      alert.setHeaderText(null);
      alert.setContentText("Please select at least one piece to create a map template.");
      alert.show();
      return;
    }

    try {
      this.mapEditorModel.addMapTemplate(this.mapNameTextField.getText(),
          this.mapSizeComboBox.getValue(), this.mapSizeComboBox.getValue(),
          this.numberTeamsComboBox.getValue(), this.numberFlagsComboBox.getValue(),
          this.obstaclesComboBox.getValue(), this.piecePlacementComboBox.getValue(),
          this.totalGameTimeComboBox.getValue(), this.timePerMoveComboBox.getValue());
    } catch (MapTemplateNameTaken e) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Map Name Already Exists");
      alert.setHeaderText(null);
      alert.setContentText("A map template with this name already exists. Please choose a different name.");
      alert.show();
      return;
    }

    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("Map Template Created");
    alert.setHeaderText(null);
    alert.setContentText("The map template has been successfully created.");
    alert.show();
  }

  @FXML
  public void renderPreview() {
    MapTemplate mapTemplate = this.mapEditorModel.createMapTemplate(this.mapSizeComboBox.getValue(), this.mapSizeComboBox.getValue(),
        this.numberTeamsComboBox.getValue(), this.numberFlagsComboBox.getValue(),
        this.obstaclesComboBox.getValue(), this.piecePlacementComboBox.getValue(),
        this.totalGameTimeComboBox.getValue(), this.timePerMoveComboBox.getValue());

    this.mapPreview.getChildren().clear();
    if (this.serverListController.getSelectedServer() != null) {
      try {
        BoardModel boardModel = new BoardModel(URI.create("http://" + this.serverListController.getSelectedServer().getUrl()), mapTemplate);
        boardModel.stopUpdateThread();
        BoardView boardView = new BoardView(boardModel);
        AnchorPane.setTopAnchor(boardView, 0.0);
        AnchorPane.setBottomAnchor(boardView, 0.0);
        AnchorPane.setLeftAnchor(boardView, 0.0);
        AnchorPane.setRightAnchor(boardView, 0.0);
        this.mapPreview.getChildren().add(boardView);
      } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    } else {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("No Server Selected");
      alert.setHeaderText(null);
      alert.setContentText("Please select a server to render the map preview.");
      alert.show();
    }
  }

}
