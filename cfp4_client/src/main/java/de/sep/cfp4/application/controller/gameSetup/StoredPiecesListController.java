package de.sep.cfp4.application.controller.gameSetup;

import de.sep.cfp4.application.Launcher;
import de.sep.cfp4.application.model.listItems.PieceItem;
import de.sep.cfp4.application.model.MapEditorModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class StoredPiecesListController {

  @FXML
  private TableView<PieceItem> pieceTable;
  @FXML
  private TableColumn<PieceItem, String> nameColumn;
  @FXML
  private TableColumn<PieceItem, Image> viewColumn;
  @FXML
  private TableColumn<PieceItem, Void> addButtonColumn;
  @FXML
  private TableColumn<PieceItem, Void> deleteColumn;

  private final MapEditorModel mapEditorModel = MapEditorModel.getInstance();

  private final ObservableList<PieceItem> storedPieceList = mapEditorModel.getStoredPiecesList();

  public void initialize() {
    this.pieceTable.setSelectionModel(null);
    this.nameColumn.setCellValueFactory(new PropertyValueFactory<>("pieceName"));
    this.viewColumn.setCellValueFactory(new PropertyValueFactory<>("pieceView"));
    this.nameColumn.setCellFactory(column -> new TableCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
          setText(null);
        } else {
          setText(item);
        }
      }
    });

    this.viewColumn.setCellFactory(column -> new TableCell<>() {
      @Override
      protected void updateItem(Image image, boolean empty) {
        super.updateItem(image, empty);
        if (image == null || empty) {
          setGraphic(null);
        } else {
          ImageView imageView = new ImageView(image);
          imageView.setFitWidth(40);
          imageView.setFitHeight(40);
          setGraphic(imageView);
        }
      }
    });

    this.addButtonColumn.setCellFactory(column -> new TableCell<>() {
      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setGraphic(null);
        } else {
          Button addButton = new Button("Add");
          addButton.getStyleClass().add("black-button");
          addButton.setOnMouseClicked(event -> {
            PieceItem pieceItem = getTableView().getItems().get(getIndex());
            System.out.println(pieceItem);
            mapEditorModel.addSelectedPiece(pieceItem);
          });
          setGraphic(addButton);
        }
      }
    });

    this.deleteColumn.setCellFactory(column -> new TableCell<>() {
      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setGraphic(null);
        } else {
          Button deleteButton = new Button();
          ImageView deleteIcon = new ImageView(
              Launcher.class.getResource("images/Trash.png").toExternalForm());
          deleteIcon.setFitWidth(20);
          deleteIcon.setFitHeight(20);
          deleteButton.setGraphic(deleteIcon);
          deleteButton.getStyleClass().add("trash-button");
          deleteButton.setOnMouseClicked(event -> {
            PieceItem pieceItem = getTableView().getItems().get(getIndex());
            mapEditorModel.removeStoredPiece(pieceItem);
          });
          setGraphic(deleteButton);
        }
      }
    });

    this.pieceTable.setItems(this.storedPieceList);
  }

}
