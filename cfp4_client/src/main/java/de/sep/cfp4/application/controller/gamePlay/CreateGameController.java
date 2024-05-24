package de.sep.cfp4.application.controller.gamePlay;

import de.sep.cfp4.application.controller.gameSetup.MapListController;
import de.sep.cfp4.application.controller.gameSetup.ServerListController;
import de.sep.cfp4.technicalServices.ai.EasyBot;
import de.sep.cfp4.technicalServices.ai.NormalBot;
import de.sep.cfp4.technicalServices.database.DatabaseHandler;
import de.sep.cfp4.technicalServices.resource.PlayerType;
import de.sep.cfp4.technicalServices.database.interfaces.Database;
import de.sep.cfp4.application.model.BoardModel;
import de.sep.cfp4.application.model.listItems.MapItem;
import de.sep.cfp4.application.model.listItems.Server;
import de.sep.cfp4.ui.customComponents.BoardView;
import de.sep.cfp4.technicalServices.ai.mcts.MCTSClient;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameSessionNotFound;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 * Controller for the create game view.
 *
 * @author dcebulla
 * @version 0.0.2
 */
public class CreateGameController {

  @FXML
  private TextField teamID;
  @FXML
  private AnchorPane mapPreview;

  // Controller instance for the server list
  @FXML
  private ServerListController serverListController;

  // Controller instance for the map list
  @FXML
  private MapListController mapTemplateListController;

  // Controller instance for the map list
  @FXML
  private OpponentListController opponentListController;

  // DatabaseHandler instance
  private final Database database = DatabaseHandler.getInstance();

  @FXML
  public void initialize() {
    this.mapTemplateListController.selectedMapProperty()
        .addListener(((observable, oldValue, newValue) -> {
          this.opponentListController.updateBasedOnMapSelection(newValue);
        }));
  }

  @FXML
  public void createNewGame() {
    Server server = this.serverListController.getSelectedServer();

    if (server == null) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("No Server Selected");
      alert.setHeaderText("Please select a server to create a new game session.");
      alert.show();
      return;
    } else if (!server.getStatus()) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Server Offline");
      alert.setHeaderText("The selected server is currently offline.");
      alert.show();
      return;
    } else if (this.mapTemplateListController.getSelectedMap() == null) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("No Map Selected");
      alert.setHeaderText("Please select a map template to create a new game session.");
      alert.show();
      return;
    } else if (this.teamID.getText().isEmpty()) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("No Team ID");
      alert.setHeaderText("Please enter a team ID to create a new game session.");
      alert.show();
      return;
    }

    // Create a new game session
    MapItem mapItem = this.mapTemplateListController.getSelectedMap();
    PlayerType[] players = this.opponentListController.getPlayers();
    System.out.println("Creating new game session on server " + server.getUrl() + " with map "
        + mapItem.getMapName());
    MapTemplate mapTemplate = mapItem.getMapTemplate();

    try {
      // First player creates the game session
      BoardModel boardModel = this.database.createGameSession(new URI("http://" + server.getUrl()),
          this.teamID.getText(), mapTemplate);
      String gameSessionID = boardModel.getGameSessionID();
      Thread.sleep(200);

      if (!players[0].equals(PlayerType.HUMAN_PLAYER)) {;
        Runnable botTask = getBotTask(boardModel, players[0]);
        Thread botThread = new Thread(botTask);
        botThread.setDaemon(true);
        botThread.start();
      }

      System.out.println("Player length: " + players.length);
      for (int i = 1; i < players.length; i++) {
        if (!players[i].equals(PlayerType.HUMAN_PLAYER)) {
          String botTeamID = "bot" + i;
          BoardModel aiModel= new BoardModel(URI.create("http://" + server.getUrl()), gameSessionID, botTeamID);
          Thread.sleep(500);
          Runnable botTask = getBotTask(aiModel, players[i]);
          Thread botThread = new Thread(botTask);
          botThread.setDaemon(true);
          botThread.start();
        }
      }

    } catch (URISyntaxException e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText(
          "The provided server URL is invalid. Please try to re-enter the server URL to the server list.");
      alert.show();
    } catch (GameSessionNotFound e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("GameSession not found");
      alert.setHeaderText(null);
      alert.setContentText(
          "The requested GameSession was not found. Please enter a valid GameSession ID.");
      alert.show();
    } catch (IOException | InterruptedException e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Unexpected Error");
      alert.setHeaderText(null);
      alert.setContentText(
          "An unexpected error occurred while trying to communicate with the server. Please try again later.");
      alert.show();
    }

  }

  private Runnable getBotTask(BoardModel boardModel, PlayerType playerType) {
    return switch (playerType) {
      case HUMAN_PLAYER -> throw new IllegalArgumentException("Human player cannot be a bot.");
      case EASY_BOT -> () -> {
        try {
          if (boardModel.isGameInProgress()) {
            boardModel.updateGameState();
            Thread.sleep(3000);
          }
          new EasyBot(boardModel);
        } catch (IOException | InterruptedException e) {
          throw new RuntimeException("Error creating EasyBot", e);
        }
      };
      case NORMAL_BOT -> () -> {
        try {
          if (boardModel.isGameInProgress()) {
            boardModel.updateGameState();
            Thread.sleep(3000);
          }
          new NormalBot(boardModel);
        } catch (IOException | InterruptedException e) {
          throw new RuntimeException("Error creating NormalBot", e);
        }
      };
      case HARD_BOT -> () -> {
        try {
          if (boardModel.isGameInProgress()) {
            boardModel.updateGameState();
            Thread.sleep(3000);
          }
          new MCTSClient(boardModel);
        } catch (IOException | InterruptedException e) {
          throw new RuntimeException("Error creating HardBot", e);
        }
      };
    };
  }


  /**
   * Renders the preview of the selected map template.
   */
  @FXML
  public void renderPreview() {
    this.mapPreview.getChildren().clear();
    MapItem mapItem = this.mapTemplateListController.getSelectedMap();
    Server server = this.serverListController.getSelectedServer();
    if (mapItem != null && server != null && server.getStatus()) {
      try {
        BoardModel boardModel = new BoardModel(URI.create("http://" + server.getUrl()),
            mapItem.getMapTemplate());
        boardModel.deleteGameSession();
        BoardView boardView = new BoardView(boardModel);
        AnchorPane.setTopAnchor(boardView, 0.0);
        AnchorPane.setRightAnchor(boardView, 0.0);
        AnchorPane.setBottomAnchor(boardView, 0.0);
        AnchorPane.setLeftAnchor(boardView, 0.0);
        this.mapPreview.getChildren().add(boardView);
      } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }


}
