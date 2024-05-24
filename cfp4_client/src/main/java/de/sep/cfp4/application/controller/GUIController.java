package de.sep.cfp4.application.controller;


import de.sep.cfp4.application.controller.gamePlay.BoardController;
import de.sep.cfp4.technicalServices.database.DatabaseHandler;
import de.sep.cfp4.technicalServices.database.interfaces.Database;
import de.sep.cfp4.application.Launcher;
import de.sep.cfp4.application.model.BoardModel;
import de.sep.cfp4.application.model.MapEditorModel;
import java.io.IOException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Controller for the main GUI view.
 * @author dcebulla
 * @version 0.0.2
 */
public class GUIController {

  @FXML
  private BorderPane gui;
  @FXML
  private ListView<BoardController> gameSessionList;

  private Node homeScene;
  private Node joinGameScene;
  private Node createGameScene;
  private Node mapEditorScene;
  private Node settingsScene;

  private final Database database = DatabaseHandler.getInstance();

  private final ObservableList<BoardController> boardControllers = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    this.loadScenes();
    this.addBoardModelListListener();
    this.addMapEditorModelListener();
    this.setUPGameList();
  }


  /**
   * Loads the all scenes from the FXML files on startup of the application and stores them in the controller for quick access.
   */
  private void loadScenes() {
    try {
      this.homeScene = new FXMLLoader(Launcher.class.getResource("Home.fxml")).load();
      this.gui.setCenter(this.homeScene);

      this.settingsScene = new FXMLLoader(Launcher.class.getResource("Settings.fxml")).load();
      this.mapEditorScene = new FXMLLoader(Launcher.class.getResource("MapEditor.fxml")).load();
      this.joinGameScene = new FXMLLoader(Launcher.class.getResource("JoinGame.fxml")).load();
      this.createGameScene = new FXMLLoader(Launcher.class.getResource("CreateGame.fxml")).load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Adds a listener to the board model list in the database to update the game session list view
   * and switch to the game view when a new game session is added.
   */
  private void addBoardModelListListener() {
    this.database.addListenerBoardModelList(change -> {
      if (change.wasAdded()) {
        BoardModel boardModel = change.getValueAdded();
        try {
          FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("Board.fxml"));
          this.gui.setCenter(loader.load());
          BoardController boardController = loader.getController();
          boardController.initModel(boardModel);
          //boardController.renderBoard();
          boardControllers.add(boardController);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else if (change.wasRemoved()) {
        BoardModel boardModel = change.getValueRemoved();
        // Remove the board controller from the list, AI suggested to use removeIf
        boardControllers.removeIf(boardController -> boardController.getBoardModel().equals(boardModel));
        this.gui.setCenter(this.homeScene);
      }
    });
  }

  /**
   * Adds a listener to the map editor model to switch to the map editor view when a map is loaded
   * into the editor for editing.
   */
  private void addMapEditorModelListener() {
    MapEditorModel.getInstance().selectedMapProperty().addListener((observable, oldValue, newValue) -> {
      this.gui.setCenter(this.mapEditorScene);
    });
  }

  /**
   * Sets up the game list view.
   */
  private void setUPGameList() {
    this.gameSessionList.setCellFactory(param -> new ListCell<BoardController>() {
      @Override
      protected void updateItem(BoardController boardController, boolean empty) {
        super.updateItem(boardController, empty);
        if (empty || boardController == null) {
          setText(null);
          setGraphic(null);
        } else {
          Button button = new Button();
          button.getStyleClass().add("transparent-button");
          button.setOnAction(event -> {
            //boardController.renderBoard();
            gui.setCenter(boardController.getRoot());
          });
          button.setMaxWidth(Double.MAX_VALUE);
          button.setAlignment(Pos.CENTER_LEFT);

          // Set the game name to the team names
          String[] teamNames = boardController.getBoardModel().getAllTeamIDs();
          String gameName = String.join(" vs ", teamNames);
          button.setText(gameName);

          // Update the game name if the team names change
          boardController.getBoardModel().addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("teamNames")) {
              String[] newTeamNames = (String[]) evt.getNewValue();
              String newGameName = String.join(" vs ", newTeamNames);
              Platform.runLater(() -> button.setText(newGameName));
            }
          });

          setGraphic(button);
        }
      }
    });
    this.gameSessionList.setItems(this.boardControllers);
  }


  // Switching between different scenes in the GUI
  @FXML
  public void setHomeScene() {
    this.gui.setCenter(this.homeScene);
  }

  @FXML
  public void setJoinGameScene() {
    this.gui.setCenter(this.joinGameScene);
  }

  @FXML
  public void setCreateGameScene() {
    this.gui.setCenter(this.createGameScene);
  }

  @FXML
  public void setMapEditorScene() {
    this.gui.setCenter(this.mapEditorScene);
  }

  @FXML
  public void setSettingsScene() {
    this.gui.setCenter(this.settingsScene);
  }

  @FXML
  public void confirmAndExit() {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Exit Warning");
    alert.setHeaderText(null);
    alert.setContentText("""
        Do you want to exit the application?
        All unfinished games will be forfeited!"""
    );

    DialogPane dialogPane = alert.getDialogPane();
    dialogPane.getStylesheets().add(Launcher.class.getResource("Default.css").toExternalForm());

    ImageView icon = new ImageView(new Image(Launcher.class.getResourceAsStream("images/Warning.png")));
    icon.setFitWidth(50);
    icon.setFitHeight(50);
    dialogPane.setGraphic(icon);

    ((Stage) dialogPane.getScene().getWindow()).getIcons().add(new Image(Launcher.class.getResourceAsStream("images/Flag.png")));

    alert.getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().add("black-button");
    alert.getDialogPane().lookupButton(ButtonType.OK).getStyleClass().add("grey-button");

    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        Platform.exit();
      }
    });

  }

}