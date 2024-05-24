package de.sep.cfp4.application.controller.gameSetup;

import de.sep.cfp4.technicalServices.database.DatabaseHandler;
import de.sep.cfp4.technicalServices.database.interfaces.Database;
import de.sep.cfp4.application.Launcher;
import de.sep.cfp4.application.model.listItems.MapItem;
import de.sep.cfp4.application.model.MapEditorModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


/**
 * Controller for the map template list view.
 * @author dcebulla
 * @version 0.0.1
 */
public class MapListController {

  @FXML
  private TableView<MapItem> mapTable;
  @FXML
  private TableColumn<MapItem, String> mapNameColumn;
  @FXML
  private TableColumn<MapItem, Void> openEditorColumn;
  @FXML
  private TableColumn<MapItem, Boolean> selectionColumn;
  @FXML
  private TableColumn<MapItem, Void> deleteColumn;

  private final ReadOnlyObjectWrapper<MapItem> selectedMap = new ReadOnlyObjectWrapper<>();

  //private OpponentListController opponentListController;

  private ObservableList<MapItem> mapList;

  private final Database database = DatabaseHandler.getInstance();

  /**
   * Initializes the map list and specifies how entries to the map list are to be displayed.
   */
  @FXML
  public void initialize() {
    this.mapList = this.database.getMapTemplateList();
    this.mapTable.setSelectionModel(null);

    // Set up the columns in the table view
    this.mapNameColumn.setCellValueFactory(new PropertyValueFactory<>("mapName"));
    this.selectionColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));


    // Adapted from https://edencoding.com/tableview-customization-cellfactory/ and CellFactory Entity Cycle
    this.selectionColumn.setCellFactory(column -> {
      TableCell<MapItem, Boolean> cell = new TableCell<>();
      Button selectButton = new Button();
      cell.itemProperty().addListener((obs, wasSelected, isSelected) -> {
        if (isSelected != null) {
          if (isSelected) {
            selectButton.setText("Selected");
            selectButton.getStyleClass().remove("grey-button");
            selectButton.getStyleClass().add("black-button");
          } else {
            selectButton.setText("Select");
            selectButton.getStyleClass().remove("black-button");
            selectButton.getStyleClass().add("grey-button");
          }
        }
      });

      selectButton.setOnAction(event -> {
        MapItem mapClicked = this.mapTable.getItems().get(cell.getIndex());
        boolean selected = mapClicked.isSelected();
        this.mapTable.getItems().forEach(mapItem -> mapItem.setSelected(false));
        mapClicked.setSelected(!selected);

        // Set selected map property
        if (mapClicked.isSelected()) {
          this.selectedMap.set(mapClicked);
        } else {
          this.selectedMap.set(null);
        }

        // Update number of opponents
        //this.opponentListController.updateBasedOnMapSelection(mapClicked.isSelected() ? mapClicked : null);
      });

      cell.graphicProperty()
          .bind(Bindings.when(cell.emptyProperty()).then((Button) null).otherwise(selectButton));
      return cell;
    });

    this.deleteColumn.setCellFactory(column -> {
      TableCell<MapItem, Void> cell = new TableCell<>();
      Button deleteButton = new Button();
      ImageView deleteIcon = new ImageView(
          new Image(Launcher.class.getResourceAsStream("images/Trash.png")));
      deleteIcon.setFitWidth(20);
      deleteIcon.setFitHeight(20);
      deleteButton.setGraphic(deleteIcon);
      deleteButton.getStyleClass().add("trash-button");

      deleteButton.setOnAction(event -> {
        MapItem server = this.mapTable.getItems().get(cell.getIndex());
        database.removeMapItem(server);
      });

      // The binding ensures that the button is deleted once the server entry is removed from the server list
      // It avoids the need to manually remove the button from the cell via a listener
      cell.graphicProperty()
          .bind(Bindings.when(cell.emptyProperty()).then((Button) null).otherwise(deleteButton));
      return cell;
    });

    this.openEditorColumn.setCellFactory(column -> {
      TableCell<MapItem, Void> cell = new TableCell<>();
      Button openEditorButton = new Button("Open in Editor");
      openEditorButton.getStyleClass().add("grey-button");

      openEditorButton.setOnAction(event -> {
        // Open map editor for the selected map
        MapItem mapItem = this.mapTable.getItems().get(cell.getIndex());
        MapEditorModel.getInstance().openInMapEditor(mapItem);
      });

      cell.graphicProperty()
          .bind(Bindings.when(cell.emptyProperty()).then((Button) null).otherwise(openEditorButton));
      return cell;
    });

    this.mapTable.setItems(this.mapList);
  }

  /**
   * Returns the selected map item from the map list.
   * @return the selected map
   */
  public MapItem getSelectedMap() {
    return this.mapList.stream().filter(MapItem::isSelected).findFirst().orElse(null);
  }

  public ReadOnlyObjectProperty<MapItem> selectedMapProperty() {
    return selectedMap.getReadOnlyProperty();
  }

  //public void setOpponentListController(OpponentListController opponentListController) {
  //  this.opponentListController = opponentListController;
  //}

}
