package de.sep.cfp4.technicalServices.database;

import de.sep.cfp4.technicalServices.resource.BoardTheme;
import de.sep.cfp4.application.exceptions.MapTemplateNameTaken;
import de.sep.cfp4.technicalServices.database.interfaces.Database;
import de.sep.cfp4.technicalServices.database.interfaces.ResourceLoader;
import de.sep.cfp4.application.model.BoardModel;
import de.sep.cfp4.application.model.listItems.MapItem;
import de.sep.cfp4.application.model.listItems.PieceItem;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameSessionNotFound;
import de.unimannheim.swt.pse.ctf.game.exceptions.NoMoreTeamSlots;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.image.Image;

/**
 * DatabaseHandler class to store all game sessions and map templates.
 *
 * @author dcebulla
 * @version 0.1.0
 */
public class DatabaseHandler implements Database {

  private static final DatabaseHandler DATABASE_HANDLER = new DatabaseHandler();
  private final ResourceLoader resourceLoader = new ResourceController();
  private final ObservableMap<String, BoardModel> boardModelList = FXCollections.observableMap(
      new HashMap<>());
  private final ObservableList<MapItem> mapTemplateList = FXCollections.observableArrayList();
  private final ObservableList<PieceItem> predefinedPiecesList = FXCollections.observableArrayList();
  private final SimpleObjectProperty<BoardTheme> boardTheme = new SimpleObjectProperty<>(
      BoardTheme.DEFAULT);

  private DatabaseHandler() {
    //All maps in the mapTemplates directory are loaded
    this.mapTemplateList.addAll(resourceLoader.getMapItems());
    //All predefined pieces are loaded as piece items
    this.predefinedPiecesList.addAll(this.resourceLoader.getPredefinedPieces().stream().map(pieceDescription -> {
      pieceDescription.setCount(1);
      PieceItem pieceItem = new PieceItem(pieceDescription, this.resourceLoader.getImage(this.boardTheme.getValue(), pieceDescription.getType(), 0));
      this.boardTheme.addListener((observable, oldValue, newValue) -> {
        Image newImage = this.resourceLoader.getImage(newValue, pieceDescription.getType(), 0);
        pieceItem.setPieceView(newImage);
      });
      return pieceItem;
    }).toList());
  }

  public static DatabaseHandler getInstance() {
    return DATABASE_HANDLER;
  }

  /**
   * Creates a new game session with the given server URL, team ID and map template.
   *
   * @param serverURL   The URL of the game server
   * @param teamID      The teamID of the player
   * @param mapTemplate The map template to be used
   * @throws IOException          Thrown if the connection to the server fails
   * @throws InterruptedException Thrown if the connection to the server is interrupted
   * @throws GameSessionNotFound  (Probably server error) Thrown if the game session is not found by
   *                              the server after creation.
   * @throws NoMoreTeamSlots      (Probably server or map template error) Thrown if there are no
   *                              more team slots available even though the game was just created
   */
  public BoardModel createGameSession(URI serverURL, String teamID, MapTemplate mapTemplate)
      throws IOException, InterruptedException, GameSessionNotFound, NoMoreTeamSlots {
    BoardModel boardModel = new BoardModel(serverURL, mapTemplate, teamID);
    this.boardModelList.put(
        "sessionID:%s+teamID:%s".formatted(boardModel.getGameSessionID(), boardModel.getTeamID()),
        boardModel);
    return boardModel;
  }

  /**
   * Joins an existing game session with the given server URL, session ID and team ID as spectator.
   * An AI Bot will play instead of you
   *
   * @param uri
   * @param boardModel The BoardModel tpo join as spectator
   * @throws GameSessionNotFound Thrown if the game session is not found by the server
   * @throws NoMoreTeamSlots     Thrown if there are no more team slots available
   */
  public void joinAsSpectator(URI uri, BoardModel boardModel)
          throws GameSessionNotFound, NoMoreTeamSlots {
    this.boardModelList.put(
            "sessionID:%s+teamID:%s".formatted(boardModel.getGameSessionID(), boardModel.getTeamID()),
            boardModel);
      try {
          new BoardModel(uri,boardModel.getGameSessionID());
      } catch (IOException e) {
          throw new RuntimeException(e);
      } catch (InterruptedException e) {
          throw new RuntimeException(e);
      }
  }

  /**
   * Joins an existing game session with the given server URL, session ID and team ID.
   *
   * @param serverURL The URL of the game server
   * @param sessionID The game session ID of the game session
   * @param teamID    The team ID of the player
   * @throws IOException          Thrown if the connection to the server fails
   * @throws InterruptedException Thrown if the connection to the server is interrupted
   * @throws GameSessionNotFound  Thrown if the game session is not found by the server
   * @throws NoMoreTeamSlots      Thrown if there are no more team slots available
   */
  public BoardModel joinGameSession(URI serverURL, String sessionID, String teamID)
      throws IOException, InterruptedException, GameSessionNotFound, NoMoreTeamSlots {
    // Adds the board model to the hashmap. The key is a combination of the session ID and the team ID
    // Notice that the teamID is derived from the board model since in the case of multiple teams using the
    // same name the teamID is changed to a unique value by the server.
    BoardModel boardModel = new BoardModel(serverURL, sessionID, teamID);
    this.boardModelList.put("sessionID:%s+teamID:%s".formatted(sessionID, boardModel.getTeamID()),
        boardModel);
    return boardModel;
  }

  /**
   * Deletes a game session with the given session ID and team ID from both the server and
   * database.
   *
   * @param sessionID The game session ID of the game session
   * @param teamID    The team ID of the player
   */
  public void deleteGameSession(String sessionID, String teamID) {
    this.boardModelList.get("sessionID:%s+teamID:%s".formatted(sessionID, teamID))
        .deleteGameSession();
    this.boardModelList.remove("sessionID:%s+teamID:%s".formatted(sessionID, teamID));
  }

  /**
   * Removes a game session only from the database. The game session is explicitly not deleted from
   * the server. This distinction is needed to prevent the game session from being deleted from the
   * server after for example forfeiting the game in a multi-player game where the game session
   * should still be available for the other players.
   *
   * @param sessionID The game session ID of the game session
   * @param teamID    The team ID of the player
   */
  public void removeGameSessionFromDatabase(String sessionID, String teamID) {
    this.boardModelList.get("sessionID:%s+teamID:%s".formatted(sessionID, teamID))
        .stopUpdateThread();
    this.boardModelList.remove("sessionID:%s+teamID:%s".formatted(sessionID, teamID));
  }

  /**
   * Adds a change listener to the board model list.
   *
   * @param listener The listener to be added
   */
  public void addListenerBoardModelList(MapChangeListener<String, BoardModel> listener) {
    this.boardModelList.addListener(listener);
  }

  /**
   * Returns a non-modifiable observable list wrapper of the board model list containing all board
   * models.
   *
   * @return The non-modifiable observable list wrapper of the board model list
   */
  public ObservableList<MapItem> getMapTemplateList() {
    return FXCollections.unmodifiableObservableList(this.mapTemplateList);
  }

  /**
   * Adds a map item to the map template list.
   *
   * @param mapItem The map item containing the map name and map template to be added.
   * @throws MapTemplateNameTaken Thrown if the map name is already taken.
   */
  public void addMapItem(MapItem mapItem) throws MapTemplateNameTaken {
    if (this.mapTemplateList.stream()
        .anyMatch(map -> map.getMapName().equals(mapItem.getMapName()))) {
      throw new MapTemplateNameTaken("The requested map name is already taken.");
    }
    this.mapTemplateList.add(mapItem);
  }

  /**
   * Removes a map item from the map template list.
   *
   * @param mapItem The map item to be removed.
   */
  public void removeMapItem(MapItem mapItem) {
    this.mapTemplateList.remove(mapItem);
  }

  /**
   * Adds a predefined piece to the predefined pieces list.
   *
   * @param pieceItem The piece item to be added
   */
  public void addPieceItem(PieceItem pieceItem) {
    this.predefinedPiecesList.add(pieceItem);
  }

  /**
   * Removes a predefined piece from the predefined pieces list.
   *
   * @param pieceItem The piece item to be removed
   */
  public void removePieceItem(PieceItem pieceItem) {
    this.predefinedPiecesList.remove(pieceItem);
  }

  /**
   * Returns a non-modifiable observable list wrapper of the predefined pieces list containing all
   * predefined pieces.
   *
   * @return The non-modifiable observable list wrapper of the predefined pieces list
   */
  public ObservableList<PieceItem> getPredefinedPiecesList() {
    return FXCollections.unmodifiableObservableList(this.predefinedPiecesList);
  }

  public Image getPieceImage(BoardTheme boardTheme, String type, int teamNumber) {
    return this.resourceLoader.getImage(boardTheme, type, teamNumber);
  }

  public Collection<Image> getAllPieceImages() {
    return this.resourceLoader.getAllImages();
  }

  public void storePieceImage(String key, Image image) {
    this.resourceLoader.storeImage(key, image);
  }

  public void setBoardTheme(BoardTheme boardTheme) {
    if (boardTheme != null) {
      this.boardTheme.set(boardTheme);
    }
  }

  public BoardTheme getBoardTheme() {
    return this.boardTheme.get();
  }

  public void addBoardThemeListener(ChangeListener<BoardTheme> listener) {
    this.boardTheme.addListener(listener);
  }

}
