package de.sep.cfp4.application.controller.gamePlay;

import de.sep.cfp4.technicalServices.database.DatabaseHandler;
import de.sep.cfp4.technicalServices.database.interfaces.Database;
import de.sep.cfp4.application.model.BoardModel;
import de.sep.cfp4.ui.customComponents.BoardView;
import de.unimannheim.swt.pse.ctf.game.exceptions.ForbiddenMove;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameOver;
import de.unimannheim.swt.pse.ctf.game.exceptions.GameSessionNotFound;
import de.unimannheim.swt.pse.ctf.game.exceptions.InvalidMove;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Controller class for the visual GUI of the game board.
 *
 * @author dcebulla
 * @version 0.9.0
 */
public class BoardController {
  // FXML elements
  // The board view rendering the current state of the game.
  @FXML
  private BoardView gameBoard;

  // Help zu make game board resizable and keep it square.
  @FXML
  private StackPane center;
  @FXML
  private AnchorPane anchor;

  // View element that show additional information.
  @FXML
  private Button sessionInfo;
  @FXML
  private Label gameTimeLabel;
  @FXML
  private Label labelTopLeft;
  @FXML
  private Label labelTopRight;
  @FXML
  private Label labelBottomLeft;
  @FXML
  private Label labelBottomRight;
  @FXML
  private Button forfeitButton;
  //---------------------------------------------------------
  // Stores the row and column of the currently selected square/piece on the board.
  private int[] selectedSquare;
  // Stores the squares reachable by the currently selected square/piece.
  private int[][] reachableSquares;
  //---------------------------------------------
  // The boardModel associated with the boardController and boardView
  private BoardModel boardModel;
  // Database access for access to the currently selected BoardTheme etc.
  private final Database database = DatabaseHandler.getInstance();
  //----------------------------------------------------------------

  // Initialize the BoardController
  /**
   * Adjusts settings during initialization of the board view.
   */
  // Adapted from user xeruf on StackOverflow: https://stackoverflow.com/questions/44979700/square-gridpane-of-square-cells
  @FXML
  public void initialize() {
    this.anchor.maxWidthProperty()
        .bind(Bindings.min(this.center.widthProperty(), this.center.heightProperty()));
    this.anchor.maxHeightProperty()
        .bind(Bindings.min(this.center.widthProperty(), this.center.heightProperty()));
  }

  /**
   * Initializes the boardController with the associated boardModel instance.
   * @param boardModel The boardModel that is being represented by the view and controller
   * @throws IllegalArgumentException If the boardModel has already been initialized in the past.
   */
  public void initModel(BoardModel boardModel) throws IllegalArgumentException {
    if (this.boardModel != null) {
      throw new IllegalArgumentException("The BoardModel can not be initialized twice.");
    }
    this.boardModel = boardModel;

    // Give View access to the boardModel in order to render it.
    this.gameBoard.initModel(boardModel);
    // Tell view which method to call in the controller in case of a user input.
    this.gameBoard.setOnSquareClick(e -> handleSquareClick(GridPane.getRowIndex((Node) e.getSource()), GridPane.getColumnIndex((Node) e.getSource())));

    this.boardModel.addPropertyChangeListener(evt -> {
      switch (evt.getPropertyName()) {
        case "gameState" -> Platform.runLater(() -> {
          this.gameBoard.renderBoard();
          this.setUpLabels();
        });
        case "winner" -> Platform.runLater(() -> {
          Alert alert = new Alert(AlertType.INFORMATION);
          String[] winners = (String[]) evt.getNewValue();
          if (winners.length == 1 && winners[0].equals(this.boardModel.getTeamID())) {
            alert.setTitle("Congratulations!");
            alert.setHeaderText("You have won the game!");
          } else if (winners.length > 1 && Arrays.stream(winners).anyMatch(winner -> winner.equals(this.boardModel.getTeamID()))) {
            alert.setTitle("Draw!");
            alert.setHeaderText("The game ended in a draw. You are one of the winners!");
          } else if (winners.length > 1) {
            alert.setTitle("Draw!");
            alert.setHeaderText("The game ended in a draw. Unfortunately, you have lost. Better luck next time!");
          } else {
            alert.setTitle("Game Over");
            alert.setHeaderText("The game is over. Unfortunately, you have lost. Better luck next time!");
          }
          alert.showAndWait().ifPresent(response -> this.database.removeGameSessionFromDatabase(
              this.boardModel.getGameSessionID(), this.boardModel.getTeamID()));
        });
        case "teamNames", "remainingMoveTime" -> Platform.runLater(this::setUpLabels);
        case "ownTeamLost" -> Platform.runLater(() -> {
          Alert alert = new Alert(AlertType.CONFIRMATION);
          alert.setTitle("Game Over");
          alert.setHeaderText(
              "Your base has been captured. Would you like to return to the main menu?");
          alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
              this.database.removeGameSessionFromDatabase(this.boardModel.getGameSessionID(),
                  this.boardModel.getTeamID());
            } else {
              // Reset the onSquareClick event to prevent further interactions with the board.
              this.gameBoard.setOnSquareClick(e -> {
              });
              this.forfeitButton.setText("Leave");
              this.forfeitButton.setOnAction(e -> {
                this.boardModel.stopUpdateThread();
                this.database.removeGameSessionFromDatabase(this.boardModel.getGameSessionID(),
                    this.boardModel.getTeamID());
              });
            }
          });
        });
        case "remainingGameTime" -> Platform.runLater(() -> {
          this.gameTimeLabel.setDisable(false);
          java.time.Duration duration = java.time.Duration.ofSeconds((int) evt.getNewValue());
          long minutes = duration.toMinutes();
          long remainingSeconds = duration.minusMinutes(minutes).getSeconds();
          String timeString = String.format("%02d:%02d", minutes, remainingSeconds);
          this.gameTimeLabel.setText(timeString);
        });
      }
    });
    this.addInfoButton();
    this.setUpLabels();
  }

  /**
   * Handles the click event on a square. If a piece is selected and eligible square is clicked, a
   * move request is send to the server.
   *
   */
  private void handleSquareClick(int row, int column) {
    // Reachable squares need the coordinate of the square relative to the position of the player perspective.
    String[][] grid = this.boardModel.getGrid();
    String squareString = grid[row][column];

    // Only allow selection of pieces belonging to the own team
    if (squareString.startsWith("p:") && this.boardModel.getPieceByID(squareString).getTeamId()
        .equals(this.boardModel.getTeamID())) {
      if (this.selectedSquare == null) {
        this.selectedSquare = new int[]{row, column};
        this.gameBoard.selectSquare(row, column);
        this.reachableSquares = this.boardModel.getReachableSquares(row, column);
        this.showReachableSquares(true);

      } else if (this.selectedSquare[0] == row && this.selectedSquare[1] == column) {
        // Deselect the selected piece.
        this.gameBoard.deselectSquare(row, column);
        this.selectedSquare = null;

        // Reset Reachable Squares markings in GUI
        this.showReachableSquares(false);
        this.reachableSquares = null;

      } else { // Switch selected piece to a different one.
        // Reset Reachable Squares markings in GUI
        this.showReachableSquares(false);
        this.reachableSquares = null;
        this.gameBoard.deselectSquare(this.selectedSquare[0], this.selectedSquare[1]);

        // Switch selected square to new square.
        this.selectedSquare = new int[]{row, column};
        this.gameBoard.selectSquare(row, column);

        this.reachableSquares = this.boardModel.getReachableSquares(row, column);
        this.showReachableSquares(true);
      }

    } if (this.selectedSquare != null && (this.selectedSquare[0] != row || this.selectedSquare[1] != column)) {
      // Reset Reachable Squares markings in GUI
      this.showReachableSquares(false);
      this.reachableSquares = null;

      // Send move request to server via model
      this.makeMove(row, column);
      this.gameBoard.deselectSquare(this.selectedSquare[0], this.selectedSquare[1]);
      this.selectedSquare = null;

      this.gameBoard.renderBoard();
    }

  }

  /**
   * Implements functionality for the info button used to obtain the game session token.
   */
  private void addInfoButton() {
    Tooltip tooltip = new Tooltip("Session ID: " + this.boardModel.getGameSessionID());
    tooltip.setShowDelay(Duration.millis( 200));
    this.sessionInfo.setTooltip(tooltip);

    // Copy session ID to clipboard on click
    this.sessionInfo.setOnMouseClicked(e -> {
      // Copy session ID to clipboard
      ClipboardContent content = new ClipboardContent();
      content.putString(this.boardModel.getGameSessionID());
      Clipboard.getSystemClipboard().setContent(content);
      // Set tooltip to inform user that session ID was copied
      tooltip.setText("Session ID copied to clipboard");
      tooltip.show(this.sessionInfo, e.getScreenX(), e.getScreenY());

      // Hide tooltip after 2 seconds
      PauseTransition pause = new PauseTransition(Duration.seconds(2));
      pause.setOnFinished(event -> tooltip.hide());
      pause.play();
    });
  }


  /**
   * Sets up the labels for the teams on the board.
   */
  private void setUpLabels() {
    // Reset all labels
    this.labelTopLeft.setText("");
    this.labelTopRight.setText("");
    this.labelBottomLeft.setText("");
    this.labelBottomRight.setText("");

    // Lambda function to get the appropriate status (waiting, turn or time) for a given team.
    Function<String, String> getTeamName = teamID -> {
      int remainingMoveTime = this.boardModel.getRemainingMoveTimeInSeconds();
      int minutes = remainingMoveTime / 60;
      int seconds = remainingMoveTime % 60;
      String timeString = String.format("%02d:%02d", minutes, seconds);

      if (this.boardModel.getCurrentTeamID().equals(teamID)) {
        return (this.boardModel.useMoveTimeLimit() ? timeString : "(Your Turn)");
      } else {
        return "(Waiting)";
      }
    };

    String[] teamIDs = this.boardModel.getAllTeamIDs();
    switch (teamIDs.length) {
      case 2 -> {
        this.labelBottomLeft.setText(this.boardModel.getTeamID());
        this.labelTopLeft.setText(teamIDs[0].equals(this.boardModel.getTeamID()) ? teamIDs[1] : teamIDs[0]);

        this.labelBottomRight.setText(getTeamName.apply(this.boardModel.getTeamID()));
        this.labelTopRight.setText(getTeamName.apply(teamIDs[0].equals(this.boardModel.getTeamID()) ? teamIDs[1] : teamIDs[0]));
      }
      case 3, 4 -> {
        for (String teamID : teamIDs) {
          if (this.boardModel.isUpsideDown()) {
            switch (this.boardModel.getBaseQuadrant(teamID)) {
              case 1 -> this.labelBottomLeft.setText(teamID + " " + getTeamName.apply(teamID));
              case 2 -> this.labelBottomRight.setText(teamID + " " + getTeamName.apply(teamID));
              case 3 -> this.labelTopRight.setText(teamID + " " + getTeamName.apply(teamID));
              case 4 -> this.labelTopLeft.setText(teamID + " " + getTeamName.apply(teamID));
              default -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error in setting up team labels");
                alert.setHeaderText("The base quadrant of team " + teamID + " is not supported.");
                alert.show();
              }
            }
          } else {
            switch (this.boardModel.getBaseQuadrant(teamID)) {
              case 1 -> this.labelTopRight.setText(teamID + " " + getTeamName.apply(teamID));
              case 2 -> this.labelTopLeft.setText(teamID + " " + getTeamName.apply(teamID));
              case 3 -> this.labelBottomLeft.setText(teamID + " " + getTeamName.apply(teamID));
              case 4 -> this.labelBottomRight.setText(teamID + " " + getTeamName.apply(teamID));
              default -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error in setting up team labels");
                alert.setHeaderText("The base quadrant of team " + teamID + " is not supported.");
                alert.show();
              }
            }
          }
        }
      }
    }
  }

  /**
   * Puts markers on all squares reachable from the selected pieces current position.
   * @param show Whether to show the reachable squares or not.
   */
  private void showReachableSquares(boolean show) {
    if (this.reachableSquares != null) {
      for (int i = 0; i < this.reachableSquares.length; i++) {
        for (int j = 0; j < this.reachableSquares[i].length; j++) {
          // If the square is reachable, mark it.
          if (this.reachableSquares[i][j] != -1) {
            if (show) {
              this.gameBoard.setMarker(i,j);
            } else {
              this.gameBoard.removeMarker(i,j);
            }
          }
        }
      }
    }
  }

  /**
   * Sends a move request to the board model.
   *
   * @param toRow The row of the square to move to.
   * @param toColumn The column of the square to move to.
   */
  private void makeMove (int toRow, int toColumn) {
    try {
      System.out.println(toRow + " " + toColumn);
      this.boardModel.makeMove(this.selectedSquare[0], this.selectedSquare[1], toRow, toColumn);
    } catch (ForbiddenMove e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Forbidden Move");
      alert.setHeaderText("The move you tried to make is not allowed, either because it is not your turn or the move is not allowed by the game rules. Please try again.");
      alert.show();
    } catch (InvalidMove e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Invalid Move");
      alert.setHeaderText("The move you tried to make is invalid. Please try again.");
      alert.show();
    } catch (GameSessionNotFound e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Game Session Not Found");
      alert.setHeaderText("The game session associated with this board was not found. Would you like to return to the main menu?");
      alert.show();
    } catch (GameOver e) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Game Over");
      alert.setHeaderText("The game has already ended. Would you like to return to the main menu?");
      alert.show();
    } catch (IOException | InterruptedException e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("A connection error occurred");
      alert.setHeaderText("An unexpected error occurred while trying to make a move. Please try again.");
      alert.setContentText(e.getMessage());
      alert.show();
    } catch (Exception e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("An unexpected error occurred");
      alert.setHeaderText("An unexpected error occurred");
      alert.setContentText(e.getMessage());
      alert.show();
    }
  }

  //TODO: Update the Board of the other Team
  @FXML
  private void handleForfeit() {
    try {
      boardModel.forfeitGame();
      Alert alert = new Alert(AlertType.INFORMATION);
      this.database.removeGameSessionFromDatabase(this.boardModel.getGameSessionID(), this.boardModel.getTeamID());
      alert.setTitle("Give Up");
      alert.setHeaderText("You gave up the game.");
      alert.show();
      // Additional actions after forfeiting, like updating the UI or navigating to another screen
    } catch (ForbiddenMove e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Forbidden Move");
      alert.setHeaderText("The move you tried to make is not allowed, either because it is not your turn or the move is not allowed by the game rules. Please try again.");
      alert.show();
    } catch (InvalidMove e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Invalid Move");
      alert.setHeaderText("The move you tried to make is invalid. Please try again.");
      alert.show();
    } catch (GameSessionNotFound e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Game Session Not Found");
      alert.setHeaderText("The game session associated with this board was not found. Would you like to return to the main menu?");
      alert.show();
    } catch (GameOver e) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Game Over");
      alert.setHeaderText("The game has already ended. Would you like to return to the main menu?");
      alert.show();
    } catch (IOException | InterruptedException e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("A connection error occurred");
      alert.setHeaderText("An unexpected error occurred while trying to make a move. Please try again.");
      alert.setContentText(e.getMessage());
      alert.show();
    } catch (Exception e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("An unexpected error occurred");
      alert.setHeaderText("An unexpected error occurred");
      alert.setContentText(e.getMessage());
      alert.show();
    }
  }

  // Getter- and setter- methods ----------------------------------------------------
  /**
   * Returns the root node of the view associated with the BoardController
   * @return The view root node associated with this boardController.
   */
  public Node getRoot() {
    return this.center;
  }

  /**
   * Returns the boardModel associated with the BoardController.
   * @return The boardModel instance associated with this boardController.
   */
  public BoardModel getBoardModel() {
    return this.boardModel;
  }

}
