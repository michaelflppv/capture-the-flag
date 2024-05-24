package de.sep.cfp4.application.controller.gamePlay;

import de.sep.cfp4.application.controller.gameSetup.ServerListController;
import de.sep.cfp4.technicalServices.database.DatabaseHandler;
import de.sep.cfp4.technicalServices.database.interfaces.Database;
import de.sep.cfp4.application.model.listItems.Server;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameSessionNotFound;
import de.unimannheim.swt.pse.ctf.game.exceptions.NoMoreTeamSlots;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;

/**
 * Controller for the join game view.
 *
 * @author dcebulla
 * @version 0.1.0
 */
public class JoinGameController {

  @FXML
  private TextField teamID;
  @FXML
  private TextField gameSessionField;

  @FXML
  private ServerListController serverListController;

  private final Database database = DatabaseHandler.getInstance();

  /**
   * Handles joining a new game based on the specified game instance.
   */
  @FXML
  public void joinGame() {
    Server server = this.serverListController.getSelectedServer();

    if (this.gameSessionField.getText().isEmpty()) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("No GameSession ID");
      alert.setHeaderText("Please enter a GameSession ID to join a game.");
      alert.show();
      return;
    } else if (this.teamID.getText().isEmpty()) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("No Team Name");
      alert.setHeaderText("Please enter a Team Name to join a game.");
      alert.show();
      return;
    } else if (server == null || !server.getStatus()) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Server Offline");
      alert.setHeaderText("The selected server is currently offline.");
      alert.show();
      return;
    }

    String gameSessionID = this.gameSessionField.getText();
    try {
      this.database.joinGameSession(new URI("http://" + server.getUrl()), gameSessionID, this.teamID.getText());
    } catch (GameSessionNotFound e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("GameSession Invalid");
      alert.setHeaderText("The requested GameSession was not found.");
      alert.show();
    } catch (NoMoreTeamSlots e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("No More Team Slots");
      alert.setHeaderText("The requested GameSession is already full.");
      alert.show();
    } catch (URISyntaxException e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error while constructing Server URL");
      alert.setHeaderText(null);
      alert.setContentText("An unexpected error occurred while trying to construct the server URL. Please re-add the server to the list and try again.");
    } catch (IOException | InterruptedException e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Unexpected Error");
      alert.setHeaderText(null);
      alert.setContentText("An unexpected error occurred while trying to communicate with the server.");
      // Update Server so that server status is automatically updated in case the server went offline in the meantime.
      this.serverListController.updateServerOnline(server);
    }

  }

}