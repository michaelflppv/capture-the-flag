package de.sep.cfp4.application.controller.gameSetup;

import de.sep.cfp4.technicalServices.database.DatabaseHandler;
import de.sep.cfp4.technicalServices.database.interfaces.Database;
import de.sep.cfp4.application.model.listItems.PieceItem;
import de.sep.cfp4.application.model.MapEditorModel;
import de.unimannheim.swt.pse.ctf.game.map.Directions;
import de.unimannheim.swt.pse.ctf.game.map.Movement;
import de.unimannheim.swt.pse.ctf.game.map.PieceDescription;
import de.unimannheim.swt.pse.ctf.game.map.Shape;
import de.unimannheim.swt.pse.ctf.game.map.ShapeType;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PieceCreationController {

  @FXML
  private TextField pieceName;
  @FXML
  private TextField pieceAttackPower;
  @FXML
  private ComboBox<String> pieceShape;
  @FXML
  private TextField directionsUpLeft;
  @FXML
  private TextField directionsUp;
  @FXML
  private TextField directionsUpRight;
  @FXML
  private TextField directionsLeft;
  @FXML
  private TextField directionsRight;
  @FXML
  private TextField directionsDownLeft;
  @FXML
  private TextField directionsDown;
  @FXML
  private TextField directionsDownRight;
  @FXML
  private ComboBox<Image> pieceImageComboBox;

  private final MapEditorModel mapEditorModel = MapEditorModel.getInstance();
  private final Database database = DatabaseHandler.getInstance();
  private final ObservableList<PieceItem> storedPieceList = mapEditorModel.getStoredPiecesList();

  @FXML
  public void initialize() {
    this.pieceShape.getItems().addAll("Directions", "lshape");
    this.pieceShape.getSelectionModel().selectFirst();

    this.pieceImageComboBox.getItems().addAll(this.database.getAllPieceImages());

    this.pieceImageComboBox.setCellFactory(param -> new ListCell<>() {
      @Override
      protected void updateItem(Image item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
          setGraphic(null);
        } else {
          ImageView imageView = new ImageView(item);
          imageView.setFitWidth(50);
          imageView.setFitHeight(50);
          setGraphic(imageView);
        }
      }
    });
    this.pieceImageComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(Image item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
          setGraphic(null);
        } else {
          ImageView imageView = new ImageView(item);
          imageView.setFitWidth(50);
          imageView.setFitHeight(50);
          setGraphic(imageView);
        }
      }
    });


  }

  @FXML
  public void createPiece() {
    if (pieceName.getText().isEmpty() || pieceAttackPower.getText().isEmpty() || (
        pieceShape.getValue().equals("Directions") && (directionsUpLeft.getText().isEmpty()
            || directionsUp.getText().isEmpty() || directionsUpRight.getText().isEmpty()
            || directionsLeft.getText().isEmpty() || directionsRight.getText().isEmpty()
            || directionsDownLeft.getText().isEmpty() || directionsDown.getText().isEmpty()
            || directionsDownRight.getText().isEmpty()))) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Piece Creation");
      alert.setHeaderText(null);
      alert.setContentText("Please fill out all fields");
      alert.showAndWait();
      return;
    }

    int attackPower;
    try {
      attackPower = Integer.parseInt(pieceAttackPower.getText());
    } catch (NumberFormatException e) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Piece Creation");
      alert.setHeaderText(null);
      alert.setContentText("Please enter only integers for attack power");
      alert.showAndWait();
      return;
    }

    if (attackPower < 1) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Piece Creation");
      alert.setHeaderText(null);
      alert.setContentText("Attack power must be greater than 0");
      alert.showAndWait();
      return;
    }

    PieceDescription pieceDescription = new PieceDescription();
    pieceDescription.setType(this.pieceName.getText());
    pieceDescription.setAttackPower(attackPower);
    Movement movement = new Movement();
    pieceDescription.setMovement(movement);

    switch (pieceShape.getValue()) {
      case "Directions" -> {
        // Parse all direction fields into an integer array and check if all fields are integers. If a field is not an integer, clear this field.
        int[] directions = new int[8];
        TextField[] directionFields = new TextField[]{directionsUpLeft,
            directionsUp, directionsUpRight, directionsLeft, directionsRight, directionsDownLeft,
            directionsDown, directionsDownRight};

        for (int i = 0; i < directionFields.length; i++) {
          try {
            directions[i] = Integer.parseInt(directionFields[i].getText());
            if (directions[i] < 0) {
              throw new NumberFormatException();
            }
          } catch (NumberFormatException e) {
            directionFields[i].clear();
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Piece Creation");
            alert.setHeaderText(null);
            alert.setContentText(
                "Please enter only whole numbers greater than or equal to 0 for directions");
            alert.showAndWait();
            return;
          }
        }

        Directions pieceDirections = new Directions();
        pieceDirections.setUpLeft(directions[0]);
        pieceDirections.setUp(directions[1]);
        pieceDirections.setUpRight(directions[2]);
        pieceDirections.setLeft(directions[3]);
        pieceDirections.setRight(directions[4]);
        pieceDirections.setDownLeft(directions[5]);
        pieceDirections.setDown(directions[6]);
        pieceDirections.setDownRight(directions[7]);
        movement.setShape(null);
        movement.setDirections(pieceDirections);
      }
      case "lshape" -> {
        Shape shape = new Shape();
        shape.setType(ShapeType.lshape);
        movement.setShape(shape);
      }
      default -> {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Piece Creation");
        alert.setHeaderText(null);
        alert.setContentText(
            "The provided shape type is invalid. Please try selecting the type manually again or restart the application.");
        alert.showAndWait();
        return;
      }
    }

    // Stores the selected image of the custom piece in the database.
    PieceItem pieceItem = new PieceItem(pieceDescription, this.pieceImageComboBox.getValue());
    this.mapEditorModel.addStoredPiece(pieceItem);
  }

}

