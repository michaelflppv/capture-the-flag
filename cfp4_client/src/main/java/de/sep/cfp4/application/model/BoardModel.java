package de.sep.cfp4.application.model;

import com.google.gson.Gson;
import de.sep.cfp4.technicalServices.network.GameAPI;
import de.sep.cfp4.technicalServices.network.GameClient;
import de.unimannheim.swt.pse.ctf.controller.data.*;
import de.unimannheim.swt.pse.ctf.game.exceptions.ForbiddenMove;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameOver;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameSessionNotFound;
import de.unimannheim.swt.pse.ctf.game.exceptions.InvalidMove;
import de.unimannheim.swt.pse.ctf.game.exceptions.NoMoreTeamSlots;
import de.unimannheim.swt.pse.ctf.game.map.Directions;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import de.unimannheim.swt.pse.ctf.game.state.Team;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;


/**
 * Model for the game board. Transforms the game state received from the server into an oriented
 * grid and provides methods to interact with the game state for the business logic.
 *
 * @author dcebulla
 * @version 0.9.0
 */
public class BoardModel {

  // API to communicate with the server.
  private final GameAPI API;
  // The last game session response from the server.
  private GameSessionResponse gameSessionResponse;
  // The current game state the model is representing.
  private GameState gameState;
  // The game board from the player's perspective (upside down if the player is on the upper side of the board).
  private String[][] grid;
  // Maps piece IDs from the grid to their respective pieces.
  private HashMap<String, Piece> pieceByID;

  // ---------------------------------------------------------------------------
  // Internal storage of relevant data
  private final String gameSessionID;
  private String teamID;
  private String teamSecret;
  private String teamColor;
  private boolean upsideDown;
  private boolean useGameTimeLimit;
  private boolean useMoveTimeLimit;

  // ---------------------------------------------------------------------------
  // PropertyChangeSupport for notifying the GUI of changes in the game state.
  private final PropertyChangeSupport support = new PropertyChangeSupport(this);
  // ---------------------------------------------------------------------------
  // Auxiliary lock object used to notify the update Thread when e.g. a move has been made.
  private final Object lock = new Object();
  private boolean gameInProgress = true;


  /**
   * Creates a new BoardModel by registering a new game session with the server without joining the
   * game.
   *
   * @param serverURL   The URL of the server.
   * @param mapTemplate The map template to use for the game session.
   * @throws IOException          If an error occurs while trying to create the game session with
   *                              the server.
   * @throws InterruptedException If the thread is interrupted while trying to create the game
   *                              session with the server.
   */
  public BoardModel(URI serverURL, MapTemplate mapTemplate)
      throws IOException, InterruptedException {
    this.API = new GameClient(serverURL);
    GameSessionResponse gameSessionResponse = this.API.createGameSession(mapTemplate);
    this.gameSessionID = gameSessionResponse.getId();

    // Sets the teamID to the first team in the game session as a spectator.
    this.teamID = this.API.getGameState(this.gameSessionID).getTeams()[0].getId();
    this.startUpdateThread();
  }

  /**
   * Creates a new BoardModel by registering a new game session with the server.
   *
   * @param serverURL   The URL of the server.
   * @param mapTemplate The map template to use for the game session.
   * @param teamID      The desired team name of the player.
   * @throws IOException          If an error occurs while trying to create the game session with
   *                              the server.
   * @throws InterruptedException If the thread is interrupted while trying to create the game
   *                              session with the server.
   * @throws GameSessionNotFound  If the game session could not be found by the server after
   *                              creating it with the server.
   * @throws GameSessionNotFound  (Probably server error) If the game session could not be found by
   *                              the server right after supposedly creating it on said server.
   * @throws NoMoreTeamSlots      (Probably server or map template error) If there are no more team
   *                              slots available in the game session even though the game session
   *                              was just created.
   */
  public BoardModel(URI serverURL, MapTemplate mapTemplate, String teamID)
      throws IOException, InterruptedException, GameSessionNotFound, NoMoreTeamSlots {
    this.API = new GameClient(serverURL);
    GameSessionResponse gameSessionResponse = this.API.createGameSession(mapTemplate);
    this.gameSessionID = gameSessionResponse.getId();
    this.joinGame(teamID);
    this.startUpdateThread();

    System.out.println(gameSessionID);
  }


  /**
   * Creates a new BoardModel by joining an existing game session on the server as a spectator.
   *
   * @param serverURL     The URL of the server.
   * @param gameSessionID The ID of the game session to join.
   * @throws IOException          If an error occurs while trying to get the game state from the
   *                              server.
   * @throws InterruptedException If the thread is interrupted while trying to get the game state
   *                              from the server.
   * @throws GameSessionNotFound  If the game session could not be found by the server.
   */
  public BoardModel(URI serverURL, String gameSessionID)
      throws IOException, InterruptedException, GameSessionNotFound {
    this.API = new GameClient(serverURL);
    this.gameSessionID = gameSessionID;

    // Sets the teamID to the first team in the game session as a spectator.
    this.teamID = this.API.getGameState(gameSessionID).getTeams()[0].getId();
    this.startUpdateThread();
  }

  /**
   * Creates a new BoardModel by joining an existing game session on the server.
   *
   * @param serverURL     The URL of the server.
   * @param gameSessionID The ID of the game session to join.
   * @param teamID        The desired team name of the player.
   * @throws IOException          If an error occurs while trying to get the game state from the
   *                              server.
   * @throws InterruptedException If the thread is interrupted while trying to get the game state
   *                              from the server.
   * @throws GameSessionNotFound  If the game session could not be found by the server.
   * @throws NoMoreTeamSlots      If there are no more team slots available in the game session.
   */
  public BoardModel(URI serverURL, String gameSessionID, String teamID)
      throws IOException, InterruptedException, GameSessionNotFound, NoMoreTeamSlots {
    this.API = new GameClient(serverURL);
    this.gameSessionID = gameSessionID;
    this.joinGame(teamID);
    this.startUpdateThread();
  }


  /**
   * Starts a new thread that consistently checks for updates in the game state and updates the
   * model accordingly.
   */
  private void startUpdateThread() {
    // We need to call the updateGameState method once before starting the thread to avoid
    // NullPointerExceptions with the grid caused by race conditions.
    try {
      this.updateGameState();
      GameSessionResponse response = this.API.getGameSession(this.gameSessionID);
      this.useGameTimeLimit = response.getRemainingGameTimeInSeconds() != -1;
      this.useMoveTimeLimit = response.getRemainingMoveTimeInSeconds() != -1;
    } catch (IOException | InterruptedException | GameSessionNotFound e) {
      this.gameInProgress = false;
      return;
    }
    Thread thread = new Thread(() -> {
      while (this.gameInProgress) {
        synchronized (this.lock) {
          try {
            // Get the latest game session response from the server.
            GameSessionResponse oldGameSessionResponse = this.gameSessionResponse;
            this.gameSessionResponse = this.API.getGameSession(this.gameSessionID);

            if (this.useGameTimeLimit) {
              int oldRemainingGameTime = oldGameSessionResponse == null ? -1 : oldGameSessionResponse.getRemainingGameTimeInSeconds();
              this.support.firePropertyChange("remainingGameTime", oldRemainingGameTime, this.gameSessionResponse.getRemainingGameTimeInSeconds());
            }
            if (this.useMoveTimeLimit) {
              int oldRemainingMoveTime = oldGameSessionResponse == null ? -1 : oldGameSessionResponse.getRemainingMoveTimeInSeconds();
              this.support.firePropertyChange("remainingMoveTime", oldRemainingMoveTime, this.gameSessionResponse.getRemainingMoveTimeInSeconds());
            }

            // Check if the game is over.
            if (this.gameSessionResponse.isGameOver()) {
              this.support.firePropertyChange("winner", null,
                  this.gameSessionResponse.getWinner());
              this.gameInProgress = false;
              break;
            }
            // Only update the game state if it has changed.
            // TODO: Change grid check to comparing the last move once the server sends the last move.
            // The server currently does not update the last move after a piece has been captured.
            GameState newGameState = this.API.getGameState(this.gameSessionID);
            if (this.gameState == null || !Arrays.deepEquals(this.gameState.getGrid(),
                newGameState.getGrid())) {
              this.updateGameState();
            }

            this.lock.wait(1000);
          } catch (IOException | InterruptedException | GameSessionNotFound e) {
            break;
          }
        }
      }
    });
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Joins the game session with the specified team name.
   *
   * @param teamID The desired team name of the player.
   * @throws GameSessionNotFound If the game session could not be found by the server.
   * @throws NoMoreTeamSlots     If there are no more team slots available in the game session.
   */
  public void joinGame(String teamID)
      throws IOException, InterruptedException, GameSessionNotFound, NoMoreTeamSlots {
    JoinGameRequest joinGameRequest = new JoinGameRequest();
    joinGameRequest.setTeamId(teamID);
    JoinGameResponse joinGameResponse = this.API.joinGame(this.gameSessionID, joinGameRequest);

    this.teamID = joinGameResponse.getTeamId(); // In the case two teams have the same name, the server will for example append a number to the name.
    this.teamSecret = joinGameResponse.getTeamSecret();
    this.teamColor = joinGameResponse.getTeamColor();

    this.gameState = this.API.getGameState(this.gameSessionID);
    this.upsideDown = this.calculateUpsideDown();

    this.updateGameState();
  }


  /**
   * Updates the board model with the latest game state from the server.
   *
   * @throws IOException          If an error occurs while trying to get the game state from the
   *                              server.
   * @throws InterruptedException If the thread is interrupted while trying to get the game state
   *                              from the server.
   * @throws GameSessionNotFound  If the game session could not be found by the server.
   */
  public void updateGameState() throws IOException, InterruptedException, GameSessionNotFound {

    //System.out.println("Updating game state");
    GameState oldGameState = this.gameState;
    this.gameState = this.API.getGameState(this.gameSessionID);

    String[][] grid = this.gameState.getGrid();
    int gridHeight = grid.length;
    int gridWidth = grid[0].length;
    //this.upsideDown = this.calculateUpsideDown();

    // Rotate the board 180 degrees if the player is on the upper side of the board.
    if (this.upsideDown) {
      String[][] upsideDownGrid = new String[gridHeight][gridWidth];
      for (int i = 0; i < gridHeight; i++) {
        for (int j = 0; j < gridWidth; j++) {
          upsideDownGrid[i][j] = grid[gridHeight - i - 1][gridWidth - j - 1];
        }
      }
      grid = upsideDownGrid;
    }
    this.grid = grid;

    this.pieceByID = new HashMap<>();
    for (Team team : this.gameState.getTeams()) {
      if (team != null) {
        for (Piece piece : team.getPieces()) {
          if (piece
              != null) { // Bandaid fix for wrong null value in gameState. Needs to be fixed in the server-module @jannis.
            pieceByID.put("p:" + piece.getTeamId() + "_" + piece.getId(), piece);
          }
        }
      }
    }

    this.support.firePropertyChange("gameState", oldGameState, this.gameState);

    // Can be null in the beginning
    if (oldGameState != null) {

      // Check game over for own team in a multi-player game
      if (Arrays.stream(oldGameState.getTeams()).filter(Objects::nonNull).map(Team::getId)
          .anyMatch(teamID::equals) && Arrays.stream(this.gameState.getTeams())
          .filter(Objects::nonNull).map(Team::getId).noneMatch(teamID::equals)) {
        System.out.println("Own team lost");
        this.support.firePropertyChange("ownTeamLost", false, true);
      }

      // Notify observers if the team names have changed.
      String[] oldTeamNames = Arrays.stream(oldGameState.getTeams())
          .filter(Objects::nonNull)  // This will filter out null teams
          .map(Team::getId)
          .toArray(String[]::new);

      String[] newTeamNames = Arrays.stream(this.gameState.getTeams())
          .filter(Objects::nonNull)  // Add this line to filter out null teams
          .map(Team::getId)
          .toArray(String[]::new);

      if (!Arrays.equals(oldTeamNames, newTeamNames)) {
        this.support.firePropertyChange("teamNames", oldTeamNames, newTeamNames);
      }
    }

  }

  /**
   * Deletes the game session from the server.
   */
  public void deleteGameSession() {
    this.stopUpdateThread();
    try {
      this.API.deleteGameSession(this.gameSessionID);
    } catch (IOException | InterruptedException | GameSessionNotFound ignored) {
      // Ignore exceptions when deleting the game session. The game session is already over.
      // We only want to tell the server that it should delete the game session, if it still exists.
    }
  }

  /**
   * Stops the update thread that consistently checks for updates in the game state.
   */
  public void stopUpdateThread() {
    this.gameInProgress = false;
    // Notify the update thread that the game is over and the board model does not need to be updated anymore.
    synchronized (this.lock) {
      this.lock.notify();
    }
  }

  /**
   * Sends a move request to the server to make a move on the game board.
   *
   * @param fromRow    The row the piece is on.
   * @param fromColumn The column the piece is on.
   * @param toRow      The row the piece should move to.
   * @param toColumn   The column the piece should move to.
   * @throws ForbiddenMove        If the move is forbidden by the game rules.
   * @throws GameSessionNotFound  If the game session could not be found by the server.
   * @throws InvalidMove          If the move is invalid.
   * @throws GameOver             If the game has already ended.
   * @throws IOException          If an error occurs while trying to make the move with the server.
   * @throws InterruptedException If the thread is interrupted while trying to make the move with
   *                              the server.
   */
  public void makeMove(int fromRow, int fromColumn, int toRow, int toColumn)
      throws ForbiddenMove, GameSessionNotFound, InvalidMove, GameOver, IOException, InterruptedException {
    MoveRequest moveRequest = new MoveRequest();
    moveRequest.setPieceId(this.getPieceByID(this.grid[fromRow][fromColumn]).getId());

    // Move Requests need the absolute fixed coordinates of the square.
    if (this.upsideDown) {
      moveRequest.setNewPosition(new int[]{grid.length - toRow - 1, grid[0].length - toColumn - 1});
    } else {
      moveRequest.setNewPosition(new int[]{toRow, toColumn});
    }

    moveRequest.setTeamId(this.teamID);
    moveRequest.setTeamSecret(this.teamSecret);

    System.out.println(new Gson().toJson(moveRequest));

    this.API.makeMove(this.gameSessionID, moveRequest);

    // Notify the update thread that a move has been made and the board model needs to be updated with a new game state.
    synchronized (this.lock) {
      this.lock.notify();
    }

  }

  /**
   * Calculates if the board needs to be rotated 180 degrees. Adapted from
   * ValidMoveHelperMethods.java in the ctf-service module. Updated to use the base position
   * attribute instead.
   *
   * @return True if the board needs to be rotated 180 degrees, false otherwise.
   */
  private boolean calculateUpsideDown() {
    // If the teamID is not in the game state, the player is a spectator.
    // We can not determine the orientation of the board in this case.
    // We therefore set the convention that the board is not rotated.
    if (this.getTeamNumberByID(this.teamID) == -1) {
      return false;
    }

    int[][] basePositions = Arrays.stream(this.gameState.getTeams())
        .filter(Objects::nonNull) // Filters out null teams
        .map(Team::getBase)           // Maps to Team's base
        .toArray(int[][]::new);       // Collects into an array of int[][]

    // Smaller than, since indexing start from 0
    return basePositions[this.getTeamNumberByID(this.teamID)][0]
        < this.gameState.getGrid().length / 2;
  }


  /**
   * Returns the quadrant of the base of a team.
   *
   * @param teamID The ID of the team.
   * @return The quadrant of the base of the team. 1: Top right, 2: Top left, 3: Bottom right, 4:
   * Bottom left.
   */
  public int getBaseQuadrant(String teamID) {
    int[] basePosition = this.gameState.getTeams()[this.getTeamNumberByID(teamID)].getBase();
    boolean isTopHalf = basePosition[0] < this.gameState.getGrid().length / 2;
    boolean isLeftHalf = basePosition[1] < this.gameState.getGrid()[0].length / 2;

    if (isTopHalf) {
      return isLeftHalf ? 2 : 1;
    } else {
      return isLeftHalf ? 3 : 4;
    }
  }

  /**
   * Returns a boolean array representing the squares a piece can move to.
   *
   * @param row    The row the piece is on.
   * @param column The column the piece in on.
   * @return A boolean array representing the squares a piece can move to.
   */
  public int[][] getReachableSquares(int row, int column) {
    int gridHeight = this.grid.length;
    int gridWidth = this.grid[0].length;

    //Default value of squares is automatically false
    int[][] reachableSquares = new int[gridHeight][gridWidth];

    // Initialize reachableSquares array with -1, meaning the square is not reachable.
    // Improved by IntelliSense
    for (int[] reachableSquare : reachableSquares) {
      Arrays.fill(reachableSquare, -1);
    }

    Piece piece = this.pieceByID.get(this.grid[row][column]);

    // If piece is null, return the current state of reachableSquares (all -1)
    if (piece == null) {
      return reachableSquares;
    }

    // Piece has a shape, so it can move in a specific way, allows for easily adding more shapes in the future.
    if (piece.getDescription().getMovement().getShape() != null) {
      return switch (piece.getDescription().getMovement().getShape().getType()) {
        case lshape -> {
          int[][] positionOffset = new int[][]{{-2, -1}, {-2, 1}, {-1, 2}, {1, 2}, {2, 1}, {2, -1},
              {1, -2}, {-1, -2}};
          for (int[] offset : positionOffset) {
            int newRow = row + offset[0];
            int newColumn = column + offset[1];
            if (!squareOutsideBoard(newRow, newColumn)) {
              reachableSquares[newRow][newColumn] = squareReachable(newRow, newColumn,
                  piece.getDescription().getAttackPower());
            }
          }
          yield reachableSquares;
        }
        default -> {
          yield reachableSquares;
        }
      };
    }

    Directions movement = piece.getDescription().getMovement().getDirections();
    int[] moves = new int[]{movement.getLeft(), movement.getRight(), movement.getUp(),
        movement.getDown(), movement.getUpLeft(), movement.getUpRight(), movement.getDownLeft(),
        movement.getDownRight()};

    // Directions for left, right, up, down, upLeft, upRight, downLeft, downRight
    int[][] moveDirections = new int[][]{{0, -1}, {0, 1}, {-1, 0}, {1, 0}, {-1, -1}, {-1, 1},
        {1, -1}, {1, 1}};

    for (int i = 0; i < moves.length; i++) {
      for (int j = 1; j <= moves[i]; j++) {
        int newRow = row + j * moveDirections[i][0];
        int newColumn = column + j * moveDirections[i][1];

        // Stops the loop if a square is outside the board, since all the following squares will be outside the board as well.
        if (this.squareOutsideBoard(newRow, newColumn)) {
          break;
        }

        reachableSquares[newRow][newColumn] = squareReachable(newRow, newColumn,
            piece.getDescription().getAttackPower());

        // If the square is not empty, the piece can not move further in this direction.
        if (reachableSquares[newRow][newColumn] != 0) {
          break;
        }
      }
    }
    return reachableSquares;
  }

  public int[][] getReachableSquaresByPieceID(String pieceID) {
    Piece piece = this.pieceByID.get(pieceID);
    int row = upsideDown ? grid.length - piece.getPosition()[0] - 1 : piece.getPosition()[0];
    int column = upsideDown ? grid[0].length - piece.getPosition()[1] - 1 : piece.getPosition()[1];
    return getReachableSquares(row, column);
  }



  /**
   * Returns whether a square specified by row and column is outside the game board.
   *
   * @param row    The row the square is on.
   * @param column The column the square in on.
   * @return if the square is outside the board.
   */
  private boolean squareOutsideBoard(int row, int column) {
    return row < 0 || row >= this.grid.length || column < 0
        || column >= this.grid[0].length;
  }

  /**
   * Returns whether a square is reachable by a piece.
   *
   * @param row         The row the piece is on.
   * @param column      The column the piece in on.
   * @param attackPower The attack power of the piece.
   * @return if the square is reachable. -1: Square not reachable, 0: Square reachable (empty
   * square), 1: Square reachable (enemy piece)
   */
  private int squareReachable(int row, int column, int attackPower) {
    // The specified square is outside the board.
    if (this.squareOutsideBoard(row, column)) {
      return -1;
    }

    String squareString = this.grid[row][column];
    if (squareString.isEmpty()) {
      return 0;
    }
    if (squareString.equals("b")) {
      return -1;
    }
    if (squareString.startsWith("b:")) {
      if (squareString.endsWith(teamID)) {
        // Square not reachable if own base.
        return -1;
      } else {
        // Must be enemy base
        return 1;
      }
    }

    // Must be a piece
    Piece piece = this.pieceByID.get(squareString);
    if (piece.getTeamId().equals(teamID)) {
      return -1;
    }

    // Must be enemy piece
    if (piece.getDescription().getAttackPower() <= attackPower) {
      return 1;
    } else {
      return -1;
    }

  }

  // ---------------------------------------------------------------------------
  // PropertyChangeSupport methods

  /**
   * Adds a PropertyChangeListener to the BoardModel to listen for changes in the game state.
   * The following properties can be listened for:
   * - gameState: The game state has changed.
   * - teamNames: The team names have changed.
   * - winner: The game has ended and a winner has been determined.
   * - ownTeamLost: The own team has lost the game.
   * - remainingGameTime: The remaining game time has changed.
   * - remainingMoveTime: The remaining move time has changed.
   * @param listener The PropertyChangeListener to add.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    this.support.addPropertyChangeListener(listener);
  }

  /**
   * Removes a PropertyChangeListener from the BoardModel to stop listening for changes in the game
   * state.
   *
   * @param listener The PropertyChangeListener to remove.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    this.support.removePropertyChangeListener(listener);
  }

  // ---------------------------------------------------------------------------
  // Getters and Setters

  /**
   * Returns the game state of the board model.
   *
   * @param pieceID The ID of the piece.
   * @return The piece with the specified ID.
   */
  public Piece getPieceByID(String pieceID) {
    return this.pieceByID.get(pieceID);
  }

  /**
   * Returns the current oriented grid.
   *
   * @return The current oriented grid.
   */
  public String[][] getGrid() {
    return this.grid;
  }

  /**
   * Returns the team name of the player.
   *
   * @return The team name of the player.
   */
  public String getTeamID() {
    return this.teamID;
  }

  /**
   * Returns the teamIDs of all players in the game.
   *
   * @return The teamIDs of all players in the game.
   */
  public String[] getAllTeamIDs() {
    return Arrays.stream(this.gameState.getTeams())
        .filter(Objects::nonNull)  // This filters out null teams
        .map(Team::getId)
        .toArray(String[]::new);
  }

  /**
   * Returns the team color of the player.
   *
   * @return The team color of the player.
   */
  public String getGameSessionID() {
    return this.gameSessionID;
  }

  /**
   * Returns the team that has to make the next move.
   * @return The int representation of the current team.
   */
  public int getCurrentTeam() {
    return this.gameState.getCurrentTeam();
  }

  /**
   * Returns the id of the team that has to make the next move.
   * @return The String representation of the current team.
   */
  public String getCurrentTeamID() {
    return this.gameState.getTeams()[this.gameState.getCurrentTeam()].getId();
  }

  /**
   * Returns if the game is still in progress.
   * @return The boolean for gameInProgress
   */
  public boolean isGameInProgress() {
    return this.gameInProgress;
  }

  /**
   * Returns the lock Object used to notify the update Thread when e.g. a move has been made.
   * @return The lock Object.
   */
  public Object getLock() {
    return this.lock;
  }

  /**
   * Returns the team number of a team with a given ID.
   *
   * @param teamID The unique TeamID of the team.
   * @return The team number of the team according to the array of teams in the game state. -1 if
   * the team does not exist.
   */
  public int getTeamNumberByID(String teamID) {
    for (int i = 0; i < this.gameState.getTeams().length; i++) {
      if (this.gameState.getTeams()[i] != null && this.gameState.getTeams()[i].getId()
          .equals(teamID)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the current game state.
   *
   * @return The game state
   * @see GameState
   */
  public GameState getGameState() {
    return this.gameState;
  }

  /**
   * Returns whether the game is over.
   *
   * @return True if the game is over, false otherwise.
   */
  public boolean isGameOver() {
    return !this.gameInProgress;
  }

  /**
   * Returns whether the game board is upside down.
   *
   * @return True if the game board is upside down, false otherwise.
   */
  public boolean isUpsideDown() {
    return upsideDown;
  }

  public int getRemainingGameTimeInSeconds() {
    return this.gameSessionResponse.getRemainingGameTimeInSeconds();
  }

  public int getRemainingMoveTimeInSeconds() {
    return this.gameSessionResponse.getRemainingMoveTimeInSeconds();
  }

  public boolean useGameTimeLimit() {
    return useGameTimeLimit;
  }

  public boolean useMoveTimeLimit() {
    return useMoveTimeLimit;
  }

  /**
   * Forfeits the current game session.
   *
   * @throws IOException          If an error occurs while trying to forfeit the game with the
   *                              server.
   * @throws InterruptedException If the thread is interrupted while trying to forfeit the game with
   *                              the server.
   * @throws GameSessionNotFound  If the game session could not be found by the server.
   * @throws ForbiddenMove        If the move is forbidden by the game rules.
   * @throws GameOver             If the game has already ended.
   * @throws InvalidMove          If the move is invalid.
   * @author jwiederh
   */
  public void forfeitGame()
      throws IOException, InterruptedException, GameSessionNotFound, ForbiddenMove, GameOver {
    // Create a new instance of GiveupRequest
    GiveupRequest giveupRequest = new GiveupRequest();
    giveupRequest.setTeamId(this.teamID);  // Set the team ID
    giveupRequest.setTeamSecret(this.teamSecret);  // Set the team secret

    // Call the giveUp method on the API with the prepared request
    API.giveUp(this.gameSessionID, giveupRequest);
  }

}
